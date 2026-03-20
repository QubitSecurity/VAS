## [U-48] Apache 웹 서비스 정보 숨김

### 개요

| 항목 | 내용 |
|------|------|
| 점검 코드 | U-48 |
| 항목명 | Apache 웹 서비스 정보 숨김 |
| 위험도 | 중 (Medium) |
| 범주 | 서비스 관리 |

### 점검 목적

Apache 서버는 기본적으로 HTTP 응답 헤더에 버전 정보를 포함합니다. 이 정보를 통해 공격자가 해당 버전의 알려진 취약점을 식별하고 표적 공격을 수행할 수 있습니다. 서버 버전 정보를 숨겨 공격 정보 수집을 어렵게 해야 합니다.

### 점검 기준

| 구분 | 기준 | 설명 |
|------|------|------|
| 안전 | ServerTokens Prod, ServerSignature Off로 설정된 경우 | HTTP 응답에 Apache 버전 정보 미포함 |
| 취약 | ServerTokens Full 또는 기본값으로 설정된 경우 | HTTP 응답에 Apache 버전, OS 정보 노출 |

## 베이스라인 기준

**안전(양호)**

- `ServerTokens Prod`
- `ServerSignature Off`
- 설정 파일 경로: `/etc/apache2/conf-available/security.conf` 또는 `/etc/apache2/apache2.conf`

**취약**

- `ServerTokens Full` 또는 `ServerTokens OS` (버전 정보 노출)
- `ServerSignature On` (에러 페이지에 버전 표시)

### 참고 문서

- KISA 주요정보통신기반시설 기술적 취약점 분석·평가 방법 상세가이드 (Linux/Unix)
  서비스 관리 > 3. 서비스 관리 [U-48]
