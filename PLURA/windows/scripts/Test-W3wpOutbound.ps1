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
