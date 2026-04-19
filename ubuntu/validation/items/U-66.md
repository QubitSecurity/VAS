## [U-66] Syslog 설정

### 개요

| 항목 | 내용 |
|------|------|
| 점검 코드 | U-66 |
| 항목명 | Syslog 설정 |
| 위험도 | 상 (High) |
| 범주 | 보안 관리 |

### 점검 목적

주요 시스템 이벤트(인증, 커널 메시지, 데몬 오류 등)가 적절히 로그되지 않으면 보안 사고 발생 시 원인 분석이 불가능합니다. rsyslog의 주요 facility가 모두 활성화되어 있는지 확인해야 합니다.

### 점검 기준

| 구분 | 기준 | 설명 |
|------|------|------|
| 안전 | 주요 로그 항목(kern, auth, daemon 등)이 설정되어 있는 경우 | 보안 관련 이벤트 모두 기록 |
| 취약 | 로그 설정이 누락되어 중요 이벤트가 기록되지 않는 경우 | 침해 사고 분석 불가 |

## 베이스라인 기준

**안전(양호)**

Ubuntu rsyslog 기본 설정 파일(`/etc/rsyslog.d/50-default.conf`)에 다음 설정이 모두 존재:

```
auth,authpriv.*                 /var/log/auth.log
kern.*                          -/var/log/kern.log
*.*;auth,authpriv.none          -/var/log/syslog
mail.*                          -/var/log/mail.log
cron.*                          /var/log/cron.log
```

| RedHat 로그 파일 | Ubuntu 대응 로그 파일 |
|------------------|-----------------------|
| `/var/log/secure` | `/var/log/auth.log` |
| `/var/log/messages` | `/var/log/syslog` |
| `/var/log/maillog` | `/var/log/mail.log` |
| `/var/log/cron` | `/var/log/cron.log` |

**취약**

- 주요 facility 로그 설정 누락
- rsyslog 서비스 비활성화

### 참고 문서

- KISA 주요정보통신기반시설 기술적 취약점 분석·평가 방법 상세가이드 (Linux/Unix)
  보안 관리 > 4. 로그 관리 [U-66]
