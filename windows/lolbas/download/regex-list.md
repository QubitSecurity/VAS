# LOLBAS Download (55) 탐지용 정규식 정책표
작성일: 2026-03-14

이 문서는 앞서 정리한 **LOLBAS Download 55개**를 기준으로, **CLI 출력/Process CommandLine 문자열 기반 탐지 정책**을 설계하기 위한 운영용 정리본입니다.

## 설계 원칙
- 기본 전제: **명령행 문자열에서 실행파일명 + URL/원격경로/특정 옵션**을 탐지한다.
- 정규식은 **대소문자 무시 `(?i)`** 기준이다.
- URL은 보통 `http/https/ftp`, 원격 파일은 `UNC/WebDAV`, 일부는 `ms-appinstaller://`, `/dev/tcp/`, 레지스트리/입력파일 기반 간접형이다.
- **직접 URL형**과 **간접형**을 같은 정책으로 억지로 묶지 않는다.
- `devtunnel.exe`는 공식 Download 목록에 있으나 운영상은 **Tunnel/Remote Access** 정책으로 분리하는 것을 권장한다.

## 권장 운영 구조
1. **Broad prefilter**: 다운로드형 LOLBAS 후보 실행파일명만 먼저 좁힌다.
   - <code>(?i)\b(?:appinstaller|bitsadmin|certoc|certreq|certutil|cmd|cmdl32|configsecuritypolicy|desktopimgdownldr|diantz|esentutl|expand|extrac32|findstr|finger|ftp|hh|ieexec|imewdbld|installutil|ldifde|makecab|mmc|mpcmdrun|msedge|msedge_proxy|mshta|ngen|onedrivestandaloneupdater|presentationhost|printbrm|replace|winget|xwizard|rundll32|bcp|devtunnel|ecmangen|excel|msaccess|msohtmed|mspub|msxsl|powerpnt|protocolhandler|squirrel|update|visio|vslaunchbrowser|winproj|winword|wsl|xsd)(?:\.exe)?\b</code>
2. **그룹 규칙**: Direct URL / Option+URL / UNC-WebDAV / Office URL / Rundll32 DLL / Package / Indirect / Non-URL 네트워크형으로 나눠 적용한다.
3. **예외 규칙**: `cmdl32`, `ldifde`, `mmc`, `OneDriveStandaloneUpdater`, `winget`, `devtunnel`은 별도 룰 또는 보조 시그널(파일/레지스트리/자식프로세스)을 함께 본다.

## 그룹 공통 정규식
- **Direct URL**: <code>(?i)\b(?:hh|mshta|configsecuritypolicy|ieexec|imewdbld|installutil|ngen|presentationhost|ecmangen|protocolhandler|vslaunchbrowser|xsd)(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s\&quot;&#x27;&lt;&gt;]+)</code>
- **Option + URL**: <code>(?i)\b(?:certoc|certreq|certutil|desktopimgdownldr|mpcmdrun|xwizard)(?:\.exe)?\b.*?(?:(?:https?|ftp)://[^\s\&quot;&#x27;&lt;&gt;]+)</code>
- **Office URL**: <code>(?i)\b(?:excel|msaccess|msohtmed|mspub|powerpnt|visio|winproj|winword)(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s\&quot;&#x27;&lt;&gt;]+)</code>
- **UNC/WebDAV**: <code>(?i)\b(?:cmd|diantz|esentutl|expand|extrac32|findstr|makecab|printbrm|replace)(?:\.exe)?\b.*?(\\\\[^\s\&quot;&#x27;&lt;&gt;]+\\[^\s\&quot;&#x27;&lt;&gt;]+(?:\\[^\s\&quot;&#x27;&lt;&gt;]+)*)</code>
- **Rundll32 DLL URL**: <code>(?i)\brundll32(?:\.exe)?\b.*?(?:photoviewer\.dll|scrobj\.dll|shimgvw\.dll).*?(?:(?:https?|ftp)://[^\s\&quot;&#x27;&lt;&gt;]+)</code>
- **Package/URI**: <code>(?i)(?:\bstart\s+ms-appinstaller://\?source=(?:(?:https?|ftp)://[^\s\&quot;&#x27;&lt;&gt;]+))|(?:\b(?:winget|squirrel|update)(?:\.exe)?\b.*?(?:(?:https?|ftp)://[^\s\&quot;&#x27;&lt;&gt;]+))</code>
- **Indirect/Special**: <code>(?i)\b(?:cmdl32|ldifde|mmc|onedrivestandaloneupdater|bcp|devtunnel|wsl|finger|ftp)(?:\.exe)?\b</code>

## 정책표
| No | Tool | Group | Regex | FP 주의 | FN 주의 | 권장 정책명 |
|---:|---|---|---|---|---|---|
| 1 | AppInstaller.exe | URI/AppInstaller | <code>(?i)\bstart\s+ms-appinstaller://\?source=(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | `start ms-appinstaller://` 정상 배포 스크립트 | PowerShell `Start-Process`로 우회 가능 | `LOLBAS.Download.AppInstaller.URI` |
| 2 | Bitsadmin.exe | BITS job + URL | <code>(?i)\bbitsadmin(?:\.exe)?\b.*?\b/addfile\b.*?(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)\s+[^\r\n]+</code> | 정상 BITS 배포/업데이트 | `/transfer` 변형, 줄바꿈 분리 | `LOLBAS.Download.BITS.AddFile` |
| 3 | CertOC.exe | Option + URL | <code>(?i)\bcertoc(?:\.exe)?\b.*?\b-GetCACAPS\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 인증서/PKI 테스트 | 대소문자/공백 변형 | `LOLBAS.Download.CertOC.GetCACAPS` |
| 4 | CertReq.exe | Option + URL | <code>(?i)\bcertreq(?:\.exe)?\b.*?\b-post\b.*?\b-config\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 ADCS 등록 | URL이 `-config`가 아닌 파일 내부면 누락 | `LOLBAS.Download.CertReq.Post` |
| 5 | Certutil.exe | Option + URL | <code>(?i)\bcertutil(?:\.exe)?\b.*?\b(?:-urlcache|-verifyctl|-URL)\b.*?(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 인증서 점검/CRL 조회 | 분리 실행, 인코딩된 cmdline | `LOLBAS.Download.Certutil.URL` |
| 6 | Cmd.exe | UNC/WebDAV copy | <code>(?i)\bcmd(?:\.exe)?\b.*?\btype\b\s+(\\\\[^\s&quot;&#x27;&lt;&gt;]+\\[^\s&quot;&#x27;&lt;&gt;]+(?:\\[^\s&quot;&#x27;&lt;&gt;]+)*)\s*&gt;</code> | 배치 파일 내 일반 파일 copy | `cmd /c` 내부 따옴표/escape 다양 | `LOLBAS.Download.Cmd.TypeUNC` |
| 7 | cmdl32.exe | Indirect config-driven | <code>(?i)\bcmdl32(?:\.exe)?\b.*?\b/(?:vpn|lan)\b</code> | 정상 VPN/네트워크 구성 | URL이 CLI에 없어 다운로드 단정 어려움 | `LOLBAS.Download.Cmdl32.Indirect` |
| 8 | ConfigSecurityPolicy.exe | Direct URL | <code>(?i)\bconfigsecuritypolicy(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 정책/테스트 파일 로드 | 파일 URL/UNC 변형 누락 | `LOLBAS.Download.ConfigSecurityPolicy.URL` |
| 9 | Desktopimgdownldr.exe | Option + URL | <code>(?i)\bdesktopimgdownldr(?:\.exe)?\b.*?/lockscreenurl:(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 배경화면 설정 | 환경변수·상위 cmd 래핑 | `LOLBAS.Download.DesktopImg.URL` |
| 10 | Diantz.exe | UNC/WebDAV copy | <code>(?i)\bdiantz(?:\.exe)?\b\s+(\\\\[^\s&quot;&#x27;&lt;&gt;]+\\[^\s&quot;&#x27;&lt;&gt;]+(?:\\[^\s&quot;&#x27;&lt;&gt;]+)*)\s+[^\r\n]+</code> | 정상 CAB 생성 | 로컬 파일 CAB 생성과 혼동 | `LOLBAS.Download.Diantz.UNC` |
| 11 | Esentutl.exe | UNC/WebDAV copy | <code>(?i)\besentutl(?:\.exe)?\b.*?\b/y\b\s+(\\\\[^\s&quot;&#x27;&lt;&gt;]+\\[^\s&quot;&#x27;&lt;&gt;]+(?:\\[^\s&quot;&#x27;&lt;&gt;]+)*).*?\b/d\b</code> | 정상 파일 복구/복사 | 원격→원격 복사와 구분 필요 | `LOLBAS.Download.Esentutl.Copy` |
| 12 | Expand.exe | UNC/WebDAV copy | <code>(?i)\bexpand(?:\.exe)?\b\s+(\\\\[^\s&quot;&#x27;&lt;&gt;]+\\[^\s&quot;&#x27;&lt;&gt;]+(?:\\[^\s&quot;&#x27;&lt;&gt;]+)*)\s+[^\r\n]+</code> | 정상 CAB 확장 | 로컬 CAB 처리와 혼동 | `LOLBAS.Download.Expand.UNC` |
| 13 | Extrac32.exe | UNC/WebDAV copy | <code>(?i)\bextrac32(?:\.exe)?\b.*?\b/[YC]\b.*?(\\\\[^\s&quot;&#x27;&lt;&gt;]+\\[^\s&quot;&#x27;&lt;&gt;]+(?:\\[^\s&quot;&#x27;&lt;&gt;]+)*)</code> | 정상 CAB/파일 추출 | 옵션 순서 변화 | `LOLBAS.Download.Extrac32.Remote` |
| 14 | Findstr.exe | UNC/WebDAV copy | <code>(?i)\bfindstr(?:\.exe)?\b.*?(\\\\[^\s&quot;&#x27;&lt;&gt;]+\\[^\s&quot;&#x27;&lt;&gt;]+(?:\\[^\s&quot;&#x27;&lt;&gt;]+)*)\s*&gt;</code> | 로그/텍스트 검색 정상 사용 | 원격 경로 검색 자체는 정상일 수 있음 | `LOLBAS.Download.Findstr.Stream` |
| 15 | Finger.exe | Network response pipeline | <code>(?i)\bfinger(?:\.exe)?\b\s+[^\s@]+@[^\s]+\s*\|\s*more\s*\+?\d*\s*\|\s*cmd</code> | 레거시 진단 명령 | 파이프라인 변형 많음 | `LOLBAS.Download.Finger.Pipeline` |
| 16 | Ftp.exe | Scripted FTP | <code>(?i)\bftp(?:\.exe)?\b.*?\b-s:[^\s&quot;&#x27;]+</code> | 정상 FTP 자동화 | 스크립트 내부 GET 내용은 별도 로그 필요 | `LOLBAS.Download.FTP.Script` |
| 17 | Hh.exe | Direct URL | <code>(?i)\bhh(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 CHM/문서 열기 | 로컬 CHM은 제외해야 함 | `LOLBAS.Download.HH.URL` |
| 18 | Ieexec.exe | Direct URL | <code>(?i)\bieexec(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 구형 .NET 원격 앱 | 다른 URL scheme 변형 | `LOLBAS.Download.Ieexec.URL` |
| 19 | IMEWDBLD.exe | Direct URL | <code>(?i)\bimewdbld(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 사전/입력기 구성 | 희귀 도구라 baseline 필요 | `LOLBAS.Download.IMEWDBLD.URL` |
| 20 | Installutil.exe | Direct URL | <code>(?i)\binstallutil(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 설치/관리 작업 | 실행형 LOLBAS와 중첩 | `LOLBAS.Download.Installutil.URL` |
| 21 | Ldifde.exe | Indirect file-driven | <code>(?i)\bldifde(?:\.exe)?\b.*?\b-i\b.*?\b-f\b\s+[^\s&quot;&#x27;]+</code> | 정상 LDAP import | URL이 LDF 내부에 있어 CLI만으로 확증 어려움 | `LOLBAS.Download.Ldifde.FileDriven` |
| 22 | Makecab.exe | UNC/WebDAV copy | <code>(?i)\bmakecab(?:\.exe)?\b\s+(\\\\[^\s&quot;&#x27;&lt;&gt;]+\\[^\s&quot;&#x27;&lt;&gt;]+(?:\\[^\s&quot;&#x27;&lt;&gt;]+)*)\s+[^\r\n]+</code> | 정상 CAB 생성 | 로컬 파일과 혼동 | `LOLBAS.Download.Makecab.UNC` |
| 23 | Mmc.exe | Indirect MSC-driven | <code>(?i)\bmmc(?:\.exe)?\b.*?\b-Embedding\b\s+[^\s&quot;&#x27;]+\.msc\b</code> | 정상 MMC snap-in | URL이 CLI에 없고 .msc 내부 행위 필요 | `LOLBAS.Download.MMC.MSC` |
| 24 | MpCmdRun.exe | Option + URL | <code>(?i)\bmpcmdrun(?:\.exe)?\b.*?\b-DownloadFile\b.*?\b-url\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+).*?\b-path\b\s+[^\r\n]+</code> | Defender 진단/업데이트 | 플랫폼 폴더 복사 후 실행 변형 | `LOLBAS.Download.MpCmdRun.URL` |
| 25 | Msedge.exe | Browser URL | <code>(?i)\bmsedge(?:\.exe)?\b(?:\s+--headless\b.*?\b--dump-dom\b.*?(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)|\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+))</code> | 정상 브라우저 열기 | 브라우저 실행 자체는 오탐 가능 | `LOLBAS.Download.MSEdge.URL` |
| 26 | msedge_proxy.exe | Browser URL | <code>(?i)\bmsedge_proxy(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 Edge helper 호출 | 브라우저 보조 프로세스 baseline 필요 | `LOLBAS.Download.MSEdgeProxy.URL` |
| 27 | Mshta.exe | Direct URL | <code>(?i)\bmshta(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 HTA/웹앱 테스트 | data:, javascript: 등 다른 scheme 누락 | `LOLBAS.Download.MSHTA.URL` |
| 28 | Ngen.exe | Direct URL | <code>(?i)\bngen(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 희귀 정상 사용 | 실행형 LOLBAS와 중첩 | `LOLBAS.Download.NGEN.URL` |
| 29 | OneDriveStandaloneUpdater.exe | Indirect registry-driven | <code>(?i)\bonedrivestandaloneupdater(?:\.exe)?\b\b</code> | 정상 OneDrive 업데이트 | URL이 레지스트리에 있어 CLI만으로 식별 불가 | `LOLBAS.Download.OneDrive.Registry` |
| 30 | Presentationhost.exe | Direct URL | <code>(?i)\bpresentationhost(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 XBAP/프레젠테이션 | 브라우저 호출 체인 변형 | `LOLBAS.Download.PresentationHost.URL` |
| 31 | PrintBrm.exe | UNC/WebDAV copy | <code>(?i)\bprintbrm(?:\.exe)?\b.*?\b-b\b.*?\b-d\b\s+(\\\\[^\s&quot;&#x27;&lt;&gt;]+\\[^\s&quot;&#x27;&lt;&gt;]+(?:\\[^\s&quot;&#x27;&lt;&gt;]+)*).*?\b-f\b</code> | 정상 프린터 백업 | 관리자 작업과 중첩 | `LOLBAS.Download.PrintBrm.UNC` |
| 32 | Replace.exe | UNC/WebDAV copy | <code>(?i)\breplace(?:\.exe)?\b\s+(\\\\[^\s&quot;&#x27;&lt;&gt;]+\\[^\s&quot;&#x27;&lt;&gt;]+(?:\\[^\s&quot;&#x27;&lt;&gt;]+)*)\s+[^\r\n]+\s+/A\b</code> | 정상 파일 교체 | 원격 공유 배포와 혼동 | `LOLBAS.Download.Replace.UNC` |
| 33 | winget.exe | Package manager | <code>(?i)\bwinget(?:\.exe)?\b\s+install\b.*?\b(?:-s|--source)\b\s+msstore\b</code> | 정상 앱 설치 | 실제 원격 URL이 안 보임 | `LOLBAS.Download.Winget.Store` |
| 34 | Xwizard.exe | Option + URL | <code>(?i)\bxwizard(?:\.exe)?\b\s+RunWizard\b.*?/z(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 희귀 정상 사용 | 공백/인자 순서 변화 | `LOLBAS.Download.Xwizard.URL` |
| 35 | PhotoViewer.dll | Rundll32 DLL URL | <code>(?i)\brundll32(?:\.exe)?\b.*?photoviewer\.dll\s*,\s*ImageView_Fullscreen\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 이미지 보기 | 엔트리포인트 철자 변형 | `LOLBAS.Download.PhotoViewer.Rundll32` |
| 36 | Scrobj.dll | Rundll32 DLL URL | <code>(?i)\brundll32(?:\.exe)?\b.*?scrobj\.dll\s*,\s*GenerateTypeLib\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 사용 드묾 | 공백/쉼표 변형 | `LOLBAS.Download.Scrobj.Rundll32` |
| 37 | Shimgvw.dll | Rundll32 DLL URL | <code>(?i)\brundll32(?:\.exe)?\b.*?shimgvw\.dll\s*,\s*ImageView_Fullscreen[A]?\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 이미지 보기 | 엔트리포인트 변형 | `LOLBAS.Download.Shimgvw.Rundll32` |
| 38 | Bcp.exe | SQL queryout | <code>(?i)\bbcp(?:\.exe)?\b\s+&quot;?select\b.*?\bqueryout\b\s+&quot;?[^\r\n]+</code> | 정상 DB export | 인터넷 URL이 아니라 DB→파일 export | `LOLBAS.Download.BCP.QueryOut` |
| 39 | devtunnel.exe | Tunnel/Remote Access | <code>(?i)\bdevtunnel(?:\.exe)?\b\s+host\b.*?\b-p\b\s+\d+</code> | 개발용 포트포워딩 | 다운로드 regex가 아니라 터널 정책으로 분리 권장 | `LOLBAS.Tunnel.DevTunnel.Host` |
| 40 | ECMangen.exe | Direct URL | <code>(?i)\becmangen(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 희귀 정상 사용 | 벤더/경로 차이 | `LOLBAS.Download.ECMangen.URL` |
| 41 | Excel.exe | Office URL | <code>(?i)\bexcel(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 Office URL 열기 | 브라우저 핸드오프·파일 연결 변형 | `LOLBAS.Download.Excel.URL` |
| 42 | MSAccess.exe | Office URL | <code>(?i)\bmsaccess(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 Access 원격 파일 열기 | 확장자 의존 동작 | `LOLBAS.Download.MSAccess.URL` |
| 43 | MsoHtmEd.exe | Office URL | <code>(?i)\bmsohtmed(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 희귀 정상 사용 | Office 구성요소 경로 차이 | `LOLBAS.Download.MsoHtmEd.URL` |
| 44 | Mspub.exe | Office URL | <code>(?i)\bmspub(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 Publisher 원격 열기 | 파일 연결/래핑 변형 | `LOLBAS.Download.MSPub.URL` |
| 45 | msxsl.exe | Remote XML/XSL | <code>(?i)\bmsxsl(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+).*?\b-o\b</code> | 정상 XML/XSL 처리 | URL 하나만 원격인 변형 | `LOLBAS.Download.MSXSL.Remote` |
| 46 | Powerpnt.exe | Office URL | <code>(?i)\bpowerpnt(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 PowerPoint URL 열기 | 브라우저 핸드오프 | `LOLBAS.Download.PowerPoint.URL` |
| 47 | ProtocolHandler.exe | Browser/Handler URL | <code>(?i)\bprotocolhandler(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 URI handler | 기본 브라우저 호출만 남는 환경 있음 | `LOLBAS.Download.ProtocolHandler.URL` |
| 48 | Squirrel.exe | Package updater | <code>(?i)\bsquirrel(?:\.exe)?\b.*?(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 앱 업데이트 | 인자명이 앱별로 상이 | `LOLBAS.Download.Squirrel.URL` |
| 49 | Update.exe | Package updater | <code>(?i)\bupdate(?:\.exe)?\b.*?(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 앱 업데이트 | 벤더별 옵션 차이 | `LOLBAS.Download.Update.URL` |
| 50 | Visio.exe | Office URL | <code>(?i)\bvisio(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 Visio URL 열기 | 브라우저 핸드오프 | `LOLBAS.Download.Visio.URL` |
| 51 | VSLaunchBrowser.exe | Browser launcher URL | <code>(?i)\bvslaunchbrowser(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 개발 도구 동작 | Visual Studio 연계 정상 호출 | `LOLBAS.Download.VSLaunchBrowser.URL` |
| 52 | WinProj.exe | Office URL | <code>(?i)\bwinproj(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 Project URL 열기 | 브라우저 핸드오프 | `LOLBAS.Download.WinProj.URL` |
| 53 | Winword.exe | Office URL | <code>(?i)\bwinword(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 Word URL 열기 | 브라우저 핸드오프 | `LOLBAS.Download.WinWord.URL` |
| 54 | Wsl.exe | WSL/TCP | <code>(?i)\bwsl(?:\.exe)?\b.*?/dev/tcp/\d{1,3}(?:\.\d{1,3}){3}/\d+</code> | 정상 보안 테스트/스크립트 | URL이 아니라 TCP 경로 | `LOLBAS.Download.WSL.DevTCP` |
| 55 | xsd.exe | Direct URL | <code>(?i)\bxsd(?:\.exe)?\b\s+(?:(?:https?|ftp)://[^\s&quot;&#x27;&lt;&gt;]+)</code> | 정상 개발/스키마 작업 | 드문 사용이라 baseline 중요 | `LOLBAS.Download.XSD.URL` |
