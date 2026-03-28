import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
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

    // 카테고리 정보를 포함하는 Rule 객체
    static class GrokRule {
        String category;
        String name;
        String rawPattern;

        public GrokRule(String category, String name, String rawPattern) {
            this.category = category;
            this.name = name;
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
            // 1. 카테고리가 분류된 YAML 파일 읽기
            List<GrokRule> rules = readCategorizedYamlPatterns("patterns.yml");
            if (rules.isEmpty()) {
                System.err.println("[!] patterns.yml 에서 패턴을 찾을 수 없습니다.");
                return;
            }

            // 2. 패턴 매칭 수행
            boolean matched = false;
            for (GrokRule rule : rules) {
                Pattern compiledRegex = compileGrokToRegex(rule.rawPattern);
                Matcher matcher = compiledRegex.matcher(inputLog);

                if (matcher.matches() || matcher.find()) {
                    matched = true;
                    System.out.println("========================================");
                    // 카테고리명의 언더스코어를 공백으로 복원하여 예쁘게 출력
                    System.out.println("[*] 공격 카테고리 : " + rule.category.replace("_", " "));
                    System.out.println("[*] 탐지 시그니처 : " + rule.name);
                    System.out.println("----------------------------------------");
                    
                    Map<String, Integer> namedGroups = compiledRegex.namedGroups();
                    for (String groupName : namedGroups.keySet()) {
                        String extractedValue = matcher.group(groupName);
                        if (extractedValue != null) {
                            System.out.println("[+] " + groupName + " : " + extractedValue.trim());
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

    /**
     * 들여쓰기를 기반으로 카테고리와 룰을 파싱합니다. (의존성 없음)
     */
    private static List<GrokRule> readCategorizedYamlPatterns(String filePath) throws IOException {
        List<GrokRule> rules = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of(filePath));
        
        String currentCategory = "Uncategorized";
        
        // 정규식: 공백 2개로 시작하는 키 (카테고리)
        Pattern categoryPattern = Pattern.compile("^\\s{2}([a-zA-Z0-9_]+):\\s*$");
        // 정규식: 공백 4개 이상으로 시작하는 "키": "값" (실제 룰)
        Pattern rulePattern = Pattern.compile("^\\s{4,}([a-zA-Z0-9_]+):\\s*\"([^\"]+)\"\\s*$");

        for (String line : lines) {
            if (line.trim().isEmpty() || line.trim().startsWith("#") || line.startsWith("grok_patterns:")) {
                continue;
            }

            Matcher catMatcher = categoryPattern.matcher(line);
            if (catMatcher.matches()) {
                currentCategory = catMatcher.group(1).trim(); // 카테고리 갱신
                continue;
            }

            Matcher ruleMatcher = rulePattern.matcher(line);
            if (ruleMatcher.matches()) {
                String ruleName = ruleMatcher.group(1);
                String pattern = ruleMatcher.group(2);
                rules.add(new GrokRule(currentCategory, ruleName, pattern));
            }
        }
        return rules;
    }

    private static Pattern compileGrokToRegex(String grokPattern) {
        Pattern grokSyntaxPattern = Pattern.compile("%\\{([^:]+):([^}]+)\\}");
        Matcher matcher = grokSyntaxPattern.matcher(grokPattern);
        
        StringBuilder javaRegexBuilder = new StringBuilder();
        
        while (matcher.find()) {
            String type = matcher.group(1);
            String rawName = matcher.group(2);
            
            String safeName = rawName.replaceAll("[^a-zA-Z0-9]", "");
            String typeRegex = GROK_TYPES.getOrDefault(type, ".*");
            
            String replacement = "(?<" + safeName + ">" + typeRegex + ")";
            matcher.appendReplacement(javaRegexBuilder, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(javaRegexBuilder);
        
        return Pattern.compile(javaRegexBuilder.toString());
    }
}
