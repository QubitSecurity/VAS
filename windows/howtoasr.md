## ASR 규칙 “모드” (WDAC의 Audit/Enforce 같은 운영 값)

ASR 규칙은 규칙별로 다음 상태로 운영합니다(Defender 기준). ([Microsoft Learn][1])

* **0 = Disabled / Not configured**
* **1 = Block**
* **2 = Audit**
* **6 = Warn** (경고 후 사용자가 24시간 우회 가능. OS 버전에 따라 동작 차이 존재)

PowerShell에선 보통 아래 문자열로 설정합니다. ([Microsoft Learn][2])

* `Enabled` / `AuditMode` / `Warn` / `Disabled`

---

## (핵심) 사용자가 언급한 ASR 규칙 2종

### 1) 오피스 자식 프로세스 차단

* **규칙명:** Block all Office applications from creating child processes
* **GUID:** `d4f940ab-401b-4efc-aadc-ad5f3c50688a` ([Microsoft Learn][1])
* **의미:** Word/Excel/PowerPoint/OneNote/Access 등이 **cmd/powershell/기타 프로세스를 자식으로 생성**하는 패턴을 차단(매크로 기반 드로퍼/다운로더에 매우 효과적). ([Microsoft Learn][1])
* **운영 팁:** 일부 업무용 매크로/애드인에서 합법적으로 프로세스를 띄우는 경우가 있어 **초기엔 Audit → 영향 확인 후 Block**이 안전합니다. (배제/예외를 남발하기보다는 감사로 먼저 확인 권장) ([Microsoft Learn][2])

> **중요(적용 조건):** 일부 Office 계열 ASR 규칙은 Office 실행 파일이 **%ProgramFiles% / %ProgramFiles(x86)% 경로에 설치된 경우에만 강제**될 수 있습니다(커스텀 경로 설치 시 미적용 이슈). ([Microsoft Learn][2])

### 2) 의심(난독) 스크립트 차단

* **규칙명:** Block execution of potentially obfuscated scripts
* **GUID:** `5beb7efe-fd9a-4556-801d-275e5ffc04cc` ([Microsoft Learn][1])
* **의미:** 난독화 특성이 있는 스크립트(js/vbs/ps/macro 등) 실행을 탐지/차단합니다. (PowerShell 스크립트 지원 포함) ([Microsoft Learn][1])
* **필수 조건:** **Cloud-delivered protection(클라우드 보호)**가 켜져 있어야 합니다. ([Microsoft Learn][1])
* **의존성:** Defender AV + AMSI + Cloud Protection ([Microsoft Learn][1])

---

## ASR 규칙 전체 목록 (GUID 매트릭스 기준)

아래는 Microsoft Learn의 **ASR rule-to-GUID matrix**에 있는 “현재 GA(일반 공급) 기준” 규칙 목록입니다. ([Microsoft Learn][1])


| No | 분류 | 코드 | 규칙(관리에서 보통 이렇게 묶어봄) | Rule name | GUID |
| --- | --- | --- | --- | --- | --- |
| 1 | 드라이버/커널 | 01Driver | 취약한 서명 드라이버 악용 차단 | Block abuse of exploited vulnerable signed drivers | `56a863a9-875e-4185-98a7-b882c64b5ce5` |
| 2 | 문서 리더 | 02Document Reader | Adobe Reader 자식 프로세스 차단 | Block Adobe Reader from creating child processes | `7674ba52-37eb-4a4f-a9a1-f0f9a1619a2c` |
| 3 | Office | 03OfficeChild | **Office 자식 프로세스 차단** | Block all Office applications from creating child processes | `d4f940ab-401b-4efc-aadc-ad5f3c50688a` |
| 4 | 자격증명 | 04Credential | LSASS 자격 증명 도용 차단 | Block credential stealing from the Windows local security authority subsystem (lsass.exe) | `9e6c4e1f-7d60-472f-ba1a-a39ef669e4b2` |
| 5 | 메일 | 05Email | 메일/웹메일 실행 파일 콘텐츠 차단 | Block executable content from email client and webmail | `be9ba2d9-53ea-4cdc-84e5-9b1eeee46550` |
| 6 | 실행/평판 | 06Execution | 평판/연령/신뢰 조건 미충족 실행 파일 차단 | Block executable files from running unless they meet a prevalence, age, or trusted list criterion | `01443614-cd74-433a-b99e-2ecdc07bfc25` |
| 7 | 스크립트 | 07Script | **난독 스크립트 차단** | Block execution of potentially obfuscated scripts | `5beb7efe-fd9a-4556-801d-275e5ffc04cc` |
| 8 | 스크립트/다운로더 | 08Downloader | JS/VBS가 다운로드 실행파일 실행하는 행위 차단 | Block JavaScript or VBScript from launching downloaded executable content | `d3e037e1-3eb8-44c8-a917-57927947596d` |
| 9 | Office | 09OfficeContent | Office가 실행 파일 콘텐츠 만드는 것 차단 | Block Office applications from creating executable content | `3b576869-a4ec-4529-8536-b80a7769e899` |
| 10 | Office | 10OfficeInjection | Office의 프로세스 인젝션 차단 | Block Office applications from injecting code into other processes | `75668c1f-73b5-4cf0-bb93-3ecf5cb7cc84` |
| 11 | Office(Outlook) | 11OfficeOutlook | Outlook(Office comm) 자식 프로세스 차단 | Block Office communication application from creating child processes | `26190899-1602-49e8-8b27-eb1d0a1ce869` |
| 12 | 지속성 | 12Persistence | WMI 이벤트 구독 통한 지속성 차단 | Block persistence through WMI event subscription | `e6db77e5-3df2-4cf1-b95a-636979351e5b` |
| 13 | 횡적이동 | 13LateralMovement | PsExec/WMI에서 시작된 프로세스 생성 차단 | Block process creations originating from PSExec and WMI commands | `d1e49aac-8f56-4280-b9ba-993a6d77406c` |
| 14 | 방해/복구 | 14Disruption | 안전모드 재부팅 악용 차단 | Block rebooting machine in Safe Mode | `33ddedf1-c6e0-47cb-833e-de6133960387` |
| 15 | USB | 15USB | USB 실행 신뢰불가/미서명 프로세스 차단 | Block untrusted and unsigned processes that run from USB | `b2b3f03d-6a65-4f7b-a9c7-1c7ef74a9ba4` |
| 16 | LOLBins | 16LOLBins | 복사/가장된 시스템 도구 악용 차단 | Block use of copied or impersonated system tools | `c0033c00-d16d-4114-a5a0-dc9b3a7d2ceb` |
| 17 | 서버(Exchange) | 17ServerExchange | 서버 웹셸 생성 차단 | Block Webshell creation for Servers | `a8f5898e-1dc8-49a9-9878-85004b8a61e6` |
| 18 | Office 매크로 | 18OfficeMacro | Win32 API 호출 매크로 차단 | Block Win32 API calls from Office macros | `92e97fa1-2edf-4476-bdd6-9dd0b4dddc7b` |
| 19 | 랜섬웨어 | 19Ransomware | 고급 랜섬웨어 보호 | Use advanced protection against ransomware | `c1db55ab-c21a-4637-bb3f-a12568109d35` |

---

## 운영/구성 관점에서의 “정리 포인트” (WDAC 항목처럼 관리하려면)

### 1) 전제 조건(ASR 공통)

* Defender AV가 **Primary antivirus**로 동작해야 하고, **실시간 보호**가 켜져 있어야 합니다. ([Microsoft Learn][2])
* 일부 규칙(예: 난독 스크립트/평판 기반 실행 차단/고급 랜섬웨어 보호)은 **Cloud-delivered protection**이 사실상 필수입니다. ([Microsoft Learn][2])

### 2) 추천 운영 순서

* **(1) Audit로 배포 → (2) 업무 영향/오탐 확인 → (3) Block(또는 Warn) 전환**
* 배제(exclusion)는 보호 수준을 크게 떨어뜨릴 수 있으므로 “우선 Audit로 검증”이 Microsoft 문서에서도 권장됩니다. ([Microsoft Learn][2])

### 3) PowerShell로 규칙 상태 확인/설정(참고)

Microsoft 문서 기준 예시는 아래 흐름입니다. ([Microsoft Learn][2])

```powershell
# 현재 ASR 규칙/상태 확인
Get-MpPreference

# (주의) Set-MpPreference는 기존 규칙 목록을 "덮어씀"
# 규칙 추가/누적은 Add-MpPreference가 안전
Add-MpPreference -AttackSurfaceReductionRules_Ids d4f940ab-401b-4efc-aadc-ad5f3c50688a `
                 -AttackSurfaceReductionRules_Actions AuditMode

# 차단 모드(Enabled=Block)
Add-MpPreference -AttackSurfaceReductionRules_Ids 5beb7efe-fd9a-4556-801d-275e5ffc04cc `
                 -AttackSurfaceReductionRules_Actions Enabled
```

---

원하시면, WDAC 항목(현재 `mgmt.category.SystemSettings.WDAC.*`처럼)과 같은 형태로:

* `mgmt.category.SystemSettings.ASR.<규칙명>` = `Block/Audit/Warn/Off`
* `mgmt.category.SystemSettings.ASR.Assigned` = `ON/OFF`(Defender 활성 + 규칙 목록 존재 여부)

같은 **“ASR 관리 키/출력 포맷” 정리본**까지 붙여서, WDAC/ASR을 한 화면에서 컴플라이언스로 보게 만드는 방식으로도 같이 정리해 드릴게요.

[1]: https://learn.microsoft.com/en-us/defender-endpoint/attack-surface-reduction-rules-reference "Attack surface reduction rules reference - Microsoft Defender for Endpoint | Microsoft Learn"
[2]: https://learn.microsoft.com/en-us/defender-endpoint/enable-attack-surface-reduction "Enable attack surface reduction rules - Microsoft Defender for Endpoint | Microsoft Learn"
