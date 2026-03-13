## [W-15] IIS 웹 프로세스 권한 제한

### 개요
| 항목 | 내용 |
|------|------|
| 점검 코드 | W-15 |
| 항목명 | IIS 웹 프로세스 권한 제한 |
| 위험도 | 상 (High) |
| 범주 | IIS 관리 |

### 점검 목적
IIS 웹 프로세스(w3wp.exe)가 SYSTEM 또는 Administrator 권한으로 실행되는 경우, 웹 애플리케이션 취약점을 통해 공격자가 서버 전체에 대한 완전한 제어권을 획득할 수 있습니다. 웹 프로세스는 반드시 최소 권한 계정(예: NetworkService, ApplicationPoolIdentity)으로 실행해야 합니다.

### 점검 기준
| 구분 | 기준 | 설명 |
|------|------|------|
| 안전 | 웹 프로세스가 NetworkService, ApplicationPoolIdentity 등 최소 권한 계정으로 실행되는 경우 | 웹 취약점 악용 시에도 공격자의 권한이 제한되어 피해 범위가 최소화된다 |
| 취약 | 웹 프로세스가 SYSTEM 또는 Administrator 권한으로 실행되는 경우 | 웹 취약점 악용 시 공격자가 서버 전체를 장악할 수 있다 |

### 참고 문서
- 웹 프로세스가 관리자 권한(SYSTEM, Administrator)으로 구동될 경우, 공격자가 웹 취약점을 통해 서버의 모든 권한을 장악하고 데이터를 무단으로 변경, 훼손하거나 유출할 수 있는 치명적인 위험이 존재한다. 시스템 보안을 위해 가급적 LocalSystem 대신 ApplicationPoolIdentity나 NetworkService와 같은 낮은 수준의 권한 계정을 사용 [WEB-09]
