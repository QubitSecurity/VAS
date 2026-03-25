# Forensic Integration Overview (v2)

## 1. 목적

탐지 이벤트가 발생했을 때, 단순 탐지 결과만 저장하는 데서 끝나지 않고,
에이전트가 추가 분석에 필요한 값을 직접 추출하여 포렌식 분석기로 연계하는 구조를 정의한다.

본 구조의 목표는 다음과 같다.

- 탐지(Detection)와 포렌식 분석(Forensic)의 책임 분리
- 에이전트 중심의 경량 구조 유지
- Backend / ETL 단순화
- 탐지 필터 변경 최소화

---

## 2. 핵심 구조

```text
Detection Filter Match
→ Agent
→ Forensic Value Extraction
→ Forensic Analyzer Invocation
→ Backend / ETL 저장
```

---

## 3. 구성 요소별 역할

| 구성 요소 | 역할 |
| --- | --- |
| 탐지 필터 | 포렌식 연동 대상 여부 표시 (`forensic.isUse`) |
| Forensic Link Meta | osVariant별 분석기 선택 정보 정의 |
| Agent | 값 추출, 포렌식 분석기 선택, payload 생성 및 전송 |
| Backend | 저장 |
| ETL | 저장 |

---

## 4. 핵심 원칙

### 원칙 1. 탐지 필터는 최소 정보만 가진다
탐지 필터에는 다음 정보만 포함한다.

```json
"forensic": {
  "isUse": true
}
```

이는 해당 탐지가 포렌식 연동 대상인지 여부만 표시한다.

### 원칙 2. 상세 연동 정보는 별도 Link Meta 파일로 관리한다
실제로 어떤 포렌식 분석기를 호출할지는 탐지 필터와 분리된 별도 meta 파일에서 관리한다.

예:
- `M0284o09uova0qzd-rhel.json` → 탐지 필터
- `M0284o09uova0qzd-rhel-link.json` → 포렌식 연동 정보

### 원칙 3. 값 추출은 Agent가 수행한다
추출 대상 필드와 추출 규칙은 Agent가 `agent-forensic-rules.v2.yaml` 을 기준으로 해석한다.

### 원칙 4. 추출 방식은 grok / regex 기반으로 통일한다
에이전트는 원본 이벤트 필드에서 필요한 값을 추출할 때 grok 또는 regex를 사용한다.

### 원칙 5. Backend / ETL은 추출 로직을 해석하지 않는다
Backend와 ETL은 에이전트가 생성한 결과를 저장만 수행한다.

---

## 5. 실행 흐름

1. 탐지 이벤트 발생
2. 탐지 필터의 `forensic.isUse` 확인
3. `isUse=true` 이면 탐지 ID 기준으로 Link Meta 조회
4. 현재 호스트의 `osVariant` 에 맞는 `forensicId` 선택
5. `needForensicValue=true` 이면 Agent가 YAML 규칙 기반으로 값 추출 수행
6. 포렌식 payload 생성
7. 서버 전송
8. Backend / ETL 저장

---

## 6. forensicId의 의미

`forensicId` 는 포렌식 분석기의 고유 식별자이다.

예:
- `wd-integrated-malware-file-analyzer`
- `ws-integrated-malware-file-analyzer`

Agent는 `forensicId` 기준으로 분석기를 선택하고,
필요 시 해당 분석기에 맞는 입력값을 추출한다.

---

## 7. 데이터 파일 구조

### 7.1 탐지 필터 예시

```json
{
  "id": "M0284o09uova0qzd",
  "eventKey": "Syslog",
  "osType": "RHEL",
  "detects": [
    {
      "elementTag": "msg",
      "values": [
        {
          "elementSearchText": "(?i:\\b(curl|wget)\\b\\s+.+?https?:\\/\\/.+?\\.sh\\s+\\|\\s+(bash|eval)\\b)",
          "elementSearchType": "regex",
          "elementSearchFlag": "eq"
        }
      ]
    }
  ],
  "forensic": {
    "isUse": true
  }
}
```

### 7.2 Link Meta 예시

```json
{
  "forensic": {
    "isUse": true,
    "forensicOsVariants": [
      {
        "osVariant": "server",
        "forensicValues": [
          {
            "forensicType": "1",
            "forensicId": "ws-integrated-malware-file-analyzer",
            "needForensicValue": true
          }
        ]
      },
      {
        "osVariant": "desktop",
        "forensicValues": [
          {
            "forensicType": "1",
            "forensicId": "wd-integrated-malware-file-analyzer",
            "needForensicValue": true
          }
        ]
      }
    ]
  }
}
```

---

## 8. 결론

이 구조의 핵심은 다음 한 문장으로 정리된다.

> 탐지는 필터가 정의하고, 포렌식 값 추출과 분석기 연동은 Agent가 수행하며, Backend와 ETL은 저장만 한다.
