# LOLBAS Remote (31) 탐지용 정규식 정책표
작성일: 2026-03-14

이 문서는 앞서 정리한 **사용자 정의 Remote 그룹 31개**를 기준으로, **CLI 출력 / Process CommandLine 문자열 기반 탐지 정책**을 설계하기 위한 운영용 정리본입니다.

> **중요:** 여기의 `Remote (31)`은 LOLBAS 공식 `Execute: Remote` 태그만 그대로 모은 것이 아닙니다. 앞서 재분류한 운영형 Remote 버킷을 그대로 사용했기 때문에, **원격 호스트 제어**, **원격 URL/UNC/WebDAV 기반 로더**, **패키지/업데이터형 원격 실행**, 그리고 공식적으로는 `Upload`로 분류된 `DataSvcUtil.exe`, `TestWindowRemoteAgent.exe`까지 함께 포함합니다. 반대로 `Sc.exe`, `Schtasks.exe`, `wbemtest.exe`는 LOLBAS 공식 페이지에서 항상 `Remote`로 표기되지는 않더라도 운영상 원격 제어 기능이 명확해 Remote 그룹에 배치했습니다.

## 설계 원칙
- 기본 전제: **도구명 + 원격 지표(URL/UNC/원격 호스트 옵션/사용자@호스트/manifest·updater 스위치)** 를 함께 잡습니다.
- 정규식은 모두 **대소문자 무시 `(?i)`** 기준입니다.
- Remote 계열은 단순히 `remote`라는 문자열보다 **`/S`, `/node:`, `\\host`, `user@host`, `https://`, `file://`, `--update`, `--manifest`, `/uri:`** 같은 운영 신호를 보는 편이 훨씬 정확합니다.
- 원격 호스트 조작형(`sc`, `schtasks`, `wmic`, `winrm`)은 **대상 호스트의 후속 프로세스/서비스/작업 생성 이벤트**와 결합해야 품질이 올라갑니다.
- 원격 로더형(`mshta`, `regsvr32`, `rundll32`, `ieexec`, `dfsvc`, `dfshim`)은 **네트워크 연결 + 파일 생성 + 모듈 로드 + child process**를 함께 봐야 확정됩니다.
- GUI/간접형(`wbemtest`, 일부 `winget`, 일부 `VSLaunchBrowser`)은 **CLI-only 탐지에 본질적 한계**가 있습니다.
- `Upload`를 Remote 안에 흡수했기 때문에 `DataSvcUtil.exe`, `TestWindowRemoteAgent.exe`는 **원격 통신·유출 관점의 예외 bucket**으로 따로 다룹니다.

## 권장 운영 구조
1. **Broad prefilter**: Remote 계열 후보 도구명을 먼저 좁힙니다.
   - <code>(?i)\b(?:Bginfo&#124;Cmstp&#124;DataSvcUtil&#124;Devinit&#124;Dfsvc&#124;Dnscmd&#124;Hh&#124;Ieexec&#124;Mpiexec&#124;Mshta&#124;Msiexec&#124;Pcalua&#124;Regsvr32&#124;Remote&#124;Rundll32&#124;Sc&#124;Schtasks&#124;Scriptrunner&#124;Sftp&#124;Squirrel&#124;ssh&#124;Update&#124;VSLaunchBrowser&#124;wbemtest&#124;winget&#124;Wmic&#124;msxsl)(?:\.exe)?\b&#124;\b(?:Desk\.cpl&#124;Dfshim\.dll)\b&#124;\bwinrm\.vbs\b</code>
2. **그룹 규칙**: 원격 호스트 제어 / 세션·파일 연산 / 원격 로더 / 패키지·업데이터 / Exfil·RPC 로 나눠 적용합니다.
3. **상관분석 규칙**: `sc + 서비스 생성`, `schtasks + 원격 작업 생성`, `wmic/winrm + 대상 호스트 child process`, `ssh/sftp + 네트워크 세션`, `mshta/regsvr32/rundll32 + 외부 연결`, `winget/update/squirrel + package cache/RELEASES`, `DataSvcUtil/TestWindowRemoteAgent + 외부 도메인 질의`를 결합합니다.
4. **환경 allowlist**: 개발자 PC, Visual Studio 설치 단말, ClickOnce/Teams 배포 환경, 관리자 점프 서버, DNS 서버, SQL/HPC 관리 노드는 별도 baseline을 두는 것이 좋습니다.

## 그룹 요약
| Group | Count | 운영 의미 |
|---|---:|---|
| Remote Host Control / Lateral Movement | 8 | 원격 호스트에 서비스/작업/WMI/WinRM/클러스터 실행을 가하는 계열. |
| Remote Session / File Operators | 4 | SSH/SFTP, SMB/WebDAV, 원격 스크립트/파일 호출처럼 원격 세션 또는 원격 파일을 직접 다루는 계열. |
| Remote Payload / Loader | 13 | URL/UNC/WebDAV/ClickOnce/HTA/SCT/XSL/remote DLL/remote package를 받아 실행·로딩하는 계열. |
| Package / Updater / Manifest | 4 | 원격 manifest, NuGet/RELEASES, 스토어/패키지 체인을 통해 다운로드 후 실행하는 계열. |
| Remote Exfil / RPC | 2 | 공식 LOLBAS에선 Upload로 표기되지만, 운영상 원격 통신·유출로 관리해야 하는 계열. |

## 그룹 공통 정규식
- **Remote Host Control / Lateral Movement**: <code>(?i)(?:\b(?:dnscmd&#124;mpiexec&#124;remote&#124;sc&#124;schtasks&#124;wbemtest&#124;wmic)(?:\.exe)?\b&#124;\b(?:cscript&#124;wscript)(?:\.exe)?\b.*?\bwinrm\.vbs\b).*(?:\\\\[^\r\n]*&#124;/s\b&#124;/node:&#124;process\s+call\s+create&#124;invoke\b.*\bcreate\b)?</code>
- **Remote Session / File Operators**: <code>(?i)\b(?:bginfo&#124;scriptrunner&#124;sftp&#124;ssh)(?:\.exe)?\b.*?(?:\\\\\\\\&#124;@&#124;-[LRDb]\b)</code>
- **Remote Payload / Loader**: <code>(?i)(?:\b(?:cmstp&#124;dfsvc&#124;hh&#124;ieexec&#124;mshta&#124;msiexec&#124;msxsl&#124;pcalua&#124;regsvr32&#124;rundll32&#124;vslaunchbrowser)(?:\.exe)?\b&#124;\b(?:desk\.cpl&#124;dfshim\.dll)\b).*(?:(?:https?&#124;ftp&#124;file)://&#124;\\\\[^\s"\']+\\[^\s"\']+)</code>
- **Package / Updater / Manifest**: <code>(?i)\b(?:devinit&#124;squirrel&#124;update&#124;winget)(?:\.exe)?\b.*?(?:--download&#124;--update(?:Rollback)?&#124;--manifest&#124;-s\s+msstore&#124;-t\s+msi-install)</code>
- **Remote Exfil / RPC**: <code>(?i)\b(?:datasvcutil&#124;testwindowremoteagent)(?:\.exe)?\b.*?(?:/uri:&#124;-h\b.*?-p\b)</code>

## 특기사항
- `DataSvcUtil.exe`, `TestWindowRemoteAgent.exe`는 공식 LOLBAS에서 **Upload**로 관리되지만, 이번 taxonomy에서는 별도 Upload bucket이 없으므로 Remote 안에 흡수했습니다.
- `Sc.exe`, `Schtasks.exe`는 LOLBAS 공식 페이지의 대표 abuse 예제가 항상 `Remote`로 적히진 않지만, **원격 서비스/원격 작업 조작**이라는 운영 의미 때문에 Remote 그룹이 더 적합합니다.
- `wbemtest.exe`는 **GUI 중심 도구**라서 command line 정규식만으로는 탐지력이 약합니다. WMI Activity, 대상 호스트의 `Win32_Process.Create`, child process를 같이 보아야 합니다.
- `winget.exe`는 **원격 URL이 local manifest `.yml` 안에 숨어 있는 경우가 많아**, 정규식이 잡는 건 `--manifest` 또는 `-s msstore` 같은 간접 신호입니다.
- `Update.exe`와 `Squirrel.exe`는 실제 악성 실행이 `RELEASES`/NuGet 체인 안에서 일어나므로, **네트워크/파일 캐시/child process** 결합이 중요합니다.
- `Bginfo.exe`, `VSLaunchBrowser.exe`, `Hh.exe`, `Mshta.exe`, `Regsvr32.exe`, `Rundll32.exe`는 **부모-자식 관계**와 **네트워크 접근**을 함께 보는 것이 좋습니다.
- `ssh.exe`는 Tunnel 그룹과도 닿아 있습니다. 이번 문서에서는 Remote 세션/포워딩까지만 다루고, 전용 터널 정책은 별도 문서로 빼는 것이 운영상 깔끔합니다.

## 정책표
| No | Tool | Group | Regex | FP 주의 | FN 주의 | 권장 정책명 |
|---:|---|---|---|---|---|---|
| 1 | Dnscmd.exe | Remote Host Control / Lateral Movement | <code>(?i)\bdnscmd(?:\.exe)?\b\s+\S+\s+/config\s+/serverlevelplugindll\s+(?:\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*\.dll\b&#124;[A-Za-z]:\\[^\r\n"\']+\.dll\b)</code> | 정상 DNS 서버 관리, DnsAdmins 운영, 플러그인 변경 작업과 겹칠 수 있다. | 원격 서버명이 변수/스크립트에서 주입되거나 플러그인 경로가 후속 단계에 숨어 있으면 일부 놓칠 수 있다. | `LOLBAS.Remote.Dnscmd.ServerLevelPluginDLL` |
| 2 | Mpiexec.exe | Remote Host Control / Lateral Movement | <code>(?i)\bmpiexec(?:\.exe)?\b.*?(?:-host(?:s)?\b&#124;-machinefile\b).*?(?:\b-n\b&#124;\b-c\b&#124;\.exe\b)</code> | HPC/분산 연산, 테스트 랩, 클러스터 운영 환경에서는 정상 사용이 가능하다. | 호스트 목록이 machinefile 안에만 있고 실제 명령이 별도 스크립트에 숨어 있으면 CLI 단독 탐지 한계가 있다. | `LOLBAS.Remote.Mpiexec.ClusterExec` |
| 3 | Remote.exe | Remote Host Control / Lateral Movement | <code>(?i)\bremote(?:\.exe)?\b\s+/s\b\s+(?:\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*\.exe\b)</code> | 디버깅 툴킷을 쓰는 개발/디버그 환경에서는 정상 실행이 있을 수 있다. | 로컬 경로 대상 `/s`는 Execute 문서 쪽 규칙으로 다루는 편이 맞고, 원격 UNC가 아닌 변형은 여기서 누락될 수 있다. | `LOLBAS.Remote.RemoteExe.UNC` |
| 4 | Sc.exe | Remote Host Control / Lateral Movement | <code>(?i)\bsc(?:\.exe)?\b\s+\\\\[^\s"\']+\s+(?:create&#124;config&#124;start&#124;stop&#124;query&#124;delete)\b.*?(?:binpath=&#124;obj=&#124;type=&#124;start=&#124;[A-Za-z]:\\&#124;\\\\[^\s"\']+)</code> | 정상 원격 서비스 관리, 소프트웨어 배포, 서버 운영 자동화와 겹친다. | 실제 실행 파일은 원격 서비스 생성 뒤 별도 프로세스로 나타나므로 대상 호스트 이벤트와 결합해야 확정된다. | `LOLBAS.Remote.SC.RemoteServiceControl` |
| 5 | Schtasks.exe | Remote Host Control / Lateral Movement | <code>(?i)\bschtasks(?:\.exe)?\b.*?\b/s\b\s+[^\s"\']+\b.*?\b(?:/create&#124;/run&#124;/change&#124;/delete)\b(?:.*?\b/tr\b\s+[^\r\n]+)?</code> | 정상 원격 작업 배포, 관리 서버 운영, IT 자동화와 겹칠 수 있다. | 작업 정의 XML이나 원격 작업 본문은 대상 시스템에 남기 때문에 로컬 Process CommandLine만으로는 세부 payload를 놓칠 수 있다. | `LOLBAS.Remote.Schtasks.RemoteTask` |
| 6 | wbemtest.exe | Remote Host Control / Lateral Movement | <code>(?i)\bwbemtest(?:\.exe)?\b(?:\s&#124;$)</code> | WMI 진단/개발/관리자가 GUI 도구를 정상 사용하면 그대로 매치된다. | 핵심 악성 행위는 GUI 안의 클래스/메서드 호출과 원격 호스트 쪽 프로세스 생성에 있으므로 CLI-only 탐지력은 매우 약하다. | `LOLBAS.Remote.WBEMTest.GUIWMI` |
| 7 | winrm.vbs | Remote Host Control / Lateral Movement | <code>(?i)(?:\b(?:cscript&#124;wscript)(?:\.exe)?\b.*?\bwinrm\.vbs\b&#124;\bwinrm(?:\.vbs)?\b).*?\binvoke\b.*?\bcreate\b</code> | 정상 원격관리 스크립트, WinRM 운영 자동화와 겹칠 수 있다. | XSL 기반 AWL bypass나 후속 원격 프로세스는 별도 WinRM Operational 로그와 child process correlation이 필요하다. | `LOLBAS.Remote.WinRM.InvokeCreate` |
| 8 | Wmic.exe | Remote Host Control / Lateral Movement | <code>(?i)\bwmic(?:\.exe)?\b.*?(?:/node:\s*["\']?[^"\s\']+["\']?.*?\bprocess\s+call\s+create\b&#124;\bprocess\s+get\s+brief\b.*?/format:\s*["\']?(?:(?:https?&#124;ftp)://[^\s"\']+&#124;\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*))</code> | 정상 WMI 관리, 자산 조회, 원격 시스템 운영과 겹칠 수 있다. | 실제 원격 프로세스나 XSL 내부 스크립트는 대상 호스트/네트워크 쪽 telemetry를 함께 봐야 한다. | `LOLBAS.Remote.WMIC.NodeOrXSL` |
| 9 | Bginfo.exe | Remote Session / File Operators | <code>(?i)(?:\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*\\bginfo(?:\.exe)?\b&#124;\bbginfo(?:\.exe)?\b\s+\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*\.bgi\b).*?\b/popup\b.*?\b/nolicprompt\b</code> | BGInfo 배너 배포 환경이나 WebDAV/SMB 운영 공유에서 정상 사용이 가능하다. | VBScript payload는 `.bgi` 내부에 있어 command line만으로 실제 스크립트 내용을 식별하기 어렵다. | `LOLBAS.Remote.BGInfo.WebDAVorSMB` |
| 10 | Scriptrunner.exe | Remote Session / File Operators | <code>(?i)\bscriptrunner(?:\.exe)?\b.*?-appvscript\s+(?:\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*\.(?:cmd&#124;ps1&#124;vbs&#124;exe)\b)</code> | App-V 배포나 레거시 스크립트 실행 환경에서는 정상 사용과 겹칠 수 있다. | 실제 본문이 원격 `.cmd/.ps1/.vbs` 안에 있어 후속 child process와 파일 접근 로그를 봐야 한다. | `LOLBAS.Remote.ScriptRunner.RemoteScript` |
| 11 | Sftp.exe | Remote Session / File Operators | <code>(?i)\bsftp(?:\.exe)?\b.*?(?:\s+-b\b\s+[^\s"\']+\s+)?(?:[^\s@]+@)[A-Za-z0-9._-]+(?:\.[A-Za-z0-9._-]+)*(?::\d+)?(?:\s&#124;$)</code> | 정상 파일 전송/운영 자동화와 매우 자주 겹친다. | 배치 파일 내부 `get/put` 명령이나 실제 전송 대상 경로는 별도 batch file 내용과 네트워크 로그를 봐야 한다. | `LOLBAS.Remote.SFTP.Session` |
| 12 | ssh.exe | Remote Session / File Operators | <code>(?i)\bssh(?:\.exe)?\b.*?(?:\s+-[LRD]\b[^\r\n]*&#124;\s+(?:[^\s@]+@)[A-Za-z0-9._-]+(?:\.[A-Za-z0-9._-]+)*(?::\d+)?(?:\s+.+)?)</code> | 정상 원격접속, 관리용 점프호스트, 포트포워딩 운영과 겹친다. | 세션 내부에서 실행된 실제 명령은 local CLI에 남지 않을 수 있고, 터널 목적지는 네트워크 telemetry가 더 정확하다. | `LOLBAS.Remote.SSH.SessionOrForward` |
| 13 | Cmstp.exe | Remote Payload / Loader | <code>(?i)\bcmstp(?:\.exe)?\b.*?\b/ni\b.*?\b/s\b.*?(?:(?:https?&#124;ftp)://[^\s"\']+\.inf\b)</code> | 희귀하지만 정상 VPN/Connection Manager 배포와 겹칠 수 있다. | 원격 INF가 로컬에 저장된 뒤 후속 SCT/OCX 호출로 이어지므로 자식 프로세스와 네트워크 접근을 함께 봐야 한다. | `LOLBAS.Remote.CMSTP.RemoteINF` |
| 14 | Desk.cpl | Remote Payload / Loader | <code>(?i)\brundll32(?:\.exe)?\b.*?\bdesk\.cpl\s*,\s*InstallScreenSaver\b\s+(?:\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*\.scr\b)</code> | 스크린세이버 관련 관리/테스트에서는 드물게 정상 사용이 있을 수 있다. | 실제 실행 파일은 `.scr` 확장자로 가장되어 있고 SMB 접근 이후 child process로 분리될 수 있다. | `LOLBAS.Remote.DeskCPL.RemoteSCR` |
| 15 | Dfshim.dll | Remote Payload / Loader | <code>(?i)\brundll32(?:\.exe)?\b.*?\bdfshim\.dll\s*,\s*ShOpenVerbApplication\b\s+(?:(?:https?&#124;file)://[^\s"\']+)</code> | ClickOnce 배포 환경에서는 정상 사용 가능성이 있다. | 실제 원격 매니페스트와 후속 실행 파일은 ClickOnce 체인 안에서 해석되므로 네트워크/파일 생성 이벤트와 결합해야 한다. | `LOLBAS.Remote.Dfshim.ClickOnceURL` |
| 16 | Dfsvc.exe | Remote Payload / Loader | <code>(?i)\bdfsvc(?:\.exe)?\b\s+(?:(?:https?&#124;file)://[^\s"\']+&#124;[A-Za-z]:\\[^\r\n"\']+\.application\b)</code> | ClickOnce/사내 배포 매니페스트 사용 환경과 겹칠 수 있다. | 로컬 `.application` 파일이 다시 원격 리소스를 참조하는 경우, URL이 command line에 직접 보이지 않을 수 있다. | `LOLBAS.Remote.Dfsvc.ClickOnce` |
| 17 | Hh.exe | Remote Payload / Loader | <code>(?i)\bhh(?:\.exe)?\b\s+(?:(?:https?&#124;file)://[^\s"\']+)</code> | 원격 CHM/help 링크 열기 같은 희귀 정상 동작과 겹칠 수 있다. | 원격 URL이 상위 프로세스나 `.url/.chm` 내부에 숨어 있으면 CLI 단독으로 놓칠 수 있다. | `LOLBAS.Remote.HH.RemoteURL` |
| 18 | Ieexec.exe | Remote Payload / Loader | <code>(?i)\bieexec(?:\.exe)?\b\s+(?:(?:https?&#124;file)://[^\s"\']+&#124;[A-Za-z]:\\[^\r\n"\']+\.(?:exe&#124;dll&#124;application)\b)</code> | 드문 .NET ClickOnce/managed application 실행과 겹칠 수 있다. | 후속 .NET 어셈블리 로딩과 AppDomain 실행은 process cmdline보다 CLR/module telemetry가 더 유용하다. | `LOLBAS.Remote.IEExec.ManagedURL` |
| 19 | Mshta.exe | Remote Payload / Loader | <code>(?i)\bmshta(?:\.exe)?\b.*?(?:(?:https?&#124;file)://[^\s"\']+&#124;script:(?:(?:https?&#124;ftp)://[^\s"\']+&#124;\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*))</code> | 레거시 HTA 앱, 내부 관리 스크립트, 테스트 환경과 겹칠 수 있다. | 실제 HTA/VBS/JS 본문은 원격 파일 내부에 있고, 간접 script moniker 난독화는 추가 패턴이 필요하다. | `LOLBAS.Remote.MSHTA.RemotePayload` |
| 20 | Msiexec.exe | Remote Payload / Loader | <code>(?i)\bmsiexec(?:\.exe)?\b.*?\b(?:/i&#124;/package&#124;/a&#124;/y&#124;/z)\b.*?(?:(?:https?)://[^\s"\']+&#124;\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*\.(?:msi&#124;dll&#124;mst)\b)</code> | 정상 소프트웨어 설치/수정/배포 작업과 겹친다. | 실제 CustomAction, MST, DLL 호출은 설치 단계 내부에서 해석되므로 child process/msi install log와 결합이 필요하다. | `LOLBAS.Remote.MSIExec.RemotePackage` |
| 21 | msxsl.exe | Remote Payload / Loader | <code>(?i)\bmsxsl(?:\.exe)?\b(?=.*(?:(?:https?&#124;file)://&#124;\\\\[^\s"\']+\\[^\s"\']+)).*</code> | 정상 XSL 변환이나 XML 처리 자동화와 겹칠 수 있다. | 실제 스크립트 로직은 원격/로컬 XSL 내부에 있고, URL이 include/import 안에 있으면 단일 regex로는 놓칠 수 있다. | `LOLBAS.Remote.MSXSL.RemoteTransform` |
| 22 | Pcalua.exe | Remote Payload / Loader | <code>(?i)\bpcalua(?:\.exe)?\b.*?\b-a\b\s+(?:\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*\.(?:dll&#124;cpl&#124;exe)\b)</code> | 프로그램 호환성 테스트나 레거시 앱 호환성 작업과 겹칠 수 있다. | 원격 DLL 로딩 후 실제 실행은 PCA 내부/후속 프로세스에서 이뤄질 수 있어 module load를 함께 보는 편이 낫다. | `LOLBAS.Remote.Pcalua.RemoteBinary` |
| 23 | Regsvr32.exe | Remote Payload / Loader | <code>(?i)\bregsvr32(?:\.exe)?\b.*?(?:/i:(?:(?:https?&#124;ftp)://[^\s"\']+&#124;\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*\.(?:sct&#124;dll)\b)&#124;\s+(?:\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*\.dll\b)).*?(?:\bscrobj\.dll\b)?</code> | 정상 DLL/OCX 등록과 겹칠 수 있지만 원격 URL/UNC는 훨씬 더 희귀하다. | 응답 파일, 환경 변수, 난독화된 `/i:` 인자 또는 원격 SCT 리다이렉션은 추가 정규식이 필요할 수 있다. | `LOLBAS.Remote.Regsvr32.RemoteSCTorDLL` |
| 24 | Rundll32.exe | Remote Payload / Loader | <code>(?i)\brundll32(?:\.exe)?\b.*?(?:\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*\.(?:dll&#124;cpl&#124;scr)\b\s*,\s*[^\s,]+&#124;(?:mshtml&#124;url)\.dll\s*,\s*(?:RunHTMLApplication&#124;PrintHTML&#124;OpenURL&#124;FileProtocolHandler)\b.*?(?:https?://[^\s"\']+&#124;file://[^\s"\']+&#124;\\\\[^\s"\']+\\[^\s"\']+))</code> | 정상 제어판/쉘 DLL 호출이 많아서 함수명과 원격 경로를 함께 보는 것이 중요하다. | COM/object moniker, PhotoViewer/Scrobj/JavaScript 변형은 별도 규칙이 필요하고, 실제 payload는 후속 프로세스에서 나타날 수 있다. | `LOLBAS.Remote.Rundll32.RemoteLoader` |
| 25 | VSLaunchBrowser.exe | Remote Payload / Loader | <code>(?i)\bvslaunchbrowser(?:\.exe)?\b\s+\.\w+\s+(?:(?:https?&#124;file)://[^\s"\']+&#124;\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*)</code> | Visual Studio 디버깅/웹앱 개발 환경에서는 정상 실행이 있을 수 있다. | 실제 대상 확장자 핸들러와 child process가 핵심이므로 부모-자식 관계 분석이 중요하다. | `LOLBAS.Remote.VSLaunchBrowser.RemoteOpen` |
| 26 | Devinit.exe | Package / Updater / Manifest | <code>(?i)\bdevinit(?:\.exe)?\b\s+run\b.*?\b-t\b\s+msi-install\b.*?\b-i\b\s+(?:(?:https?)://[^\s"\']+\.msi\b)</code> | Visual Studio 관련 내부 배포/설치 테스트와 겹칠 수 있다. | 실제 MSI 내부 CustomAction은 설치 로그와 child process까지 봐야 확정된다. | `LOLBAS.Remote.Devinit.RemoteMSI` |
| 27 | Squirrel.exe | Package / Updater / Manifest | <code>(?i)\bsquirrel(?:\.exe)?\b.*?(?:--download\b\s+(?:(?:https?)://[^\s"\']+)&#124;--update(?:Rollback)?(?:=&#124;\s+)(?:(?:https?)://[^\s"\']+))</code> | Teams/Squirrel 기반 정상 업데이트와 겹친다. | 실제 다운로드된 NuGet 패키지와 `RELEASES` 처리 흐름은 파일/네트워크 telemetry가 더 정확하다. | `LOLBAS.Remote.Squirrel.RemoteNuget` |
| 28 | Update.exe | Package / Updater / Manifest | <code>(?i)\bupdate(?:\.exe)?\b.*?(?:--download\b\s+(?:(?:https?)://[^\s"\']+)&#124;--update(?:Rollback)?(?:=&#124;\s+)(?:(?:https?)://[^\s"\']+&#124;\\\\[^\s"\']+\\[^\s"\']+(?:\\[^\s"\']+)*))</code> | Teams/Slack/Electron 계열 정상 업데이터와 쉽게 겹친다. | 원격 NuGet 패키지 처리와 실제 실행은 `RELEASES` 파일, package cache, child process를 같이 봐야 한다. | `LOLBAS.Remote.Update.RemoteNuget` |
| 29 | winget.exe | Package / Updater / Manifest | <code>(?i)\bwinget(?:\.exe)?\b\s+install\b.*?(?:--manifest\b\s+[^\s"\']+\.yml\b&#124;\b-s\b\s+msstore\b\s+[^\r\n]+)</code> | 정상 패키지 설치, 개발자 워크스테이션, 셀프서비스 앱 배포와 겹친다. | 원격 URL은 local manifest `.yml` 안에 숨어 있는 경우가 많아서 command line만으로 실제 다운로드 주소를 알기 어렵다. | `LOLBAS.Remote.Winget.ManifestOrStore` |
| 30 | DataSvcUtil.exe | Remote Exfil / RPC | <code>(?i)\bdatasvcutil(?:\.exe)?\b.*?/out:[^\s"\']+.*?/uri:(?:(?:https?)://[^\s"\']+)</code> | 매우 드물지만 WCF Data Services 개발/테스트 환경에서는 정상 사용이 가능하다. | 실제 유출 데이터는 URI 대상과 네트워크 트래픽에서 확인되는 경우가 많아, CLI만으로는 내용 확인이 불가능하다. | `LOLBAS.Remote.DataSvcUtil.URIExfil` |
| 31 | TestWindowRemoteAgent.exe | Remote Exfil / RPC | <code>(?i)\btestwindowremoteagent(?:\.exe)?\b\s+start\b.*?\b-h\b\s+[^\s"\']+\.[A-Za-z0-9.-]+\b.*?\b-p\b\s+\d{1,5}\b</code> | Visual Studio 테스트/원격 에이전트 환경과 겹칠 수 있다. | DNS exfil 데이터는 호스트명 부분에 인코딩되어 있어, 길이/엔트로피/도메인 평판 분석을 추가로 붙이는 편이 좋다. | `LOLBAS.Remote.TestWindowRemoteAgent.DNSExfil` |
