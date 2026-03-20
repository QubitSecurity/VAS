## [U-72] SSH 보안 설정

### 개요

| 항목 | 내용 |
|------|------|
| 점검 코드 | U-72 |
| 항목명 | SSH 보안 설정 |
| 위험도 | 상 (High) |
| 범주 | 보안 관리 |

### 점검 목적

SSH는 Linux 시스템의 주요 원격 접속 수단입니다. 기본 설정으로는 다양한 보안 위험이 존재하며, 프로토콜 버전, root 로그인 허용, 빈 패스워드 허용 등의 설정을 강화해야 합니다.

### 점검 기준

| 구분 | 기준 | 설명 |
|------|------|------|
| 안전 | SSH 보안 설정이 적절히 강화된 경우 | SSHv2 사용, root 로그인 차단, 빈 패스워드 차단 등 |
| 취약 | SSH 기본 설정 또는 취약한 설정으로 운영 중인 경우 | root 직접 로그인, SSHv1 사용, 빈 패스워드 허용 |

## 베이스라인 기준

**안전(양호)**

| 설정 항목 | 권장값 | 설명 |
|---|---|---|
| `Protocol` | `2` | SSHv2만 사용 |
| `PermitRootLogin` | `no` | root 직접 로그인 차단 |
| `PermitEmptyPasswords` | `no` | 빈 패스워드 접속 차단 |
| `MaxAuthTries` | `5` 이하 | 로그인 시도 제한 |
| `LoginGraceTime` | `60` 이하 | 인증 대기 시간 제한 |
| `ClientAliveInterval` | `300` 이하 | 유휴 세션 타임아웃 |
| `X11Forwarding` | `no` | X11 포워딩 차단 |

**취약**

- `PermitRootLogin yes`
- `PermitEmptyPasswords yes`
- `Protocol 1` (SSHv1 허용)

### 참고 문서

- KISA 주요정보통신기반시설 기술적 취약점 분석·평가 방법 상세가이드 (Linux/Unix)
  보안 관리 > 4. 보안 관리 [U-72]
