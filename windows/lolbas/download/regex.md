사용자 목표가:

```text
실행파일명 <url> 명령어
```

형태의 **CLI 출력 탐지 정책**이라면,
실제로는 아래처럼 가는 것이 가장 좋습니다.

---

# 권장 방향

## 1) 1개 초광역 regex

* 장점: 간단
* 단점: 오탐 많음, LOLBAS별 옵션 차이 반영 어려움

## 2) 6~8개 그룹 regex

* 장점: 현실적
* 단점: 정책 수 조금 늘어남

제가 보기에는 **그룹형 7개 정도**가 가장 좋습니다.

---

# 먼저 전처리 기준

탐지 전에 command line 문자열을 아래처럼 normalize 하는 것이 좋습니다.

```text
1. 대소문자 무시
2. 연속 공백 1칸으로 축소
3. 따옴표 유무 차이 허용
4. 전체 경로/파일명 둘 다 허용
   예: C:\Windows\System32\hh.exe / hh.exe
5. URL / UNC / WebDAV 를 각각 분리
```

---

# 공통 토큰

## URL 토큰

```regex
(?:(?:https?|ftp)://[^\s"'<>]+)
```

## UNC / SMB / WebDAV 토큰

```regex
(?:\\\\[^\s"'<>]+\\[^\s"'<>]+(?:\\[^\s"'<>]+)*)
```

## 실행파일명 공통 형식

```regex
(?:^|[\\/"'\s])(?:[A-Za-z]:\\[^"\r\n]*\\)?
```

실전에서는 보통 아래처럼 바로 exe 이름별로 잡는 편이 낫습니다.

---

# A안: 전체를 아우르는 1개 broad regex

이건 **“다운로드형 LOLBAS 명령행 흔적”**을 넓게 잡는 용도입니다.

```regex
(?i)\b(?:appinstaller|bitsadmin|certoc|certreq|certutil|cmd|configsecuritypolicy|desktopimgdownldr|diantz|esentutl|expand|extrac32|findstr|finger|ftp|hh|ieexec|imewdbld|installutil|ldifde|makecab|mpcmdrun|msedge|msedge_proxy|mshta|ngen|presentationhost|printbrm|replace|winget|xwizard|excel|msaccess|msohtmed|mspub|msxsl|powerpnt|protocolhandler|squirrel|update|visio|vslaunchbrowser|winproj|winword|wsl|xsd)(?:\.exe)?\b.*?(?:(?:https?|ftp)://[^\s"'<>]+|\\\\[^\s"'<>]+\\[^\s"'<>]+(?:\\[^\s"'<>]+)*|ms-appinstaller://\?source=[^\s"'<>]+)
```

### 의미

* 특정 LOLBAS 실행파일명
* 뒤에

  * URL
  * UNC 경로
  * AppInstaller URI
* 가 있으면 탐지

### 한계

* `cmdl32.exe`, `Mmc.exe`, `OneDriveStandaloneUpdater.exe`처럼
  **URL이 명령행에 직접 안 보이는 타입**은 잘 못 잡습니다.
* `bitsadmin`처럼 여러 토큰 흐름이 긴 경우 정확도가 떨어집니다.
* `devtunnel.exe`는 다운로드보다 **Tunnel**로 별도 관리하는 것이 맞습니다.

즉, 이 1개는 **1차 broad hit** 용도로만 추천합니다.

---

# B안: 운영용 그룹 regex 7개

이게 추천안입니다.

---

## Group 1. direct URL after exe

가장 단순한 유형입니다.
`hh.exe <url>`, `mshta.exe <url>` 같은 계열.

### 대상 예

* `hh.exe`
* `mshta.exe`
* `configsecuritypolicy.exe`
* `installutil.exe`
* `ngen.exe`
* `presentationhost.exe`
* `ecmangen.exe`
* `xsd.exe`
* `protocolhandler.exe`

### regex

```regex
(?i)\b(?:hh|mshta|configsecuritypolicy|installutil|ngen|presentationhost|ecmangen|xsd|protocolhandler)(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s"'<>]+)
```

---

## Group 2. option + URL type

URL 앞에 옵션이 오는 유형입니다.

### 대상 예

* `certutil.exe -urlcache -f <url> ...`
* `certreq.exe -post -config <url> ...`
* `desktopimgdownldr.exe /lockscreenurl:<url>`
* `certoc.exe -GetCACAPS <url>`

### regex

```regex
(?i)\b(?:certutil|certreq|desktopimgdownldr|certoc)(?:\.exe)?\b.*?(?:(?:-urlcache|-verifyctl|-url|-post|-config|-getcacaps|/lockscreenurl:)\s*|/lockscreenurl:)(?:(?:https?|ftp)://[^\s"'<>]+)
```

### 조금 더 안전하게 분리하면

#### certutil

```regex
(?i)\bcertutil(?:\.exe)?\b.*?\b(?:-urlcache|-verifyctl|-url)\b.*?(?:(?:https?|ftp)://[^\s"'<>]+)
```

#### certreq

```regex
(?i)\bcertreq(?:\.exe)?\b.*?\b-post\b.*?\b-config\b\s+(?:(?:https?|ftp)://[^\s"'<>]+)
```

#### desktopimgdownldr

```regex
(?i)\bdesktopimgdownldr(?:\.exe)?\b.*?/lockscreenurl:(?:(?:https?|ftp)://[^\s"'<>]+)
```

---

## Group 3. Office URL open type

Office 앱에 URL을 직접 넘기는 유형입니다.

### 대상 예

* `winword.exe <url>`
* `excel.exe <url>`
* `powerpnt.exe <url>`

### regex

```regex
(?i)\b(?:excel|msaccess|msohtmed|mspub|powerpnt|visio|winproj|winword)(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s"'<>]+)
```

---

## Group 4. UNC / SMB / WebDAV copy type

URL이 아니라 원격 파일 경로를 사용하는 유형입니다.

### 대상 예

* `expand \\host\share\file ...`
* `extrac32 ... \\host\share\file ...`
* `findstr ... \\host\share\file > local`
* `cmd.exe ... type \\host\share\file > local`
* `replace.exe \\host\share\... localdir`

### regex

```regex
(?i)\b(?:cmd|expand|extrac32|findstr|replace|esentutl|diantz|makecab|printbrm)(?:\.exe)?\b.*?(\\\\[^\s"'<>]+\\[^\s"'<>]+(?:\\[^\s"'<>]+)*)
```

### 실무 팁

이 그룹은 **Download라기보다 Remote file pull/copy** 성격이 강해서
별도 태그를 붙이면 좋습니다.

```text
Download.RemotePath
```

---

## Group 5. BITS job type

`bitsadmin`은 전용 규칙으로 따로 잡는 게 좋습니다.

### regex

```regex
(?i)\bbitsadmin(?:\.exe)?\b.*?\b/addfile\b.*?\b[a-z0-9_-]+\b\s+(?:(?:https?|ftp)://[^\s"'<>]+)\s+[^\r\n]+
```

조금 느슨하게 하려면:

```regex
(?i)\bbitsadmin(?:\.exe)?\b.*?\b/addfile\b.*?(?:(?:https?|ftp)://[^\s"'<>]+)
```

---

## Group 6. AppInstaller / package / updater type

앱 패키지/업데이트 계열입니다.

### 대상 예

* `start ms-appinstaller://?source=<url>`
* `winget ...`
* `squirrel.exe --download <url>` 계열
* `update.exe --download <url>` 계열

### regex

```regex
(?i)(?:\bstart\s+ms-appinstaller://\?source=(?:(?:https?|ftp)://[^\s"'<>]+))|(?:\b(?:winget|squirrel|update)(?:\.exe)?\b.*?(?:(?:https?|ftp)://[^\s"'<>]+))
```

---

## Group 7. transform / remote loader type

원격 리소스를 입력으로 받아 처리하는 유형입니다.

### 대상 예

* `msxsl.exe a.xml b.xsl`
* `ieexec.exe <url>`
* `wsl.exe ... /dev/tcp/...`
* `ftp.exe -s:file.txt`

### regex

```regex
(?i)\b(?:msxsl|ieexec|wsl|ftp)(?:\.exe)?\b.*?(?:(?:https?|ftp)://[^\s"'<>]+|/dev/tcp/\d{1,3}(?:\.\d{1,3}){3}/\d+|-s:\S+)
```

---

# 탐지 정책 설계 권장 구조

실전에서는 아래 3단계가 제일 좋습니다.

## 정책 1. Broad prefilter

```regex
(?i)\b(?:appinstaller|bitsadmin|certoc|certreq|certutil|cmd|configsecuritypolicy|desktopimgdownldr|diantz|esentutl|expand|extrac32|findstr|finger|ftp|hh|ieexec|imewdbld|installutil|ldifde|makecab|mpcmdrun|msedge|msedge_proxy|mshta|ngen|presentationhost|printbrm|replace|winget|xwizard|excel|msaccess|msohtmed|mspub|msxsl|powerpnt|protocolhandler|squirrel|update|visio|vslaunchbrowser|winproj|winword|wsl|xsd)(?:\.exe)?\b
```

## 정책 2. URL/UNC 존재 여부

```regex
(?i)(?:(?:https?|ftp)://[^\s"'<>]+|\\\\[^\s"'<>]+\\[^\s"'<>]+(?:\\[^\s"'<>]+)*|ms-appinstaller://\?source=[^\s"'<>]+)
```

## 정책 3. 그룹형 상세 규칙

위 7개 그룹 regex

---

# 추천 결론

## 가장 추천

**1개 + 7개 구조**

* 1개: broad prefilter
* 7개: 유형별 상세 탐지

이유는 간단합니다.

```text
1개만 쓰면 너무 넓음
55개 개별로 쓰면 관리가 무거움
7개 정도면 운영 가능
```

---

# 꼭 분리해야 하는 예외

다음은 **Download 정규식 하나로 묶지 않는 것**이 좋습니다.

## 1. devtunnel.exe

* Download보다 `Tunnel`
* 별도 정책 추천

## 2. Mmc.exe

* 명령행에서 URL이 직접 안 보일 수 있음
* parent-child / file artifact 기반 탐지 필요

## 3. OneDriveStandaloneUpdater.exe

* URL이 registry/config에 숨을 수 있음
* command line 단독 regex 한계

## 4. cmdl32.exe

* URL이 config 파일 내부에 있음
* command line 단독으로는 indirect download

---

# 바로 써볼 수 있는 실무형 예시

## Sigma/EDR용 간단 버전

```regex
(?i)\b(?:hh|mshta|certutil|certreq|desktopimgdownldr|bitsadmin|winword|excel|powerpnt|visio|installutil|ngen|protocolhandler)(?:\.exe)?\b.*?(?:(?:https?|ftp)://[^\s"'<>]+|ms-appinstaller://\?source=[^\s"'<>]+|\\\\[^\s"'<>]+\\[^\s"'<>]+(?:\\[^\s"'<>]+)*)
```

이건 **핵심 다운로드형만 우선 잡는 축약본**입니다.

---
