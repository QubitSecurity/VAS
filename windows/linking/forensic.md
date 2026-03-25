# Forensic Integration Overview (v2)

## 1. 목적

탐지 이벤트 발생 시, 단순 알림을 넘어
추가적인 분석(Forensic)을 수행하여
정확한 위협 판단과 증거 확보를 가능하게 한다.

본 구조는 다음을 목표로 한다:

- 탐지(Detection)와 분석(Forensic)의 분리
- 에이전트 중심의 경량 구조
- Backend / ETL 의 단순화

---

## 2. 핵심 구조

### 기존 구조
Detection → ETL → Backend 분석

### 변경 구조 (v2)

Detection → Agent → Forensic Extraction → Backend 저장

---

## 3. 역할 분리

| 구성 요소 | 역할 |
|----------|------|
| 탐지 필터 | 포렌식 연동 여부 및 대상 정의 |
| Agent | 값 추출 + 포렌식 실행 |
| Backend | 저장 |
| ETL | 저장 |

---

## 4. 핵심 원칙

### 원칙 1
탐지 필터는 **포렌식 대상 여부만 정의한다**

### 원칙 2
실제 값 추출은 **Agent가 수행한다**

### 원칙 3
추출 방식은 **grok / regex 기반**이다

### 원칙 4
Backend / ETL 은 **추출 로직을 해석하지 않는다**

---

## 5. 실행 흐름

1. 탐지 이벤트 발생
2. 탐지 필터의 `forensic.isUse` 확인
3. OS 유형에 맞는 `forensicId` 선택
4. 필요 시 값 추출 수행 (grok/regex)
5. 포렌식 payload 생성
6. 서버 전송
7. Backend / ETL 저장

---

## 6. forensicId 의미

- 포렌식 분석기의 고유 식별자
- 예:
  - wd-integrated-malware-file-analyzer
  - ws-xxx

Agent는 forensicId 기반으로 분석기를 선택한다.

---

## 7. 결론

이 구조는 다음을 달성한다:

- Backend 복잡도 제거
- Agent 중심 실시간 분석
- 확장 가능한 포렌식 구조

> 탐지는 선언, 분석은 Agent가 수행한다.
