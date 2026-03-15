좋습니다.
LOLBAS **227개 전체를 WDAC 관점에서 분류**할 때는 보통 다음 4단계로 관리하는 것이 가장 현실적입니다.

사용자께서 설계 중인 **Secure OS + WDAC 정책 구조**에 맞추어 정리했습니다.

---

# 1. WDAC 분류 기준

LOLBAS는 모두 차단하면 OS가 망가질 수 있기 때문에
**다음 4단계 정책으로 분류**하는 것이 가장 안전합니다.

| WDAC 정책                   | 의미           | 예          |
| ------------------------- | ------------ | ---------- |
| **Block (Deny)**          | WDAC에서 실행 차단 | mshta      |
| **Restricted (Allow 일부)** | 관리자만 허용      | powershell |
| **Audit**                 | 로그만 수집       | systeminfo |
| **Allow**                 | 정상 OS 기능     | notepad    |

---

# 2. WDAC 차단 권장 (TOP 위험 LOLBAS)

이 그룹은 **WDAC에서 차단해도 OS 영향이 거의 없습니다.**

### WDAC Block

| Tool                         |
| ---------------------------- |
| mshta.exe                    |
| regsvr32.exe                 |
| rundll32.exe                 |
| installutil.exe              |
| msbuild.exe                  |
| wmic.exe                     |
| hh.exe                       |
| bash.exe                     |
| pcalua.exe                   |
| syncappvpublishingserver.exe |
| shdocvw.dll                  |
| ieexec.exe                   |
| dfsvc.exe                    |
| odbcconf.exe                 |
| msdt.exe                     |

약 **15~20개**

이 그룹은 실제 공격에서 **매우 많이 사용됩니다.**

---

# 3. WDAC 제한 실행 (관리자만)

이 그룹은 완전 차단하면 운영 문제가 생길 수 있습니다.

### WDAC Restricted

| Tool           |
| -------------- |
| powershell.exe |
| pwsh.exe       |
| wscript.exe    |
| cscript.exe    |
| schtasks.exe   |
| at.exe         |
| sc.exe         |
| reg.exe        |
| cmd.exe        |

약 **10개**

정책

```
관리자만 실행 허용
일반 사용자 차단
```

---

# 4. Remote Execution 도구

이 그룹은 **Lateral Movement 핵심**입니다.

### WDAC Restricted / Audit

| Tool         |
| ------------ |
| winrm.vbs    |
| wmic.exe     |
| psexec       |
| sc.exe       |
| schtasks.exe |

---

# 5. Download LOLBAS

Payload 다운로드용

### WDAC Restricted

| Tool                  |
| --------------------- |
| certutil.exe          |
| bitsadmin.exe         |
| desktopimgdownldr.exe |
| powershell.exe        |
| hh.exe                |

---

# 6. Tunnel / C2 도구

이 그룹은 가능하면 **차단**이 좋습니다.

| Tool           |
| -------------- |
| devtunnels.exe |
| ssh.exe        |
| plink.exe      |

---

# 7. Persistence 도구

| Tool         |
| ------------ |
| schtasks.exe |
| at.exe       |
| reg.exe      |
| sc.exe       |

---

# 8. Recon 도구

이 그룹은 차단하면 관리가 불편합니다.

### Audit

| Tool           |
| -------------- |
| systeminfo.exe |
| whoami.exe     |
| tasklist.exe   |
| net.exe        |
| ipconfig.exe   |

---

# 9. 최종 WDAC 정책 구조

Secure OS 기준 추천 구조

```
WDAC Policy

1. Block
   mshta
   regsvr32
   rundll32
   msbuild
   installutil
   hh

2. Restricted
   powershell
   wscript
   cscript
   cmd

3. Audit
   systeminfo
   whoami
   tasklist

4. Allow
   기본 OS
```

---

# 10. 실제 Secure OS 적용 시

대부분 조직은 **227개 모두 관리하지 않습니다.**

보통

```
TOP 30 LOLBAS
```

만 관리합니다.

대표

```
powershell
pwsh
mshta
rundll32
regsvr32
installutil
msbuild
wmic
schtasks
certutil
bitsadmin
hh
```

---

# 11. 가장 중요한 3개 그룹

Secure OS 설계에서 핵심

```
Script engines
Execution LOLBAS
Download LOLBAS
```

---

# 12. 사용자의 Secure OS 구조 추천

```
Secure OS

├ Script Engines
│  powershell
│  wscript
│  cscript
│
├ Execution LOLBAS
│  mshta
│  rundll32
│  regsvr32
│  installutil
│
├ Download LOLBAS
│  certutil
│  bitsadmin
│  hh
│
├ Remote Execution
│  winrm
│  wmic
│
└ Tunnel
   devtunnels
```

---

# 13. 매우 중요한 사실

LOLBAS **227개 전체를 WDAC 차단하는 것은 불가능합니다.**

이유

```
OS 구성요소 포함
관리 기능 포함
.NET 포함
```

그래서 보안 제품들은

```
TOP 30 ~ 50
```

만 관리합니다.

---

# 14. 다음 단계 (추천)

지금 Secure OS 설계에서 **가장 중요한 작업**이 하나 남아 있습니다.

제가 다음을 만들어 드리겠습니다.

```
LOLBAS 227
↓
위험도 기반
↓
TOP 40 WDAC 차단 목록
```

그리고

```
Rename 대상
WDAC 대상
Audit 대상
```

까지 완성된 **Secure OS Master Table**을 만들어 드릴 수 있습니다.

이 표는 실제로 **EDR/XDR 제품 설계에서 사용하는 구조**입니다.

원하시면 **227개 전체 WDAC 정책 표**도 만들어 드리겠습니다.
