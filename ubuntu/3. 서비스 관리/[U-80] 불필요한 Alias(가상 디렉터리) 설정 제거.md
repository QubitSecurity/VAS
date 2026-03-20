## [U-80] 불필요한 Alias(가상 디렉터리) 설정 제거

### 개요

| 항목 | 내용 |
|------|------|
| 점검 코드 | U-80 |
| 항목명 | 불필요한 Alias(가상 디렉터리) 설정 제거 |
| 위험도 | 상 (High) |
| 범주 | 서비스 관리 |

### 점검 목적

Apache의 `Alias`, `AliasMatch`, `ScriptAlias` 설정은 웹 URL과 실제 파일 시스템 경로를 매핑합니다. 불필요한 Alias가 존재하면 의도치 않은 파일 경로가 웹으로 노출될 수 있으며, 특히 `/icons/`, `/cgi-bin/` 등 기본 Alias는 버전 정보 노출이나 CGI 실행 경로로 악용될 수 있습니다.

### 점검 기준

| 구분 | 기준 | 설명 |
|------|------|------|
| 안전 | 운영에 필요한 Alias만 존재하고 불필요한 기본 Alias가 제거된 경우 | 의도치 않은 경로 노출 없음 |
| 취약 | /icons/, /cgi-bin/ 등 불필요한 기본 Alias가 활성화된 경우 | 기본 경로를 통한 정보 노출 위험 |

## 베이스라인 기준

**안전(양호)**

- `/etc/apache2/conf-enabled/`에 불필요한 Alias 없음
- `a2disconf serve-cgi-bin`, `a2dismod autoindex` 적용
- 운영 필요 Alias만 명시적으로 설정

**취약**

- `Alias /icons/ "/usr/share/apache2/icons/"` 활성 상태
- `/etc/apache2/conf-enabled/serve-cgi-bin.conf` 활성 상태

### 참고 문서

- KISA 주요정보통신기반시설 기술적 취약점 분석·평가 방법 상세가이드 (Linux/Unix)
