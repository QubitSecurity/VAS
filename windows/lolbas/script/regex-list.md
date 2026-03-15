# LOLBAS Script (31) 탐지용 정규식 정책표
작성일: 2026-03-14

이 문서는 앞서 정리한 **사용자 정의 Script 그룹 31개**를 기준으로, **CLI 출력 / Process CommandLine 문자열 기반 탐지 정책**을 설계하기 위한 운영용 정리본입니다.

> **중요:** 여기의 `Script (31)`은 LOLBAS의 공식 `Scripts` 폴더만 의미하지 않습니다. 앞서 재분류한 운영형 Script 버킷을 그대로 사용했기 때문에, **Binaries / Libraries / OtherMSBinaries / Scripts** 중에서 스크립트 실행·프록시·호스트 특성이 있는 항목을 함께 포함합니다. 예를 들어 `Mshta.exe`, `Regsvr32.exe`, `Rundll32.exe`, `Wmic.exe`, `Mshtml.dll`, `Url.dll`도 Script 그룹에 포함됩니다.

## 설계 원칙
- 기본 전제: **실행파일명(또는 스크립트명) + 핵심 옵션 + 스크립트/프로젝트/모니커/원격 지표**를 정규식으로 잡습니다.
- 정규식은 모두 **대소문자 무시 `(?i)`** 기준입니다.
- Script 계열은 **스크립트 파일 자체가 악성인지**, **호스트가 프록시 실행인지**, **원격 moniker/XSL/SCT를 가져오는지**에 따라 탐지 품질이 크게 달라집니다.
- `WSH/VBS/WSF`, `PowerShell 브리지`, `HTA/HTML`, `SCT/XSL/원격 스크립트`, `빌드/컴파일`, `Node/Custom Runtime`를 분리하면 운영이 훨씬 수월합니다.
- 일부 항목은 CLI만으로는 부족합니다. 특히 `.bgi`, `.xsl`, `.sct`, `.hta`, `.wsc`, `.ps1`, `.xoml`, `.rsp`, `.proj` 내부 내용은 **Parent-Child / Module Load / Network / File Create / Script Block Logging**과 결합해야 확정됩니다.

## 권장 운영 구조
1. **Broad prefilter**: Script 계열 후보 도구/스크립트명을 먼저 좁힙니다.
   - <code>(?i)\b(?:AgentExecutor&#124;Bginfo&#124;Cscript&#124;Jsc&#124;Microsoft\.Workflow\.Compiler&#124;Msbuild&#124;Mshta&#124;msxsl&#124;Regsvr32&#124;Rundll32&#124;Runscripthelper&#124;Sqlps&#124;SQLToolsPS&#124;SyncAppvPublishingServer&#124;te&#124;Teams&#124;Wfc&#124;Wmic&#124;Wscript)(?:\.exe)?\b&#124;\b(?:CL_Invocation&#124;CL_LoadAssembly&#124;CL_Mutexverifiers&#124;Launch-VsDevShell&#124;Manage-bde&#124;Pubprn&#124;Syncappvpublishingserver&#124;UtilityFunctions&#124;winrm)\.(?:ps1&#124;wsf&#124;vbs)\b&#124;\bpester\.bat\b&#124;\b(?:Mshtml&#124;Url)\.dll\b</code>
2. **그룹 규칙**: WSH / PowerShell Bridge / HTA / Scriptlet-XSL / Build / Node 로 나눠 적용합니다.
3. **상관분석 규칙**: `regsvr32 + scrobj.dll`, `wmic /format:.xsl`, `rundll32 + mshtml/url.dll`, `powershell + CL_* / UtilityFunctions / Launch-VsDevShell`, `Teams --gpu-launcher`, `BGInfo + .bgi` 같은 결합 규칙을 둡니다.
4. **환경 allowlist**: 개발자 PC, SQL Server 관리 서버, Intune/App-V 배포 단말, Visual Studio / SDK 설치 시스템은 별도 baseline을 두는 것이 좋습니다.

## 그룹 요약
| Group | Count | 운영 의미 |
|---|---:|---|
| WSH / Script File Hosts | 7 | 레거시 WSH/VBS/WSF/BAT 기반 프록시 실행 및 스크립트 호스트 계열. |
| PowerShell Bridges / Module Hosts | 10 | PowerShell 모듈/미니콘솔/진단 스크립트를 경유해 스크립트·DLL·명령을 실행하는 계열. |
| HTA / HTML / URL Script Hosts | 3 | HTA/HTML/URL 핸들러를 이용한 스크립트 실행 계열. |
| Scriptlet / XSL / Remote Script Proxies | 5 | SCT/XSL/WSH/원격 스크립트 파일을 프록시로 실행하는 계열. |
| Build / Compile / XOML Script Engines | 4 | JScript/XOML/프로젝트 파일을 입력으로 받아 코드를 빌드·실행하는 계열. |
| Node / Custom Script Runtimes | 2 | TAEF/Teams의 WSC/Node.js/custom format 기반 스크립트 실행 계열. |

## 그룹 공통 정규식
- **WSH / Script File Hosts**: <code>(?i)\b(?:cscript&#124;wscript)(?:\.exe)?\b.*?(?:\.(?:vbs&#124;vbe&#124;js&#124;jse&#124;wsf&#124;wsh&#124;sct)\b&#124;//e:(?:vbscript&#124;jscript)\b)&#124;\b(?:manage-bde&#124;pubprn&#124;syncappvpublishingserver&#124;winrm)\.(?:wsf&#124;vbs)\b&#124;\bpester\.bat\b</code>
- **PowerShell Bridges / Module Hosts**: <code>(?i)\b(?:agentexecutor&#124;runscripthelper&#124;sqlps&#124;sqltoolsps&#124;syncappvpublishingserver)(?:\.exe)?\b&#124;\b(?:powershell&#124;pwsh)(?:\.exe)?\b.*?\b(?:CL_Invocation&#124;CL_LoadAssembly&#124;CL_Mutexverifiers&#124;Launch-VsDevShell&#124;UtilityFunctions)\.ps1\b</code>
- **HTA / HTML / URL Script Hosts**: <code>(?i)\bmshta(?:\.exe)?\b&#124;\brundll32(?:\.exe)?\b.*?\b(?:mshtml&#124;url)\.dll\b</code>
- **Scriptlet / XSL / Remote Script Proxies**: <code>(?i)\b(?:bginfo&#124;msxsl&#124;regsvr32&#124;wmic)(?:\.exe)?\b&#124;\brundll32(?:\.exe)?\b.*?(?:javascript:&#124;vbscript:&#124;mshtml&#124;url\.dll)</code>
- **Build / Compile / XOML Script Engines**: <code>(?i)\b(?:jsc&#124;microsoft\.workflow\.compiler&#124;msbuild&#124;wfc)(?:\.exe)?\b</code>
- **Node / Custom Script Runtimes**: <code>(?i)\b(?:te&#124;teams)(?:\.exe)?\b</code>

## 특기사항
- `Teams.exe`는 **plain `teams.exe`만으로도** app/app.asar 기반 Node.js 실행이 가능하므로, **CLI-only 탐지에는 본질적 한계**가 있습니다. 이 문서의 regex는 `--gpu-launcher` 변형을 우선 대상으로 합니다.
- `Bginfo.exe`는 실제 VBScript가 `.bgi` 파일 내부에 들어 있으므로 **명령행만으로 payload 성격을 판별하기 어렵습니다.**
- `Msbuild.exe`, `Microsoft.Workflow.Compiler.exe`, `Wfc.exe`, `Jsc.exe`는 개발 환경에서 오탐이 생기기 쉬우므로 **호스트 역할(개발자 PC/빌드 서버/일반 PC)** 기준 분기가 중요합니다.
- `SyncAppvPublishingServer.exe/.vbs`, `Sqlps.exe`, `SQLToolsPS.exe`, `AgentExecutor.exe`는 정상 관리·배포 환경이 있을 수 있어 **환경별 allowlist가 필수**입니다.
- `winrm.vbs`와 `Wmic.exe`는 Script이면서 동시에 Remote 성격이 강하므로 **Remote 그룹 규칙과 함께 교차 적용**하는 것이 좋습니다.

## 정책표
| No | Tool | Group | Regex | FP 주의 | FN 주의 | 권장 정책명 |
|---:|---|---|---|---|---|---|
| 1 | Cscript.exe | WSH / Script File Hosts | <code>(?i)\bcscript(?:\.exe)?\b.*?(?://e:(?:vbscript&#124;jscript)\b&#124;[^\r\n]*\.(?:vbs&#124;vbe&#124;js&#124;jse&#124;wsf&#124;wsh&#124;sct)\b)</code> | 레거시 로그인 스크립트, 관리용 VBS/WSF, 소프트웨어 배포와 겹칠 수 있다. | 실제 스크립트 본문은 파일/ADS 내부에 있으므로 내용 기반 판단은 추가 수집이 필요하다. | `LOLBAS.Script.Cscript.WSH` |
| 2 | Wscript.exe | WSH / Script File Hosts | <code>(?i)\bwscript(?:\.exe)?\b.*?(?:\.(?:vbs&#124;vbe&#124;js&#124;jse&#124;wsf&#124;wsh&#124;sct)\b&#124;GetObject\(["\']script:&#124;//e:(?:vbscript&#124;jscript)\b)</code> | 레거시 로그인 스크립트, 관리용 VBS/WSF, 업무 자동화와 겹칠 수 있다. | 실제 스크립트 내용은 파일/ADS/원격 moniker에 있어 command line만으로는 상세 판단이 어렵다. | `LOLBAS.Script.Wscript.WSH` |
| 3 | Manage-bde.wsf | WSH / Script File Hosts | <code>(?i)(?:\bset\s+comspec\s*=\s*[^\r\n&]+&\s*)?\b(?:cscript&#124;wscript)(?:\.exe)?\b.*?\bmanage-bde\.wsf\b</code> | BitLocker 관리 자동화나 레거시 운영 스크립트와 겹칠 수 있다. | 핵심 악성 행위가 sibling `manage-bde.exe` 또는 `comspec` 하이재킹에 숨어 있어 path correlation이 필요하다. | `LOLBAS.Script.ManageBDE.WSF` |
| 4 | Pester.bat | WSH / Script File Hosts | <code>(?i)\b(?:cmd(?:\.exe)?\s+/c\s+)?(?:["\']?[^"\']*\\)?pester\.bat\b</code> | 개발/테스트 환경에서는 정상 Pester 사용이 흔하다. | 실제 테스트 스크립트나 후속 PowerShell 로직은 별도 파일/모듈 안에 있어 CLI만으로 한계가 있다. | `LOLBAS.Script.Pester.BAT` |
| 5 | Pubprn.vbs | WSH / Script File Hosts | <code>(?i)\b(?:cscript&#124;wscript)(?:\.exe)?\b.*?\bpubprn\.vbs\b.*?\bscript:(?:(?:https?&#124;ftp)://[^\s"\'<>]+&#124;\\\\[^\s"\'<>]+\\[^\s"\'<>]+(?:\\[^\s"\'<>]+)*)</code> | 정상 프린터 관리 스크립트는 가능하지만 `script:` 모니커는 매우 드물다. | 로컬 `.sct` 또는 다른 script moniker 변형은 추가 패턴이 필요할 수 있다. | `LOLBAS.Script.Pubprn.ScriptMoniker` |
| 6 | Syncappvpublishingserver.vbs | WSH / Script File Hosts | <code>(?i)\b(?:cscript&#124;wscript)(?:\.exe)?\b.*?\bsyncappvpublishingserver\.vbs\b.*?\bn;.*?(?:IEX&#124;DownloadString&#124;Invoke-Expression&#124;New-Object\s+Net\.WebClient)</code> | App-V/배포 스크립트 환경에서는 정상 가능성이 있다. | 실제 PowerShell 본문이 더 난독화되면 `n;` 기반 패턴만으로는 부족할 수 있다. | `LOLBAS.Script.SyncAppV.VBS` |
| 7 | winrm.vbs | WSH / Script File Hosts | <code>(?i)(?:\b(?:cscript&#124;wscript)(?:\.exe)?\b.*?\bwinrm\.vbs\b&#124;\bwinrm(?:\.vbs)?\b).*?(?:\binvoke\b.*?\bcreate\b&#124;\b-format:pretty\b)</code> | 정상 원격관리 스크립트와 겹칠 수 있다. | 실제 실행은 원격 호스트에서 발생할 수 있고, XSL bypass는 sibling `WsmPty.xsl`/relocated cscript를 함께 봐야 한다. | `LOLBAS.Script.WinRM.VBS` |
| 8 | AgentExecutor.exe | PowerShell Bridges / Module Hosts | <code>(?i)\bagentexecutor(?:\.exe)?\b.*?\b-powershell\b.*?\.ps1\b</code> | Intune 관리 단말에서는 AgentExecutor 자체가 정상적으로 쓰인다. | 폴더 경로에 가짜 `powershell.exe`를 두는 EXE 변형은 스크립트가 아니라 child image/path까지 봐야 한다. | `LOLBAS.Script.AgentExecutor.PowerShell` |
| 9 | Runscripthelper.exe | PowerShell Bridges / Module Hosts | <code>(?i)\brunscripthelper(?:\.exe)?\b\s+surfacecheck\b\s+\\\\\?\\[^\r\n"\']+\.(?:txt&#124;ps1)\b</code> | 정상 Windows 진단/텔레메트리 실험 환경에서는 존재할 수 있다. | 실제 PowerShell 내용이 `.txt` 내부에 있어 command line만으로는 스크립트 본문을 확인할 수 없다. | `LOLBAS.Script.Runscripthelper.SurfaceCheck` |
| 10 | Sqlps.exe | PowerShell Bridges / Module Hosts | <code>(?i)\bsqlps(?:\.exe)?\b.*?(?:-noprofile\b&#124;-command\b&#124;-file\b)</code> | SQL Server 관리 서버/DBA 작업에서는 정상 사용이 가능하다. | 후속 PowerShell 명령은 별도 콘솔/interactive 세션에서만 드러날 수 있다. | `LOLBAS.Script.SQLPS.PowerShell` |
| 11 | SQLToolsPS.exe | PowerShell Bridges / Module Hosts | <code>(?i)\bsqltoolsps(?:\.exe)?\b.*?(?:-noprofile\b&#124;-command\b&#124;-file\b)</code> | SQL Server 2016+ 관리 환경에서는 정상 사용이 가능하다. | 실행 내용이 후속 PowerShell child 또는 interactive console에만 남을 수 있다. | `LOLBAS.Script.SQLToolsPS.PowerShell` |
| 12 | SyncAppvPublishingServer.exe | PowerShell Bridges / Module Hosts | <code>(?i)\bsyncappvpublishingserver(?:\.exe)?\b.*?\bn;.*?(?:IEX&#124;DownloadString&#124;Invoke-Expression&#124;New-Object\s+Net\.WebClient)</code> | App-V가 실제로 배포된 환경은 드물지만 정상 사용 가능성은 있다. | 주입 문자열이 단순 `n;` 뒤에 난독화되면 CLI만으로 일부 놓칠 수 있다. | `LOLBAS.Script.SyncAppV.EXE` |
| 13 | CL_Invocation.ps1 | PowerShell Bridges / Module Hosts | <code>(?i)\b(?:powershell&#124;pwsh)(?:\.exe)?\b.*?\bCL_Invocation\.ps1\b.*?\bSyncInvoke\b</code> | 진단 스크립트 연구나 재현 테스트에서는 유사 호출이 가능하다. | EncodedCommand나 별도 파일에서 dot-source하면 `SyncInvoke` 문자열이 로그에 남지 않을 수 있다. | `LOLBAS.Script.CLInvocation.SyncInvoke` |
| 14 | CL_LoadAssembly.ps1 | PowerShell Bridges / Module Hosts | <code>(?i)\b(?:powershell&#124;pwsh)(?:\.exe)?\b.*?\bCL_LoadAssembly\.ps1\b.*?\bLoadAssemblyFromPath\b.*?\.dll\b</code> | PowerShell 진단 스크립트 실험/검증에서는 정상 호출이 있을 수 있다. | DLL 경로가 변수나 난독화 문자열에 숨으면 단일 regex로 일부 누락된다. | `LOLBAS.Script.CLLoadAssembly.DLL` |
| 15 | CL_Mutexverifiers.ps1 | PowerShell Bridges / Module Hosts | <code>(?i)\b(?:powershell&#124;pwsh)(?:\.exe)?\b.*?\bCL_Mutexverifiers\.ps1\b.*?\brunAfterCancelProcess\b</code> | 진단용 PowerShell 실험에서는 정상 호출 가능성이 있다. | 후속 실행 대상이 별도 변수/함수에 숨으면 child process를 함께 봐야 한다. | `LOLBAS.Script.CLMutexVerifiers.Proxy` |
| 16 | Launch-VsDevShell.ps1 | PowerShell Bridges / Module Hosts | <code>(?i)\b(?:powershell&#124;pwsh)(?:\.exe)?\b.*?\bLaunch-VsDevShell\.ps1\b.*?(?:-VsWherePath\s+[^\r\n"\']+\.exe\b&#124;-VsInstallationPath\s+["\']?[^"\']*;[^"\']+\.exe[^"\']*["\']?)</code> | Visual Studio 개발환경에서는 정상 호출이 있을 수 있다. | 매개변수가 별도 변수나 encoded command에 숨어 있으면 놓칠 수 있다. | `LOLBAS.Script.LaunchVsDevShell.Proxy` |
| 17 | UtilityFunctions.ps1 | PowerShell Bridges / Module Hosts | <code>(?i)\b(?:powershell&#124;pwsh)(?:\.exe)?\b.*?\bUtilityFunctions\.ps1\b.*?\bRegSnapin\b.*?\.dll\b</code> | Windows 진단 스크립트 분석/재현 환경에서는 정상 호출 가능성이 있다. | 로드되는 DLL과 후속 .NET entrypoint는 child/module load까지 봐야 확정된다. | `LOLBAS.Script.UtilityFunctions.RegSnapin` |
| 18 | Mshta.exe | HTA / HTML / URL Script Hosts | <code>(?i)\bmshta(?:\.exe)?\b.*?(?:\b[^\s"\']+\.hta\b&#124;(?:vbscript&#124;javascript):&#124;GetObject\(["\']script:)</code> | 레거시 HTA 기반 내부 도구나 테스트 환경에서는 정상 실행이 가능하다. | 단순 URL 인자는 Download 규칙과 겹치며, 실제 HTA/JS/VBS 본문은 원격/파일 내부에 있다. | `LOLBAS.Script.MSHTA.HTAorMoniker` |
| 19 | Mshtml.dll | HTA / HTML / URL Script Hosts | <code>(?i)\brundll32(?:\.exe)?\b.*?\bmshtml(?:\.dll)?\b\s*,\s*(?:RunHTMLApplication&#124;PrintHTML)\b.*?(?:\.hta\b&#124;javascript:&#124;vbscript:)?</code> | 드물지만 레거시 HTML/HTA 핸들러 호출과 겹칠 수 있다. | 실제 HTML/HTA 본문이 별도 파일/URL에 있으면 CLI만으로는 내용 판단이 어렵다. | `LOLBAS.Script.MSHTML.Rundll32` |
| 20 | Url.dll | HTA / HTML / URL Script Hosts | <code>(?i)\brundll32(?:\.exe)?\b.*?\burl\.dll\b\s*,\s*(?:OpenURL&#124;FileProtocolHandler)\b.*?(?:\.hta\b&#124;\.url\b&#124;file://)</code> | 정상 URL/파일 핸들러 호출과 겹칠 수 있다. | 악성 실행이 `.url` 내부나 file:// 대상에 숨어 있으면 CLI만으로 상세 payload를 알기 어렵다. | `LOLBAS.Script.URLDLL.Handler` |
| 21 | Bginfo.exe | Scriptlet / XSL / Remote Script Proxies | <code>(?i)(?:\\\\[^\s"\'<>]+\\[^\s"\'<>]+(?:\\[^\s"\'<>]+)*\\)?bginfo(?:\.exe)?\b\s+[^\r\n"\']+\.bgi\b.*?\b/popup\b.*?\b/nolicprompt\b</code> | 관리자가 BGInfo 배너를 배포하는 환경에서는 정상 실행이 가능하다. | 악성 VBScript는 `.bgi` 내부에 저장되므로 CLI만으로는 스크립트 내용을 확인할 수 없다. | `LOLBAS.Script.BGInfo.WSH` |
| 22 | msxsl.exe | Scriptlet / XSL / Remote Script Proxies | <code>(?i)\bmsxsl(?:\.exe)?\b\s+(?:(?:https?&#124;file)://[^\s"\'<>]+&#124;[^\s"\'<>]+\.xml\b)\s+(?:(?:https?&#124;file)://[^\s"\'<>]+&#124;[^\s"\'<>]+\.(?:xsl&#124;xml)\b)</code> | 정상 XSL 변환 테스트/운영에서도 동일 구문이 가능하다. | XSL 내부 COM scriptlet이나 `-o` 출력형은 추가적으로 파일 생성/네트워크와 결합해야 확정된다. | `LOLBAS.Script.MSXSL.Transform` |
| 23 | Regsvr32.exe | Scriptlet / XSL / Remote Script Proxies | <code>(?i)\bregsvr32(?:\.exe)?\b.*?\b/i:(?:(?:https?&#124;ftp)://[^\s"\'<>]+&#124;[^\s"\'<>]+\.sct\b).*?\bscrobj\.dll\b</code> | 정상 DLL 등록과는 구분되지만 보안제품/설치 도구가 regsvr32를 쓰는 경우는 있다. | SCT 경로가 변수/응답 파일에 숨거나 다른 DLL 실행형만 쓰면 이 규칙은 놓친다. | `LOLBAS.Script.Regsvr32.SCT` |
| 24 | Rundll32.exe | Scriptlet / XSL / Remote Script Proxies | <code>(?i)\brundll32(?:\.exe)?\b.*?(?:javascript:\s*["\']?\\\.\.\\mshtml,RunHTMLApplication\b&#124;mshtml(?:\.dll)?\s*,\s*(?:RunHTMLApplication&#124;PrintHTML)\b&#124;url\.dll\s*,\s*(?:OpenURL&#124;FileProtocolHandler)\b.*?(?:\.hta\b&#124;file://))</code> | 정상 제어판/쉘 DLL 호출이 많으므로 함수명까지 함께 보는 것이 중요하다. | 단순 DLL entrypoint 호출, COM 호출, 다운로드 전용 PhotoViewer/Scrobj 패턴은 이 스크립트 규칙에 포함되지 않는다. | `LOLBAS.Script.Rundll32.ScriptHost` |
| 25 | Wmic.exe | Scriptlet / XSL / Remote Script Proxies | <code>(?i)\bwmic(?:\.exe)?\b.*?\b/format:\s*["\']?(?:(?:https?&#124;ftp)://[^\s"\'<>]+&#124;\\\\[^\s"\'<>]+\\[^\s"\'<>]+(?:\\[^\s"\'<>]+)*&#124;[^\s"\'<>]+\.xsl\b)</code> | WMIC 포맷터/XSL을 내부 관리 용도로 쓰는 환경과 겹칠 수 있다. | 원격 호스트에서 실행되는 child process, XSL 내부 JScript/VBScript는 별도 telemetry가 필요하다. | `LOLBAS.Script.WMIC.XSL` |
| 26 | Jsc.exe | Build / Compile / XOML Script Engines | <code>(?i)\bjsc(?:\.exe)?\b(?:\s+/t:library)?\s+[^\r\n"\']+\.js\b</code> | 개발자 PC나 빌드 파이프라인에서는 정상 JScript 컴파일이 가능하다. | response file 또는 다른 래퍼를 통해 호출되면 `.js`가 직접 보이지 않을 수 있다. | `LOLBAS.Script.Jsc.Compile` |
| 27 | Microsoft.Workflow.Compiler.exe | Build / Compile / XOML Script Engines | <code>(?i)\bmicrosoft\.workflow\.compiler(?:\.exe)?\b\s+[^\r\n"\']+\.(?:xoml&#124;xml&#124;txt)\b(?:\s+[^\r\n"\']+\.log\b)?</code> | 드문 편이지만 .NET/Workflow 개발 환경에서는 정상 사용이 가능하다. | 실제 코드가 입력 XML/XOML이나 참조 파일에 있으므로 command line만으로 payload를 판별하기 어렵다. | `LOLBAS.Script.WorkflowCompiler.XOML` |
| 28 | Msbuild.exe | Build / Compile / XOML Script Engines | <code>(?i)\bmsbuild(?:\.exe)?\b.*?(?:\.(?:xml&#124;csproj&#124;vbproj&#124;proj)\b&#124;@[^\s"\']+\.rsp\b&#124;/logger:[^\s"\']+\.dll\b)</code> | 개발자 PC, 빌드 서버, Visual Studio 환경에서는 정상 사용이 흔하다. | 악성 Task/XSL/Logger DLL이 프로젝트 내부나 `.rsp` 파일에 숨어 있으면 상세 내용은 CLI만으로 부족하다. | `LOLBAS.Script.MSBuild.ProjectOrXSL` |
| 29 | Wfc.exe | Build / Compile / XOML Script Engines | <code>(?i)\bwfc(?:\.exe)?\b\s+[^\r\n"\']+\.xoml\b</code> | 드물지만 Windows SDK/Workflow 개발 환경에서는 정상 사용이 있다. | 실제 코드가 XOML 내부에 있으므로 파일 내용 분석 없이는 payload 판단이 어렵다. | `LOLBAS.Script.WFC.XOML` |
| 30 | te.exe | Node / Custom Script Runtimes | <code>(?i)\bte(?:\.exe)?\b\s+[^\r\n"\']+\.(?:wsc&#124;wsf&#124;vbs&#124;js)\b</code> | TAEF 테스트 환경에서는 정상 사용이 있다. | DLL/custom format 입력은 스크립트 확장자가 직접 안 보일 수 있으므로 이 규칙은 WSH 계열에 집중한다. | `LOLBAS.Script.TE.WSC` |
| 31 | Teams.exe | Node / Custom Script Runtimes | <code>(?i)\bteams(?:\.exe)?\b.*?\b--disable-gpu-sandbox\b.*?\b--gpu-launcher\s*=\s*["\'][^"\']+&&["\']</code> | 일부 디버깅/실험 환경에서는 Chromium 계열 플래그가 쓰일 수 있다. | 단순 `teams.exe`만으로 실행되는 Node.js/app.asar abuse는 CLI-only regex로 사실상 구분이 어렵다. | `LOLBAS.Script.Teams.GPULauncher` |
