# LOLBAS Function Re-Grouping (Operational)

작성일: 2026-03-13  
기준 소스:
- 공식 사이트: https://lolbas-project.github.io/
- 공식 API 안내: https://lolbas-project.github.io/api/
- 공식 카테고리 목록: https://github.com/LOLBAS-Project/LOLBAS/blob/master/CategoryList.md

## 범위

이 문서는 **현재 LOLBAS 공식 목록 227개**를 기준으로, 원본 LOLBAS의 기능 태그를 그대로 버리지 않고 **운영 목적**에 맞게 다음 7개 그룹으로 다시 묶은 문서다.

- `Download`
- `Execute`
- `Script`
- `Remote`
- `Tunnel`
- `Persistence`
- `Recon`

> **중요:** 이 분류는 **배타적(exclusive)** 분류가 아니라 **중첩(overlap)** 분류다.  
> 예를 들어 `Mshta.exe`는 `Execute`이면서 `Script`이고, 원격 HTA 사용 시 `Remote`에도 걸린다.  
> 그래서 **227개 엔트리**가 **319개 group membership**으로 집계된다.

## 원본 LOLBAS 공식 카테고리

LOLBAS 원본 공식 카테고리는 다음을 포함한다.

`ADS`, `AWL bypass`, `Compile`, `Copy`, `Credentials`, `Decode`, `Download`, `Dump`, `Encode`, `Execute`, `Reconnaissance`, `UAC bypass`, `Upload`, `Tamper`, `Conceal`

이 문서는 사용자 요청에 맞춰 7개 운영 그룹으로 재정리했다.  
원본 카테고리는 `Official LOLBAS function(s)` 컬럼에 그대로 남겨 두었다.

## 재분류 규칙

1. **중첩 허용**
   - 한 도구가 여러 그룹에 동시에 들어갈 수 있다.

2. **직접 매핑**
   - `Download` → `Download`
   - `Execute` → `Execute`
   - `Reconnaissance` → `Recon`

3. **운영 목적에 맞춘 확장 매핑**
   - `Dump`, `Credentials` → `Recon`
   - `Upload` → 별도 bucket이 없으므로 `Remote`
   - `AWL bypass`, `UAC bypass`, `Compile`, `Tamper`, `Conceal` → 실행 관점에서 `Execute`
   - `ADS`/`Copy`만 있는 항목 중 일부는 별도 `Stealth/Staging` bucket이 없으므로 `Persistence`(스테이징/은닉)로 임시 배치
   - `devtunnel.exe`는 공식 LOLBAS 태그가 `Download`지만, 실제 운영 시 `host`/포트포워딩은 `Tunnel`로 별도 관리

4. **Remote 해석**
   - `Remote`는 **원격 호스트 제어**와 **원격 URL/원격 페이로드 기반 실행**을 함께 포함한다.
   - 필요하면 이후 2차 분류로 `Remote-Host`와 `Remote-Payload`를 분리하면 된다.

5. **Recon 해석**
   - 이 문서의 `Recon`은 **정찰 + 자격증명 접근 + 메모리/프로세스 덤프/수집**을 묶은 운영용 버킷이다.

## 요약 카운트

| Group | Count | Definition |
|---|---:|---|
| Download | 55 | 원격에서 파일/패키지/콘텐츠를 내려받거나 INetCache 등에 저장하는 기능. |
| Execute | 168 | 로컬/프록시 실행, DLL/EXE/COM/INF/MSI 실행, AWL/UAC 우회, 컴파일 등 실행 계열을 넓게 포함. |
| Script | 31 | PowerShell, HTA, VBScript, JScript, WSH, SCT, XSL, Node.js 등 스크립트/스크립트형 페이로드 중심. |
| Remote | 31 | 원격 호스트 제어 또는 원격 URL/원격 페이로드를 통한 실행·로딩 기능. |
| Tunnel | 2 | 터널/포트포워딩/중계 채널 형성 중심. 현재 목록에서는 운영상 매우 좁게 적용. |
| Persistence | 14 | 작업 예약, 서비스/런 원스, ADS·복사 기반 스테이징/은닉 등 지속성 확보 또는 그에 가까운 운영 항목. |
| Recon | 18 | Reconnaissance + Credentials + Dump/Collection을 묶은 운영용 그룹. |


## 운영상 예외/주의 도구

| Entry | Type | Official LOLBAS function(s) | Custom group(s) | Note |
|---|---|---|---|---|
| devtunnel.exe | OtherMSBinaries | Download | Download, Tunnel | 공식 LOLBAS 태그는 `Download`지만, `host`/포트포워딩 시나리오는 운영상 `Tunnel`로 관리. |
| Hh.exe | Binaries | Download (EXE, GUI); Execute (EXE, GUI, CMD, CHM, Remote) | Download, Execute, Remote | URL 실행은 운영상 `Download` 우선으로 관리. 공식 태그의 `Remote`는 원격 CHM/URL 로드를 의미. |
| winrm.vbs | Scripts | Execute (CMD, Remote); AWL bypass (XSL) | Execute, Remote, Script | `Execute (CMD, Remote)`이므로 `Remote`를 별도 관리 대상으로 분리. |
| Sc.exe | Binaries | ADS (EXE) | Execute, Persistence, Remote | 공식 LOLBAS 태그는 `ADS`만 보이지만, 서비스 생성/변경/원격 제어 때문에 `Remote`/`Persistence`에 포함. |
| Schtasks.exe | Binaries | Execute (CMD) | Execute, Persistence, Remote | `/S` 사용 시 원격 작업 예약 가능. 지속성 확보에도 자주 사용. |
| ssh.exe | Binaries | Execute (CMD) | Execute, Remote, Tunnel | 명령 실행 외에 포트포워딩/터널링 관점에서 `Tunnel`에도 포함. |
| Sftp.exe | Binaries | Execute (CMD) | Execute, Remote | 원격 전송 세션으로 `Remote`에 포함. 단, `Tunnel`로는 보수적으로 미포함. |
| Colorcpl.exe | Binaries | Copy | Persistence | 공식 기능은 `Copy`. 이 문서에서는 별도 `Staging/Stealth` 그룹이 없어서 `Persistence`(스테이징/은닉)로 임시 배치. |
| Print.exe | Binaries | ADS; Copy | Persistence | 공식 기능은 `ADS; Copy`. 이 문서에서는 `Persistence`(ADS/은닉/스테이징)로 임시 배치. |
| Regedit.exe | Binaries | ADS | Persistence | 공식 기능은 `ADS`. 이 문서에서는 `Persistence`(ADS/은닉/스테이징)로 임시 배치. |
| Regini.exe | Binaries | ADS | Persistence | 공식 기능은 `ADS`. 이 문서에서는 `Persistence`(ADS/은닉/스테이징)로 임시 배치. |
| Tar.exe | Binaries | ADS (Compression); Copy (Compression) | Persistence | 공식 기능은 `ADS/Copy`. 이 문서에서는 `Persistence`(스테이징/은닉)로 임시 배치. |
| dtutil.exe | OtherMSBinaries | Copy | Persistence | 공식 기능은 `Copy`. 이 문서에서는 `Persistence`(배포/스테이징)로 임시 배치. |
| DataSvcUtil.exe | Binaries | Upload | Remote | 공식 기능은 `Upload`. 사용자 정의 taxonomy에 Upload/Exfil bucket이 없어서 `Remote`로 배치. |
| TestWindowRemoteAgent.exe | OtherMSBinaries | Upload | Remote | 공식 기능은 `Upload`. 사용자 정의 taxonomy에 Upload/Exfil bucket이 없어서 `Remote`로 배치. |


## 그룹별 보기

### Download (55)

원격에서 파일/패키지/콘텐츠를 내려받거나 INetCache 등에 저장하는 기능.

- **Binaries (34)**: `AppInstaller.exe`, `Bitsadmin.exe`, `CertOC.exe`, `CertReq.exe`, `Certutil.exe`, `Cmd.exe`, `cmdl32.exe`, `ConfigSecurityPolicy.exe`, `Desktopimgdownldr.exe`, `Diantz.exe`, `Esentutl.exe`, `Expand.exe`, `Extrac32.exe`, `Findstr.exe`, `Finger.exe`, `Ftp.exe`, `Hh.exe`, `Ieexec.exe`, `IMEWDBLD.exe`, `Installutil.exe`, `Ldifde.exe`, `Makecab.exe`, `Mmc.exe`, `MpCmdRun.exe`, `Msedge.exe`, `msedge_proxy.exe`, `Mshta.exe`, `Ngen.exe`, `OneDriveStandaloneUpdater.exe`, `Presentationhost.exe`, `PrintBrm.exe`, `Replace.exe`, `winget.exe`, `Xwizard.exe`
- **Libraries (3)**: `PhotoViewer.dll`, `Scrobj.dll`, `Shimgvw.dll`
- **OtherMSBinaries (18)**: `Bcp.exe`, `devtunnel.exe`, `ECMangen.exe`, `Excel.exe`, `MSAccess.exe`, `MsoHtmEd.exe`, `Mspub.exe`, `msxsl.exe`, `Powerpnt.exe`, `ProtocolHandler.exe`, `Squirrel.exe`, `Update.exe`, `Visio.exe`, `VSLaunchBrowser.exe`, `WinProj.exe`, `Winword.exe`, `Wsl.exe`, `xsd.exe`

### Execute (168)

로컬/프록시 실행, DLL/EXE/COM/INF/MSI 실행, AWL/UAC 우회, 컴파일 등 실행 계열을 넓게 포함.

- **Binaries (93)**: `AddinUtil.exe`, `Aspnet_Compiler.exe`, `At.exe`, `Atbroker.exe`, `Bash.exe`, `Bitsadmin.exe`, `CertOC.exe`, `Change.exe`, `Cipher.exe`, `Cmstp.exe`, `ComputerDefaults.exe`, `Conhost.exe`, `Control.exe`, `Csc.exe`, `CustomShellHost.exe`, `DeviceCredentialDeployment.exe`, `Dfsvc.exe`, `Diantz.exe`, `Diskshadow.exe`, `Dnscmd.exe`, `Eudcedit.exe`, `Eventvwr.exe`, `Explorer.exe`, `Extexport.exe`, `fltMC.exe`, `Forfiles.exe`, `Fsutil.exe`, `Ftp.exe`, `Gpscript.exe`, `Hh.exe`, `Ie4uinit.exe`, `iediagcmd.exe`, `Ieexec.exe`, `Ilasm.exe`, `Infdefaultinstall.exe`, `Installutil.exe`, `iscsicpl.exe`, `Jsc.exe`, `Makecab.exe`, `Mavinject.exe`, `Microsoft.Workflow.Compiler.exe`, `Mmc.exe`, `Msbuild.exe`, `Msconfig.exe`, `Msdt.exe`, `Msedge.exe`, `msedge_proxy.exe`, `msedgewebview2.exe`, `Mshta.exe`, `Msiexec.exe`, `Netsh.exe`, `Odbcconf.exe`, `OfflineScannerShell.exe`, `Pcalua.exe`, `Pcwrun.exe`, `Pnputil.exe`, `Presentationhost.exe`, `Provlaunch.exe`, `Query.exe`, `Rasautou.exe`, `Regasm.exe`, `Register-cimprovider.exe`, `Regsvcs.exe`, `Regsvr32.exe`, `Reset.exe`, `Rundll32.exe`, `Runexehelper.exe`, `Runonce.exe`, `Runscripthelper.exe`, `Sc.exe`, `Schtasks.exe`, `Scriptrunner.exe`, `Setres.exe`, `SettingSyncHost.exe`, `Sftp.exe`, `ssh.exe`, `Stordiag.exe`, `SyncAppvPublishingServer.exe`, `Ttdinject.exe`, `Tttracer.exe`, `Unregmp2.exe`, `vbc.exe`, `Verclsid.exe`, `Wab.exe`, `wbemtest.exe`, `winget.exe`, `Wlrmdr.exe`, `Wmic.exe`, `WorkFolders.exe`, `Wsreset.exe`, `wt.exe`, `wuauclt.exe`, `Xwizard.exe`
- **Libraries (13)**: `Advpack.dll`, `Desk.cpl`, `Dfshim.dll`, `Ieadvpack.dll`, `Ieframe.dll`, `Mshtml.dll`, `Pcwutl.dll`, `Setupapi.dll`, `Shdocvw.dll`, `Shell32.dll`, `Syssetup.dll`, `Url.dll`, `Zipfldr.dll`
- **OtherMSBinaries (52)**: `AccCheckConsole.exe`, `adplus.exe`, `AgentExecutor.exe`, `AppCert.exe`, `AppLauncher.exe`, `Appvlp.exe`, `Bginfo.exe`, `Cdb.exe`, `coregen.exe`, `csi.exe`, `DefaultPack.EXE`, `Devinit.exe`, `Devtoolslauncher.exe`, `dnx.exe`, `Dotnet.exe`, `Dxcap.exe`, `Fsi.exe`, `FsiAnyCpu.exe`, `IntelliTrace.exe`, `Mftrace.exe`, `Microsoft.NodejsTools.PressAnyKey.exe`, `Mpiexec.exe`, `Msdeploy.exe`, `msxsl.exe`, `Ntsd.exe`, `OpenConsole.exe`, `Pixtool.exe`, `Procdump.exe`, `rcsi.exe`, `Remote.exe`, `Sqlps.exe`, `SQLToolsPS.exe`, `Squirrel.exe`, `te.exe`, `Teams.exe`, `Tracker.exe`, `Update.exe`, `VisualUiaVerifyNative.exe`, `VSDiagnostics.exe`, `Vshadow.exe`, `VSIISExeLauncher.exe`, `vsjitdebugger.exe`, `VSLaunchBrowser.exe`, `vsls-agent.exe`, `vstest.console.exe`, `Wfc.exe`, `WFMFormat.exe`, `WinDbg.exe`, `winfile.exe`, `Wsl.exe`, `XBootMgr.exe`, `XBootMgrSleep.exe`
- **Scripts (10)**: `CL_Invocation.ps1`, `CL_LoadAssembly.ps1`, `CL_Mutexverifiers.ps1`, `Launch-VsDevShell.ps1`, `Manage-bde.wsf`, `Pester.bat`, `Pubprn.vbs`, `Syncappvpublishingserver.vbs`, `UtilityFunctions.ps1`, `winrm.vbs`

### Script (31)

PowerShell, HTA, VBScript, JScript, WSH, SCT, XSL, Node.js 등 스크립트/스크립트형 페이로드 중심.

- **Binaries (11)**: `Cscript.exe`, `Jsc.exe`, `Microsoft.Workflow.Compiler.exe`, `Msbuild.exe`, `Mshta.exe`, `Regsvr32.exe`, `Rundll32.exe`, `Runscripthelper.exe`, `SyncAppvPublishingServer.exe`, `Wmic.exe`, `Wscript.exe`
- **Libraries (2)**: `Mshtml.dll`, `Url.dll`
- **OtherMSBinaries (8)**: `AgentExecutor.exe`, `Bginfo.exe`, `msxsl.exe`, `Sqlps.exe`, `SQLToolsPS.exe`, `te.exe`, `Teams.exe`, `Wfc.exe`
- **Scripts (10)**: `CL_Invocation.ps1`, `CL_LoadAssembly.ps1`, `CL_Mutexverifiers.ps1`, `Launch-VsDevShell.ps1`, `Manage-bde.wsf`, `Pester.bat`, `Pubprn.vbs`, `Syncappvpublishingserver.vbs`, `UtilityFunctions.ps1`, `winrm.vbs`

### Remote (31)

원격 호스트 제어 또는 원격 URL/원격 페이로드를 통한 실행·로딩 기능.

#### Remote-Host / lateral-movement 성격이 강한 항목 (15)

`Bginfo.exe`, `DataSvcUtil.exe`, `Devinit.exe`, `Dnscmd.exe`, `Mpiexec.exe`, `Remote.exe`, `Sc.exe`, `Schtasks.exe`, `Scriptrunner.exe`, `Sftp.exe`, `ssh.exe`, `TestWindowRemoteAgent.exe`, `wbemtest.exe`, `winrm.vbs`, `Wmic.exe`

#### Remote-Payload / remote-URL / remote-loader 성격이 강한 항목 (16)

`Cmstp.exe`, `Desk.cpl`, `Dfshim.dll`, `Dfsvc.exe`, `Hh.exe`, `Ieexec.exe`, `Mshta.exe`, `Msiexec.exe`, `msxsl.exe`, `Pcalua.exe`, `Regsvr32.exe`, `Rundll32.exe`, `Squirrel.exe`, `Update.exe`, `VSLaunchBrowser.exe`, `winget.exe`

### Tunnel (2)

터널/포트포워딩/중계 채널 형성 중심. 현재 목록에서는 운영상 매우 좁게 적용.

- **Binaries (1)**: `ssh.exe`
- **OtherMSBinaries (1)**: `devtunnel.exe`

### Persistence (14)

작업 예약, 서비스/런 원스, ADS·복사 기반 스테이징/은닉 등 지속성 확보 또는 그에 가까운 운영 항목.

#### Scheduler / Service / Autorun 중심 (8)

`At.exe`, `Bitsadmin.exe`, `Dnscmd.exe`, `Pnputil.exe`, `Runonce.exe`, `Sc.exe`, `Schtasks.exe`, `Update.exe`

#### ADS / Staging / Stealth 대체 배치 (6)

`Colorcpl.exe`, `dtutil.exe`, `Print.exe`, `Regedit.exe`, `Regini.exe`, `Tar.exe`

### Recon (18)

Reconnaissance + Credentials + Dump/Collection을 묶은 운영용 그룹.

#### Reconnaissance (2)

`Pktmon.exe`, `Psr.exe`

#### Credentials (4)

`Cmdkey.exe`, `Findstr.exe`, `Reg.exe`, `Rpcping.exe`

#### Dump / Collection (12)

`adplus.exe`, `Comsvcs.dll`, `Createdump.exe`, `Diskshadow.exe`, `dsdbutil.exe`, `Dump64.exe`, `DumpMinitool.exe`, `ntdsutil.exe`, `rdrleakdiag.exe`, `Sqldumper.exe`, `Tttracer.exe`, `wbadmin.exe`

## 227개 전체 Master Table

아래 표는 **모든 현재 엔트리 227개**에 대해 원본 LOLBAS 기능과 사용자 정의 그룹을 같이 적은 것이다.

<details>
<summary>표 펼치기 / 접기</summary>

| Entry | Type | Official LOLBAS function(s) | Custom group(s) | Note |
|---|---|---|---|---|
| AddinUtil.exe | Binaries | Execute (.NetObjects) | Execute |  |
| AppInstaller.exe | Binaries | Download (INetCache) | Download |  |
| Aspnet_Compiler.exe | Binaries | AWL bypass | Execute |  |
| At.exe | Binaries | Execute (CMD) | Execute, Persistence |  |
| Atbroker.exe | Binaries | Execute (EXE) | Execute |  |
| Bash.exe | Binaries | Execute (CMD); AWL bypass (CMD) | Execute |  |
| Bitsadmin.exe | Binaries | ADS; Download; Copy; Execute | Download, Execute, Persistence |  |
| CertOC.exe | Binaries | Execute (DLL); Download | Download, Execute |  |
| CertReq.exe | Binaries | Download; Upload | Download |  |
| Certutil.exe | Binaries | Download (GUI); ADS; Encode; Decode | Download |  |
| Change.exe | Binaries | Execute (EXE, Rename) | Execute |  |
| Cipher.exe | Binaries | Tamper | Execute |  |
| Cmd.exe | Binaries | ADS; Download; Upload | Download |  |
| Cmdkey.exe | Binaries | Credentials | Recon |  |
| cmdl32.exe | Binaries | Download | Download |  |
| Cmstp.exe | Binaries | Execute (INF); AWL bypass (INF, Remote) | Execute, Remote |  |
| Colorcpl.exe | Binaries | Copy | Persistence | 공식 기능은 `Copy`. 이 문서에서는 별도 `Staging/Stealth` 그룹이 없어서 `Persistence`(스테이징/은닉)로 임시 배치. |
| ComputerDefaults.exe | Binaries | UAC bypass | Execute |  |
| ConfigSecurityPolicy.exe | Binaries | Upload; Download (INetCache) | Download |  |
| Conhost.exe | Binaries | Execute (CMD) | Execute |  |
| Control.exe | Binaries | ADS (DLL); Execute (DLL) | Execute |  |
| Csc.exe | Binaries | Compile | Execute |  |
| Cscript.exe | Binaries | ADS (WSH) | Script |  |
| CustomShellHost.exe | Binaries | Execute (EXE) | Execute |  |
| DataSvcUtil.exe | Binaries | Upload | Remote | 공식 기능은 `Upload`. 사용자 정의 taxonomy에 Upload/Exfil bucket이 없어서 `Remote`로 배치. |
| Desktopimgdownldr.exe | Binaries | Download | Download |  |
| DeviceCredentialDeployment.exe | Binaries | Conceal | Execute |  |
| Dfsvc.exe | Binaries | AWL bypass (ClickOnce, Remote) | Execute, Remote |  |
| Diantz.exe | Binaries | ADS (Compression); Download (Compression); Execute (Compression) | Download, Execute |  |
| Diskshadow.exe | Binaries | Dump (CMD); Execute (CMD) | Execute, Recon |  |
| Dnscmd.exe | Binaries | Execute (DLL, Remote) | Execute, Persistence, Remote |  |
| Esentutl.exe | Binaries | Copy; ADS; Download | Download |  |
| Eudcedit.exe | Binaries | UAC bypass (CMD, GUI) | Execute |  |
| Eventvwr.exe | Binaries | UAC bypass (GUI, EXE, .NetObjects) | Execute |  |
| Expand.exe | Binaries | Download; Copy; ADS | Download |  |
| Explorer.exe | Binaries | Execute (EXE) | Execute |  |
| Extexport.exe | Binaries | Execute (DLL) | Execute |  |
| Extrac32.exe | Binaries | ADS (Compression); Download; Copy | Download |  |
| Findstr.exe | Binaries | ADS; Credentials; Download | Download, Recon |  |
| Finger.exe | Binaries | Download | Download |  |
| fltMC.exe | Binaries | Tamper | Execute |  |
| Forfiles.exe | Binaries | Execute (EXE); ADS (EXE) | Execute |  |
| Fsutil.exe | Binaries | Tamper; Execute (EXE) | Execute |  |
| Ftp.exe | Binaries | Execute (CMD); Download | Download, Execute |  |
| Gpscript.exe | Binaries | Execute (CMD) | Execute |  |
| Hh.exe | Binaries | Download (EXE, GUI); Execute (EXE, GUI, CMD, CHM, Remote) | Download, Execute, Remote | URL 실행은 운영상 `Download` 우선으로 관리. 공식 태그의 `Remote`는 원격 CHM/URL 로드를 의미. |
| IMEWDBLD.exe | Binaries | Download (INetCache) | Download |  |
| Ie4uinit.exe | Binaries | Execute (INF) | Execute |  |
| iediagcmd.exe | Binaries | Execute (EXE) | Execute |  |
| Ieexec.exe | Binaries | Download (Remote, EXE (.NET)); Execute (Remote, EXE (.NET)) | Download, Execute, Remote |  |
| Ilasm.exe | Binaries | Compile | Execute |  |
| Infdefaultinstall.exe | Binaries | Execute (INF) | Execute |  |
| Installutil.exe | Binaries | AWL bypass (DLL (.NET), EXE (.NET)); Execute (DLL (.NET), EXE (.NET)); Download (INetCache) | Download, Execute |  |
| iscsicpl.exe | Binaries | UAC bypass (DLL, CMD, GUI) | Execute |  |
| Jsc.exe | Binaries | Compile (JScript) | Execute, Script |  |
| Ldifde.exe | Binaries | Download | Download |  |
| Makecab.exe | Binaries | ADS (Compression); Download (Compression); Execute (Compression) | Download, Execute |  |
| Mavinject.exe | Binaries | Execute (DLL); ADS (DLL) | Execute |  |
| Microsoft.Workflow.Compiler.exe | Binaries | Execute (VB.Net, Csharp, XOML); AWL bypass (XOML) | Execute, Script |  |
| Mmc.exe | Binaries | Execute (COM); UAC bypass (DLL); Download (GUI) | Download, Execute |  |
| MpCmdRun.exe | Binaries | Download; ADS | Download |  |
| Msbuild.exe | Binaries | AWL bypass (CSharp); Execute (CSharp, DLL, XSL, CMD) | Execute, Script |  |
| Msconfig.exe | Binaries | Execute (CMD) | Execute |  |
| Msdt.exe | Binaries | Execute (GUI, MSI); AWL bypass (GUI, MSI, CMD) | Execute |  |
| Msedge.exe | Binaries | Download; Execute (CMD) | Download, Execute |  |
| Mshta.exe | Binaries | Execute (HTA, Remote, VBScript, JScript); ADS (HTA); Download (INetCache) | Download, Execute, Remote, Script |  |
| Msiexec.exe | Binaries | Execute (MSI, Remote, DLL, MST) | Execute, Remote |  |
| Netsh.exe | Binaries | Execute (DLL) | Execute |  |
| Ngen.exe | Binaries | Download (INetCache) | Download |  |
| Odbcconf.exe | Binaries | Execute (DLL) | Execute |  |
| OfflineScannerShell.exe | Binaries | Execute (DLL) | Execute |  |
| OneDriveStandaloneUpdater.exe | Binaries | Download | Download |  |
| Pcalua.exe | Binaries | Execute (EXE, DLL, Remote) | Execute, Remote |  |
| Pcwrun.exe | Binaries | Execute (EXE) | Execute |  |
| Pktmon.exe | Binaries | Reconnaissance | Recon |  |
| Pnputil.exe | Binaries | Execute (INF) | Execute, Persistence |  |
| Presentationhost.exe | Binaries | Execute (XBAP); Download (INetCache) | Download, Execute |  |
| Print.exe | Binaries | ADS; Copy | Persistence | 공식 기능은 `ADS; Copy`. 이 문서에서는 `Persistence`(ADS/은닉/스테이징)로 임시 배치. |
| PrintBrm.exe | Binaries | Download (Compression); ADS (Compression) | Download |  |
| Provlaunch.exe | Binaries | Execute (CMD) | Execute |  |
| Psr.exe | Binaries | Reconnaissance | Recon |  |
| Query.exe | Binaries | Execute (EXE, Rename) | Execute |  |
| Rasautou.exe | Binaries | Execute (DLL) | Execute |  |
| rdrleakdiag.exe | Binaries | Dump | Recon |  |
| Reg.exe | Binaries | ADS; Credentials | Recon |  |
| Regasm.exe | Binaries | AWL bypass (DLL (.NET)); Execute (DLL (.NET)) | Execute |  |
| Regedit.exe | Binaries | ADS | Persistence | 공식 기능은 `ADS`. 이 문서에서는 `Persistence`(ADS/은닉/스테이징)로 임시 배치. |
| Regini.exe | Binaries | ADS | Persistence | 공식 기능은 `ADS`. 이 문서에서는 `Persistence`(ADS/은닉/스테이징)로 임시 배치. |
| Register-cimprovider.exe | Binaries | Execute (DLL) | Execute |  |
| Regsvcs.exe | Binaries | Execute (DLL (.NET)); AWL bypass (DLL (.NET)) | Execute |  |
| Regsvr32.exe | Binaries | AWL bypass (SCT, Remote); Execute (SCT, Remote, DLL) | Execute, Remote, Script |  |
| Replace.exe | Binaries | Copy; Download | Download |  |
| Reset.exe | Binaries | Execute (EXE, Rename) | Execute |  |
| Rpcping.exe | Binaries | Credentials | Recon |  |
| Rundll32.exe | Binaries | Execute (DLL, Remote, JScript, COM); ADS (DLL) | Execute, Remote, Script |  |
| Runexehelper.exe | Binaries | Execute (EXE) | Execute |  |
| Runonce.exe | Binaries | Execute (CMD) | Execute, Persistence |  |
| Runscripthelper.exe | Binaries | Execute (PowerShell) | Execute, Script |  |
| Sc.exe | Binaries | ADS (EXE) | Execute, Persistence, Remote | 공식 LOLBAS 태그는 `ADS`만 보이지만, 서비스 생성/변경/원격 제어 때문에 `Remote`/`Persistence`에 포함. |
| Schtasks.exe | Binaries | Execute (CMD) | Execute, Persistence, Remote | `/S` 사용 시 원격 작업 예약 가능. 지속성 확보에도 자주 사용. |
| Scriptrunner.exe | Binaries | Execute (EXE, Remote, CMD) | Execute, Remote |  |
| Setres.exe | Binaries | Execute (EXE) | Execute |  |
| SettingSyncHost.exe | Binaries | Execute (EXE, CMD) | Execute |  |
| Sftp.exe | Binaries | Execute (CMD) | Execute, Remote | 원격 전송 세션으로 `Remote`에 포함. 단, `Tunnel`로는 보수적으로 미포함. |
| ssh.exe | Binaries | Execute (CMD) | Execute, Remote, Tunnel | 명령 실행 외에 포트포워딩/터널링 관점에서 `Tunnel`에도 포함. |
| Stordiag.exe | Binaries | Execute (EXE) | Execute |  |
| SyncAppvPublishingServer.exe | Binaries | Execute (PowerShell) | Execute, Script |  |
| Tar.exe | Binaries | ADS (Compression); Copy (Compression) | Persistence | 공식 기능은 `ADS/Copy`. 이 문서에서는 `Persistence`(스테이징/은닉)로 임시 배치. |
| Ttdinject.exe | Binaries | Execute (EXE) | Execute |  |
| Tttracer.exe | Binaries | Execute (EXE); Dump | Execute, Recon |  |
| Unregmp2.exe | Binaries | Execute (EXE) | Execute |  |
| vbc.exe | Binaries | Compile | Execute |  |
| Verclsid.exe | Binaries | Execute (COM) | Execute |  |
| Wab.exe | Binaries | Execute (DLL) | Execute |  |
| wbadmin.exe | Binaries | Dump | Recon |  |
| wbemtest.exe | Binaries | Execute (GUI, CMD) | Execute, Remote |  |
| winget.exe | Binaries | Execute (Remote, EXE); Download; AWL bypass | Download, Execute, Remote |  |
| Wlrmdr.exe | Binaries | Execute (EXE) | Execute |  |
| Wmic.exe | Binaries | ADS (EXE); Execute (CMD, Remote, XSL); Copy | Execute, Remote, Script |  |
| WorkFolders.exe | Binaries | Execute (EXE) | Execute |  |
| Wscript.exe | Binaries | ADS (WSH) | Script |  |
| Wsreset.exe | Binaries | UAC bypass | Execute |  |
| wuauclt.exe | Binaries | Execute (DLL) | Execute |  |
| Xwizard.exe | Binaries | Execute (COM); Download (INetCache) | Download, Execute |  |
| msedge_proxy.exe | Binaries | Download; Execute (CMD) | Download, Execute |  |
| msedgewebview2.exe | Binaries | Execute (EXE, CMD) | Execute |  |
| wt.exe | Binaries | Execute (CMD) | Execute |  |
| Advpack.dll | Libraries | AWL bypass (INF); Execute (DLL, EXE, CMD) | Execute |  |
| Desk.cpl | Libraries | Execute (EXE, Remote) | Execute, Remote |  |
| Dfshim.dll | Libraries | AWL bypass (ClickOnce, Remote) | Execute, Remote |  |
| Ieadvpack.dll | Libraries | AWL bypass (INF); Execute (DLL, EXE, CMD) | Execute |  |
| Ieframe.dll | Libraries | Execute (URL) | Execute |  |
| Mshtml.dll | Libraries | Execute (HTA) | Execute, Script |  |
| Pcwutl.dll | Libraries | Execute (EXE) | Execute |  |
| PhotoViewer.dll | Libraries | Download (INetCache) | Download |  |
| Scrobj.dll | Libraries | Download (INetCache) | Download |  |
| Setupapi.dll | Libraries | AWL bypass (INF); Execute (INF) | Execute |  |
| Shdocvw.dll | Libraries | Execute (URL) | Execute |  |
| Shell32.dll | Libraries | Execute (DLL, EXE, CMD) | Execute |  |
| Shimgvw.dll | Libraries | Download (INetCache) | Download |  |
| Syssetup.dll | Libraries | AWL bypass (INF); Execute (INF) | Execute |  |
| Url.dll | Libraries | Execute (HTA, URL, EXE) | Execute, Script |  |
| Zipfldr.dll | Libraries | Execute (EXE) | Execute |  |
| Comsvcs.dll | Libraries | Dump | Recon |  |
| AccCheckConsole.exe | OtherMSBinaries | Execute (DLL (.NET)); AWL bypass (DLL (.NET)) | Execute |  |
| adplus.exe | OtherMSBinaries | Dump; Execute (CMD, EXE) | Execute, Recon |  |
| AgentExecutor.exe | OtherMSBinaries | Execute (PowerShell, EXE) | Execute, Script |  |
| AppLauncher.exe | OtherMSBinaries | Execute (EXE) | Execute |  |
| AppCert.exe | OtherMSBinaries | Execute (EXE, MSI) | Execute |  |
| Appvlp.exe | OtherMSBinaries | Execute (CMD, EXE) | Execute |  |
| Bcp.exe | OtherMSBinaries | Download | Download |  |
| Bginfo.exe | OtherMSBinaries | Execute (WSH, Remote); AWL bypass (WSH, Remote) | Execute, Remote, Script |  |
| Cdb.exe | OtherMSBinaries | Execute (Shellcode, CMD) | Execute |  |
| coregen.exe | OtherMSBinaries | Execute (DLL); AWL bypass (DLL) | Execute |  |
| Createdump.exe | OtherMSBinaries | Dump | Recon |  |
| csi.exe | OtherMSBinaries | Execute (CSharp) | Execute |  |
| DefaultPack.EXE | OtherMSBinaries | Execute (CMD) | Execute |  |
| Devinit.exe | OtherMSBinaries | Execute (MSI, Remote) | Execute, Remote |  |
| Devtoolslauncher.exe | OtherMSBinaries | Execute (CMD) | Execute |  |
| dnx.exe | OtherMSBinaries | Execute (CSharp) | Execute |  |
| Dotnet.exe | OtherMSBinaries | AWL bypass (DLL (.NET), CSharp); Execute (DLL (.NET), FSharp) | Execute |  |
| dsdbutil.exe | OtherMSBinaries | Dump | Recon |  |
| dtutil.exe | OtherMSBinaries | Copy | Persistence | 공식 기능은 `Copy`. 이 문서에서는 `Persistence`(배포/스테이징)로 임시 배치. |
| Dump64.exe | OtherMSBinaries | Dump | Recon |  |
| DumpMinitool.exe | OtherMSBinaries | Dump | Recon |  |
| Dxcap.exe | OtherMSBinaries | Execute (EXE) | Execute |  |
| ECMangen.exe | OtherMSBinaries | Download (INetCache) | Download |  |
| Excel.exe | OtherMSBinaries | Download (INetCache) | Download |  |
| Fsi.exe | OtherMSBinaries | AWL bypass (FSharp) | Execute |  |
| FsiAnyCpu.exe | OtherMSBinaries | AWL bypass (FSharp) | Execute |  |
| IntelliTrace.exe | OtherMSBinaries | Execute (EXE) | Execute |  |
| Mftrace.exe | OtherMSBinaries | Execute (EXE) | Execute |  |
| Microsoft.NodejsTools.PressAnyKey.exe | OtherMSBinaries | Execute (EXE) | Execute |  |
| Mpiexec.exe | OtherMSBinaries | Execute (CMD) | Execute, Remote |  |
| MSAccess.exe | OtherMSBinaries | Download (INetCache) | Download |  |
| Msdeploy.exe | OtherMSBinaries | Execute (CMD); AWL bypass (CMD); Copy | Execute |  |
| MsoHtmEd.exe | OtherMSBinaries | Download (INetCache) | Download |  |
| Mspub.exe | OtherMSBinaries | Download (INetCache) | Download |  |
| msxsl.exe | OtherMSBinaries | Execute (XSL, Remote); AWL bypass (XSL, Remote); Download; ADS | Download, Execute, Remote, Script |  |
| ntdsutil.exe | OtherMSBinaries | Dump | Recon |  |
| Ntsd.exe | OtherMSBinaries | Execute (CMD) | Execute |  |
| OpenConsole.exe | OtherMSBinaries | Execute (EXE) | Execute |  |
| Pixtool.exe | OtherMSBinaries | Execute (EXE) | Execute |  |
| Powerpnt.exe | OtherMSBinaries | Download (INetCache) | Download |  |
| Procdump.exe | OtherMSBinaries | Execute (DLL) | Execute |  |
| ProtocolHandler.exe | OtherMSBinaries | Download | Download |  |
| rcsi.exe | OtherMSBinaries | Execute (CSharp); AWL bypass (CSharp) | Execute |  |
| Remote.exe | OtherMSBinaries | AWL bypass (EXE); Execute (EXE, Remote) | Execute, Remote |  |
| Sqldumper.exe | OtherMSBinaries | Dump | Recon |  |
| Sqlps.exe | OtherMSBinaries | Execute (PowerShell) | Execute, Script |  |
| SQLToolsPS.exe | OtherMSBinaries | Execute (PowerShell) | Execute, Script |  |
| Squirrel.exe | OtherMSBinaries | Download; AWL bypass (Nuget, Remote); Execute (Nuget, Remote) | Download, Execute, Remote |  |
| te.exe | OtherMSBinaries | Execute (WSH, DLL, Custom Format) | Execute, Script |  |
| Teams.exe | OtherMSBinaries | Execute (Node.JS, CMD) | Execute, Script |  |
| TestWindowRemoteAgent.exe | OtherMSBinaries | Upload | Remote | 공식 기능은 `Upload`. 사용자 정의 taxonomy에 Upload/Exfil bucket이 없어서 `Remote`로 배치. |
| Tracker.exe | OtherMSBinaries | Execute (DLL); AWL bypass (DLL) | Execute |  |
| Update.exe | OtherMSBinaries | Download; AWL bypass (Nuget, Remote, CMD); Execute (Nuget, Remote, CMD, EXE) | Download, Execute, Persistence, Remote |  |
| VSDiagnostics.exe | OtherMSBinaries | Execute (EXE, CMD) | Execute |  |
| VSIISExeLauncher.exe | OtherMSBinaries | Execute (EXE) | Execute |  |
| Visio.exe | OtherMSBinaries | Download (INetCache) | Download |  |
| VisualUiaVerifyNative.exe | OtherMSBinaries | AWL bypass (.NetObjects) | Execute |  |
| VSLaunchBrowser.exe | OtherMSBinaries | Download (INetCache); Execute (EXE, Remote) | Download, Execute, Remote |  |
| Vshadow.exe | OtherMSBinaries | Execute (EXE) | Execute |  |
| vsjitdebugger.exe | OtherMSBinaries | Execute (EXE) | Execute |  |
| WFMFormat.exe | OtherMSBinaries | Execute (EXE, .NET Framework 3.5) | Execute |  |
| Wfc.exe | OtherMSBinaries | AWL bypass (XOML) | Execute, Script |  |
| WinDbg.exe | OtherMSBinaries | Execute (CMD) | Execute |  |
| WinProj.exe | OtherMSBinaries | Download (INetCache) | Download |  |
| Winword.exe | OtherMSBinaries | Download (INetCache) | Download |  |
| Wsl.exe | OtherMSBinaries | Execute (EXE, CMD); Download | Download, Execute |  |
| XBootMgr.exe | OtherMSBinaries | Execute (EXE) | Execute |  |
| XBootMgrSleep.exe | OtherMSBinaries | Execute (EXE) | Execute |  |
| devtunnel.exe | OtherMSBinaries | Download | Download, Tunnel | 공식 LOLBAS 태그는 `Download`지만, `host`/포트포워딩 시나리오는 운영상 `Tunnel`로 관리. |
| vsls-agent.exe | OtherMSBinaries | Execute (DLL) | Execute |  |
| vstest.console.exe | OtherMSBinaries | AWL bypass (DLL) | Execute |  |
| winfile.exe | OtherMSBinaries | Execute (EXE) | Execute |  |
| xsd.exe | OtherMSBinaries | Download (INetCache) | Download |  |
| CL_LoadAssembly.ps1 | Scripts | Execute (DLL (.NET)) | Execute, Script |  |
| CL_Mutexverifiers.ps1 | Scripts | Execute (PowerShell) | Execute, Script |  |
| CL_Invocation.ps1 | Scripts | Execute (CMD) | Execute, Script |  |
| Launch-VsDevShell.ps1 | Scripts | Execute (EXE) | Execute, Script |  |
| Manage-bde.wsf | Scripts | Execute (EXE) | Execute, Script |  |
| Pubprn.vbs | Scripts | Execute (SCT) | Execute, Script |  |
| Syncappvpublishingserver.vbs | Scripts | Execute (PowerShell) | Execute, Script |  |
| UtilityFunctions.ps1 | Scripts | Execute (DLL (.NET)) | Execute, Script |  |
| winrm.vbs | Scripts | Execute (CMD, Remote); AWL bypass (XSL) | Execute, Remote, Script | `Execute (CMD, Remote)`이므로 `Remote`를 별도 관리 대상으로 분리. |
| Pester.bat | Scripts | Execute (EXE) | Execute, Script |  |

</details>

## 운영 적용 제안

- **1차 우선 관리**: `Remote`, `Tunnel`, `Script`, `Download`
- **2차 관리**: `Persistence`, `Recon`
- **기본 차단/감시군**: `Execute`

실제 정책(WDAC, rename, allowlist, audit)을 만들 때는 다음 순서를 권장한다.

1. `Tunnel`과 `Remote`를 먼저 별도 태그로 분리
2. `Script`를 별도 통제군으로 분리
3. `Download`를 외부 반입 경로로 분리
4. `Persistence`를 스케줄러/서비스/ADS 중심으로 별도 점검
5. 나머지 실행 계열은 `Execute` 공통군으로 유지

## 후속 확장 아이디어

이 문서는 사용자 요청에 맞춘 7개 그룹 버전이다.  
운영이 더 정교해지면 아래처럼 2단계 taxonomy로 확장하는 것이 좋다.

- `Download`
- `Execute`
- `Script`
- `Remote-Host`
- `Remote-Payload`
- `Tunnel`
- `Persistence`
- `Recon`
- `CredentialAccess`
- `Dump`
- `Stealth/ADS`
- `Upload/Exfil`

