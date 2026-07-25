import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LogParser {

    private static final String PATTERNS_FILE = "patterns.yml";
    private static final String PATH_RESOLVER_CONFIG_FILE = "path-resolver.properties";

    private static final Map<String, String> GROK_TYPES = new HashMap<>();
    static {
        GROK_TYPES.put("WORD", "\\w+");
        GROK_TYPES.put("URI", "https?://[^\\s]+");
        GROK_TYPES.put("NOTSPACE", "\\S+");
        GROK_TYPES.put("GREEDYDATA", ".*");
    }

    static class GrokRule {
        String category;
        String name;
        Pattern compiledRegex;
        List<String> groupNames;
        String rawPattern;

        GrokRule(
                String category,
                String name,
                Pattern compiledRegex,
                List<String> groupNames,
                String rawPattern) {
            this.category = category;
            this.name = name;
            this.compiledRegex = compiledRegex;
            this.groupNames = groupNames;
            this.rawPattern = rawPattern;
        }
    }

    enum ResolverPolicy {
        KEEP_SILENT,
        KEEP_WARN,
        ERROR;

        static ResolverPolicy parse(String value, ResolverPolicy defaultValue, String propertyName) {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }

            try {
                return ResolverPolicy.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        propertyName + " has an invalid value: " + value
                                + " (allowed values: KEEP_SILENT, KEEP_WARN, ERROR)",
                        e);
            }
        }
    }

    static class RegexRule {
        String key;
        Pattern pattern;

        RegexRule(String key, Pattern pattern) {
            this.key = key;
            this.pattern = pattern;
        }
    }

    static class RegexMatch {
        String ruleKey;
        String value;

        RegexMatch(String ruleKey, String value) {
            this.ruleKey = ruleKey;
            this.value = value;
        }
    }

    static class PathResolverConfig {
        boolean enabled;
        boolean debug;
        boolean preserveTrailingSlash;
        ResolverPolicy onMissingBase;
        ResolverPolicy onInvalidPath;
        List<RegexRule> pathFieldRules;
        List<RegexRule> baseRules;
        List<RegexRule> overrideRules;
        List<RegexRule> skipRules;

        static PathResolverConfig disabled() {
            PathResolverConfig config = new PathResolverConfig();
            config.enabled = false;
            config.debug = false;
            config.preserveTrailingSlash = true;
            config.onMissingBase = ResolverPolicy.KEEP_SILENT;
            config.onInvalidPath = ResolverPolicy.KEEP_SILENT;
            config.pathFieldRules = Collections.emptyList();
            config.baseRules = Collections.emptyList();
            config.overrideRules = Collections.emptyList();
            config.skipRules = Collections.emptyList();
            return config;
        }

        static PathResolverConfig load(String filePath) throws IOException {
            Path configPath = Path.of(filePath);
            if (!Files.exists(configPath)) {
                System.err.println(
                        "[!] " + filePath
                                + " was not found. Relative-path resolution is disabled; "
                                + "Grok values will be printed unchanged.");
                return disabled();
            }

            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }

            PathResolverConfig config = new PathResolverConfig();
            config.enabled = Boolean.parseBoolean(
                    properties.getProperty("resolver.enabled", "true").trim());
            config.debug = Boolean.parseBoolean(
                    properties.getProperty("resolver.debug", "false").trim());
            config.preserveTrailingSlash = Boolean.parseBoolean(
                    properties.getProperty("resolver.preserveTrailingSlash", "true").trim());
            config.onMissingBase = ResolverPolicy.parse(
                    properties.getProperty("resolver.onMissingBase"),
                    ResolverPolicy.KEEP_WARN,
                    "resolver.onMissingBase");
            config.onInvalidPath = ResolverPolicy.parse(
                    properties.getProperty("resolver.onInvalidPath"),
                    ResolverPolicy.KEEP_WARN,
                    "resolver.onInvalidPath");

            config.pathFieldRules = loadRegexRules(properties, "resolver.pathField.", false);
            config.baseRules = loadRegexRules(properties, "resolver.baseRule.", true);
            config.overrideRules = loadRegexRules(properties, "resolver.overrideRule.", true);
            config.skipRules = loadRegexRules(properties, "resolver.skipRule.", false);

            if (config.enabled && config.pathFieldRules.isEmpty()) {
                throw new IllegalArgumentException(
                        "resolver.enabled=true, but no resolver.pathField.* rules are configured.");
            }

            return config;
        }

        private static List<RegexRule> loadRegexRules(
                Properties properties,
                String prefix,
                boolean requireCaptureGroup) {
            List<String> keys = new ArrayList<>();
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith(prefix)) {
                    keys.add(key);
                }
            }
            Collections.sort(keys);

            List<RegexRule> rules = new ArrayList<>();
            for (String key : keys) {
                String regex = properties.getProperty(key);
                if (regex == null || regex.trim().isEmpty()) {
                    continue;
                }

                try {
                    Pattern pattern = Pattern.compile(regex.trim());
                    if (requireCaptureGroup && pattern.matcher("").groupCount() < 1) {
                        throw new IllegalArgumentException(
                                key + " must contain at least one capture group (...) for value extraction: "
                                        + regex);
                    }
                    rules.add(new RegexRule(key, pattern));
                } catch (PatternSyntaxException e) {
                    throw new IllegalArgumentException(
                            key + " contains an invalid regular expression: " + regex,
                            e);
                }
            }
            return rules;
        }

        boolean isPathField(String fieldName) {
            for (RegexRule rule : pathFieldRules) {
                if (rule.pattern.matcher(fieldName).matches()) {
                    return true;
                }
            }
            return false;
        }

        boolean shouldSkipValue(String value) {
            for (RegexRule rule : skipRules) {
                if (rule.pattern.matcher(value).find()) {
                    debug("skip rule matched: " + rule.key + ", value=" + value);
                    return true;
                }
            }
            return false;
        }

        RegexMatch findFirstCapturedValue(List<RegexRule> rules, String inputLog) {
            for (RegexRule rule : rules) {
                Matcher matcher = rule.pattern.matcher(inputLog);
                if (matcher.find()) {
                    String value = matcher.group(1);
                    if (value != null && !value.trim().isEmpty()) {
                        return new RegexMatch(rule.key, value.trim());
                    }
                }
            }
            return null;
        }

        void debug(String message) {
            if (debug) {
                System.err.println("[DEBUG] PathResolver: " + message);
            }
        }
    }

    static class PathResolver {
        private final PathResolverConfig config;

        PathResolver(PathResolverConfig config) {
            this.config = config;
        }

        String resolve(String fieldName, String rawValue, String inputLog) {
            String value = rawValue == null ? null : rawValue.trim();
            if (value == null || value.isEmpty()) {
                return value;
            }

            if (!config.enabled || !config.isPathField(fieldName)) {
                return value;
            }

            if (config.shouldSkipValue(value)) {
                return value;
            }

            if (isLinuxAbsolutePath(value)) {
                config.debug("already an absolute path; keeping original: field=" + fieldName + ", value=" + value);
                return value;
            }

            RegexMatch baseMatch = config.findFirstCapturedValue(config.baseRules, inputLog);
            RegexMatch overrideMatch = config.findFirstCapturedValue(config.overrideRules, inputLog);

            String baseDirectory = null;
            String baseRuleKey = null;

            if (baseMatch != null) {
                baseDirectory = baseMatch.value;
                baseRuleKey = baseMatch.ruleKey;
            }

            if (overrideMatch != null) {
                String overrideDirectory = overrideMatch.value;
                if (config.shouldSkipValue(overrideDirectory)) {
                    return handlePolicy(
                            config.onInvalidPath,
                            value,
                            "cannot determine the command-internal working directory: " + overrideDirectory);
                }

                if (isLinuxAbsolutePath(overrideDirectory)) {
                    baseDirectory = normalizeLinuxAbsolutePath(
                            overrideDirectory,
                            config.preserveTrailingSlash);
                    baseRuleKey = overrideMatch.ruleKey;
                } else if (baseDirectory != null && isLinuxAbsolutePath(baseDirectory)) {
                    baseDirectory = resolveLinuxPath(
                            baseDirectory,
                            overrideDirectory,
                            config.preserveTrailingSlash);
                    baseRuleKey = baseRuleKey + " + " + overrideMatch.ruleKey;
                } else {
                    return handlePolicy(
                            config.onMissingBase,
                            value,
                            "no external base directory is available to resolve the relative command "
                                    + "working directory: " + overrideDirectory);
                }
            }

            if (baseDirectory == null) {
                return handlePolicy(
                        config.onMissingBase,
                        value,
                        "no pwd/cwd/workdir base directory was found for the relative path.");
            }

            if (config.shouldSkipValue(baseDirectory) || !isLinuxAbsolutePath(baseDirectory)) {
                return handlePolicy(
                        config.onInvalidPath,
                        value,
                        "the base directory is not a Linux absolute path: " + baseDirectory);
            }

            try {
                String resolved = resolveLinuxPath(
                        baseDirectory,
                        value,
                        config.preserveTrailingSlash);
                config.debug(
                        "path resolved: field=" + fieldName
                                + ", baseRule=" + baseRuleKey
                                + ", base=" + baseDirectory
                                + ", value=" + value
                                + ", resolved=" + resolved);
                return resolved;
            } catch (IllegalArgumentException e) {
                return handlePolicy(
                        config.onInvalidPath,
                        value,
                        "path normalization failed: " + e.getMessage());
            }
        }

        private String handlePolicy(ResolverPolicy policy, String originalValue, String reason) {
            if (policy == ResolverPolicy.ERROR) {
                throw new IllegalArgumentException(reason + " (value=" + originalValue + ")");
            }
            if (policy == ResolverPolicy.KEEP_WARN) {
                System.err.println(
                        "[!] Relative-path resolution skipped: " + reason
                                + " originalValue=" + originalValue);
            }
            return originalValue;
        }

        private static boolean isLinuxAbsolutePath(String value) {
            return value.startsWith("/");
        }

        private static String resolveLinuxPath(
                String baseDirectory,
                String relativePath,
                boolean preserveTrailingSlash) {
            if (!isLinuxAbsolutePath(baseDirectory)) {
                throw new IllegalArgumentException(
                        "the base directory is not an absolute path: " + baseDirectory);
            }
            if (isLinuxAbsolutePath(relativePath)) {
                return relativePath;
            }

            String combined = baseDirectory.endsWith("/")
                    ? baseDirectory + relativePath
                    : baseDirectory + "/" + relativePath;
            return normalizeLinuxAbsolutePath(combined, preserveTrailingSlash);
        }

        private static String normalizeLinuxAbsolutePath(
                String absolutePath,
                boolean preserveTrailingSlash) {
            if (!isLinuxAbsolutePath(absolutePath)) {
                throw new IllegalArgumentException(
                        "not a Linux absolute path: " + absolutePath);
            }

            boolean hadTrailingSlash = preserveTrailingSlash
                    && absolutePath.length() > 1
                    && absolutePath.endsWith("/");

            Deque<String> segments = new ArrayDeque<>();
            String[] rawSegments = absolutePath.split("/+");
            for (String segment : rawSegments) {
                if (segment.isEmpty() || ".".equals(segment)) {
                    continue;
                }
                if ("..".equals(segment)) {
                    if (!segments.isEmpty()) {
                        segments.removeLast();
                    }
                    continue;
                }
                segments.addLast(segment);
            }

            StringBuilder normalized = new StringBuilder("/");
            boolean first = true;
            for (String segment : segments) {
                if (!first) {
                    normalized.append('/');
                }
                normalized.append(segment);
                first = false;
            }

            if (hadTrailingSlash
                    && normalized.length() > 1
                    && normalized.charAt(normalized.length() - 1) != '/') {
                normalized.append('/');
            }
            return normalized.toString();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java LogParser \"log string to analyze\"");
            System.exit(1);
        }

        String inputLog = args[0];
        System.out.println("[*] Input log: " + inputLog);

        try {
            PathResolverConfig resolverConfig =
                    PathResolverConfig.load(PATH_RESOLVER_CONFIG_FILE);
            PathResolver pathResolver = new PathResolver(resolverConfig);

            List<GrokRule> rules = readCategorizedYamlPatterns(PATTERNS_FILE);
            if (rules.isEmpty()) {
                System.err.println("[!] No patterns were found in " + PATTERNS_FILE + ".");
                return;
            }

            // Check longer, more specific Grok patterns first.
            rules.sort((r1, r2) -> Integer.compare(r2.rawPattern.length(), r1.rawPattern.length()));

            boolean matched = false;
            for (GrokRule rule : rules) {
                Matcher matcher = rule.compiledRegex.matcher(inputLog);

                if (matcher.matches() || matcher.find()) {
                    matched = true;
                    System.out.println("========================================");
                    System.out.println("[*] Attack category : " + rule.category.replace("_", " "));
                    System.out.println("[*] Detection signature : " + rule.name);
                    System.out.println("----------------------------------------");

                    for (String groupName : rule.groupNames) {
                        String extractedValue = matcher.group(groupName);
                        if (extractedValue != null) {
                            String outputValue = pathResolver.resolve(
                                    groupName,
                                    extractedValue,
                                    inputLog);
                            System.out.println("[+] " + groupName + " : " + outputValue);
                        }
                    }
                    System.out.println("========================================");
                    break;
                }
            }

            if (!matched) {
                System.out.println("[-] No attack pattern matched the input log.");
            }

        } catch (Exception e) {
            System.err.println("[!] Error while processing the log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<GrokRule> readCategorizedYamlPatterns(String filePath) throws IOException {
        List<GrokRule> rules = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));

        String currentCategory = "Uncategorized";
        Pattern categoryPattern = Pattern.compile("^\\s{2}([a-zA-Z0-9_]+):\\s*$");

        for (String line : lines) {
            if (line.trim().isEmpty()
                    || line.trim().startsWith("#")
                    || line.startsWith("grok_patterns:")) {
                continue;
            }

            Matcher catMatcher = categoryPattern.matcher(line);
            if (catMatcher.matches()) {
                currentCategory = catMatcher.group(1).trim();
                continue;
            }

            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String ruleName = line.substring(0, colonIndex).trim();
                int firstQuote = line.indexOf('"', colonIndex);
                int lastQuote = line.lastIndexOf('"');

                if (firstQuote > 0 && lastQuote > firstQuote) {
                    String rawPattern = line.substring(firstQuote + 1, lastQuote);
                    rawPattern = rawPattern.replace("\\\\", "\\").replace("\\\"", "\"");

                    rules.add(compileGrokRule(
                            currentCategory,
                            ruleName,
                            rawPattern));
                }
            }
        }
        return rules;
    }

    private static GrokRule compileGrokRule(
            String category,
            String ruleName,
            String grokPattern) {
        Pattern grokSyntaxPattern = Pattern.compile("%\\{([^:]+):([^}]+)\\}");
        Matcher matcher = grokSyntaxPattern.matcher(grokPattern);

        StringBuilder javaRegexBuilder = new StringBuilder();
        List<String> extractedGroupNames = new ArrayList<>();
        Map<String, Integer> nameCounters = new HashMap<>();

        while (matcher.find()) {
            String type = matcher.group(1);
            String rawName = matcher.group(2);

            String safeName = rawName.replaceAll("[^a-zA-Z0-9]", "");

            int count = nameCounters.getOrDefault(safeName, 0) + 1;
            nameCounters.put(safeName, count);
            String uniqueName = count == 1 ? safeName : safeName + count;

            extractedGroupNames.add(uniqueName);

            String typeRegex = GROK_TYPES.getOrDefault(type, ".*");
            String replacement = "(?<" + uniqueName + ">" + typeRegex + ")";
            matcher.appendReplacement(
                    javaRegexBuilder,
                    Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(javaRegexBuilder);

        Pattern compiledPattern = Pattern.compile(javaRegexBuilder.toString());
        return new GrokRule(
                category,
                ruleName,
                compiledPattern,
                extractedGroupNames,
                grokPattern);
    }
}
