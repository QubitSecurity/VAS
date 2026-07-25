import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogParser {

    private static final Map<String, String> GROK_TYPES = new HashMap<>();
    static {
        GROK_TYPES.put("WORD", "\\w+");
        GROK_TYPES.put("URI", "https?://[^\\s]+");
        GROK_TYPES.put("NOTSPACE", "\\S+");
        GROK_TYPES.put("GREEDYDATA", ".*");
    }

    /*
     * Linux msg의 현재 작업 디렉터리(pwd=... 또는 cwd=...)를 추출합니다.
     * 다음 형식을 처리합니다.
     *   pwd=/home/qubit/filter-test
     *   pwd="/path with spaces"
     *   pwd='/path with spaces'
     * 대소문자를 구분하지 않으므로 PWD=와 CWD=도 처리합니다.
     */
    private static final Pattern WORKING_DIRECTORY_PATTERN = Pattern.compile(
        "(?i)(?:^|[\\s,])(?:pwd|cwd)=(?:\\\"([^\\\"]*)\\\"|'([^']*)'|([^\\s,;}]+))"
    );

    static class GrokRule {
        String category;
        String name;
        Pattern compiledRegex;
        List<String> groupNames;
        String rawPattern; // 패턴의 길이를 비교하기 위해 원본 패턴 저장

        public GrokRule(String category, String name, Pattern compiledRegex, List<String> groupNames, String rawPattern) {
            this.category = category;
            this.name = name;
            this.compiledRegex = compiledRegex;
            this.groupNames = groupNames;
            this.rawPattern = rawPattern;
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
            List<GrokRule> rules = readCategorizedYamlPatterns("patterns.yml");
            if (rules.isEmpty()) {
                System.err.println("[!] patterns.yml 에서 패턴을 찾을 수 없습니다.");
                return;
            }

            // 패턴 길이가 가장 긴(가장 구체적인) 룰부터 먼저 검사하도록 내림차순 정렬
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

                    Map<String, String> extractedValues = new LinkedHashMap<>();
                    for (String groupName : rule.groupNames) {
                        String extractedValue = matcher.group(groupName);
                        if (extractedValue != null) {
                            extractedValues.put(groupName, extractedValue.trim());
                        }
                    }

                    // targetFile이 상대경로이면 msg의 pwd를 기준으로 절대경로로 변환합니다.
                    String workingDirectory = extractWorkingDirectory(inputLog);
                    for (Map.Entry<String, String> entry : extractedValues.entrySet()) {
                        if (isTargetFileGroup(entry.getKey())) {
                            entry.setValue(resolveTargetFile(entry.getValue(), workingDirectory));
                        }
                    }

                    for (Map.Entry<String, String> entry : extractedValues.entrySet()) {
                        System.out.println("[+] " + entry.getKey() + " : " + entry.getValue());
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

    private static boolean isTargetFileGroup(String groupName) {
        return groupName != null && groupName.matches("targetFile\\d*");
    }

    private static String extractWorkingDirectory(String inputLog) {
        Matcher matcher = WORKING_DIRECTORY_PATTERN.matcher(inputLog);
        if (!matcher.find()) {
            return null;
        }

        for (int i = 1; i <= matcher.groupCount(); i++) {
            String value = matcher.group(i);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    /**
     * 상대 targetFile을 pwd 기준 절대경로로 변환합니다.
     *
     * 변환:
     *   pwd=/home/qubit/filter-test, targetFile=test_bin
     *   -> /home/qubit/filter-test/test_bin
     *
     * 그대로 유지:
     *   /etc/audit/auditd.conf  (이미 절대경로)
     *   ~/.config/autostart/x  (홈 경로는 로그만으로 확정할 수 없음)
     *   $HOME/file             (환경변수 값은 로그만으로 확정할 수 없음)
     */
    private static String resolveTargetFile(String targetFile, String workingDirectory) {
        if (targetFile == null) {
            return null;
        }

        String value = targetFile.trim();
        if (value.isEmpty()) {
            return value;
        }

        // 옵션, 표준 입출력 표시, URI, 홈/환경변수 경로는 임의 해석하지 않습니다.
        if (value.startsWith("-")
                || value.startsWith("~")
                || value.startsWith("$")
                || value.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            return value;
        }

        try {
            Path targetPath = Path.of(value);

            // 이미 절대경로이면 기존 표현을 그대로 유지합니다.
            if (targetPath.isAbsolute()) {
                return value;
            }

            if (workingDirectory == null || workingDirectory.isBlank()) {
                return value;
            }

            Path basePath = Path.of(workingDirectory.trim());
            if (!basePath.isAbsolute()) {
                return value;
            }

            boolean hadTrailingSlash = value.endsWith("/");
            String resolved = basePath.resolve(targetPath).normalize().toString();

            if (hadTrailingSlash && !resolved.endsWith("/")) {
                resolved += "/";
            }
            return resolved;

        } catch (InvalidPathException e) {
            // 경로 문법이 비정상적이면 원문 값을 보존합니다.
            return value;
        }
    }

    private static List<GrokRule> readCategorizedYamlPatterns(String filePath) throws IOException {
        List<GrokRule> rules = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));

        String currentCategory = "Uncategorized";
        Pattern categoryPattern = Pattern.compile("^\\s{2}([a-zA-Z0-9_]+):\\s*$");

        for (String line : lines) {
            if (line.trim().isEmpty() || line.trim().startsWith("#") || line.startsWith("grok_patterns:")) {
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

                    rules.add(compileGrokRule(currentCategory, ruleName, rawPattern));
                }
            }
        }
        return rules;
    }

    private static GrokRule compileGrokRule(String category, String ruleName, String grokPattern) {
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
            String uniqueName = (count == 1) ? safeName : safeName + count;

            extractedGroupNames.add(uniqueName);

            String typeRegex = GROK_TYPES.getOrDefault(type, ".*");
            String replacement = "(?<" + uniqueName + ">" + typeRegex + ")";
            matcher.appendReplacement(javaRegexBuilder, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(javaRegexBuilder);

        Pattern compiledPattern = Pattern.compile(javaRegexBuilder.toString());
        return new GrokRule(category, ruleName, compiledPattern, extractedGroupNames, grokPattern);
    }
}
