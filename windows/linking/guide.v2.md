# 📄 2. guide.v2.md (Agent 추출 규칙 정의)

# Agent Forensic Extraction Guide (v2)

## 1. 목적

Agent가 탐지 이벤트를 기반으로
포렌식 분석에 필요한 값을 추출하는 방법을 정의한다.

---

## 2. 적용 범위

- CommandLine
- Syslog msg
- Process path
- Script content
- 기타 이벤트 필드

---

## 3. 추출 방식

Agent는 다음 두 가지 방식만 사용한다:

### 3.1 Regex
- PCRE 기반
- 캡처 그룹 사용 가능

### 3.2 Grok
- 로그 패턴 기반 파싱
- 구조화된 필드 추출

---

## 4. 기본 흐름

1. 탐지 이벤트 수신
2. forensicId 확인
3. needForensicValue 확인
4. 필요 시 필드 추출 수행
5. 포렌식 payload 구성

---

## 5. 주요 추출 대상

| 필드 | 설명 |
|------|------|
| CommandLine | 프로세스 실행 명령 |
| msg | Syslog 메시지 |
| Image | 실행 파일 경로 |
| ParentCommandLine | 부모 프로세스 |
| file_path | 파일 경로 |

---

## 6. 예시 (Regex)

```regex
(?i)(https?:\/\/\S+\.sh)
````

→ URL 추출

---

## 7. 예시 (Grok)

```grok
%{WORD:tool} %{URI:url} \| %{WORD:shell}
```

→ tool, url, shell 분리

---

## 8. 추출 결과 예

```json
{
  "target": "/tmp/malware.sh",
  "url": "http://evil.com/x.sh",
  "tool": "curl"
}
```

---

## 9. 주의사항

* quoted / unquoted 모두 대응
* false positive 최소화
* 성능 고려 (heavy regex 지양)

---

## 10. 핵심 원칙

* 추출은 Agent 책임
* 탐지 필터는 추출 로직을 포함하지 않음
* Backend는 결과만 저장

---

## 11. 결론

> Agent는 탐지를 해석하고,
> 필요한 정보를 직접 추출하여 포렌식을 완성한다.
