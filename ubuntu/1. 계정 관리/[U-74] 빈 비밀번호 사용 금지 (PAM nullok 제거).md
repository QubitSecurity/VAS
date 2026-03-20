## [U-74] 빈 비밀번호 사용 금지 (PAM nullok 제거)

### 개요

| 항목 | 내용 |
|------|------|
| 점검 코드 | U-74 |
| 항목명 | 빈 비밀번호 사용 금지 (PAM nullok 제거) |
| 위험도 | 상 (High) |
| 범주 | 계정 관리 |

### 점검 목적

PAM 설정의 `pam_unix.so`에 `nullok` 옵션이 포함되면 패스워드가 없는 계정으로 로컬 및 원격 로그인이 가능해집니다. `nullok`을 제거하여 빈 패스워드 계정의 로그인을 전면 차단해야 합니다.

### 점검 기준

| 구분 | 기준 | 설명 |
|------|------|------|
| 안전 | PAM 설정에서 nullok 옵션이 제거된 경우 | 빈 패스워드 계정 로그인 불가 |
| 취약 | pam_unix.so 또는 관련 모듈에 nullok 옵션이 포함된 경우 | 패스워드 없이 로그인 가능 |

## 베이스라인 기준

**안전(양호)**

- `/etc/pam.d/common-auth` 및 `/etc/pam.d/common-password` 전체에 `nullok` 또는 `nullok_secure` 옵션 없음

```
grep -r "nullok" /etc/pam.d/   결과 없음
```

**취약**

- `/etc/pam.d/common-auth`에 `pam_unix.so nullok` 또는 `nullok_secure` 존재

### 참고 문서

- KISA 주요정보통신기반시설 기술적 취약점 분석·평가 방법 상세가이드 (Linux/Unix)
