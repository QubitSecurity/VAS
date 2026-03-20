## [U-01] root 계정 원격 접속 제한

### 개요

| 항목 | 내용 |
|------|------|
| 점검 코드 | U-01 |
| 항목명 | root 계정 원격 접속 제한 |
| 위험도 | 상 (High) |
| 범주 | 계정 관리 |

### 점검 목적

root 계정으로 원격 직접 접속이 허용될 경우, 공격자가 무차별 대입(Brute Force) 공격을 통해 시스템 최고 권한을 탈취할 수 있습니다. SSH/Telnet 등 원격 접속 서비스에서 root 직접 로그인을 차단하고, 일반 계정으로 접속 후 su/sudo를 통해 권한을 상승하는 방식을 강제해야 합니다.

### 점검 기준

| 구분 | 기준 | 설명 |
|------|------|------|
| 안전 | root 계정의 원격 직접 접속이 차단된 경우 | SSH PermitRootLogin no 설정으로 원격 root 로그인 불가 |
| 취약 | root 계정의 원격 직접 접속이 허용된 경우 | 공격자가 root 계정 직접 로그인 시도 가능, 시스템 전체 권한 탈취 위험 |

## 베이스라인 기준

**안전(양호)**

- `/etc/ssh/sshd_config`: `PermitRootLogin no` 설정
- Ubuntu는 `/etc/securetty` 파일이 기본으로 존재하지 않으므로 SSH `PermitRootLogin no` 설정이 핵심 기준
- `/etc/pam.d/login`에 `pam_securetty.so` 포함 여부 확인 (파일 존재 시)

**취약**

- `PermitRootLogin yes` 또는 `PermitRootLogin without-password` 설정
- `PermitRootLogin` 항목 누락

### 참고 문서

- KISA 주요정보통신기반시설 기술적 취약점 분석·평가 방법 상세가이드 (Linux/Unix)
  계정 관리 > 1. 계정 관리 [U-01]
