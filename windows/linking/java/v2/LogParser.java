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
                        propertyName + " 값이 올바르지 않습니다: " + value
                                + " (허용값: KEEP_SILENT, KEEP_WARN, ERROR)",
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
                                + " 파일이 없어 상대경로 정규화를 비활성화합니다. "
                                + "Grok 추출 결과는 원문 그대로 출력됩니다.");
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
                        "resolver.enabled=true 이지만 resolver.pathField.* 규칙이 없습니다.");
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
                                key + " 규칙에는 추출용 캡처 그룹 (...)이 최소 1개 필요합니다: "
                                        + regex);
                    }
                    rules.add(new RegexRule(key, pattern));
                } catch (PatternSyntaxException e) {
                    throw new IllegalArgumentException(
                            key + " 정규식이 올바르지 않습니다: " + regex,
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
                    debug("skipRule 적용: " + rule.key + ", value=" + value);
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
                config.debug("이미 절대경로이므로 유지: field=" + fieldName + ", value=" + value);
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
                            "명령 내부 작업 디렉터리를 확정할 수 없습니다: " + overrideDirectory);
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
                            "상대 명령 작업 디렉터리 " + overrideDirectory
                                    + "를 해석할 외부 기준 디렉터리가 없습니다.");
                }
            }

            if (baseDirectory == null) {
                return handlePolicy(
                        config.onMissingBase,
                        value,
                        "상대경로를 해석할 pwd/cwd/workdir 기준 디렉터리를 찾지 못했습니다.");
            }

            if (config.shouldSkipValue(baseDirectory) || !isLinuxAbsolutePath(baseDirectory)) {
                return handlePolicy(
                        config.onInvalidPath,
                        value,
                        "기준 디렉터리가 Linux 절대경로가 아닙니다: " + baseDirectory);
            }

            try {
                String resolved = resolveLinuxPath(
                        baseDirectory,
                        value,
                        config.preserveTrailingSlash);
                config.debug(
                        "경로 변환: field=" + fieldName
                                + ", baseRule=" + baseRuleKey
                                + ", base=" + baseDirectory
                                + ", value=" + value
                                + ", resolved=" + resolved);
                return resolved;
            } catch (IllegalArgumentException e) {
                return handlePolicy(
                        config.onInvalidPath,
                        value,
                        "경로 정규화 실패: " + e.getMessage());
            }
        }

        private String handlePolicy(ResolverPolicy policy, String originalValue, String reason) {
            if (policy == ResolverPolicy.ERROR) {
                throw new IllegalArgumentException(reason + " (value=" + originalValue + ")");
            }
            if (policy == ResolverPolicy.KEEP_WARN) {
                System.err.println(
                        "[!] 상대경로 정규화를 건너뜁니다: " + reason
                                + " 원본값=" + originalValue);
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
                        "기준 디렉터리가 절대경로가 아닙니다: " + baseDirectory);
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
                        "Linux 절대경로가 아닙니다: " + absolutePath);
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
            System.err.println("사용법: java LogParser \"분석할 로그 문자열\"");
            System.exit(1);
        }

        String inputLog = args[0];
        System.out.println("[*] 입력된 로그: " + inputLog);

        try {
            PathResolverConfig resolverConfig =
                    PathResolverConfig.load(PATH_RESOLVER_CONFIG_FILE);
            PathResolver pathResolver = new PathResolver(resolverConfig);

            List<GrokRule> rules = readCategorizedYamlPatterns(PATTERNS_FILE);
            if (rules.isEmpty()) {
                System.err.println("[!] " + PATTERNS_FILE + " 에서 패턴을 찾을 수 없습니다.");
                return;
            }

            // 원본 Grok 패턴 길이가 긴(구체적인) 룰부터 검사합니다.
            rules.sort((r1, r2) -> Integer.compare(r2.rawPattern.length(), r1.rawPattern.length()));

            boolean matched = false;
            for (GrokRule rule : rules) {
                Matcher matcher = rule.compiledRegex.matcher(inputLog);

                if (matcher.matches() || matcher.find()) {
                    matched = true;
                    System.out.println("========================================");
                    System.out.println("[*] 공격 카테고리 : " + rule.category.replace("_", " "));
                    System.out.println("[*] 탐지 시그니처 : " + rule.name);
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
                System.out.println("[-] 입력된 로그와 일치하는 공격 패턴이 없습니다.");
            }

        } catch (Exception e) {
            System.err.println("[!] 실행 중 오류 발생: " + e.getMessage());
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
