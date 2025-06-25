다음은 **Windows 10/11 EDR 관점에서 반드시 점검해야 할 항목**을 하나의 리스트로 병합하고, **일련번호**를 추가하여 정리한 표입니다.

EDR 시스템의 탐지 효과성, 실시간 대응 능력, 초기 침투 및 내부 확산 방지를 위한 구성 요소를 기준으로 추출한 항목입니다.

---

| No. | 코드 | 점검 항목 설명                 | English Description                                              |
| --- | ----- | -------------------------------- | ---------------------------------------------------------------- |
| 1   | W-01  | Administrator 계정 이름 변경 또는 보안성 강화 | Rename or strengthen the security of the Administrator account   |
| 2   | W-02  | Guest 계정 비활성화                    | Disable the Guest account                                        |
| 3   | W-04  | 계정 잠금 임계값 설정                     | Set account lockout threshold                                    |
| 4   | W-06  | 관리자 그룹에 최소한의 사용자 포함              | Include only essential users in the Administrators group         |
| 5   | W-43  | Autologon 기능 제어                  | Disable or control Autologon                                     |
| 6   | W-48  | 패스워드 복잡성 설정                      | Enforce password complexity requirements                         |
| 7   | W-49  | 패스워드 최소 암호 길이                    | Set minimum password length                                      |
| 8   | W-55  | 최근 암호 기억                         | Enable password history                                          |
| 9   | W-56  | 콘솔 로그온 시 로컬 계정에서 빈 암호 사용 제한      | Restrict blank passwords for local accounts during console logon |
| 10  | W-09  | 불필요한 서비스 제거                      | Remove unnecessary services                                      |
| 11  | W-25  | FTP 서비스 구동 점검                    | Verify if FTP service is running                                 |
| 12  | W-65  | Telnet 보안 설정                     | Secure or disable Telnet                                         |
| 13  | W-66  | 불필요한 ODBC/OLE-DB 데이터소스와 드라이브 제거  | Remove unnecessary ODBC/OLE-DB data sources and drivers          |
| 14  | W-81  | 시작 프로그램 목록 분석                    | Analyze startup program list                                     |
| 15  | W-68  | 예약된 작업에 의심스러운 명령이 등록되어 있는지 점검    | Check for suspicious commands in scheduled tasks                 |
| 16  | W-34  | 로그의 정기적 검토 및 보고                  | Regularly review and report logs                                 |
| 17  | W-41  | 보안감사를 로그할 수 없는 경우 즉시 시스템 종료 해제   | Prevent shutdown when security auditing fails                    |
| 18  | W-69  | 정책에 따른 시스템 로깅설정                  | Configure system logging based on policy                         |
| 19  | W-70  | 이벤트 로그 관리 설정                     | Set event log management options                                 |
| 20  | W-71  | 원격에서 이벤트 로그파일 접근 차단              | Block remote access to event log files                           |
| 21  | W-07  | 공유 권한 및 사용자 그룹 설정                | Set appropriate share permissions and user group access          |
| 22  | W-08  | 하드디스크 기본 공유 제거                   | Disable default hard drive sharing                               |
| 23  | W-42  | SAM 계정과 공유의 익명 열거 허용 안함          | Disallow anonymous enumeration of SAM accounts and shares        |
| 24  | W-54  | 익명 SID/이름 변환 허용 해제               | Disable anonymous SID/name translation                           |
| 25  | W-57  | 원격터미널 접속 가능한 사용자 그룹 제한           | Limit user groups for remote terminal access                     |
| 26  | W-67  | 원격터미널 접속 타임아웃 설정                 | Set session timeout for remote terminal access                   |
| 27  | W-31  | 최신 서비스팩 적용                       | Apply the latest service packs                                   |
| 28  | W-32  | 최신 HOT FIX 적용                    | Apply the latest hotfixes                                        |
| 29  | W-33  | 백신 프로그램 업데이트                     | Keep antivirus software updated                                  |
| 30  | W-36  | 백신 프로그램 설치                       | Ensure antivirus software is installed                           |
| 31  | W-45  | 디스크 볼륨 암호화 설정 (예: BitLocker)     | Enable disk volume encryption (e.g., BitLocker)                  |

---

### 📌 참고

* **W-65**, **W-25**: 클라이언트 단에서도 비활성화 상태 확인 필요 (악성코드가 서비스로 재활성화하는 경우 존재).
* **W-45**: 장치 도난 대비 중요 (EFS보다 BitLocker 권장).
* **W-41** 및 **W-69\~71**: EDR 연계 시 로그 누락을 방지하기 위해 중요.
