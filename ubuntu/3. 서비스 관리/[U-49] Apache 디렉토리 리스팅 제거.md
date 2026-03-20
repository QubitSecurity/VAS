## [U-49] Apache 디렉토리 리스팅 제거

### 개요

| 항목 | 내용 |
|------|------|
| 점검 코드 | U-49 |
| 항목명 | Apache 디렉토리 리스팅 제거 |
| 위험도 | 상 (High) |
| 범주 | 서비스 관리 |

### 점검 목적

디렉토리 리스팅(Directory Listing)이 활성화되면, 웹 디렉토리에 `index.html`이 없을 경우 해당 디렉토리의 파일 목록이 브라우저에 노출됩니다. 이를 통해 소스 코드, 설정 파일, 백업 파일 등 민감한 정보가 노출될 수 있습니다.

### 점검 기준

| 구분 | 기준 | 설명 |
|------|------|------|
| 안전 | Options 지시어에서 Indexes 옵션이 제거된 경우 | 디렉토리 파일 목록 노출 차단 |
| 취약 | Options 지시어에 Indexes 옵션이 포함된 경우 | 웹 디렉토리 파일 목록 외부 노출 |

## 베이스라인 기준

**안전(양호)**

- `Options -Indexes` 또는 `Options FollowSymLinks` (Indexes 없음)
- 설정 파일 경로: `/etc/apache2/apache2.conf`

**취약**

- `Options Indexes` 또는 `Options +Indexes`

### 참고 문서

- KISA 주요정보통신기반시설 기술적 취약점 분석·평가 방법 상세가이드 (Linux/Unix)
  서비스 관리 > 3. 서비스 관리 [U-49]
