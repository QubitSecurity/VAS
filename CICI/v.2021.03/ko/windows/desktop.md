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

| No. | 코드 | 점검 항목 (Desktop 특화) | 설명 및 점검 이유 |
| --- | --- | --- | --- |
| **Cloud & Network** |  |  |  |
| 1 | **D-01** | **퍼블릭 클라우드 스토리지 동기화 제어** | OneDrive, Google Drive, Dropbox 등 개인용 클라우드 스토리지의 데스크톱 앱 설치 및 자동 동기화 활성화 여부 점검 (정보 유출 경로) |
| 2 | **D-02** | **웹 브라우저 계정/패스워드 저장 비활성화** | Chrome, Edge 등 브라우저 내 '비밀번호 저장' 기능 활성화 여부 점검 (악성코드 감염 시 브라우저 내 저장된 계정정보 탈취 위험 높음) |
| 3 | **D-03** | **무선 네트워크(Wi-Fi) 및 Wi-Fi Direct 제어** | 유선망 연결 시 무선망 동시 접속 차단 또는 Wi-Fi Direct를 통한 우회 접속 허용 여부 점검 |
| 4 | **D-04** | **블루투스 파일 전송 프로파일 차단** | 블루투스 연결은 허용하되, 파일을 주고받는 프로파일(FTP/OPP) 활성화 여부 점검 |
| **Application & Software** |  |  |  |
| 5 | **D-05** | **Microsoft Store 앱 설치 제한** | 검증되지 않은 앱 설치를 막기 위해 Microsoft Store 접근 제어 또는 '회사 포털'만 이용하도록 설정되었는지 점검 |
| 6 | **D-06** | **Office 매크로 보안 설정 (인터넷 차단)** | 인터넷에서 다운로드한 Office 파일에 대해 매크로 실행을 원천 차단하는 설정(Mark of the Web) 적용 여부 |
| 7 | **D-07** | **상용 메신저/협업 툴 자동 다운로드 제어** | 카카오톡, Slack, Telegram 등의 '파일 자동 다운로드' 설정 및 저장 경로가 기본 경로(내 문서 등)로 방치되었는지 점검 |
| 8 | **D-08** | **브라우저 개발자 모드 및 확장 프로그램 통제** | 브라우저의 '개발자 모드' 활성화 여부 및 승인되지 않은 악성 확장 프로그램(Extension) 설치 여부 점검 |
| **OS Feature & Privacy** |  |  |  |
| 9 | **D-09** | **SmartScreen 필터 활성화** | Windows Defender SmartScreen(앱 및 브라우저 컨트롤)이 비활성화되어 있는지 점검 (사용자 임의 해제 방지) |
| 10 | **D-10** | **사용자 활동 기록(Timeline) 수집 비활성화** | Windows 10/11의 '작업 기록(Activity History)'이 MS 클라우드로 전송되거나 로컬에 저장되는지 점검 (프라이버시 및 민감정보 노출) |
| 11 | **D-11** | **Xbox Game Bar 및 화면 녹화 기능 제어** | 업무용 PC에서 게임 바(Game Bar)를 통한 화면 녹화 기능이 활성화되어 있는지 점검 (화면 유출 및 리소스 낭비) |
| 12 | **D-12** | **클립보드 클라우드 동기화 차단** | Windows 10/11의 '클립보드 검색 기록' 및 '타 장치와 동기화' 기능 활성화 여부 점검 (복사한 패스워드/데이터가 타 기기로 유출) |
| 13 | **D-13** | **원격 지원(Quick Assist) 허용 범위 점검** | RDP(W-57)와 별개로 Windows 기본 탑재된 '빠른 지원' 앱을 통한 무단 원격 접속 허용 여부 |
| 14 | **D-14** | **DMA(Direct Memory Access) 보호 설정** | Thunderbolt 등 외부 포트를 통한 DMA 공격 방어를 위해 'Kernel DMA Protection' 활성화 여부 확인 (최신 랩탑 필수 점검) |

---


### 📌 참고

* **W-65**, **W-25**: 클라이언트 단에서도 비활성화 상태 확인 필요 (악성코드가 서비스로 재활성화하는 경우 존재).
* **W-45**: 장치 도난 대비 중요 (EFS보다 BitLocker 권장).
* **W-41** 및 **W-69\~71**: EDR 연계 시 로그 누락을 방지하기 위해 중요.
