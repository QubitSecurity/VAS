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
