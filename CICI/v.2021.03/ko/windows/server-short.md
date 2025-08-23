## ✅ Windows 서버 취약점 점검 항목 (축약/판정형)

| No. | 코드   | 축약 설명 (KR)  | English Short       | 판단 근거 (있다=Insecure / 없다=Secure) |
| --- | ---- | ----------- | ------------------- | ------------------------------- |
| 1   | W-01 | 관리자 계정      | Admin account       | 기본 Admin 계정 존재 시 Insecure       |
| 2   | W-02 | Guest 계정    | Guest account       | Guest 계정 활성화 시 Insecure         |
| 3   | W-03 | 불필요 계정      | Unused accounts     | 불필요 계정 존재 시 Insecure            |
| 4   | W-04 | 계정 잠금       | Lockout policy      | 미설정 시 Insecure                  |
| 5   | W-05 | 평문 암호 저장    | Plain PW stored     | 해독가능 암호 저장 시 Insecure           |
| 6   | W-06 | 관리자 그룹      | Admins group        | 불필요 사용자 있으면 Insecure            |
| 7   | W-07 | 공유 권한       | Share perms         | 과도 권한 있으면 Insecure              |
| 8   | W-08 | 디스크 공유      | Disk shares         | 기본 공유 있으면 Insecure              |
| 9   | W-09 | 불필요 서비스     | Unnecessary svc     | 동작 중이면 Insecure                 |
| 10  | W-10 | IIS 구동      | IIS service         | 구동 시 Insecure                   |
| 11  | W-11 | IIS 디렉토리    | IIS dir listing     | 허용 시 Insecure                   |
| 12  | W-12 | IIS CGI     | IIS CGI exec        | 허용 시 Insecure                   |
| 13  | W-13 | IIS 상위 접근   | IIS parent dir      | 허용 시 Insecure                   |
| 14  | W-14 | IIS 불필요 파일  | IIS files           | 존재 시 Insecure                   |
| 15  | W-15 | IIS 권한 제한   | IIS proc rights     | 미제한 시 Insecure                  |
| 16  | W-16 | IIS 링크      | IIS links           | 허용 시 Insecure                   |
| 17  | W-17 | IIS 업/다운    | IIS upload/download | 무제한 시 Insecure                  |
| 18  | W-18 | IIS DB      | IIS DB vuln         | 취약 시 Insecure                   |
| 19  | W-19 | IIS 가상디렉    | IIS vdir            | 존재 시 Insecure                   |
| 20  | W-20 | IIS 데이터 ACL | IIS ACL             | 미적용 시 Insecure                  |
| 21  | W-21 | IIS 스크립트    | IIS scripts         | 불필요 매핑 있으면 Insecure             |
| 22  | W-22 | IIS Exec    | IIS exec shell      | 허용 시 Insecure                   |
| 23  | W-23 | WebDAV      | WebDAV              | 활성 시 Insecure                   |
| 24  | W-24 | NetBIOS     | NetBIOS bind        | 활성 시 Insecure                   |
| 25  | W-25 | FTP 구동      | FTP service         | 구동 시 Insecure                   |
| 26  | W-26 | FTP 권한      | FTP dir perms       | 과도 권한 시 Insecure                |
| 27  | W-27 | FTP 익명      | FTP anonymous       | 허용 시 Insecure                   |
| 28  | W-28 | FTP 접근제어    | FTP access ctl      | 미설정 시 Insecure                  |
| 29  | W-29 | DNS 전송      | DNS zone xfer       | 허용 시 Insecure                   |
| 30  | W-30 | RDS         | RDS service         | 구동 시 Insecure                   |
| 31  | W-31 | 서비스팩        | Service pack        | 미적용 시 Insecure                  |
| 32  | W-32 | 핫픽스         | Hotfix update       | 미적용 시 Insecure                  |
| 33  | W-33 | 백신 업데이트     | AV update           | 미실행 시 Insecure                  |
| 34  | W-34 | 로그 검토       | Log review          | 미수행 시 Insecure                  |
| 35  | W-35 | 원격 레지스트리    | Remote reg          | 허용 시 Insecure                   |
| 36  | W-36 | 백신 설치       | AV installed        | 미설치 시 Insecure                  |
| 37  | W-37 | SAM 접근      | SAM access          | 허용 시 Insecure                   |
| 38  | W-38 | 화면보호기       | Screensaver         | 미설정 시 Insecure                  |
| 39  | W-39 | 무로그온 종료     | Shutdown no logon   | 허용 시 Insecure                   |
| 40  | W-40 | 원격 강제 종료    | Remote shutdown     | 허용 시 Insecure                   |
| 41  | W-41 | 감사 실패 종료    | Audit shutdown      | 허용 시 Insecure                   |
| 42  | W-42 | SAM 익명      | SAM anon            | 허용 시 Insecure                   |
| 43  | W-43 | Autologon   | Autologon           | 활성 시 Insecure                   |
| 44  | W-44 | 이동식 미디어     | Removable media     | 허용 시 Insecure                   |
| 45  | W-45 | 디스크 암호화     | Disk encrypt        | 미설정 시 Insecure                  |
| 46  | W-46 | Everyone 권한 | Everyone rights     | 익명 허용 시 Insecure                |
| 47  | W-47 | 계정 잠금 기간    | Lockout duration    | 미설정 시 Insecure                  |
| 48  | W-48 | 암호 복잡성      | PW complexity       | 미설정 시 Insecure                  |
| 49  | W-49 | 암호 길이       | PW length           | 짧으면 Insecure                    |
| 50  | W-50 | 암호 최대기간     | PW max age          | 미설정 시 Insecure                  |
| 51  | W-51 | 암호 최소기간     | PW min age          | 미설정 시 Insecure                  |
| 52  | W-52 | 사용자명 표시     | Last username       | 표시 시 Insecure                   |
| 53  | W-53 | 로컬 로그온      | Local logon         | 허용 시 Insecure                   |
| 54  | W-54 | SID 변환      | SID translation     | 허용 시 Insecure                   |
| 55  | W-55 | 암호 이력       | PW history          | 미설정 시 Insecure                  |
| 56  | W-56 | 빈 암호        | Blank PW            | 허용 시 Insecure                   |
| 57  | W-57 | RDP 그룹      | RDP groups          | 과도 허용 시 Insecure                |
| 58  | W-58 | RDP 암호화     | RDP encryption      | 낮으면 Insecure                    |
| 59  | W-59 | IIS 정보      | IIS info leak       | 노출 시 Insecure                   |
| 60  | W-60 | SNMP 구동     | SNMP service        | 구동 시 Insecure                   |
| 61  | W-61 | SNMP 문자열    | SNMP string         | 단순 시 Insecure                   |
| 62  | W-62 | SNMP 접근제어   | SNMP ACL            | 미설정 시 Insecure                  |
| 63  | W-63 | DNS 구동      | DNS service         | 구동 시 Insecure                   |
| 64  | W-64 | 배너 노출       | Service banners     | 노출 시 Insecure                   |
| 65  | W-65 | Telnet      | Telnet              | 활성 시 Insecure                   |
| 66  | W-66 | ODBC 제거     | ODBC drivers        | 존재 시 Insecure                   |
| 67  | W-67 | RDP 타임아웃    | RDP timeout         | 미설정 시 Insecure                  |
| 68  | W-68 | 예약 작업       | Scheduled tasks     | 악성 등록 시 Insecure                |
| 69  | W-69 | 정책 로깅       | Policy logging      | 미설정 시 Insecure                  |
| 70  | W-70 | 이벤트 로그      | Event logs          | 미설정 시 Insecure                  |
| 71  | W-71 | 원격 로그 접근    | Remote logs         | 허용 시 Insecure                   |
| 72  | W-72 | Dos 방어      | DOS registry        | 미설정 시 Insecure                  |
| 73  | W-73 | 프린터 드라이버    | Printer driver      | 설치 허용 시 Insecure                |
| 74  | W-74 | 세션 유휴       | Session idle        | 미설정 시 Insecure                  |
| 75  | W-75 | 경고 메시지      | Warning msg         | 미설정 시 Insecure                  |
| 76  | W-76 | 홈 디렉토리      | Home dir perms      | 미설정 시 Insecure                  |
| 77  | W-77 | LAN Manager | LAN Manager         | 낮으면 Insecure                    |
| 78  | W-78 | 보안 채널       | Secure channel      | 미설정 시 Insecure                  |
| 79  | W-79 | 파일 보호       | File/dir protect    | 미설정 시 Insecure                  |
| 80  | W-80 | 컴퓨터 PW 기간   | Machine PW age      | 미설정 시 Insecure                  |
| 81  | W-81 | 시작 프로그램     | Startup apps        | 불필요 등록 시 Insecure               |
| 82  | W-82 | 인증 모드       | Auth mode           | 미사용 시 Insecure                  |

---
