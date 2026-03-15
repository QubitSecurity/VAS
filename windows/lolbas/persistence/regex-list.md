# LOLBAS Persistence (14) 탐지용 정규식 정책표
작성일: 2026-03-14

이 문서는 앞서 정리한 **사용자 정의 Persistence 그룹 14개**를 기준으로, **CLI 출력 / Process CommandLine 문자열 기반 탐지 정책**을 설계하기 위한 운영용 정리본입니다.

> **중요:** 여기의 `Persistence (14)`는 LOLBAS 공식 카테고리를 그대로 옮긴 것이 아닙니다.  
> 이 버킷에는 `At.exe`, `Bitsadmin.exe`, `Dnscmd.exe`, `Pnputil.exe`, `Runonce.exe`, `Sc.exe`, `Schtasks.exe`, `Update.exe`처럼 **직접적인 지속성 확보**에 가까운 도구와, `Colorcpl.exe`, `dtutil.exe`, `Print.exe`, `Regedit.exe`, `Regini.exe`, `Tar.exe`처럼 **은닉·스테이징을 통해 지속성을 보조하는 persistence-adjacent 도구**가 함께 들어 있습니다.  
> 따라서 이 문서는 **직접 persistence**와 **staging/stealth 보조 수단**을 분리해서 다룹니다.

## 설계 원칙
- 기본 전제: **도구명 + persistence 신호(예약 생성, 서비스/드라이버 등록, Startup/RunOnce, ADS, trusted path staging)** 를 함께 봅니다.
- 정규식은 모두 **대소문자 무시 `(?i)`** 기준입니다.
- `At.exe`, `Bitsadmin.exe`, `Schtasks.exe`, `Sc.exe`, `Dnscmd.exe`, `Pnputil.exe`, `Runonce.exe`, `Update.exe`는 **직접 persistence** 또는 그에 매우 가까운 패턴입니다.
- `Colorcpl.exe`, `dtutil.exe`, `Print.exe`, `Regedit.exe`, `Regini.exe`, `Tar.exe`는 단독으로는 **지속성 그 자체가 아니라 스테이징/은닉**인 경우가 많습니다. 따라서 단독 탐지 시 **severity 를 낮추고** 후속 이벤트와 결합하는 편이 좋습니다.
- 서비스/작업/드라이버 계열은 **Registry / Service Control Manager / Task Scheduler / Driver Install 이벤트**와 함께 봐야 정확도가 올라갑니다.
- ADS 계열은 **파일 생성/열기 이벤트**, **후속 import/load/execute 이벤트**, **Startup/Run key 생성 여부**까지 같이 봐야 진짜 persistence로 판별할 수 있습니다.
- 개발자 단말, SQL/ETL 서버, DNS 서버, 패키지 배포 서버는 정상 운영 도구와 겹칠 수 있으므로 allowlist 또는 baseline 분리가 필요합니다.

## 권장 운영 구조
1. **Broad prefilter**: Persistence 계열 후보 도구명을 먼저 좁힙니다.  
   - <code>(?i)\b(?:at&#124;bitsadmin&#124;colorcpl&#124;dnscmd&#124;dtutil&#124;pnputil&#124;print&#124;regedit&#124;regini&#124;runonce&#124;sc&#124;schtasks&#124;tar&#124;update)(?:\.exe)?\b</code>
2. **그룹 규칙**: 예약 작업/서비스/Run/Startup/ADS·staging 으로 나눠 적용합니다.
3. **상관분석 규칙**
   - `at`, `schtasks`, `bitsadmin` → 작업 생성/완료 후 child process, Task/Job artifact
   - `sc`, `dnscmd`, `pnputil` → 서비스/드라이버/플러그인 등록, Registry/SCM 이벤트
   - `runonce`, `update` → RunOnce/Startup folder 변경
   - `print`, `regedit`, `regini`, `tar` → ADS 파일/레지스트리 흔적 + 후속 import/execute
   - `colorcpl`, `dtutil` → trusted path/package store 복사 후 후속 로드/실행
4. **환경 allowlist**
   - IT 운영 자동화 서버
   - DNS 서버 / DnsAdmins 관리 구간
   - SQL Server / SSIS 서버
   - 정식 드라이버 배포 구간
   - Teams / Electron / Squirrel 기반 승인 앱

## 그룹 요약
| Group | Count | 운영 의미 |
|---|---:|---|
| Scheduled Task / Job Persistence | 3 | 예약 작업, BITS callback, 스케줄 기반 명령 실행으로 지속성을 확보하는 계열 |
| Service / Driver / Auto-Start Persistence | 3 | 서비스·DNS plugin·드라이버 설치처럼 재부팅 후에도 남는 자동 시작 계열 |
| Run / Startup Trigger Persistence | 2 | RunOnce / Startup shortcut 같은 로그인 트리거 계열 |
| Trusted Path / Package Staging (Persistence-Adjacent) | 2 | 신뢰 경로 또는 패키지 저장소로 복사해 후속 지속성에 쓰는 계열 |
| ADS / Hidden Registry Staging (Persistence-Adjacent) | 4 | ADS/숨김 레지스트리 데이터를 저장해 후속 import/load를 준비하는 계열 |

## 그룹 공통 정규식
- **Scheduled Task / Job Persistence**: <code>(?i)(?:\bat(?:\.exe)?\b(?:\s+\\\\[^\s"\']+)?\s+\d{1,2}:\d{2}\b(?:.*?\b/(?:interactive&#124;every&#124;next):?[^\s"\']*)?.+&#124;\bbitsadmin(?:\.exe)?\b.*?\b/SetNotifyCmdLine\b\s+\S+\s+\S+(?:\s+.+)?&#124;\bschtasks(?:\.exe)?\b.*?\b/create\b.*?(?:\b/sc\b\s+\S+&#124;\b/xml\b\s+\S+).*?(?:\b/tn\b\s+\S+&#124;\b/tr\b\s+[^\r\n]+))</code>
- **Service / Driver / Auto-Start Persistence**: <code>(?i)(?:\bdnscmd(?:\.exe)?\b\s+\S+\s+/config\s+/serverlevelplugindll\s+(?:\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*\.dll\b&#124;[A-Za-z]:\\[^\r\n"\']+\.dll\b)&#124;\bpnputil(?:\.exe)?\b.*?(?:\b-i\b.*?\b-a\b&#124;\b/add-driver\b).*?\.inf\b(?:.*?(?:\b/install\b&#124;\b-i\b))?&#124;\bsc(?:\.exe)?\b(?:\s+\\\\[^\s"\']+)?\s+(?:create\b.*?\bbinpath=\s*[^\r\n]+&#124;config\b.*?(?:\bstart=\s*auto\b&#124;\bbinpath=\s*[^\r\n]+)))</code>
- **Run / Startup Trigger Persistence**: <code>(?i)(?:\brunonce(?:\.exe)?\b.*?\b(?:/AlternateShellStartup&#124;/RunOnceExProcess)\b&#124;\bupdate(?:\.exe)?\b.*?\b--createShortcut(?:=&#124;\s+)[^\s"\']+\.exe\b.*?(?:-l=Startup&#124;--shortcut-locations=Startup)\b)</code>
- **Trusted Path / Package Staging (Persistence-Adjacent)**: <code>(?i)(?:\bcolorcpl(?:\.exe)?\b\s+(?:"?(?:[A-Za-z]:\\&#124;\\\\)[^"\r\n]+)"?&#124;\bdtutil(?:\.exe)?\b.*?\b/FILE\b\s+[^\r\n]+?\s+\b/COPY\b\s+FILE;[^\r\n]+)</code>
- **ADS / Hidden Registry Staging (Persistence-Adjacent)**: <code>(?i)(?:\bprint(?:\.exe)?\b\s+/D:&#124;\bregedit(?:\.exe)?\b(?:\s+/E)?\s+[^\r\n]*:[^\\/\s:"\']+\.(?:reg&#124;txt&#124;dat)\b&#124;\bregini(?:\.exe)?\b\s+[^\r\n]*:[^\\/\s:"\']+\.(?:ini&#124;txt)\b&#124;\btar(?:\.exe)?\b\s+-[cx]f\b\s+[^\r\n]*:[^\\/\s:"\']+)</code>

## 특기사항
- `At.exe`는 LOLBAS 기준 **Windows 7 or older** 중심의 레거시 스케줄링 패턴이라 현대 단말에서는 **매우 강한 희귀성 시그널**입니다.
- `Bitsadmin.exe`는 LOLBAS와 Microsoft Learn 모두 **job completion 시 command line 을 실행**하는 `SetNotifyCmdLine`을 문서화하고 있습니다. 이 값은 persistence/execute 양쪽에서 중요합니다.
- `Pnputil.exe`는 LOLBAS 예시가 예전 문법인 `-i -a`를 쓰지만, 최신 Microsoft Learn 문서는 `/add-driver ... /install` 문법도 제공합니다. 운영 규칙은 **양쪽을 모두 포함**하는 것이 좋습니다.
- `Runonce.exe`의 핵심은 명령행보다 **RunOnce / Active Setup 레지스트리 키**입니다. 그래서 `runonce.exe /AlternateShellStartup` 단독 매치보다 **직전 레지스트리 변경**과 결합했을 때 품질이 크게 올라갑니다.
- `Update.exe`는 LOLBAS 페이지에 **`--createShortcut=... -l=Startup`** 패턴이 직접 나옵니다. 이건 다운로드/실행보다 **명시적 startup persistence** 시그널이라 따로 빼는 것이 좋습니다.
- `Colorcpl.exe`, `dtutil.exe`, `Print.exe`, `Regedit.exe`, `Regini.exe`, `Tar.exe`는 단독으로는 `Persistence`보다 `Staging/Stealth`에 가깝습니다. 이 문서에서는 앞선 재분류 문서와 동일하게 **Persistence-adjacent**로 유지했습니다.
- `Sc.exe`는 로컬 서비스 생성뿐 아니라 `\\server` 원격 서비스 생성도 가능합니다. 이 경우 `Remote`와 `Persistence`가 동시에 성립할 수 있습니다.

## 정책표
| No | Tool | Group | Regex | FP 주의 | FN 주의 | 권장 정책명 |
|---:|---|---|---|---|---|---|
| 1 | At.exe | Scheduled Task / Job Persistence | <code>(?i)\bat(?:\.exe)?\b(?:\s+\\\\[^\s"\']+)?\s+\d{1,2}:\d{2}\b(?:\s+/interactive\b)?(?:\s+/(?:every&#124;next):[^\s"\']+)?\s+.+</code> | 레거시 시스템의 정상 작업 예약, 호환성 스크립트, 구형 운영 자동화와 겹칠 수 있습니다. 다만 현대 Windows 환경에서는 자체 사용 빈도가 매우 낮습니다. | 원격 ATSVC/RPC 호출만 남고 로컬 명령행이 생략되거나, 실제 명령이 배치 파일 내부에서 조합되면 놓칠 수 있습니다. Task 생성 흔적과 함께 보는 편이 좋습니다. | `LOLBAS.Persistence.AT.RecurringTask` |
| 2 | Bitsadmin.exe | Scheduled Task / Job Persistence | <code>(?i)\bbitsadmin(?:\.exe)?\b.*?\b/SetNotifyCmdLine\b\s+\S+\s+\S+(?:\s+.+)?</code> | 정상 BITS 작업 완료 후 후속 프로그램을 실행하는 엔터프라이즈 배포/업데이트와 겹칠 수 있습니다. 하지만 일반 사용자 단말에서는 드문 패턴입니다. | BITS job 생성, addfile, resume, complete가 여러 개의 별도 프로세스로 나뉘면 단일 명령행만으로는 체인을 놓칠 수 있습니다. Job GUID 상관분석이 필요합니다. | `LOLBAS.Persistence.BITS.NotifyCmdLine` |
| 3 | Schtasks.exe | Scheduled Task / Job Persistence | <code>(?i)\bschtasks(?:\.exe)?\b.*?\b/create\b.*?(?:\b/sc\b\s+(?:onlogon&#124;onstart&#124;onidle&#124;daily&#124;weekly&#124;monthly&#124;minute&#124;hourly&#124;once)\b&#124;\b/xml\b\s+[^\s"\']+).*?(?:\b/tn\b\s+[^\s"\']+&#124;\b/tr\b\s+[^\r\n]+)</code> | IT 운영 자동화, 소프트웨어 배포, 백업, 에이전트 설치 등 정상 예약 작업과 많이 겹칩니다. 승인된 작업 이름과 배포 서버 baseline이 중요합니다. | /XML 안에 실제 동작이 숨거나, 원격 /S 대상 호스트에서만 후속 실행이 보이면 로컬 명령행만으로는 세부 payload를 놓칠 수 있습니다. | `LOLBAS.Persistence.Schtasks.Create` |
| 4 | Dnscmd.exe | Service / Driver / Auto-Start Persistence | <code>(?i)\bdnscmd(?:\.exe)?\b\s+\S+\s+/config\s+/serverlevelplugindll\s+(?:\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*\.dll\b&#124;[A-Za-z]:\\[^\r\n"\']+\.dll\b)</code> | 정상 DNS 서버 관리나 DnsAdmins 운영 중 플러그인 변경과 겹칠 수 있으나, 일반 워크스테이션에서는 거의 나오지 않는 강한 시그널입니다. | 플러그인 경로가 변수·스크립트에서 조합되거나 후속 단계에서 레지스트리로만 남는 경우 일부 누락될 수 있습니다. DNS 서비스 재시작과 대상 서버 이벤트를 같이 보세요. | `LOLBAS.Persistence.DNSCMD.ServerLevelPluginDLL` |
| 5 | Pnputil.exe | Service / Driver / Auto-Start Persistence | <code>(?i)\bpnputil(?:\.exe)?\b.*?(?:\b-i\b.*?\b-a\b&#124;\b/add-driver\b).*?\.inf\b(?:.*?(?:\b/install\b&#124;\b-i\b))?</code> | 정상 드라이버 설치·업데이트·배포와 겹칠 수 있습니다. 제조사 서명, INF 경로, 실행 계정, 배포 시스템 여부로 추가 분기하는 것이 좋습니다. | 실제 악성 동작은 INF 내부가 가리키는 SYS/DLL 에 있으며, GUI 설치기나 다른 래퍼가 pnputil을 호출하면 명령행만으로는 세부 내용을 파악하기 어렵습니다. | `LOLBAS.Persistence.PNPUtil.DriverInstall` |
| 6 | Sc.exe | Service / Driver / Auto-Start Persistence | <code>(?i)\bsc(?:\.exe)?\b(?:\s+\\\\[^\s"\']+)?\s+(?:create\b.*?\bbinpath=\s*[^\r\n]+&#124;config\b.*?(?:\bstart=\s*auto\b&#124;\bbinpath=\s*[^\r\n]+))</code> | 정상 서비스 설치, 에이전트 업그레이드, 서버 운영 자동화와 겹칠 수 있습니다. 승인된 서비스명·설치 경로·배포 서버 allowlist가 필요합니다. | 서비스 생성은 정상처럼 보여도 실제 payload 는 이후 서비스 시작이나 ImagePath 변경 뒤에 실행될 수 있습니다. SCM/Registry 이벤트와 결합해야 확정도가 올라갑니다. | `LOLBAS.Persistence.SC.ServiceCreateOrAutoStart` |
| 7 | Runonce.exe | Run / Startup Trigger Persistence | <code>(?i)\brunonce(?:\.exe)?\b.*?\b(?:/AlternateShellStartup&#124;/RunOnceExProcess)\b</code> | 설치 프로그램, 복구 시퀀스, 일부 시스템 구성 작업에서 정상적으로 호출될 수 있습니다. 하지만 사용자 단말 일반 활동에서는 드문 편입니다. | 핵심은 RunOnce/Active Setup 레지스트리 키에 있으므로, 해당 키가 선행 등록되고 나중에 runonce.exe 만 호출되면 전체 문맥을 보지 못할 수 있습니다. | `LOLBAS.Persistence.RunOnce.Trigger` |
| 8 | Update.exe | Run / Startup Trigger Persistence | <code>(?i)\bupdate(?:\.exe)?\b.*?\b--createShortcut(?:=&#124;\s+)[^\s"\']+\.exe\b.*?(?:-l=Startup&#124;--shortcut-locations=Startup)\b</code> | Teams/Electron/Squirrel 기반 정상 애플리케이션이 Startup shortcut 을 만드는 경우와 겹칠 수 있습니다. 대상 EXE 경로와 publisher 기준 예외처리가 중요합니다. | 앱이 내부적으로 Update.exe 를 호출하거나 shortcut 생성 옵션이 다른 래퍼 스크립트에 감춰져 있으면 누락될 수 있습니다. Startup 폴더 파일 생성도 같이 봐야 합니다. | `LOLBAS.Persistence.Update.StartupShortcut` |
| 9 | Colorcpl.exe | Trusted Path / Package Staging (Persistence-Adjacent) | <code>(?i)\bcolorcpl(?:\.exe)?\b\s+(?:"?(?:[A-Za-z]:\\&#124;\\\\)[^"\r\n]+\.(?:exe&#124;dll&#124;scr&#124;cpl&#124;ps1&#124;vbs&#124;js&#124;hta&#124;bat&#124;cmd&#124;lnk)\b"?)</code> | 정상 color profile 파일(.icc/.icm) 처리와는 구분되지만, 드물게 테스트·관리 스크립트에서 임의 파일 복사 용도로 사용될 수 있습니다. | 비실행형 확장자를 먼저 복사한 뒤 나중에 이름을 바꾸거나 다른 도구가 후속 로드하면 이 규칙만으로는 놓칠 수 있습니다. 파일 생성 위치 추적이 중요합니다. | `LOLBAS.Persistence.Colorcpl.TrustedFolderStaging` |
| 10 | dtutil.exe | Trusted Path / Package Staging (Persistence-Adjacent) | <code>(?i)\bdtutil(?:\.exe)?\b.*?\b/FILE\b\s+[^\r\n]+?\s+\b/COPY\b\s+FILE;[^\r\n]+</code> | 정상 SQL Server Integration Services 패키지 복사·배포와 쉽게 겹칩니다. SQL/ETL 서버에서는 baseline 분리가 필수입니다. | 대상이 FILE 이 아니라 SQL/package store 인 경우, 혹은 후속 실행이 다른 SQL 구성요소에서 일어나는 경우는 이 규칙만으로는 한계가 있습니다. | `LOLBAS.Persistence.DTUtil.PackageCopy` |
| 11 | Print.exe | ADS / Hidden Registry Staging (Persistence-Adjacent) | <code>(?i)\bprint(?:\.exe)?\b\s+/D:(?:"?[^"\r\n]+(?::[^\\/\s:"\']+[^"\r\n]*)?"?)[ \t]+(?:"?(?:[A-Za-z]:\\&#124;\\\\)[^"\r\n]+\.(?:exe&#124;dll&#124;scr&#124;bat&#124;cmd&#124;ps1&#124;vbs&#124;js&#124;hta&#124;lnk)\b"?)</code> | 실제 프린터 대상으로 쓰는 정상 print.exe 사용과는 형태가 많이 다르지만, 레거시 스크립트가 파일 복사 목적으로 재활용할 가능성은 있습니다. | 비실행형 확장자 사용, ADS 이름 난독화, 혹은 source/destination 이 변수에서 조합되면 일부 누락될 수 있습니다. 파일 생성 이벤트를 함께 보세요. | `LOLBAS.Persistence.Print.ADSOrCopy` |
| 12 | Regedit.exe | ADS / Hidden Registry Staging (Persistence-Adjacent) | <code>(?i)\bregedit(?:\.exe)?\b(?:\s+/E\b\s+[^\r\n]*:[^\\/\s:"\']+\.reg\b(?:\s+HK(?:LM&#124;CU&#124;CR&#124;U&#124;CC)\\[^\r\n]+)?&#124;\s+[^\r\n]*:[^\\/\s:"\']+\.reg\b)</code> | 레지스트리 백업/복구 자체는 정상일 수 있으나, ADS 안의 .reg 를 import/export 하는 패턴은 매우 희귀합니다. | 동일 목적을 reg.exe import/export 로 수행하거나, ADS 이름이 .reg 확장자를 쓰지 않으면 놓칠 수 있습니다. 레지스트리 변경과 파일 열기 이벤트를 같이 확인하세요. | `LOLBAS.Persistence.Regedit.ADSImportExport` |
| 13 | Regini.exe | ADS / Hidden Registry Staging (Persistence-Adjacent) | <code>(?i)\bregini(?:\.exe)?\b\s+(?:-m\s+\\\\[^\s"\']+\s+)?[^\r\n]*:[^\\/\s:"\']+\.ini\b</code> | 레거시 레지스트리 자동화나 오래된 배포 스크립트와 겹칠 수 있으나, 일반 단말에서는 거의 보이지 않습니다. | 스크립트 파일이 .ini 가 아닌 다른 확장자이거나, 로컬 파일을 읽어 실제로 Run/Service 키를 만드는 내용이 파일 내부에 있으면 명령행만으로는 구분이 어렵습니다. | `LOLBAS.Persistence.Regini.ADSRegistryScript` |
| 14 | Tar.exe | ADS / Hidden Registry Staging (Persistence-Adjacent) | <code>(?i)\btar(?:\.exe)?\b\s+-[cx]f\b\s+(?:"?[^"\r\n]+:[^\\/\s:"\']+[^"\r\n]*"?)(?:\s+.+)?</code> | 일반 tar 사용은 흔할 수 있지만, tar 대상이 ADS(`file:stream`) 형태인 경우는 매우 드뭅니다. 그래서 단독으로도 꽤 좋은 시그널입니다. | 장기 옵션, 다른 flag 순서, bsdtar 호환 문법, 또는 ADS 대신 일반 archive 후속 rename 방식은 이 규칙에서 빠질 수 있습니다. | `LOLBAS.Persistence.Tar.ADSCompression` |


## 운영 권장안
- **1차**: <code>(?i)\b(?:at&#124;bitsadmin&#124;colorcpl&#124;dnscmd&#124;dtutil&#124;pnputil&#124;print&#124;regedit&#124;regini&#124;runonce&#124;sc&#124;schtasks&#124;tar&#124;update)(?:\.exe)?\b</code> 로 prefilter
- **2차**: 위 정책표의 개별 정규식 적용
- **3차 상관분석**
  - `At.exe` / `Schtasks.exe` → 작업 생성 artifact, 후속 taskrun child process
  - `Bitsadmin.exe` → `/SetNotifyCmdLine` + job GUID + 후속 child process
  - `Sc.exe` / `Dnscmd.exe` / `Pnputil.exe` → 서비스/드라이버/플러그인 등록 이벤트
  - `Runonce.exe` / `Update.exe` → RunOnce key / Startup 폴더 파일 생성
  - `Print.exe` / `Regedit.exe` / `Regini.exe` / `Tar.exe` → ADS 파일/레지스트리 흔적 + 후속 import/load/execute
  - `Colorcpl.exe` / `dtutil.exe` → trusted path/package copy 후 후속 실행
- **4차 severity 분리**
  - **High**: `Dnscmd.exe`, `Sc.exe`, `Schtasks.exe`, `Bitsadmin.exe`, `Pnputil.exe`, `Runonce.exe`, `Update.exe`
  - **Medium**: `At.exe` (현대 환경에서는 high 로 상향 가능)
  - **Low/Medium + Correlation 필수**: `Colorcpl.exe`, `dtutil.exe`, `Print.exe`, `Regedit.exe`, `Regini.exe`, `Tar.exe`

## 운영 메모
- 이 버킷은 **직접 persistence**와 **persistence-adjacent staging**이 섞여 있기 때문에, 모든 항목을 같은 severity 로 다루면 오탐과 피로도가 커집니다.
- 따라서 운영에서는 아래처럼 두 단계로 보는 것이 좋습니다.
  1. **Direct Persistence**  
     `At.exe`, `Bitsadmin.exe`, `Dnscmd.exe`, `Pnputil.exe`, `Runonce.exe`, `Sc.exe`, `Schtasks.exe`, `Update.exe`
  2. **Persistence-Adjacent / Stealth Staging**  
     `Colorcpl.exe`, `dtutil.exe`, `Print.exe`, `Regedit.exe`, `Regini.exe`, `Tar.exe`
- 특히 `Regedit.exe`, `Regini.exe`, `Print.exe`, `Tar.exe`는 **ADS(Alternate Data Streams)** 를 통해 payload 나 registry script 를 숨기기 때문에, 후속 `reg import`, `regedit`, `rundll32`, `schtasks`, `runonce`, `wscript` 같은 트리거와 묶였을 때 의미가 커집니다.
- `Update.exe`는 Teams/Electron 환경에서 정상 동작과 겹치므로, **대상 EXE 경로**, **publisher**, **Startup 폴더 shortcut 생성 여부**를 같이 보는 것이 좋습니다.
- `Pnputil.exe`는 드라이버 영속성 관점에서 강하지만, 실제 악성 여부는 **INF 내부가 가리키는 SYS/DLL/서비스 등록 내용**을 확인해야 합니다.

## 참고 출처
- LOLBAS API: https://lolbas-project.github.io/api/
- LOLBAS 메인 목록: https://lolbas-project.github.io/
- LOLBAS At.exe: https://lolbas-project.github.io/lolbas/Binaries/At/
- LOLBAS Bitsadmin.exe: https://lolbas-project.github.io/lolbas/Binaries/Bitsadmin/
- LOLBAS Dnscmd.exe: https://lolbas-project.github.io/lolbas/Binaries/Dnscmd/
- LOLBAS Pnputil.exe: https://lolbas-project.github.io/lolbas/Binaries/Pnputil/
- LOLBAS Runonce.exe: https://lolbas-project.github.io/lolbas/Binaries/Runonce/
- LOLBAS Update.exe: https://lolbas-project.github.io/lolbas/OtherMSBinaries/Update/
- LOLBAS Colorcpl.exe: https://lolbas-project.github.io/lolbas/Binaries/Colorcpl/
- LOLBAS dtutil.exe: https://lolbas-project.github.io/lolbas/OtherMSBinaries/Dtutil/
- LOLBAS Print.exe: https://lolbas-project.github.io/lolbas/Binaries/Print/
- LOLBAS Regedit.exe: https://lolbas-project.github.io/lolbas/Binaries/Regedit/
- LOLBAS Regini.exe: https://lolbas-project.github.io/lolbas/Binaries/Regini/
- LOLBAS Tar.exe: https://lolbas-project.github.io/lolbas/Binaries/Tar/
- Microsoft Learn - bitsadmin / setnotifycmdline: https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/bitsadmin-setnotifycmdline
- Microsoft Learn - schtasks /create: https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/schtasks-create
- Microsoft Learn - sc.exe create/config: https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/sc-create
- Microsoft Learn - dnscmd: https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/dnscmd
- Microsoft Learn - PnPUtil command syntax: https://learn.microsoft.com/en-us/windows-hardware/drivers/devtest/pnputil-command-syntax
- Microsoft Learn - Run and RunOnce registry keys: https://learn.microsoft.com/en-us/windows/win32/setupapi/run-and-runonce-registry-keys
- Microsoft Learn - regini: https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/regini
- Microsoft Learn - reg import: https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/reg-import
