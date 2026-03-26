# 📄 Agent Forensic Runtime (v2) – 실행 흐름 설명

## 1. 개요

본 문서는 에이전트가 탐지 이벤트 발생 시
포렌식 연동을 수행하는 전체 흐름을 정의한다.

핵심 원칙은 다음과 같다.

* 탐지 필터는 **포렌식 대상 여부만 정의**
* 실제 값 추출은 **에이전트가 수행**
* Backend / ETL 은 **저장만 수행**

---

## 2. 전체 처리 흐름

에이전트는 아래 순서로 동작한다.

```
탐지 이벤트 수신
→ forensic 여부 확인
→ forensic meta 조회
→ OS 유형에 맞는 분석기 선택
→ 필요 시 값 추출 (grok/regex)
→ 포렌식 payload 생성
→ 서버 전송
```

---

## 3. 상세 처리 단계

### 3.1 탐지 이벤트 수신

에이전트는 탐지 필터에 의해 생성된 이벤트를 수신한다.

---

### 3.2 forensic 연동 여부 확인

탐지 이벤트의 필터 정보에서 다음을 확인한다.

```json
"forensic": {
  "isUse": true
}
```

* `isUse = true` → 포렌식 연동 수행
* `isUse = false` 또는 없음 → 종료

---

### 3.3 forensic meta 조회

탐지 ID 기준으로 별도 meta 파일을 조회한다.

예:

```
M0284o09uova0qzd-rhel-link.json
```

meta 파일에는 다음 정보가 포함된다.

* osVariant (server / desktop)
* forensicId
* needForensicValue

---

### 3.4 OS 유형에 맞는 분기 선택

에이전트는 자신의 환경에 따라 분기를 선택한다.

예:

* Linux 서버 → `server`
* Windows PC → `desktop`

---

### 3.5 forensicId 선택

선택된 osVariant 에서 사용할 분석기를 결정한다.

```json
{
  "forensicId": "wd-integrated-malware-file-analyzer"
}
```

---

### 3.6 추가 값 추출 여부 확인

```json
"needForensicValue": true
```

* `true` → 값 추출 수행
* `false` → 바로 포렌식 호출

---

### 3.7 값 추출 수행 (Agent)

에이전트는 YAML 규칙을 기반으로 값을 추출한다.

### 추출 방식

* Regex
* Grok

### 주요 대상 필드

* `CommandLine`
* `msg`
* `Image`
* `ParentCommandLine`
* `file_path`

---

### 3.8 추출 예시

#### Regex 예

```regex
(?i)(https?:\/\/\S+\.sh)
```

→ 다운로드 URL 추출

---

#### Grok 예

```
%{WORD:tool} %{URI:url} \| %{WORD:shell}
```

→ 실행 도구 / URL / 쉘 분리

---

### 3.9 포렌식 payload 생성

에이전트는 다음 형태로 데이터를 구성한다.

```json
{
  "forensic": {
    "forensicId": "wd-integrated-malware-file-analyzer",
    "values": {
      "target": "/tmp/a.sh",
      "url": "http://evil.com/x.sh"
    }
  }
}
```

---

### 3.10 서버 전송

생성된 forensic payload 를 서버로 전송한다.

---

### 3.11 Backend / ETL 처리

* 데이터 저장만 수행
* 추출 로직 실행 없음
* 규칙 해석 없음

---

## 4. 에이전트 내부 의사 코드

```text
function handleDetection(event):

    if not event.forensic.isUse:
        return

    meta = loadForensicMeta(event.id)

    variant = selectVariant(meta, currentHostType)

    for item in variant.forensicValues:

        if item.needForensicValue:
            values = extractValues(event, item.forensicId)
        else:
            values = {}

        payload = buildForensicPayload(item.forensicId, values)

        send(payload)
```

---

## 5. 핵심 설계 원칙

### 원칙 1

탐지 필터는 최소 정보만 가진다

### 원칙 2

포렌식 추출은 Agent 책임이다

### 원칙 3

추출 방식은 grok / regex 로 통일한다

### 원칙 4

Backend / ETL 은 저장만 수행한다

---

## 6. 장점

* 구조 단순화
* 성능 향상
* 유지보수 용이
* 확장성 확보

---

## 7. 결론

> 탐지는 필터가 정의하고,
> 추출과 분석은 Agent가 수행하며,
> Backend는 저장만 한다.

---
