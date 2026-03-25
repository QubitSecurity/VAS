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
