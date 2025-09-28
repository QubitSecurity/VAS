# IIS w3wp.exe 아웃바운드 & 자식 프로세스 로깅(간단 설정서)

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

* **목표:** 5156(허용) 중 **`Direction: Outbound`** + **`Application Name: …\w3wp.exe`**만 조회
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

* **특징:** 4688에는 **Creator Process ID(=부모 PID)**가 들어오지만, **부모 이미지 경로는 직접 표시되지 않음**
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

## 운영 팁(선택)

* **화이트리스트**: 정상 외부 연동(결제, 메일, 프록시, CRL/OCSP 등) 목적지/포트를 목록화해 제외
* **포트 축소**: 80/443 외 포트 우선 탐지로 소음 저감
* **교차 확인**: 같은 시각·같은 대상 IP/프로세스가 **Sysmon(EID 3/1)** + **Security(5156/4688)** 양쪽에 보이면 신뢰도↑

---

## 요약 체크리스트

* [ ] **Sysmon EID 3** 적용: `w3wp.exe` + **Initiated=true** + **외부 IP만**
* [ ] **Security 5156** 감사 ON: `Application Name=\w3wp.exe` + **Direction=Outbound**
* [ ] **Sysmon EID 1** 적용: `ParentImage=\w3wp.exe` (+ 필요 시 대상 실행기 목록)
* [ ] **Security 4688** 감사 ON: **Creator PID ↔ 현재 w3wp PID** 대조로 자식 생성 식별
* [ ] **화이트리스트 정리** 후 규칙/필터 정교화

원하시면 위 설정 조각들을 **통합 Sysmon XML**과 **검증용 PS 스크립트 두 개(아웃바운드/자식생성)**로 바로 붙여 쓸 수 있게 묶어 드릴게요.
