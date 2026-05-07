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

            // [핵심 해결책] 패턴 길이가 가장 긴(가장 구체적인) 룰부터 먼저 검사하도록 내림차순 정렬!
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
        // 객체 생성 시 원본 패턴의 길이 측정을 위해 grokPattern 을 전달합니다.
        return new GrokRule(category, ruleName, compiledPattern, extractedGroupNames, grokPattern);
    }
}
