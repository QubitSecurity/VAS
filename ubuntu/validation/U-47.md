## [U-47] DNS Zone Transfer 설정

### 개요

| 항목 | 내용 |
|------|------|
| 점검 코드 | U-47 |
| 항목명 | DNS Zone Transfer 설정 |
| 위험도 | 상 (High) |
| 범주 | 서비스 관리 |

### 점검 목적

DNS Zone Transfer는 Secondary DNS 서버에 Zone 정보를 복제하기 위한 기능입니다. 이를 무제한 허용하면 공격자가 도메인의 전체 호스트 정보를 수집하여 내부 네트워크 구조 파악에 활용할 수 있습니다.

### 점검 기준

| 구분 | 기준 | 설명 |
|------|------|------|
| 안전 | Zone Transfer가 허용된 Secondary DNS 서버 IP로만 제한된 경우 | 인가된 DNS 서버만 Zone 정보 복제 가능 |
| 취약 | Zone Transfer가 모든 호스트에 허용된 경우 | 내부 DNS 정보 전체 수집 가능 |

## 베이스라인 기준

**안전(양호)**

- `allow-transfer { <secondary DNS IP>; };` 명시적 설정
- 또는 `allow-transfer { none; };`

**취약**

- `allow-transfer { any; };` 또는 설정 없음(기본 허용)

### 참고 문서

- KISA 주요정보통신기반시설 기술적 취약점 분석·평가 방법 상세가이드 (Linux/Unix)
  서비스 관리 > 3. 서비스 관리 [U-47]
