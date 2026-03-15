# LOLBAS Recon (18) 탐지용 정규식 정책표
작성일: 2026-03-14

이 문서는 앞서 정리한 **사용자 정의 Recon 그룹 18개**를 기준으로, **CLI 출력 / Process CommandLine 문자열 기반 탐지 정책**을 설계하기 위한 운영용 정리본입니다.

> **중요:** 여기의 `Recon (18)`은 LOLBAS 공식 `Reconnaissance` 태그만 그대로 옮긴 것이 아닙니다.  
> 이 버킷에는 `Pktmon.exe`, `Psr.exe` 같은 **정찰/관찰** 도구뿐 아니라, `Cmdkey.exe`, `Findstr.exe`, `Reg.exe`, `Rpcping.exe` 같은 **자격증명/시크릿 접근** 도구, 그리고 `Adplus.exe`, `Comsvcs.dll`, `Createdump.exe`, `Sqldumper.exe`, `wbadmin.exe`, `ntdsutil.exe` 같이 **프로세스 메모리 덤프 / NTDS 수집 / 스냅샷 기반 컬렉션** 도구가 함께 들어 있습니다.  
> 따라서 이 문서는 **Recon / Discovery**, **Credentials / Secrets Discovery**, **Process Memory Dump**, **AD / NTDS Snapshot & Collection**으로 나눠 다룹니다.

## 설계 원칙
- 기본 전제: **도구명 + 정찰/수집 신호(화면 기록, 패킷 캡처, cached credential 열람, hive save, NTLM 강제 인증, dump 옵션, NTDS/VSS/IFM 관련 키워드)** 를 함께 봅니다.
- 정규식은 모두 **대소문자 무시 `(?i)`** 기준입니다.
- 이 버킷은 **실행형 LOLBAS**보다 **수집형 LOLBAS** 비중이 높으므로, child process보다 **출력 파일 생성**, **메모리 덤프 파일**, **VSS snapshot**, **NTDS.dit 접근**, **네트워크 캡처 ETL/ZIP 생성**을 함께 보는 편이 좋습니다.
- `Pktmon.exe`, `Psr.exe`는 단독 명령행만으로도 의미가 있지만, **도움말/지원 목적의 정상 사용**과 겹칠 수 있으므로 실행 주체와 시간대를 같이 보는 것이 좋습니다.
- `Cmdkey.exe`, `Findstr.exe`, `Reg.exe`, `Rpcping.exe`는 **자격증명 탐색/강제 인증**과 연결되는 경우가 많습니다. 특히 `Reg.exe save HKLM\SAM/SYSTEM/SECURITY`, `Findstr.exe cpassword`, `rpcping ... NTLM`은 높은 품질의 운영 신호입니다.
- `Diskshadow.exe`, `dsdbutil.exe`, `ntdsutil.exe`, `wbadmin.exe`는 **스크립트 파일 / quoted subcommand / 백업 메타데이터**에 핵심 행위가 들어가므로, command line만으로는 세부 내용을 모두 복원하기 어렵습니다.
- `Comsvcs.dll`, `Createdump.exe`, `Dump64.exe`, `DumpMinitool.exe`, `rdrleakdiag.exe`, `Sqldumper.exe`, `Tttracer.exe`, `Adplus.exe`는 **메모리 덤프 도구** 성격이 강합니다. 일반 사용자 PC에서 나타나면 희귀성이 높고, 개발/디버깅/DBA 서버에서는 baseline 분리가 필요합니다.

## 권장 운영 구조
1. **Broad prefilter**: Recon 계열 후보 도구명을 먼저 좁힙니다.  
   - <code>(?i)(?:\b(?:adplus&#124;cmdkey&#124;createdump&#124;diskshadow&#124;dsdbutil&#124;dump64&#124;dumpminitool&#124;findstr&#124;ntdsutil&#124;pktmon&#124;psr&#124;reg&#124;rpcping&#124;rdrleakdiag&#124;sqldumper&#124;tttracer&#124;wbadmin)(?:\.exe)?\b&#124;\brundll32(?:\.exe)?\b.*?\bcomsvcs\.dll\b)</code>
2. **그룹 규칙**: 정찰/관찰, 자격증명 탐색, 메모리 덤프, NTDS/VSS 수집으로 나눠 적용합니다.
3. **상관분석 규칙**
   - `pktmon`, `psr` → `.etl`, `.zip`, 화면 캡처 산출물 생성
   - `cmdkey`, `findstr`, `reg`, `rpcping` → SYSVOL 접근, hive save 파일, 인증 시도 흔적, NTLM/RPC 네트워크 이벤트
   - `comsvcs`, `createdump`, `dump64`, `dumpminitool`, `rdrleakdiag`, `sqldumper`, `tttracer`, `adplus` → `.dmp`, `.mdmp`, `dump.bin`, `minidump_<PID>.dmp` 생성
   - `diskshadow`, `dsdbutil`, `ntdsutil`, `wbadmin` → VSS snapshot, mounted volume, `NTDS.dit`, `SYSTEM` hive 접근, 백업/복구 산출물
4. **환경 allowlist**
   - 개발자/디버깅 워크스테이션
   - Visual Studio / Windows SDK / .NET 런타임 설치 단말
   - SQL Server / Power BI / Analysis Services 서버
   - AD DS / AD LDS / 백업 서버 / 도메인 컨트롤러 운영 구간
   - 헬프데스크/지원용 녹화·패킷 캡처 승인 단말

## 그룹 요약
| Group | Count | 운영 의미 |
|---|---:|---|
| Recon / Discovery | 2 | 화면·네트워크를 관찰하거나 로컬 환경을 문서화하는 정찰 계열 |
| Credentials / Secrets Discovery | 4 | 저장된 자격증명, GPP cpassword, SAM/SYSTEM/SECURITY hive, 강제 NTLM 인증 같은 시크릿 접근 계열 |
| Process Memory Dump | 8 | 특정 PID 또는 LSASS 등 민감 프로세스 메모리를 덤프하는 계열 |
| AD / NTDS Snapshot & Collection | 4 | VSS/IFM/백업/복구 체인을 통해 NTDS.dit 와 SYSTEM hive를 수집하는 계열 |

## 그룹 공통 정규식
- **Recon / Discovery**: <code>(?i)(?:\bpktmon(?:\.exe)?\b\s+(?:start\b.*?--etw\b&#124;filter\s+add\b.*?-p\s+\d{1,5}\b)&#124;\bpsr(?:\.exe)?\b\s+/start\b.*?/output\s+\S+\.zip\b.*?/gui\s+0\b)</code>
- **Credentials / Secrets Discovery**: <code>(?i)(?:\bcmdkey(?:\.exe)?\b\s+/list\b&#124;\bfindstr(?:\.exe)?\b.*?\bcpassword\b.*?\\\\[^\s"']+\\sysvol\\.*?\\policies\\.*?\.xml\b&#124;\breg(?:\.exe)?\b\s+save\b\s+HKLM\\(?:SAM&#124;SYSTEM&#124;SECURITY)\b&#124;\brpcping(?:\.exe)?\b.*?(?:-s&#124;/s)\s+\S+.*?(?:-u&#124;/u)\s+NTLM\b)</code>
- **Process Memory Dump**: <code>(?i)(?:\badplus(?:\.exe)?\b.*?-hang\b.*?-pn\s+\S+\.exe\b.*?-o\s+\S+&#124;\brundll32(?:\.exe)?\b.*?\bcomsvcs\.dll\b\s*,?\s*MiniDump\b\s+\d+\s+\S+\s+full\b&#124;\bcreatedump(?:\.exe)?\b.*?-f\s+\S+\.dmp\b.*?\b\d+\b&#124;\bdump64(?:\.exe)?\b\s+\d+\s+\S+\.dmp\b&#124;\bdumpminitool(?:\.exe)?\b.*?--processId\s+\d+\b.*?--dumpType\s+Full\b&#124;\brdrleakdiag(?:\.exe)?\b.*?/p\s+\d+\b.*?/o\s+\S+.*?/fullmemdmp\b(?:.*?/(?:wait\s+1&#124;snap)\b)?&#124;\bsqldumper(?:\.exe)?\b\s+\d+\s+0\s+0x[0-9A-Fa-f:]+\b&#124;\btttracer(?:\.exe)?\b.*?-dumpFull\b.*?-attach\s+\d+\b)</code>
- **AD / NTDS Snapshot & Collection**: <code>(?i)(?:\bdiskshadow(?:\.exe)?\b\s+/s\s+\S+\b&#124;\bdsdbutil(?:\.exe)?\b.*?"activate instance ntds".*?"snapshot".*?"(?:create&#124;mount\s+[^"]+&#124;list all&#124;delete\s+[^"]+)"&#124;\bntdsutil(?:\.exe)?\b.*?"(?:ac i ntds&#124;activate instance ntds)".*?"ifm".*?"create full [^"]+"&#124;\bwbadmin(?:\.exe)?\b\s+start\s+(?:backup&#124;recovery)\b.*?(?:NTDS\.dit&#124;C:\\Windows\\NTDS\\NTDS\.dit)\b.*?(?:SYSTEM&#124;C:\\Windows\\System32\\config\\SYSTEM)\b)</code>

## 특기사항
- 이 버킷은 **공식 LOLBAS `Reconnaissance` 태그만 모은 문서가 아니라**, `Credentials`, `Dump`를 합친 **운영용 Recon 버킷**입니다.
- `Cmdkey.exe /list`, `Findstr.exe ... cpassword ... \\sysvol\...`, `Reg.exe save HKLM\SAM/SYSTEM/SECURITY`, `rpcping ... NTLM`은 **실행 우회보다 자격증명 접근/강제 인증** 쪽 신호로 해석하는 것이 맞습니다.
- `Comsvcs.dll`는 보통 **단독 실행이 아니라 `rundll32.exe`의 인자**로 보입니다. 따라서 broad prefilter 와 개별 규칙 모두 `rundll32 + comsvcs.dll + MiniDump` 조합으로 보는 것이 좋습니다.
- `Diskshadow.exe`는 **`/s`로 넘긴 스크립트 파일** 안에 핵심 행위가 들어 있습니다. 따라서 process command line 단독 탐지 뒤, **참조된 스크립트 파일의 내용 수집**이 가능하면 정확도가 크게 올라갑니다.
- `dsdbutil.exe`, `ntdsutil.exe`, `wbadmin.exe`는 **도메인 컨트롤러 / 백업 서버**의 정상 운영과 겹칠 수 있으므로, 서버 role 기반 allowlist 가 중요합니다.
- `Adplus.exe`, `Dump64.exe`, `DumpMinitool.exe`, `Createdump.exe`는 **Visual Studio / .NET / Windows SDK** 설치 환경에서 보일 수 있지만, 일반 사무용 단말에서는 대체로 희귀합니다.
- `Pktmon.exe start --etw` 와 `psr.exe /start ... /gui 0`는 **은밀한 관찰/기록** 쪽에서 의미가 큽니다. 헬프데스크 승인 사용 여부를 같이 확인하세요.

## 정책표
| No | Tool | Group | Regex | FP 주의 | FN 주의 | 권장 정책명 |
|---:|---|---|---|---|---|---|
| 1 | Pktmon.exe | Recon / Discovery | <code>(?i)\bpktmon(?:\.exe)?\b\s+(?:start\b.*?--etw\b&#124;filter\s+add\b.*?-p\s+\d{1,5}\b)</code> | 네트워크 문제 분석, 드라이버/패킷 디버깅, 헬프데스크 진단과 겹칠 수 있습니다. | `start`와 `filter add`가 여러 번 나뉘어 실행되면 단일 명령행만으로 전체 흐름을 놓칠 수 있습니다. `.etl` 파일 생성도 같이 보세요. | `LOLBAS.Recon.Pktmon.Capture` |
| 2 | Psr.exe | Recon / Discovery | <code>(?i)\bpsr(?:\.exe)?\b\s+/start\b.*?/output\s+\S+\.zip\b.*?/gui\s+0\b</code> | 사용자 지원·문제 재현을 위해 정상적으로 쓰일 수 있습니다. | 사용자가 GUI로 직접 시작했거나 `/gui 0` 없이 실행하면 이 규칙만으로는 놓칠 수 있습니다. `psr.exe /stop`과 ZIP 산출물도 같이 봐야 합니다. | `LOLBAS.Recon.PSR.SilentCapture` |
| 3 | Cmdkey.exe | Credentials / Secrets Discovery | <code>(?i)\bcmdkey(?:\.exe)?\b\s+/list\b</code> | 관리자나 사용자가 저장된 자격증명 점검 목적으로 정상 실행할 수 있습니다. | 공격자가 `/add`, `/generic`, `/delete` 같은 다른 서브명령을 쓰면 이 규칙은 잡지 못합니다. 이 문서는 Recon 관점의 `/list`에 집중합니다. | `LOLBAS.Recon.Cmdkey.ListCachedCreds` |
| 4 | Findstr.exe | Credentials / Secrets Discovery | <code>(?i)\bfindstr(?:\.exe)?\b.*?\bcpassword\b.*?\\\\[^\s"']+\\sysvol\\.*?\\policies\\.*?\.xml\b</code> | 레드팀/블루팀 검증, GPP 점검 스크립트와 겹칠 수 있습니다. 하지만 일반 사용자 단말에서는 매우 희귀합니다. | 대상 경로가 변수·배치파일 안에서 조합되거나 `Select-String` 같은 다른 도구로 대체되면 놓칠 수 있습니다. | `LOLBAS.Recon.Findstr.GPPCPassword` |
| 5 | Reg.exe | Credentials / Secrets Discovery | <code>(?i)\breg(?:\.exe)?\b\s+save\b\s+HKLM\\(?:SAM&#124;SYSTEM&#124;SECURITY)\b</code> | 백업/복구, 포렌식 수집, 일부 관리 자동화와 겹칠 수 있습니다. | 공격자는 `reg export`나 다른 도구(`esentutl`, `vssadmin` 체인)를 사용할 수 있습니다. 이 규칙은 hive save 자체에 초점을 둡니다. | `LOLBAS.Recon.Reg.SaveSensitiveHives` |
| 6 | Rpcping.exe | Credentials / Secrets Discovery | <code>(?i)\brpcping(?:\.exe)?\b.*?(?:-s&#124;/s)\s+\S+.*?(?:-u&#124;/u)\s+NTLM\b</code> | RPC 연결 점검, 인증 테스트, 운영 환경 진단과 겹칠 수 있습니다. | 실제 악용은 릴레이 대상 포트/엔드포인트에 의미가 있으므로 네트워크 로그와 결합해야 합니다. Kerberos 등 다른 인증 옵션은 이 규칙에서 빠집니다. | `LOLBAS.Recon.Rpcping.NTLMProbe` |
| 7 | Adplus.exe | Process Memory Dump | <code>(?i)\badplus(?:\.exe)?\b.*?-hang\b.*?-pn\s+\S+\.exe\b.*?-o\s+\S+</code> | 개발/디버깅 워크스테이션, Windows SDK 환경의 정상 크래시 분석과 겹칠 수 있습니다. | LOLBAS 예시의 `-c {PATH:.xml}` 기반 덤프/실행은 별도 규칙이 필요합니다. 실제 대상 프로세스가 설정 파일 내부에 있으면 command line만으로 모를 수 있습니다. | `LOLBAS.Recon.Adplus.ProcessDump` |
| 8 | Comsvcs.dll | Process Memory Dump | <code>(?i)\brundll32(?:\.exe)?\b.*?\bcomsvcs\.dll\b\s*,?\s*MiniDump\b\s+\d+\s+\S+\s+full\b</code> | 정상 운영에서 거의 보기 어렵지만, 일부 포렌식/IR 수집 도구가 유사 패턴을 쓸 수 있습니다. | 인자 난독화, 경로 축약, `full` 대신 변형 인자가 사용되면 일부 누락될 수 있습니다. 프로세스 접근 이벤트와 `.dmp` 생성도 함께 보세요. | `LOLBAS.Recon.Comsvcs.MiniDump` |
| 9 | Createdump.exe | Process Memory Dump | <code>(?i)\bcreatedump(?:\.exe)?\b.*?-f\s+\S+\.dmp\b.*?\b\d+\b</code> | .NET 런타임/개발 환경에서 정상 crash dump 수집과 겹칠 수 있습니다. | 기본 출력 경로(`%TEMP%\dump.%p.dmp`)를 쓰고 `-f`를 생략하면 이 규칙은 놓칠 수 있습니다. 대상 PID 식별도 별도 상관분석이 필요합니다. | `LOLBAS.Recon.Createdump.ProcessDump` |
| 10 | Dump64.exe | Process Memory Dump | <code>(?i)\bdump64(?:\.exe)?\b\s+\d+\s+\S+\.dmp\b</code> | Visual Studio 설치/테스트 환경에서 정상 덤프 수집과 겹칠 수 있습니다. | 출력 파일명이 `.dmp`가 아니거나 래퍼 스크립트가 인자를 조합하면 누락될 수 있습니다. 일반 사용자 단말에서는 희귀성을 우선 보세요. | `LOLBAS.Recon.Dump64.ProcessDump` |
| 11 | DumpMinitool.exe | Process Memory Dump | <code>(?i)\bdumpminitool(?:\.exe)?\b.*?--file\s+\S+.*?--processId\s+\d+\b.*?--dumpType\s+Full\b</code> | Visual Studio Test Platform, 개발자/QA 환경과 겹칠 수 있습니다. | `--dumpType` 값이 `Mini` 등으로 바뀌거나 인자 순서가 바뀌면 변형 규칙이 추가로 필요할 수 있습니다. | `LOLBAS.Recon.DumpMinitool.FullDump` |
| 12 | rdrleakdiag.exe | Process Memory Dump | <code>(?i)\brdrleakdiag(?:\.exe)?\b.*?/p\s+\d+\b.*?/o\s+\S+.*?/fullmemdmp\b(?:.*?/(?:wait\s+1&#124;snap)\b)?</code> | 메모리 누수 진단, 파일 서버 문제 분석과 겹칠 수 있습니다. | 후속 `/snap` 실행이나 출력 파일 사용은 별도 이벤트와 결합해야 합니다. PID만으로 대상 프로세스를 파악해야 하는 한계가 있습니다. | `LOLBAS.Recon.Rdrleakdiag.FullMemDump` |
| 13 | Sqldumper.exe | Process Memory Dump | <code>(?i)\bsqldumper(?:\.exe)?\b\s+\d+\s+0\s+0x[0-9A-Fa-f:]+\b</code> | SQL Server, Analysis Services, Power BI Desktop 환경에서 정상 진단과 겹칠 수 있습니다. | 플래그 값만으로는 대상 프로세스가 LSASS인지 SQL 프로세스인지 구분되지 않습니다. PID 상관분석과 생성된 `.mdmp` 파일 확인이 필요합니다. | `LOLBAS.Recon.SqlDumper.ProcessDump` |
| 14 | Tttracer.exe | Process Memory Dump | <code>(?i)\btttracer(?:\.exe)?\b.*?-dumpFull\b.*?-attach\s+\d+\b</code> | Time Travel Debugging, 개발/디버깅 워크스테이션에서 정상적으로 보일 수 있습니다. | `tttracer.exe`는 실행/추적 용도도 있어, dump 관련 옵션 없이 쓰인 경우는 이 규칙에서 제외됩니다. | `LOLBAS.Recon.TTTracer.FullDump` |
| 15 | Diskshadow.exe | AD / NTDS Snapshot & Collection | <code>(?i)\bdiskshadow(?:\.exe)?\b\s+/s\s+\S+\b</code> | 백업 서버, 스토리지 관리, 운영 점검 스크립트와 겹칠 수 있습니다. | 실제 NTDS/VSS abuse는 `/s`로 전달된 스크립트 안에 있으므로, 스크립트 파일 내용이 없으면 의도를 완전히 판별하기 어렵습니다. | `LOLBAS.Recon.Diskshadow.ScriptedVSS` |
| 16 | dsdbutil.exe | AD / NTDS Snapshot & Collection | <code>(?i)\bdsdbutil(?:\.exe)?\b.*?"activate instance ntds".*?"snapshot".*?"(?:create&#124;mount\s+[^"]+&#124;list all&#124;delete\s+[^"]+)"</code> | AD LDS/도메인 컨트롤러 운영, 디렉터리 서비스 유지보수와 겹칠 수 있습니다. | 줄바꿈 또는 여러 quoted subcommand 조합이 달라지면 일부 누락될 수 있습니다. 스냅샷 생성 후 `copy` 명령은 별도 이벤트에서 봐야 합니다. | `LOLBAS.Recon.DSDBUtil.NTDSSnapshot` |
| 17 | ntdsutil.exe | AD / NTDS Snapshot & Collection | <code>(?i)\bntdsutil(?:\.exe)?\b.*?"(?:ac i ntds&#124;activate instance ntds)".*?"ifm".*?"create full [^"]+"</code> | 도메인 컨트롤러의 합법적 IFM(Install From Media) 작업과 겹칠 수 있습니다. | 약식/정식 명령 문자열이 조금씩 다를 수 있고, 따옴표 없이 인터랙티브로 입력하면 process command line만으로는 모두 보이지 않을 수 있습니다. | `LOLBAS.Recon.NTDSUtil.IFMCreate` |
| 18 | wbadmin.exe | AD / NTDS Snapshot & Collection | <code>(?i)\bwbadmin(?:\.exe)?\b\s+start\s+(?:backup&#124;recovery)\b.*?(?:NTDS\.dit&#124;C:\\Windows\\NTDS\\NTDS\.dit)\b.*?(?:SYSTEM&#124;C:\\Windows\\System32\\config\\SYSTEM)\b</code> | 정상 백업/복구 작업, DR 리허설, 백업 오퍼레이터 활동과 겹칠 수 있습니다. | `wbadmin get versions` 후속 체인이나 include/items가 스크립트/별도 파라미터로 나뉘면 단일 명령행 규칙만으로는 체인이 끊길 수 있습니다. | `LOLBAS.Recon.WBAdmin.NTDSBackupOrRecovery` |

## 운영 권장안
- **1차**: <code>(?i)(?:\b(?:adplus&#124;cmdkey&#124;createdump&#124;diskshadow&#124;dsdbutil&#124;dump64&#124;dumpminitool&#124;findstr&#124;ntdsutil&#124;pktmon&#124;psr&#124;reg&#124;rpcping&#124;rdrleakdiag&#124;sqldumper&#124;tttracer&#124;wbadmin)(?:\.exe)?\b&#124;\brundll32(?:\.exe)?\b.*?\bcomsvcs\.dll\b)</code> 로 prefilter
- **2차**: 위 정책표의 개별 정규식 적용
- **3차 상관분석**
  - `Pktmon.exe` / `Psr.exe` → `.etl`, `.zip`, 사용자 화면 기록 산출물
  - `Cmdkey.exe`, `Findstr.exe`, `Reg.exe`, `Rpcping.exe` → cached credential 접근, SYSVOL XML 접근, hive save 파일, NTLM 인증 시도
  - `Adplus.exe`, `Comsvcs.dll`, `Createdump.exe`, `Dump64.exe`, `DumpMinitool.exe`, `rdrleakdiag.exe`, `Sqldumper.exe`, `Tttracer.exe` → `.dmp/.mdmp`, 대상 PID, 프로세스 접근 이벤트
  - `Diskshadow.exe`, `dsdbutil.exe`, `ntdsutil.exe`, `wbadmin.exe` → VSS snapshot, mounted volume, `NTDS.dit`, `SYSTEM` hive 접근, 백업/복구 파일
- **4차 severity 분리**
  - **High**: `Comsvcs.dll`, `Createdump.exe`, `Dump64.exe`, `DumpMinitool.exe`, `rdrleakdiag.exe`, `Sqldumper.exe`, `Tttracer.exe`, `Reg.exe save HKLM\SAM/SYSTEM/SECURITY`, `Diskshadow.exe`, `dsdbutil.exe`, `ntdsutil.exe`, `wbadmin.exe`
  - **Medium**: `Cmdkey.exe /list`, `Findstr.exe cpassword`, `Rpcping.exe ... NTLM`, `Pktmon.exe start --etw`, `Psr.exe /gui 0`
  - **Medium/High (환경 의존)**: `Adplus.exe` — 일반 단말에서는 high, 개발/디버그 환경에서는 medium

## 운영 메모
- 이 버킷은 **정찰**, **자격증명 탐색**, **메모리 덤프**, **AD 데이터 수집**이 섞여 있기 때문에, 모든 항목을 같은 severity 로 취급하면 오탐과 피로도가 커집니다.
- 운영에서는 아래처럼 두 단계로 나누는 편이 좋습니다.
  1. **High-Impact Credential / Data Collection**  
     `Reg.exe`, `Comsvcs.dll`, `Createdump.exe`, `Dump64.exe`, `DumpMinitool.exe`, `rdrleakdiag.exe`, `Sqldumper.exe`, `Tttracer.exe`, `Diskshadow.exe`, `dsdbutil.exe`, `ntdsutil.exe`, `wbadmin.exe`
  2. **Recon / Access Discovery**  
     `Pktmon.exe`, `Psr.exe`, `Cmdkey.exe`, `Findstr.exe`, `Rpcping.exe`, `Adplus.exe`
- 특히 `Diskshadow.exe`, `dsdbutil.exe`, `ntdsutil.exe`, `wbadmin.exe`는 **도메인 컨트롤러**나 **백업 서버**에서만 의미 있는 경우가 많으므로, host role 기반 분기 없이 일괄 경보를 올리면 운영 노이즈가 커질 수 있습니다.
- `Reg.exe save HKLM\SAM/SYSTEM/SECURITY`는 **매우 높은 품질의 credential access 시그널**입니다. 다른 이벤트가 부족해도 우선적으로 triage 할 가치가 큽니다.
- `Pktmon.exe`와 `Psr.exe`는 각각 **네트워크**, **사용자 화면**을 조용히 캡처할 수 있어, 지원 도구처럼 보이지만 공격 흐름에서는 정찰/수집에 매우 유용합니다.
- `Findstr.exe cpassword`와 `Rpcping.exe ... NTLM`은 **직접적인 실행이나 드롭퍼가 아닌데도 공격 사전 단계에서 중요한 신호**이므로, Execute 계열 정책과 따로 분리해 운영하는 것이 좋습니다.

## 참고 출처
- LOLBAS API: https://lolbas-project.github.io/api/
- LOLBAS 메인 목록: https://lolbas-project.github.io/
- LOLBAS Cmdkey.exe: https://lolbas-project.github.io/lolbas/Binaries/Cmdkey/
- LOLBAS Pktmon.exe: https://lolbas-project.github.io/lolbas/Binaries/Pktmon/
- LOLBAS Psr.exe: https://lolbas-project.github.io/lolbas/Binaries/Psr/
- LOLBAS Reg.exe: https://lolbas-project.github.io/lolbas/Binaries/Reg/
- LOLBAS Findstr.exe: https://lolbas-project.github.io/lolbas/Binaries/Findstr/
- LOLBAS Rpcping.exe: https://lolbas-project.github.io/lolbas/Binaries/Rpcping/
- LOLBAS Adplus.exe: https://lolbas-project.github.io/lolbas/OtherMSBinaries/Adplus/
- LOLBAS Comsvcs.dll: https://lolbas-project.github.io/lolbas/Libraries/comsvcs/
- LOLBAS Createdump.exe: https://lolbas-project.github.io/lolbas/OtherMSBinaries/Createdump/
- LOLBAS Dump64.exe: https://lolbas-project.github.io/lolbas/OtherMSBinaries/Dump64/
- LOLBAS DumpMinitool.exe: https://lolbas-project.github.io/lolbas/OtherMSBinaries/DumpMinitool/
- LOLBAS rdrleakdiag.exe: https://lolbas-project.github.io/lolbas/Binaries/Rdrleakdiag/
- LOLBAS Sqldumper.exe: https://lolbas-project.github.io/lolbas/OtherMSBinaries/Sqldumper/
- LOLBAS Tttracer.exe: https://lolbas-project.github.io/lolbas/Binaries/Tttracer/
- LOLBAS Diskshadow.exe: https://lolbas-project.github.io/lolbas/Binaries/Diskshadow/
- LOLBAS dsdbutil.exe: https://lolbas-project.github.io/lolbas/OtherMSBinaries/Dsdbutil/
- LOLBAS ntdsutil.exe: https://lolbas-project.github.io/lolbas/OtherMSBinaries/Ntdsutil/
- LOLBAS wbadmin.exe: https://lolbas-project.github.io/lolbas/Binaries/Wbadmin/
