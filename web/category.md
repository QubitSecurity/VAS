# 1️⃣ 한국어 표현 검토

| 현재 한국어                           | 검토          | 권장                |
| -------------------------------- | ----------- | ----------------- |
| CSRF(Cross-Site Request Forgery) | 정확          | 그대로 사용            |
| Deserialization                  | 한국어 없음      | **역직렬화 공격** 권장    |
| Injection                        | 정확          | 그대로 사용            |
| 알려진 취약점                          | 의미는 맞음      | **알려진 취약점 악용** 권장 |
| 프로토콜 위반                          | 정확          | 그대로 사용            |
| 스캐너                              | 약간 모호       | **취약점 스캐너** 권장    |
| 보안 설정 오류                         | 정확          | 그대로 사용            |
| 세션 하이재킹                          | 정확          | 그대로 사용            |
| SQL Injection                    | 정확          | 그대로 사용            |
| 서버사이드 요청 위조(SSRF)                | 정확          | 그대로 사용            |
| 웹쉘(WebShell)                     | 정확          | 그대로 사용            |
| XML 외부 개체(XXE)                   | 정확          | 그대로 사용            |
| 블랙리스트 규칙                         | WAF 문맥에서 적절 | 그대로 사용            |

---

# 2️⃣ 최종 번역 (KO / EN / JA)

| 한국어                               | English                            | 日本語                      |
| --------------------------------- | ---------------------------------- | ------------------------ |
| CSRF (Cross-Site Request Forgery) | CSRF (Cross-Site Request Forgery)  | CSRF（クロスサイトリクエストフォージェリ）  |
| 역직렬화 공격                           | Deserialization                    | デシリアライゼーション              |
| Injection                         | Injection                          | インジェクション                 |
| 알려진 취약점 악용                        | Known Vulnerabilities              | 既知の脆弱性の悪用                |
| 프로토콜 위반                           | Protocol Violation                 | プロトコル違反                  |
| 취약점 스캐너                           | Vulnerability Scanner              | 脆弱性スキャナー                 |
| 보안 설정 오류                          | Security Misconfiguration          | セキュリティ設定不備               |
| 세션 하이재킹                           | Session Hijacking                  | セッションハイジャック              |
| SQL Injection                     | SQL Injection                      | SQLインジェクション              |
| 서버사이드 요청 위조 (SSRF)                | Server-Side Request Forgery (SSRF) | サーバーサイドリクエストフォージェリ（SSRF） |
| 웹쉘 (WebShell)                     | Web Shell                          | Webシェル                   |
| XML 외부 개체 (XXE)                   | XML External Entity (XXE)          | XML外部エンティティ（XXE）         |
| 블랙리스트 규칙                          | Blacklist Rule                     | ブラックリストルール               |
| 사용자 정의 타입                         | Custom                        | カスタム                  |

---

# 3️⃣ WAF / XDR 제품에서 실제 많이 쓰는 형태 (추천)

PLURA-WAF 같은 **웹 공격 탐지 UI**에서는 보통 아래 형태가 가장 자연스럽습니다.

```
CSRF
Deserialization
Injection
Known Vulnerabilities
Protocol Violation
Vulnerability Scanner
Security Misconfiguration
Session Hijacking
SQL Injection
Server-Side Request Forgery (SSRF)
Web Shell
XML External Entity (XXE)
Blacklist Rule
```

일본어 UI는 일반적으로 아래가 가장 많이 사용됩니다.

```
CSRF
デシリアライゼーション
インジェクション
既知の脆弱性の悪用
プロトコル違反
脆弱性スキャナー
セキュリティ設定不備
セッションハイジャック
SQLインジェクション
サーバーサイドリクエストフォージェリ（SSRF）
Webシェル
XML外部エンティティ（XXE）
ブラックリストルール
```

---

