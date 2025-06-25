Windows 10 및 11 환경에서 **EDR(Endpoint Detection and Response)** 관점에서 반드시 점검해야 할 항목을 **일반 서버 보안 항목 목록**(W-01\~W-82)에서 선별하였습니다. 클라이언트(PC/노트북) 중심의 EDR 적용 대상 특성과, 랜섬웨어·파일리스 공격·정보 탈취 대응 등 현실 위협 대응력을 중심으로 판단하였습니다.

---

## ✅ Windows 10/11 EDR 관점 필수 점검 항목 (EDR Mandatory Check Items)

### 1. **계정 및 인증 보안 (Account & Authentication Security)**

| No | 코드   | 설명                               | English Description                         | 평가 등급                 | 비고 (판단 조건 및 설명)                                 |
| -- | ---- | -------------------------------- | ------------------------------------------- | --------------------- | --------------------------------------------- |
| 1  | W-01 | Administrator 계정 이름 변경 또는 보안성 강화 | Rename or secure the Administrator account  | ✅ Secure / ❌ Insecure | 계정명이 "Administrator"가 아닌 경우 Secure            |
| 2  | W-02 | Guest 계정 비활성화                    | Disable the Guest account                   | ✅ Secure / ❌ Insecure | Guest 계정이 Disabled 상태이면 Secure                |
| 3  | W-04 | 계정 잠금 임계값 설정                     | Set account lockout threshold               | ✅ Secure / ❌ Insecure | 임계값이 3\~5 사이이면 Secure, 0이거나 범위를 벗어나면 Insecure |
| 4  | W-06 | 관리자 그룹 최소 구성                     | Minimize users in the Administrators group  | ✅ Secure / ❌ Insecure | Administrators 그룹 사용자가 2명 이하이면 Secure         |
| 5  | W-43 | Autologon 기능 제어                  | Disable or control Autologon                | ✅ Secure / ❌ Insecure | AutoAdminLogon이 "0"이면 Secure                  |
| 6  | W-48 | 패스워드 복잡성 설정                      | Enforce password complexity                 | ✅ Secure / ❌ Insecure | PasswordComplexity 값이 1이면 Secure              |
| 7  | W-49 | 패스워드 최소 길이 설정                    | Set minimum password length                 | ✅ Secure / ❌ Insecure | 최소 길이가 8자 이상이면 Secure                         |
| 8  | W-55 | 최근 암호 기억 설정                      | Configure password history                  | ✅ Secure / ❌ Insecure | 기억 횟수가 10회 이상이면 Secure                        |
| 9  | W-56 | 빈 암호 사용 금지 (로컬 계정)               | Prohibit blank passwords for local accounts | ✅ Secure / ❌ Insecure | LimitBlankPasswordUse 값이 1이면 Secure           |

> **이유:** 계정 탈취 방지, 초기 침투 탐지 및 대응을 위해 필수.

---

### 2. **실행 및 서비스 제어 (Execution & Service Control)**

| No | 코드 | 설명                        | English Description                              | 평가 등급                  | 비고 (판단 조건 및 설명)                            |
| -- | ----- | ------------------------- | ------------------------------------------------ | ---------------------- | ---------------------------------------- |
| 10 | W-09  | 불필요한 서비스 제거               | Remove unnecessary services                      | 📝 Manual Review       | 실행 중 서비스 목록만 출력됨. 보안성 판단은 사람의 판단 필요      |
| 11 | W-25  | FTP 서비스 구동 점검             | Check if FTP service is running                  | ✅ Secure / ❌ Insecure  | FTP 서비스 실행 여부에 따라 자동 판단 가능               |
| 12 | W-65  | Telnet 보안 설정              | Secure or disable Telnet                         | ✅ Secure / ❌ Insecure  | Telnet 서비스 실행 여부에 따라 자동 판단 가능            |
| 13 | W-66  | 불필요한 ODBC/OLE-DB 제거       | Remove unnecessary ODBC/OLE-DB components        | 📝 Manual Review       | 설치된 ODBC 드라이버만 출력되며, 보안성 판단은 수동으로 판단 필요  |
| 14 | W-81  | 시작 프로그램 목록 분석             | Analyze startup program list                     | 📝 Manual Review       | 시작 프로그램만 나열되며 악성 여부나 불필요 여부 판단은 수동 리뷰 필요 |
| 15 | W-68  | 예약된 작업에 의심스러운 명령 등록 여부 점검 | Check for suspicious commands in scheduled tasks | ✅ / ❌ / 📝 병합형 (혼합 판단) | 의심 명령어 존재 여부에 따라 자동 판단, 없을 경우 수동 확인 필요   |

> **이유:** 파일리스 공격, 후킹·자동 실행, C2 통신 탐지 가능성 확보.

---

### 3. **로그 및 이벤트 감시 (Log & Event Monitoring)**

| No | 코드 | 설명                         | English Description                              | 평가 등급                   | 비고 (판단 조건 및 설명)                                             |
| -- | ----- | -------------------------- | ------------------------------------------------ | ----------------------- | --------------------------------------------------------------- |
| 16 | W-34  | 로그 정기 검토 및 보고              | Review and report logs regularly                 | 📝 Manual Review        | 정기적 로그 검토 절차 존재 여부는 수동 확인 필요                                    |
| 17 | W-41  | 보안감사를 로그할 수 없을 때 시스템 종료 해제 | Prevent system shutdown when audit logging fails | ✅ Secure / ❌ Insecure   | `CrashOnAuditFail` 값 1이면 Secure, 0이면 Insecure                   |
| 18 | W-69  | 정책에 따른 시스템 로깅 설정           | Configure logging according to policy            | 📝 Manual Review        | `auditpol` 결과 출력되나 정책 적합 여부 수동 판단 필요                            |
| 19 | W-70  | 이벤트 로그 관리 설정               | Configure event log settings                     | ✅ Secure / 🟨 Recommend | 로그 크기 또는 보존 기간 설정 기준 미달이면 Recommend, 적합하면 Secure                |
| 20 | W-71  | 원격 이벤트 로그 접근 차단            | Block remote access to event logs                | ✅ Secure / 🟨 Recommend | 레지스트리에 이벤트 로그 원격 경로 포함 여부에 따라 평가 (포함 시 Recommend, 미포함 시 Secure) |


> **이유:** EDR은 로그 기반 탐지 연계가 핵심. 누락 시 행위 추적 불가능.

---

### 4. **네트워크·파일 공유 및 접근 제어 (Network & Access Control)**

| No | 코드 | 설명                   | English Description                                 | 평가 등급                 | 비고 (판단 조건 및 설명)                                      |
| -- | ----- | -------------------- | --------------------------------------------------- | --------------------- | ---------------------------------------------------- |
| 21 | W-07  | 공유 권한 및 사용자 그룹 설정    | Configure share permissions and user groups         | 📝 Manual Review      | 공유 폴더 권한 및 그룹 구성 확인은 사람이 직접 판단해야 함                   |
| 22 | W-08  | 하드디스크 기본 공유 제거       | Disable default hard disk sharing                   | ✅ Secure / ❌ Insecure | 기본 공유(C\$, D\$ 등) 존재 여부로 자동 평가 가능                    |
| 23 | W-42  | SAM 계정과 공유의 익명 열거 차단 | Block anonymous enumeration of SAM accounts/shares  | ✅ Secure / ❌ Insecure | `RestrictAnonymous = 1`이면 Secure, 0 또는 없음이면 Insecure |
| 24 | W-54  | 익명 SID/이름 변환 해제      | Disable anonymous SID/name translation              | ✅ Secure / ❌ Insecure | `TurnOffAnonymousBlock = 0`이면 Secure, 아니면 Insecure   |
| 25 | W-57  | 원격터미널 접속 사용자 제한      | Limit users allowed for remote terminal connections | 📝 Manual Review      | RDP 사용자 그룹 구성은 사람이 판단해야 하며 자동으로 보안 여부 판단 불가          |
| 26 | W-67  | 원격터미널 세션 타임아웃 설정     | Set timeout for remote terminal sessions            | 📝 Manual Review      | 세션 타임아웃 정책은 그룹 정책 설정 여부 확인이 필요하며 수동 검토 필요            |

> **이유:** lateral movement·정보 열람·RDP 해킹 등 공격 경로 차단.

---

### 5. **보안 소프트웨어·업데이트 및 암호화 (Security Software, Updates & Encryption)**

| No | 코드 | 설명                          | English Description                             | 평가 등급                                    | 비고 (판단 조건 및 설명)                                                 |
| -- | ----- | --------------------------- | ----------------------------------------------- | ---------------------------------------- | ---------------------------------------------------------------------- |
| 27 | W-31  | 최신 서비스팩 적용                  | Apply latest service packs                      | 📝 Manual Review                         | 서비스팩 최신 적용 여부는 OS 버전과 빌드 비교가 필요하므로 수동 검토 필요                            |
| 28 | W-32  | 최신 HOT FIX 적용               | Apply latest hotfixes                           | 📝 Manual Review                         | Hotfix 적용 여부 확인은 `Get-HotFix` 또는 WSUS 정책 확인이 필요하므로 수동 판단 필요            |
| 29 | W-33  | 백신 프로그램 업데이트                | Keep antivirus up to date                       | ❌ Insecure / 📝 Manual Review            | 백신 존재 시 정의 업데이트는 수동 판단, 백신 없으면 Insecure                                |
| 30 | W-36  | 백신 프로그램 설치                  | Install antivirus software                      | ✅ Secure / ❌ Insecure                    | 백신 제품 설치 여부만 확인하므로 자동 판단 가능                                            |
| 31 | W-45  | 디스크 볼륨 암호화 설정 (BitLocker 등) | Enable disk volume encryption (e.g., BitLocker) | ✅ Secure / ❌ Insecure / 📝 Manual Review | BitLocker 존재 시 보호 상태에 따라 Secure/Insecure 판단, 명령어 불가 시 Manual Review 필요 |

> **이유:** 제로데이 대응력 및 기기 도난·랜섬웨어 피해 최소화 목적.

---

| 등급                      | 의미                               | 적용 예시                                                  |
| ----------------------- | -------------------------------- | ------------------------------------------------------ |
| ✅ **Secure**            | 보안 정책에 부합하며 변경이 필요하지 않음          | `Telnet service not running`, `PasswordComplexity = 1` |
| ❌ **Insecure**          | 보안에 미흡하거나 위험 요소가 명확하게 존재함        | `CrashOnAuditFail = 0`, `MinimumPasswordLength = 5`    |
| 🟨 **Recommend** *(신규)* | 현재 설정은 작동하지만 보안 정책상 더 나은 구성이 권장됨 | `LogMaxSize = 20MB`, `Retention = 0`                   |
| 📝 **Manual Review**    | 수동 점검이 필요한 항목으로 자동으로 판단할 수 없음    | `W-07: WinRM 구성`, `W-67: 감사 정책이 누락되었거나 명확하지 않음` 등      |

---

## 🟨 참고: Windows 서버에만 해당하거나 EDR 비중이 낮은 항목

다음 항목들은 일반적으로 **Windows Server 또는 웹/IIS/DNS/FTP 시스템 보안에 특화**되어 있어 Windows 10/11 클라이언트 기준에서는 필수 대상이 아닙니다:

* `W-10`\~`W-23`: IIS 관련 설정
* `W-24`, `W-29`, `W-60`\~`W-63`: NetBIOS, DNS, SNMP 등 서버 서비스
* `W-27`: Anonymous FTP (클라이언트 측 FTP 기능은 보통 사용 안함)

---

## ✳️ 결론

Windows 10 및 11 환경의 **EDR 기반 위협 탐지 및 실시간 대응**을 위해서는 계정·로그·서비스·프로세스·네트워크 접근 제어를 중심으로 **최소 40개 이상 항목이 적극 점검되어야 합니다**. 위 목록은 PLURA-XDR, Microsoft Defender for Endpoint, CrowdStrike 등 주요 EDR 솔루션의 가이드라인과도 일치합니다.
