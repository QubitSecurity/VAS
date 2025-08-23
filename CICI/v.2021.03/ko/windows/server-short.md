## ✅ Windows 서버 취약점 점검 항목 (KR / EN / JP + Desktop)

| No. | 코드   | 축약 설명 (KR)  | English Short       | 日本語 (JP)      | 판단 근거 (있다=Insecure / 없다=Secure) | Desktop |
| --- | ---- | ----------- | ------------------- | ------------- | ------------------------------- | ------- |
| 1   | W-01 | 관리자 계정      | Admin account       | 管理者アカウント      | 기본 Admin 계정 존재 시 Insecure       | O       |
| 2   | W-02 | Guest 계정    | Guest account       | ゲストアカウント      | Guest 계정 활성화 시 Insecure         | O       |
| 3   | W-03 | 불필요 계정      | Unused accounts     | 不要アカウント       | 불필요 계정 존재 시 Insecure            | O       |
| 4   | W-04 | 계정 잠금       | Lockout policy      | アカウントロック      | 미설정 시 Insecure                  | O       |
| 5   | W-05 | 평문 암호 저장    | Plain PW stored     | 平文パスワード保存     | 해독가능 암호 저장 시 Insecure           | O       |
| 6   | W-06 | 관리자 그룹      | Admins group        | 管理者グループ       | 불필요 사용자 있으면 Insecure            | O       |
| 7   | W-07 | 공유 권한       | Share perms         | 共有権限          | 과도 권한 있으면 Insecure              | O       |
| 8   | W-08 | 디스크 공유      | Disk shares         | ディスク共有        | 기본 공유 있으면 Insecure              | O       |
| 9   | W-09 | 불필요 서비스     | Unnecessary svc     | 不要サービス        | 동작 중이면 Insecure                 | O       |
| 10  | W-10 | IIS 구동      | IIS service         | IIS稼働         | 구동 시 Insecure                   |         |
| 11  | W-11 | IIS 디렉토리    | IIS dir listing     | IISディレクトリ表示   | 허용 시 Insecure                   |         |
| 12  | W-12 | IIS CGI     | IIS CGI exec        | IIS CGI実行     | 허용 시 Insecure                   |         |
| 13  | W-13 | IIS 상위 접근   | IIS parent dir      | IIS上位アクセス     | 허용 시 Insecure                   |         |
| 14  | W-14 | IIS 불필요 파일  | IIS files           | IIS不要ファイル     | 존재 시 Insecure                   |         |
| 15  | W-15 | IIS 권한 제한   | IIS proc rights     | IIS権限制限       | 미제한 시 Insecure                  |         |
| 16  | W-16 | IIS 링크      | IIS links           | IISリンク        | 허용 시 Insecure                   |         |
| 17  | W-17 | IIS 업/다운    | IIS upload/download | IISアップ/ダウンロード | 무제한 시 Insecure                  |         |
| 18  | W-18 | IIS DB      | IIS DB vuln         | IIS DB脆弱性     | 취약 시 Insecure                   |         |
| 19  | W-19 | IIS 가상디렉    | IIS vdir            | IIS仮想ディレクトリ   | 존재 시 Insecure                   |         |
| 20  | W-20 | IIS 데이터 ACL | IIS ACL             | IISデータACL     | 미적용 시 Insecure                  |         |
| 21  | W-21 | IIS 스크립트    | IIS scripts         | IISスクリプト      | 불필요 매핑 있으면 Insecure             |         |
| 22  | W-22 | IIS Exec    | IIS exec shell      | IIS Execシェル   | 허용 시 Insecure                   |         |
| 23  | W-23 | WebDAV      | WebDAV              | WebDAV        | 활성 시 Insecure                   |         |
| 24  | W-24 | NetBIOS     | NetBIOS bind        | NetBIOSサービス   | 활성 시 Insecure                   | O       |
| 25  | W-25 | FTP 구동      | FTP service         | FTP稼働         | 구동 시 Insecure                   |         |
| 26  | W-26 | FTP 권한      | FTP dir perms       | FTP権限         | 과도 권한 시 Insecure                |         |
| 27  | W-27 | FTP 익명      | FTP anonymous       | 匿名FTP         | 허용 시 Insecure                   |         |
| 28  | W-28 | FTP 접근제어    | FTP access ctl      | FTPアクセス制御     | 미설정 시 Insecure                  |         |
| 29  | W-29 | DNS 전송      | DNS zone xfer       | DNSゾーン転送      | 허용 시 Insecure                   |         |
| 30  | W-30 | RDS         | RDS service         | RDSサービス       | 구동 시 Insecure                   |         |
| 31  | W-31 | 서비스팩        | Service pack        | サービスパック       | 미적용 시 Insecure                  | O       |
| 32  | W-32 | 핫픽스         | Hotfix update       | ホットフィックス      | 미적용 시 Insecure                  | O       |
| 33  | W-33 | 백신 업데이트     | AV update           | ウイルス対策更新      | 미실행 시 Insecure                  | O       |
| 34  | W-34 | 로그 검토       | Log review          | ログ確認          | 미수행 시 Insecure                  | O       |
| 35  | W-35 | 원격 레지스트리    | Remote reg          | リモートレジストリ     | 허용 시 Insecure                   | O       |
| 36  | W-36 | 백신 설치       | AV installed        | ウイルス対策導入      | 미설치 시 Insecure                  | O       |
| 37  | W-37 | SAM 접근      | SAM access          | SAMアクセス       | 허용 시 Insecure                   |         |
| 38  | W-38 | 화면보호기       | Screensaver         | スクリーンセーバ      | 미설정 시 Insecure                  | O       |
| 39  | W-39 | 무로그온 종료     | Shutdown no logon   | 未ログオン終了       | 허용 시 Insecure                   | O       |
| 40  | W-40 | 원격 강제 종료    | Remote shutdown     | リモート強制終了      | 허용 시 Insecure                   |         |
| 41  | W-41 | 감사 실패 종료    | Audit shutdown      | 監査失敗終了        | 허용 시 Insecure                   | O       |
| 42  | W-42 | SAM 익명      | SAM anon            | SAM匿名列挙       | 허용 시 Insecure                   | O       |
| 43  | W-43 | Autologon   | Autologon           | 自動ログオン        | 활성 시 Insecure                   | O       |
| 44  | W-44 | 이동식 미디어     | Removable media     | リムーバブル媒体      | 허용 시 Insecure                   | O       |
| 45  | W-45 | 디스크 암호화     | Disk encrypt        | ディスク暗号化       | 미설정 시 Insecure                  | O       |
| 46  | W-46 | Everyone 권한 | Everyone rights     | Everyone権限    | 익명 허용 시 Insecure                | O       |
| 47  | W-47 | 계정 잠금 기간    | Lockout duration    | ロック期間         | 미설정 시 Insecure                  | O       |
| 48  | W-48 | 암호 복잡성      | PW complexity       | パスワード複雑性      | 미설정 시 Insecure                  | O       |
| 49  | W-49 | 암호 길이       | PW length           | パスワード長        | 짧으면 Insecure                    | O       |
| 50  | W-50 | 암호 최대기간     | PW max age          | パスワード最大期間     | 미설정 시 Insecure                  | O       |
| 51  | W-51 | 암호 최소기간     | PW min age          | パスワード最小期間     | 미설정 시 Insecure                  | O       |
| 52  | W-52 | 사용자명 표시     | Last username       | 最終ユーザー名表示     | 표시 시 Insecure                   | O       |
| 53  | W-53 | 로컬 로그온      | Local logon         | ローカルログオン      | 허용 시 Insecure                   | O       |
| 54  | W-54 | SID 변환      | SID translation     | SID変換         | 허용 시 Insecure                   | O       |
| 55  | W-55 | 암호 이력       | PW history          | パスワード履歴       | 미설정 시 Insecure                  | O       |
| 56  | W-56 | 빈 암호        | Blank PW            | 空パスワード        | 허용 시 Insecure                   | O       |
| 57  | W-57 | RDP 그룹      | RDP groups          | RDPグループ       | 과도 허용 시 Insecure                | O       |
| 58  | W-58 | RDP 암호화     | RDP encryption      | RDP暗号化        | 낮으면 Insecure                    | O       |
| 59  | W-59 | IIS 정보      | IIS info leak       | IIS情報漏洩       | 노출 시 Insecure                   |         |
| 60  | W-60 | SNMP 구동     | SNMP service        | SNMP稼働        | 구동 시 Insecure                   |         |
| 61  | W-61 | SNMP 문자열    | SNMP string         | SNMP文字列       | 단순 시 Insecure                   |         |
| 62  | W-62 | SNMP 접근제어   | SNMP ACL            | SNMPアクセス制御    | 미설정 시 Insecure                  |         |
| 63  | W-63 | DNS 구동      | DNS service         | DNS稼働         | 구동 시 Insecure                   |         |
| 64  | W-64 | 배너 노출       | Service banners     | サービスバナー表示     | 노출 시 Insecure                   | O       |
| 65  | W-65 | Telnet      | Telnet              | Telnet        | 활성 시 Insecure                   |         |
| 66  | W-66 | ODBC 제거     | ODBC drivers        | ODBCドライバ      | 존재 시 Insecure                   |         |
| 67  | W-67 | RDP 타임아웃    | RDP timeout         | RDPタイムアウト     | 미설정 시 Insecure                  | O       |
| 68  | W-68 | 예약 작업       | Scheduled tasks     | 予約タスク         | 악성 등록 시 Insecure                | O       |
| 69  | W-69 | 정책 로깅       | Policy logging      | ポリシーログ        | 미설정 시 Insecure                  | O       |
| 70  | W-70 | 이벤트 로그      | Event logs          | イベントログ        | 미설정 시 Insecure                  | O       |
| 71  | W-71 | 원격 로그 접근    | Remote logs         | リモートログ        | 허용 시 Insecure                   | O       |
| 72  | W-72 | Dos 방어      | DOS registry        | DOS防御         | 미설정 시 Insecure                  | O       |
| 73  | W-73 | 프린터 드라이버    | Printer driver      | プリンタードライバ     | 설치 허용 시 Insecure                | O       |
| 74  | W-74 | 세션 유휴       | Session idle        | セッションアイドル     | 미설정 시 Insecure                  | O       |
| 75  | W-75 | 경고 메시지      | Warning msg         | 警告メッセージ       | 미설정 시 Insecure                  | O       |
| 76  | W-76 | 홈 디렉토리      | Home dir perms      | ホームディレクトリ権限   | 미설정 시 Insecure                  | O       |
| 77  | W-77 | LAN Manager | LAN Manager         | LAN Manager認証 | 낮으면 Insecure                    | O       |
| 78  | W-78 | 보안 채널       | Secure channel      | セキュアチャネル      | 미설정 시 Insecure                  | O       |
| 79  | W-79 | 파일 보호       | File/dir protect    | ファイル保護        | 미설정 시 Insecure                  | O       |
| 80  | W-80 | 컴퓨터 PW 기간   | Machine PW age      | コンピュータPW期間    | 미설정 시 Insecure                  | O       |
| 81  | W-81 | 시작 프로그램     | Startup apps        | スタートアップ       | 불필요 등록 시 Insecure               | O       |
| 82  | W-82 | 인증 모드       | Auth mode           | 認証モード         | 미사용 시 Insecure                  | O       |

---
