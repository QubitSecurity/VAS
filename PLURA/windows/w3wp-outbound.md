“w3wp → 외부 연결”, **w3wp.exe가 자식 프로세스를 생성** 방법을 **두 가지**(Sysmon EID 1, Security 4688)로 설명

---

# 1) Sysmon — **Event ID 1: ProcessCreate**

> Sysmon은 **부모/자식 정보(ParentImage, ParentCommandLine)**를 함께 남기므로, `w3wp.exe`가 **부모**일 때만 정확히 필터링하기 좋습니다.

## A. 최소 설정(XML 스니펫)

이미 사용 중인 Sysmon 설정에 아래만 추가/병합하세요.

```xml
<EventFiltering>
  <!-- w3wp.exe가 자식 프로세스 생성한 경우만 기록 -->
  <ProcessCreate onmatch="include">
    <ParentImage condition="end with">\w3wp.exe</ParentImage>
    <!-- 관심 대상(LOLBIN/다운로더 등) 우선 -->
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

적용:

```cmd
sysmon.exe -c sysmon_w3wp_children.xml
```

## B. 바로 확인(이벤트 뷰어/PowerShell)

* 경로: **Applications and Services Logs → Microsoft → Windows → Sysmon → Operational**
* 필터: **Event ID = 1**, `ParentImage`가 `...\w3wp.exe`

```powershell
Get-WinEvent -LogName "Microsoft-Windows-Sysmon/Operational" |
  Where-Object { $_.Id -eq 1 -and $_.Message -match 'ParentImage:\s*.*\\w3wp\.exe' } |
  Select-Object -First 50 TimeCreated, Message
```

> 필요 시 위 XML에서 `<Image …>` 라인들을 빼고, `ParentImage`만 둬서 **w3wp가 만드는 모든 자식**을 전부 남길 수도 있습니다(초기 조사 시 유용).

---

# 2) Security 로그 — **Event ID 4688: Audit Process Creation**

> 보안 로그는 **새 프로세스 생성(4688)** 을 남깁니다. 4688에는 **부모 PID(= Creator Process ID)** 가 포함됩니다.
> 단, **부모 “이미지 경로”는 직접 안 찍히므로**, **w3wp의 현재 PID**와 대조해 보는 방식으로 필터링합니다.

## A. 감사 활성화 + 커맨드라인 수집

* 로컬 보안 정책:
  **Advanced Audit Policy Configuration → Detailed Tracking → Process Creation** → **Success**
* (권장) GPO 또는 로컬 정책에서
  **“Include command line in process creation events”** 활성화
  (경로: Computer Configuration → Administrative Templates → System → Audit Process Creation)

## B. 빠른 확인(현재 실행 중인 w3wp PID들과 대조)

> 4688 메시지의 **Creator Process ID** 값은 **16진수**(예: `0x1a2b`)로 찍힙니다.

```powershell
# 1) 현재 실행 중인 w3wp PID 수집(10진수→16진수 문자열로 변환)
$w3wpPidsHex = (Get-Process w3wp -ErrorAction SilentlyContinue).Id |
  ForEach-Object { '0x{0:x}' -f $_ }

# 2) Security 로그에서 4688 중, Creator Process ID가 위 목록에 있는 것만
Get-WinEvent -LogName Security |
  Where-Object { $_.Id -eq 4688 } |
  Where-Object {
    $msg = $_.Message
    $w3wpPidsHex | ForEach-Object { if ($msg -match "Creator Process ID:\s*$_\b") { $true; break } }
  } |
  Select-Object -First 50 TimeCreated, Message
```

> 이렇게 하면 **“부모 PID가 현재의 w3wp 중 하나인 4688”**만 추려집니다. (서비스 재시작으로 PID가 바뀔 수 있으니 “현재 PID 기준”임을 유의)

---

## 운영 팁(선택)

* **소음 절감:** PowerShell 필터에 `-match 'New Process Name:\s*.*\\(cmd|powershell|rundll32|regsvr32|mshta|curl|bitsadmin|msbuild|installutil)\.exe'` 같은 정규식을 추가.
* **확증력 향상:** Sysmon EID 1(ParentImage 명시)과 Security 4688(감사 표준) 둘 다에서 같은 시각에 관측되면 신뢰도↑.
* **장기 운영:** 초기엔 “w3wp → 모든 자식”으로 넓게 수집 → 허용목록(배치/백업/모듈 업데이트 등 정상 동작) 정리 후 타이트하게 좁히기.
