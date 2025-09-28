# IIS w3wp.exe 아웃바운드 & 자식 프로세스 로깅

## 목표

* **아웃바운드**: `w3wp.exe`가 **먼저** 외부(비 RFC1918)로 연결 시 기록  
* **자식 프로세스**: `w3wp.exe`가 **cmd/powershell/rundll32/regsvr32/mshta/curl…** 등을 **자식으로 생성** 시 기록

---

## 1) Sysmon (EID 3: NetworkConnect) — *w3wp가 먼저 외부로 나갈 때만*

* **특징:** EID 3은 **프로세스가 시작한 연결**만 기록(수신 연결 제외)
* **설정 스니펫(최소)** — 기존 Sysmon 구성에 병합

```xml
<Sysmon schemaversion="4.90">
  <EventFiltering>
    <!-- w3wp.exe가 시작한 외부 연결만 수집 -->
    <NetworkConnect onmatch="include">
      <Image condition="end with">\w3wp.exe</Image>
      <!-- 내부망/루프백/링크로컬 제외 -->
      <DestinationIp condition="is not">10.0.0.0/8</DestinationIp>
      <DestinationIp condition="is not">172.16.0.0/12</DestinationIp>
      <DestinationIp condition="is not">192.168.0.0/16</DestinationIp>
      <DestinationIp condition="is not">127.0.0.0/8</DestinationIp>
      <DestinationIp condition="is not">::1/128</DestinationIp>
      <DestinationIp condition="is not">fe80::/10</DestinationIp>
    </NetworkConnect>
  </EventFiltering>
</Sysmon>
```

* **확인(이벤트 뷰어/PowerShell)**
  Event Viewer → *Applications and Services Logs → Microsoft → Windows → Sysmon → Operational* (ID=3)
  메시지에 **`Image: ...\w3wp.exe`** + **`Initiated: true`**

```powershell
Get-WinEvent -LogName "Microsoft-Windows-Sysmon/Operational" |
  Where-Object { $_.Id -eq 3 -and $_.Message -match '\\w3wp\.exe' -and $_.Message -match 'Initiated:\s*true' } |
  Select-Object -First 50 TimeCreated, Message
```

---

## 2) Security 로그 (EID 5156: 허용된 연결) — *Outbound만 보기*

* **목표:** 5156(허용) 중 **`Direction: Outbound`** + **`Application Name: …w3wp.exe`**만 조회
* **감사 활성화**

```cmd
auditpol /set /subcategory:"Filtering Platform Connection" /success:enable /failure:disable
```

* **확인(이벤트 뷰어/PowerShell)**
  Event Viewer → *Windows Logs → Security* (ID=5156)

```powershell
Get-WinEvent -LogName Security |
  Where-Object { $_.Id -eq 5156 -and $_.Message -match '\\w3wp\.exe' -and $_.Message -match 'Direction:\s*Outbound' } |
  Where-Object { $_.Message -notmatch 'DstIP=10\.' -and $_.Message -notmatch 'DstIP=192\.168\.' -and $_.Message -notmatch 'DstIP=172\.(1[6-9]|2[0-9]|3[0-1])\.' -and $_.Message -notmatch 'DstIP=127\.0\.0\.1' } |
  Select-Object -First 50 TimeCreated, Message
```

---

## 3) Sysmon (EID 1: ProcessCreate) — *w3wp의 자식 프로세스 생성*

* **특징:** `ParentImage`/`ParentCommandLine` 포함 → **w3wp가 부모**일 때만 필터링 용이
* **설정 스니펫(최소)** — 관심 실행기 우선(LOLBIN·다운로더 등)

```xml
<EventFiltering>
  <ProcessCreate onmatch="include">
    <ParentImage condition="end with">\w3wp.exe</ParentImage>
    <Image condition="end with">\cmd.exe</Image>
    <Image condition="end with">\powershell.exe</Image>
    <Image condition="end with">\rundll32.exe</Image>
    <Image condition="end with">\regsvr32.exe</Image>
    <Image condition="end with">\mshta.exe</Image>
    <Image condition="end with">\cscript.exe</Image>
    <Image condition="end with">\wscript.exe</Image>
    <Image condition="end with">\curl.exe</Image>
    <Image condition="end with">\bitsadmin.exe</Image>
    <Image condition="end with">\msbuild.exe</Image>
    <Image condition="end with">\installutil.exe</Image>
  </ProcessCreate>
</EventFiltering>
```

> 초기 조사 단계에서는 위 `<Image …>` 라인을 제거하고 **`ParentImage`만** 두어 “w3wp의 모든 자식”을 넓게 수집 → 허용목록 확정 후 좁히는 것을 권장.

* **확인**

```powershell
Get-WinEvent -LogName "Microsoft-Windows-Sysmon/Operational" |
  Where-Object { $_.Id -eq 1 -and $_.Message -match 'ParentImage:\s*.*\\w3wp\.exe' } |
  Select-Object -First 50 TimeCreated, Message
```

---

## 4) Security 로그 (EID 4688: Process Creation) — *부모 PID 대조 방식*

* **특징:** 4688에는 **Creator Process ID**(=부모 PID)가 들어오지만, **부모 이미지 경로는 직접 표시되지 않음**
  → **현재 w3wp PID 목록**을 16진수로 변환해 **Creator PID**와 대조

* **감사 + 커맨드라인 수집 권장**

  * Advanced Audit Policy → **Detailed Tracking → Process Creation** = Success
  * (권장) *Include command line in process creation events* 활성화

* **확인 스크립트**

```powershell
# 현재 실행 중인 w3wp PID를 16진수로 준비
$w3wpPidsHex = (Get-Process w3wp -ErrorAction SilentlyContinue).Id |
  ForEach-Object { '0x{0:x}' -f $_ }

# 부모 PID가 w3wp인 4688만 조회(필요 시 대상 프로세스 정규식 추가)
Get-WinEvent -LogName Security |
  Where-Object { $_.Id -eq 4688 } |
  Where-Object {
    $msg = $_.Message
    $w3wpPidsHex | ForEach-Object { if ($msg -match "Creator Process ID:\s*$_\b") { $true; break } }
  } |
  Select-Object -First 50 TimeCreated, Message
```

> 소음 절감: `-match 'New Process Name:\s*.*\\(cmd|powershell|rundll32|regsvr32|mshta|curl|bitsadmin|msbuild|installutil)\.exe'` 추가 가능

---

## 5) 통합 Sysmon 설정 (XML) — *아웃바운드 + 자식 생성 동시 수집*

**파일명:** `sysmon_w3wp_outbound_children.xml`
**목적:** `w3wp.exe`의 **외부 연결(Initiated=true)**과 **자식 프로세스 생성**만 기록. 내부망/루프백/링크로컬 제외.

```xml
<!-- sysmon_w3wp_outbound_children.xml -->
<Sysmon schemaversion="4.90">
  <HashAlgorithms>sha256</HashAlgorithms>
  <EventFiltering>
    <!-- [EID 3] w3wp.exe가 '시작'한 외부 연결만 -->
    <NetworkConnect onmatch="include">
      <Image condition="end with">\w3wp.exe</Image>
      <DestinationIp condition="is not">10.0.0.0/8</DestinationIp>
      <DestinationIp condition="is not">172.16.0.0/12</DestinationIp>
      <DestinationIp condition="is not">192.168.0.0/16</DestinationIp>
      <DestinationIp condition="is not">127.0.0.0/8</DestinationIp>
      <DestinationIp condition="is not">::1/128</DestinationIp>
      <DestinationIp condition="is not">fe80::/10</DestinationIp>
    </NetworkConnect>

    <!-- [EID 1] w3wp.exe의 자식 프로세스 생성 -->
    <!-- 초기에는 ParentImage만 두고 넓게 수집 후, 하단 Image 목록으로 축소를 권장 -->
    <ProcessCreate onmatch="include">
      <ParentImage condition="end with">\w3wp.exe</ParentImage>

      <!-- 관심 실행기(필요 시 추가/삭제) -->
      <Image condition="end with">\cmd.exe</Image>
      <Image condition="end with">\powershell.exe</Image>
      <Image condition="end with">\rundll32.exe</Image>
      <Image condition="end with">\regsvr32.exe</Image>
      <Image condition="end with">\mshta.exe</Image>
      <Image condition="end with">\cscript.exe</Image>
      <Image condition="end with">\wscript.exe</Image>
      <Image condition="end with">\curl.exe</Image>
      <Image condition="end with">\bitsadmin.exe</Image>
      <Image condition="end with">\msbuild.exe</Image>
      <Image condition="end with">\installutil.exe</Image>
    </ProcessCreate>
  </EventFiltering>
</Sysmon>
```

**적용(관리자 콘솔):**

```cmd
sysmon64.exe -c sysmon_w3wp_outbound_children.xml
:: 또는
sysmon.exe  -c sysmon_w3wp_outbound_children.xml
```

---

## 6) 검증 스크립트 A — 아웃바운드 교차 확인 (`Test-W3wpOutbound.ps1`)

> **Sysmon EID 3**(Initiated=true) + **Security 5156**(Outbound)을 함께 조회해 신뢰도↑.
> 기본 60분 범위, `-Minutes`로 조정. `-ExportCsv`로 CSV 저장 가능.

```powershell
<# 
.SYNOPSIS
  w3wp.exe가 '먼저' 외부로 나간 연결을 Sysmon(EID 3)과 Security(5156)에서 교차 확인

.PARAMETER Minutes
  조회 범위(분). 기본 60

.PARAMETER ExportCsv
  결과를 CSV로 저장할 경로 (선택)
#>

param(
  [int]$Minutes = 60,
  [string]$ExportCsv
)

$since = (Get-Date).AddMinutes(-$Minutes)

Write-Host "[-] 조회 시작: $since ~ now" -ForegroundColor Cyan

# RFC1918/Loopback/링크로컬 제외용 정규식
$priv4 = '^(10\.|192\.168\.|172\.(1[6-9]|2[0-9]|3[0-1])\.)'
$loop4 = '^127\.'
$link6 = '^fe80:'
$loop6 = '^::1$'

# 1) Sysmon EID 3 (Initiated=true)
$sysmon = Get-WinEvent -LogName "Microsoft-Windows-Sysmon/Operational" -ErrorAction SilentlyContinue |
  Where-Object { $_.TimeCreated -ge $since -and $_.Id -eq 3 } |
  Where-Object { $_.Message -match '\\w3wp\.exe' -and $_.Message -match 'Initiated:\s*true' } |
  Where-Object {
    $m = $_.Message
    if ($m -match 'DestinationIp:\s*([^\s]+)') {
      $ip = $Matches[1]
      -not ($ip -match $priv4 -or $ip -match $loop4 -or $ip -match $link6 -or $ip -match $loop6)
    } else { $false }
  } |
  ForEach-Object {
    $msg = $_.Message
    $destIp   = ($msg -match 'DestinationIp:\s*([^\s]+)')   ? $Matches[1] : ''
    $destPort = ($msg -match 'DestinationPort:\s*([^\s]+)') ? $Matches[1] : ''
    [pscustomobject]@{
      Source   = 'SysmonEID3'
      Time     = $_.TimeCreated
      DestIP   = $destIp
      DestPort = $destPort
      Raw      = $msg
    }
  }

# 2) Security 5156 (Direction: Outbound)
$sec = Get-WinEvent -LogName Security -ErrorAction SilentlyContinue |
  Where-Object { $_.TimeCreated -ge $since -and $_.Id -eq 5156 } |
  Where-Object { $_.Message -match '\\w3wp\.exe' -and $_.Message -match 'Direction:\s*Outbound' } |
  Where-Object {
    $m = $_.Message
    if ($m -match 'Dest Address:\s*([^\s]+)') {
      $ip = $Matches[1]
      -not ($ip -match $priv4 -or $ip -match $loop4 -or $ip -match $link6 -or $ip -match $loop6)
    } elseif ($m -match 'DstIP=([^\s]+)') {
      $ip = $Matches[1]
      -not ($ip -match $priv4 -or $ip -match $loop4 -or $ip -match $link6 -or $ip -match $loop6)
    } else { $false }
  } |
  ForEach-Object {
    $msg = $_.Message
    $destIp   = ($msg -match 'Dest Address:\s*([^\s]+)') ? $Matches[1] : (($msg -match 'DstIP=([^\s]+)') ? $Matches[1] : '')
    $destPort = ($msg -match 'Dest Port:\s*([^\s]+)')    ? $Matches[1] : (($msg -match 'DstPort=([^\s]+)') ? $Matches[1] : '')
    [pscustomobject]@{
      Source   = 'Security5156'
      Time     = $_.TimeCreated
      DestIP   = $destIp
      DestPort = $destPort
      Raw      = $msg
    }
  }

$result = @()
if ($sysmon) { $result += $sysmon }
if ($sec)    { $result += $sec }

$result = $result | Sort-Object Time -Descending

if ($ExportCsv) {
  $result | Export-Csv -NoTypeInformation -Encoding UTF8 -Path $ExportCsv
  Write-Host "[+] CSV 저장: $ExportCsv" -ForegroundColor Green
}

$result | Format-Table -AutoSize
```

**사용 예시(관리자 PowerShell):**

```powershell
.\Test-W3wpOutbound.ps1 -Minutes 120 -ExportCsv C:\Temp\w3wp_outbound.csv
```

---

## 7) 검증 스크립트 B — 자식 프로세스 교차 확인 (`Test-W3wpChildren.ps1`)

> **Sysmon EID 1**(`ParentImage=\w3wp.exe`)을 주력으로, **Security 4688**(Creator PID 대조)을 보조로 확인.
> 기본 60분 범위, `-Minutes`로 조정. 대상 실행기 패턴은 `-ExeRegex`로 조절.

```powershell
<#
.SYNOPSIS
  w3wp.exe가 생성한 자식 프로세스를 Sysmon(EID 1)과 Security(4688)로 확인

.PARAMETER Minutes
  조회 범위(분). 기본 60

.PARAMETER ExeRegex
  자식 프로세스 대상 정규식 (기본: LOLBIN/다운로더 위주)
  예: '^(cmd|powershell|rundll32|regsvr32|mshta|cscript|wscript|curl|bitsadmin|msbuild|installutil)\.exe$'

.PARAMETER ExportCsv
  결과 CSV 저장 경로(선택)
#>

param(
  [int]$Minutes = 60,
  [string]$ExeRegex = '^(cmd|powershell|rundll32|regsvr32|mshta|cscript|wscript|curl|bitsadmin|msbuild|installutil)\.exe$',
  [string]$ExportCsv
)

$since = (Get-Date).AddMinutes(-$Minutes)
Write-Host "[-] 조회 시작: $since ~ now" -ForegroundColor Cyan

# 1) Sysmon EID 1: ParentImage = w3wp.exe
$sysmon = Get-WinEvent -LogName "Microsoft-Windows-Sysmon/Operational" -ErrorAction SilentlyContinue |
  Where-Object { $_.TimeCreated -ge $since -and $_.Id -eq 1 } |
  Where-Object { $_.Message -match 'ParentImage:\s*.*\\w3wp\.exe' } |
  ForEach-Object {
    $m = $_.Message
    $childName = ''
    if ($m -match 'Image:\s*([^\r\n]+)') {
      $path = $Matches[1]
      $childName = (Split-Path $path -Leaf)
    }
    if ($childName -match $ExeRegex) {
      [pscustomobject]@{
        Source     = 'SysmonEID1'
        Time       = $_.TimeCreated
        ChildName  = $childName
        Raw        = $m
      }
    }
  }

# 2) Security 4688: Creator PID == w3wp PID (현재 PID + Sysmon에서 본 w3wp PID 보완)
# 2-1) 현재 실행 중인 w3wp PID (10진수 -> 16진수 문자열)
$currentW3wpHex = (Get-Process w3wp -ErrorAction SilentlyContinue).Id |
  ForEach-Object { '0x{0:x}' -f $_ }

# 2-2) 보완: Sysmon EID 1 메시지 내 ParentProcessId를 모아서 후보 PID에 추가(10진수를 16진수로)
$sysmonParentHex = @()
(Get-WinEvent -LogName "Microsoft-Windows-Sysmon/Operational" -ErrorAction SilentlyContinue |
  Where-Object { $_.TimeCreated -ge $since -and $_.Id -eq 1 } |
  Where-Object { $_.Message -match 'ParentImage:\s*.*\\w3wp\.exe' }) | ForEach-Object {
    if ($_.Message -match 'ParentProcessId:\s*([0-9]+)') {
      $dec = [int]$Matches[1]
      $sysmonParentHex += ('0x{0:x}' -f $dec)
    }
  }
$w3wpPidHexSet = ($currentW3wpHex + $sysmonParentHex) | Select-Object -Unique

$sec4688 = Get-WinEvent -LogName Security -ErrorAction SilentlyContinue |
  Where-Object { $_.TimeCreated -ge $since -and $_.Id -eq 4688 } |
  Where-Object {
    $m = $_.Message
    $matched = $false
    foreach ($hx in $w3wpPidHexSet) {
      if ($m -match "Creator Process ID:\s*$hx\b") { $matched = $true; break }
    }
    $matched
  } |
  ForEach-Object {
    $m = $_.Message
    $childName = ''
    if ($m -match 'New Process Name:\s*([^\r\n]+)') {
      $leaf = (Split-Path $Matches[1] -Leaf)
      $childName = $leaf
    }
    if ($childName -match $ExeRegex) {
      [pscustomobject]@{
        Source     = 'Security4688'
        Time       = $_.TimeCreated
        ChildName  = $childName
        Raw        = $m
      }
    }
  }

$result = @()
if ($sysmon)   { $result += $sysmon }
if ($sec4688)  { $result += $sec4688 }

$result = $result | Sort-Object Time -Descending

if ($ExportCsv) {
  $result | Export-Csv -NoTypeInformation -Encoding UTF8 -Path $ExportCsv
  Write-Host "[+] CSV 저장: $ExportCsv" -ForegroundColor Green
}

$result | Format-Table -AutoSize
```

**사용 예시(관리자 PowerShell):**

```powershell
.\Test-W3wpChildren.ps1 -Minutes 180 -ExeRegex '^(cmd|powershell|rundll32)\.exe$' -ExportCsv C:\Temp\w3wp_children.csv
```

---

## 8) 운영 팁(선택)

* **화이트리스트**: 정상 외부 연동(결제, 메일, 프록시, CRL/OCSP 등) 목적지/포트를 목록화해 제외
* **포트 축소**: 80/443 외 포트 우선 탐지로 소음 저감
* **교차 확인**: 같은 시각·같은 대상 IP/프로세스가 **Sysmon(EID 3/1)** + **Security(5156/4688)** 양쪽에 보이면 신뢰도↑

---

## 9) 요약 체크리스트

* [ ] **Sysmon EID 3** 적용: `w3wp.exe` + **Initiated=true** + **외부 IP만**
* [ ] **Security 5156** 감사 ON: `Application Name=\w3wp.exe` + **Direction=Outbound**
* [ ] **Sysmon EID 1** 적용: `ParentImage=\w3wp.exe` (+ 필요 시 대상 실행기 목록)
* [ ] **Security 4688** 감사 ON: **Creator PID ↔ 현재 w3wp PID** 대조로 자식 생성 식별
* [ ] **화이트리스트 정리** 후 규칙/필터 정교화

