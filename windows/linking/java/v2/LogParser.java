import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
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

    private PathResolver pathResolver;

    static class GrokRule {
        String category;
        String name;
        Pattern compiledRegex;
        List<String> fieldNames;
        List<String> groupNames;
        String rawPattern;

        GrokRule(
                String category,
                String name,
                Pattern compiledRegex,
                List<String> fieldNames,
                List<String> groupNames,
                String rawPattern) {
            this.category = category;
            this.name = name;
            this.compiledRegex = compiledRegex;
            this.fieldNames = fieldNames;
            this.groupNames = groupNames;
            this.rawPattern = rawPattern;
        }
    }

    /**
     * Result contract for code that launches a target-path-dependent forensic
     * script. The script may be launched only when shouldRunForensic() is true.
     */
    public enum ParseStatus {
        TARGET_EXTRACTED,
        INVALID_INPUT,
        NO_RULES,
        NO_PATTERN_MATCH,
        TARGET_KEY_NOT_FOUND,
        TARGET_VALUE_EMPTY
    }

    public static final class ParseResult {
        private final ParseStatus status;
        private final String value;
        private final String category;
        private final String ruleName;
        private final String targetKey;
        private final String message;

        private ParseResult(
                ParseStatus status,
                String value,
                String category,
                String ruleName,
                String targetKey,
                String message) {
            this.status = status;
            this.value = value;
            this.category = category;
            this.ruleName = ruleName;
            this.targetKey = targetKey;
            this.message = message;
        }

        static ParseResult extracted(
                String value,
                GrokRule rule,
                String targetKey) {
            return new ParseResult(
                    ParseStatus.TARGET_EXTRACTED,
                    value,
                    rule.category,
                    rule.name,
                    targetKey,
                    "A usable target path was extracted.");
        }

        static ParseResult skipped(
                ParseStatus status,
                GrokRule rule,
                String targetKey,
                String message) {
            return new ParseResult(
                    status,
                    null,
                    rule == null ? null : rule.category,
                    rule == null ? null : rule.name,
                    targetKey,
                    message);
        }

        public ParseStatus getStatus() {
            return status;
        }

        public String getValue() {
            return value;
        }

        public String getCategory() {
            return category;
        }

        public String getRuleName() {
            return ruleName;
        }

        public String getTargetKey() {
            return targetKey;
        }

        public String getMessage() {
            return message;
        }

        public boolean shouldRunForensic() {
            return status == ParseStatus.TARGET_EXTRACTED
                    && isUsableTargetValue(value);
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
        boolean homeEnabled;
        ResolverPolicy onMissingBase;
        ResolverPolicy onMissingHome;
        ResolverPolicy onInvalidPath;
        String homeTemplate;
        Pattern homeUserPattern;
        List<RegexRule> pathFieldRules;
        List<RegexRule> baseRules;
        List<RegexRule> overrideRules;
        List<RegexRule> userRules;
        List<RegexRule> homeRules;
        List<RegexRule> skipRules;
        Map<String, String> homeMappings;

        static PathResolverConfig disabled() {
            PathResolverConfig config = new PathResolverConfig();
            config.enabled = false;
            config.debug = false;
            config.preserveTrailingSlash = true;
            config.homeEnabled = false;
            config.onMissingBase = ResolverPolicy.KEEP_SILENT;
            config.onMissingHome = ResolverPolicy.KEEP_SILENT;
            config.onInvalidPath = ResolverPolicy.KEEP_SILENT;
            config.homeTemplate = "";
            config.homeUserPattern = Pattern.compile("^[A-Za-z0-9._-]+$");
            config.pathFieldRules = Collections.emptyList();
            config.baseRules = Collections.emptyList();
            config.overrideRules = Collections.emptyList();
            config.userRules = Collections.emptyList();
            config.homeRules = Collections.emptyList();
            config.skipRules = Collections.emptyList();
            config.homeMappings = Collections.emptyMap();
            return config;
        }

        static PathResolverConfig fromString(String content) throws IOException {
            if (content == null || content.trim().isEmpty()) {
                return disabled();
            }

            Properties properties = new Properties();
            try (Reader reader = new StringReader(content)) {
                properties.load(reader);
            }

            PathResolverConfig config = new PathResolverConfig();
            config.enabled = Boolean.parseBoolean(
                    properties.getProperty("resolver.enabled", "true").trim());
            config.debug = Boolean.parseBoolean(
                    properties.getProperty("resolver.debug", "false").trim());
            config.preserveTrailingSlash = Boolean.parseBoolean(
                    properties.getProperty("resolver.preserveTrailingSlash", "true").trim());
            config.homeEnabled = Boolean.parseBoolean(
                    properties.getProperty("resolver.home.enabled", "false").trim());
            config.onMissingBase = ResolverPolicy.parse(
                    properties.getProperty("resolver.onMissingBase"),
                    ResolverPolicy.KEEP_WARN,
                    "resolver.onMissingBase");
            config.onMissingHome = ResolverPolicy.parse(
                    properties.getProperty("resolver.onMissingHome"),
                    ResolverPolicy.KEEP_WARN,
                    "resolver.onMissingHome");
            config.onInvalidPath = ResolverPolicy.parse(
                    properties.getProperty("resolver.onInvalidPath"),
                    ResolverPolicy.KEEP_WARN,
                    "resolver.onInvalidPath");
            config.homeTemplate = properties.getProperty(
                    "resolver.homeTemplate",
                    "").trim();
            config.homeUserPattern = compileSinglePattern(
                    properties.getProperty(
                            "resolver.homeUserPattern",
                            "^[A-Za-z0-9._-]+$"),
                    "resolver.homeUserPattern");

            config.pathFieldRules = loadRegexRules(properties, "resolver.pathField.", false);
            config.baseRules = loadRegexRules(properties, "resolver.baseRule.", true);
            config.overrideRules = loadRegexRules(properties, "resolver.overrideRule.", true);
            config.userRules = loadRegexRules(properties, "resolver.userRule.", true);
            config.homeRules = loadRegexRules(properties, "resolver.homeRule.", true);
            config.skipRules = loadRegexRules(properties, "resolver.skipRule.", false);
            config.homeMappings = loadStringMappings(properties, "resolver.homeMap.");

            if (config.enabled && config.pathFieldRules.isEmpty()) {
                throw new IllegalArgumentException(
                        "resolver.enabled=true, but no resolver.pathField.* rules are configured.");
            }

            return config;
        }

        private static Pattern compileSinglePattern(String regex, String propertyName) {
            try {
                return Pattern.compile(regex.trim());
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(
                        propertyName + " contains an invalid regular expression: " + regex,
                        e);
            }
        }

        private static Map<String, String> loadStringMappings(
                Properties properties,
                String prefix) {
            List<String> keys = new ArrayList<>();
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith(prefix)) {
                    keys.add(key);
                }
            }
            Collections.sort(keys);

            Map<String, String> mappings = new HashMap<>();
            for (String key : keys) {
                String name = key.substring(prefix.length()).trim();
                String value = properties.getProperty(key);
                if (name.isEmpty() || value == null || value.trim().isEmpty()) {
                    continue;
                }
                mappings.put(name, value.trim());
            }
            return mappings;
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

        String findMappedHome(String userName) {
            return homeMappings.get(userName);
        }

        boolean isValidHomeUser(String userName) {
            return userName != null
                    && !userName.isEmpty()
                    && homeUserPattern.matcher(userName).matches();
        }

        void debug(String message) {
            if (debug) {
                System.err.println("[DEBUG] PathResolver: " + message);
            }
        }
    }

    static class TildePath {
        boolean currentUser;
        String userName;
        String remainder;
        boolean trailingSlash;

        TildePath(
                boolean currentUser,
                String userName,
                String remainder,
                boolean trailingSlash) {
            this.currentUser = currentUser;
            this.userName = userName;
            this.remainder = remainder;
            this.trailingSlash = trailingSlash;
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

            if (isLinuxAbsolutePath(value)) {
                config.debug("already an absolute path; keeping original: field=" + fieldName + ", value=" + value);
                return value;
            }

            if (config.homeEnabled && isTildePath(value)) {
                return resolveTildePath(fieldName, value, inputLog);
            }

            if (config.shouldSkipValue(value)) {
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

        private String resolveTildePath(String fieldName, String value, String inputLog) {
            TildePath tildePath = parseTildePath(value);
            if (tildePath == null) {
                return value;
            }

            String userName = tildePath.userName;
            String homeDirectory = null;
            String homeSource = null;

            if (tildePath.currentUser) {
                RegexMatch explicitHomeMatch = config.findFirstCapturedValue(
                        config.homeRules,
                        inputLog);
                if (explicitHomeMatch != null) {
                    homeDirectory = explicitHomeMatch.value;
                    homeSource = explicitHomeMatch.ruleKey;
                }

                if (homeDirectory == null) {
                    RegexMatch userMatch = config.findFirstCapturedValue(
                            config.userRules,
                            inputLog);
                    if (userMatch != null) {
                        userName = userMatch.value;
                        homeSource = userMatch.ruleKey;
                    }
                }
            }

            if (homeDirectory == null) {
                if (!config.isValidHomeUser(userName)) {
                    return handlePolicy(
                            config.onMissingHome,
                            value,
                            "no valid user name was found for tilde expansion.");
                }

                String mappedHome = config.findMappedHome(userName);
                if (mappedHome != null) {
                    homeDirectory = mappedHome;
                    homeSource = "resolver.homeMap." + userName;
                } else if (!config.homeTemplate.isEmpty()) {
                    homeDirectory = config.homeTemplate.replace("${user}", userName);
                    homeSource = "resolver.homeTemplate";
                }
            }

            if (homeDirectory == null || homeDirectory.trim().isEmpty()) {
                return handlePolicy(
                        config.onMissingHome,
                        value,
                        "no home directory could be determined for user: " + userName);
            }

            homeDirectory = homeDirectory.trim();
            if (!isLinuxAbsolutePath(homeDirectory)) {
                return handlePolicy(
                        config.onInvalidPath,
                        value,
                        "the resolved home directory is not a Linux absolute path: "
                                + homeDirectory);
            }

            try {
                String normalizedHome = normalizeLinuxAbsolutePath(
                        homeDirectory,
                        false);
                String resolved;

                if (tildePath.remainder.isEmpty()) {
                    resolved = normalizedHome;
                    if (config.preserveTrailingSlash
                            && tildePath.trailingSlash
                            && !resolved.endsWith("/")) {
                        resolved = resolved + "/";
                    }
                } else {
                    resolved = resolveLinuxPath(
                            normalizedHome,
                            tildePath.remainder,
                            config.preserveTrailingSlash);
                }

                config.debug(
                        "home path resolved: field=" + fieldName
                                + ", homeSource=" + homeSource
                                + ", user=" + userName
                                + ", home=" + normalizedHome
                                + ", value=" + value
                                + ", resolved=" + resolved);
                return resolved;
            } catch (IllegalArgumentException e) {
                return handlePolicy(
                        config.onInvalidPath,
                        value,
                        "home path normalization failed: " + e.getMessage());
            }
        }

        private static boolean isTildePath(String value) {
            return value.equals("~") || value.startsWith("~/") || value.matches("^~[^/]+(?:/.*)?$");
        }

        private static TildePath parseTildePath(String value) {
            if (value == null || value.isEmpty() || value.charAt(0) != '~') {
                return null;
            }

            boolean trailingSlash = value.length() > 1 && value.endsWith("/");
            if ("~".equals(value)) {
                return new TildePath(true, null, "", false);
            }
            if (value.startsWith("~/")) {
                return new TildePath(
                        true,
                        null,
                        value.substring(2),
                        trailingSlash);
            }

            int slashIndex = value.indexOf('/');
            String userName;
            String remainder;
            if (slashIndex < 0) {
                userName = value.substring(1);
                remainder = "";
            } else {
                userName = value.substring(1, slashIndex);
                remainder = value.substring(slashIndex + 1);
            }

            if (userName.isEmpty()) {
                return null;
            }
            return new TildePath(false, userName, remainder, trailingSlash);
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

        // Joining multiple arguments is tolerant of an accidentally unquoted
        // CLI log. The documented one-argument form remains preferred because
        // it preserves the original spacing exactly.
        String inputLog = String.join(" ", args);

        // Keep STDOUT reserved for the extracted target path. Diagnostics are
        // written to STDERR so command substitution never receives status text
        // or an accidental empty value as the forensic argument.
        System.err.println("[*] Input log: " + inputLog);

        try {
            String patterns = Files.readString(Path.of(PATTERNS_FILE));

            String resolverConfigText;
            Path resolverConfigPath = Path.of(PATH_RESOLVER_CONFIG_FILE);
            if (Files.exists(resolverConfigPath)) {
                resolverConfigText = Files.readString(resolverConfigPath);
            } else {
                System.err.println(
                        "[!] " + PATH_RESOLVER_CONFIG_FILE
                                + " was not found. Relative-path resolution is disabled; "
                                + "Grok values will be printed unchanged.");
                resolverConfigText = "";
            }

            LogParser parser = new LogParser();
            String targetKey = "targetFile";
            List<GrokRule> rules = parser.load(patterns, resolverConfigText);
            ParseResult result = parser.parseDetailed(inputLog, rules, targetKey);

            if (!result.shouldRunForensic()) {
                System.err.println(
                        "[-] Forensic execution skipped: status=" + result.getStatus()
                                + ", targetKey=" + targetKey
                                + ", message=" + result.getMessage());
                System.exit(2);
            }

            System.out.println(result.getValue());

        } catch (Exception e) {
            System.err.println("[!] Error while processing the log: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
        }
    }

    public String parse(String inputLog, String patterns, String resolverConfigText, String targetKey) throws IOException {
        if (inputLog == null || inputLog.trim().isEmpty()) {
            return null;
        }
        List<GrokRule> rules = load(patterns, resolverConfigText);
        if (rules.isEmpty()) {
            //log.error("[!] No patterns were found in " + PATTERNS_FILE + ".");
            return null;
        }
        ParseResult result = parseDetailed(inputLog, rules, targetKey);
        return result.shouldRunForensic() ? result.getValue() : null;
    }

    public List<GrokRule> load(String patterns, String resolverConfigText) throws IOException {
        PathResolverConfig resolverConfig = PathResolverConfig.fromString(resolverConfigText);
        this.pathResolver = new PathResolver(resolverConfig);

        List<GrokRule> rules = readCategorizedYamlPatterns(patterns);

        // Check longer, more specific Grok patterns first.
        rules.sort((r1, r2) -> Integer.compare(r2.rawPattern.length(), r1.rawPattern.length()));
        return rules;
    }

    public String parse(String inputLog, List<GrokRule> rules, String targetKey) {
        ParseResult result = parseDetailed(inputLog, rules, targetKey);
        return result.shouldRunForensic() ? result.getValue() : null;
    }

    /**
     * Extracts a usable target path while preserving the reason a forensic run
     * must be skipped. A matched rule with an empty target does not stop the
     * search; later matches and later rules are still evaluated.
     */
    public ParseResult parseDetailed(
            String inputLog,
            List<GrokRule> rules,
            String targetKey) {
        if (targetKey == null || targetKey.trim().isEmpty()) {
            return ParseResult.skipped(
                    ParseStatus.INVALID_INPUT,
                    null,
                    targetKey,
                    "The target key is null or blank.");
        }

        if (inputLog == null || inputLog.trim().isEmpty()) {
            return ParseResult.skipped(
                    ParseStatus.INVALID_INPUT,
                    null,
                    targetKey,
                    "The input log is null or blank.");
        }

        if (rules == null || rules.isEmpty()) {
            return ParseResult.skipped(
                    ParseStatus.NO_RULES,
                    null,
                    targetKey,
                    "No Grok rule is available.");
        }

        boolean patternMatched = false;
        boolean targetKeySeen = false;
        boolean emptyTargetSeen = false;
        GrokRule lastMatchedRule = null;

        for (GrokRule rule : rules) {
            Matcher matcher = rule.compiledRegex.matcher(inputLog);

            while (matcher.find()) {
                patternMatched = true;
                lastMatchedRule = rule;

                for (int i = 0; i < rule.groupNames.size(); i++) {
                    String fieldName = rule.fieldNames.get(i);
                    String groupName = rule.groupNames.get(i);

                    if (!targetKey.equals(fieldName)) {
                        continue;
                    }

                    targetKeySeen = true;
                    String extractedValue = normalizeTargetValue(
                            matcher.group(groupName));
                    if (extractedValue == null) {
                        emptyTargetSeen = true;
                        continue;
                    }

                    String resolvedValue = pathResolver == null
                            ? extractedValue
                            : pathResolver.resolve(fieldName, extractedValue, inputLog);
                    resolvedValue = normalizeTargetValue(resolvedValue);

                    if (resolvedValue == null) {
                        emptyTargetSeen = true;
                        continue;
                    }

                    return ParseResult.extracted(resolvedValue, rule, targetKey);
                }
            }
        }

        if (!patternMatched) {
            return ParseResult.skipped(
                    ParseStatus.NO_PATTERN_MATCH,
                    null,
                    targetKey,
                    "No attack pattern matched the input log.");
        }

        if (emptyTargetSeen) {
            return ParseResult.skipped(
                    ParseStatus.TARGET_VALUE_EMPTY,
                    lastMatchedRule,
                    targetKey,
                    "A pattern matched, but the extracted target path was empty. "
                            + "Do not launch the target-dependent forensic script.");
        }

        if (!targetKeySeen) {
            return ParseResult.skipped(
                    ParseStatus.TARGET_KEY_NOT_FOUND,
                    lastMatchedRule,
                    targetKey,
                    "A pattern matched, but it did not capture the requested target key. "
                            + "Do not launch the target-dependent forensic script.");
        }

        return ParseResult.skipped(
                ParseStatus.TARGET_VALUE_EMPTY,
                lastMatchedRule,
                targetKey,
                "No usable target path was extracted. Do not launch the forensic script.");
    }

    /**
     * Guard for existing launch code. It rejects null, blank, serialized empty
     * quotes, and NUL-containing values.
     */
    public static boolean isUsableTargetValue(String value) {
        return normalizeTargetValue(value) != null;
    }

    private static String normalizeTargetValue(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isEmpty() || normalized.indexOf('\0') >= 0) {
            return null;
        }

        // Remove one ordinary quoting layer. This converts "" and '' into an
        // empty value and converts "/tmp/file" into /tmp/file.
        if (normalized.length() >= 2) {
            char first = normalized.charAt(0);
            char last = normalized.charAt(normalized.length() - 1);
            if ((first == '"' && last == '"')
                    || (first == '\'' && last == '\'')) {
                normalized = normalized.substring(1, normalized.length() - 1).trim();
            }
        }

        // Handle one JSON-escaped quote layer such as \"/tmp/file\".
        if (normalized.length() >= 4
                && normalized.startsWith("\\\"")
                && normalized.endsWith("\\\"")) {
            normalized = normalized.substring(2, normalized.length() - 2).trim();
        }

        if (normalized.isEmpty()
                || "\"\"".equals(normalized)
                || "''".equals(normalized)
                || "\\\"\\\"".equals(normalized)) {
            return null;
        }

        return normalized;
    }

    /*
    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    */

    private static List<GrokRule> readCategorizedYamlPatterns(String patterns) throws IOException {
        List<GrokRule> rules = new ArrayList<>();
        List<String> lines = Arrays.asList(patterns.split("\r?\n"));

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
        List<String> extractedFieldNames = new ArrayList<>();
        List<String> extractedGroupNames = new ArrayList<>();
        Map<String, Integer> nameCounters = new HashMap<>();

        while (matcher.find()) {
            String type = matcher.group(1);
            String rawName = matcher.group(2).trim();

            String safeName = toSafeJavaGroupName(rawName);

            int count = nameCounters.getOrDefault(safeName, 0) + 1;
            nameCounters.put(safeName, count);
            String uniqueName = count == 1 ? safeName : safeName + count;

            extractedFieldNames.add(rawName);
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
                extractedFieldNames,
                extractedGroupNames,
                grokPattern);
    }

    private static String toSafeJavaGroupName(String rawName) {
        String safeName = rawName == null
                ? ""
                : rawName.replaceAll("[^a-zA-Z0-9]", "");

        if (safeName.isEmpty()) {
            return "g";
        }
        if (!Character.isLetter(safeName.charAt(0))) {
            return "g" + safeName;
        }
        return safeName;
    }
}
