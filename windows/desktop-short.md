## ✅ Secure / Insecure 이진 판별형 축약 문구 (KR / EN / JP)

| No. | 코드   | 점검 항목 설명                  | 축약 문구 (KR)  | 축약 문구 (EN)        | 축약文 (JP)    | 판단 근거 (있다=Insecure / 없다=Secure) |
| --- | ---- | ------------------------- | ----------- | ----------------- | ----------- | ------------------------------- |
| 1   | W-01 | Administrator 계정 이름 변경/강화 | 관리자 계정 존재   | Admin account     | 管理者アカウント    | Default Admin 있으면 Insecure      |
| 2   | W-02 | Guest 계정 비활성화             | Guest 계정    | Guest account     | ゲストアカウント    | Guest 계정 활성화 시 Insecure         |
| 3   | W-04 | 계정 잠금 임계값 설정              | 계정 잠금       | Lockout policy    | アカウントロック    | Lockout 미설정 시 Insecure          |
| 4   | W-06 | 관리자 그룹 최소화                | 관리자 그룹      | Admins group      | 管理者グループ     | 불필요 사용자 있으면 Insecure            |
| 5   | W-43 | Autologon 제어              | 자동로그인       | Autologon         | 自動ログオン      | Autologon 켜져 있으면 Insecure       |
| 6   | W-48 | 패스워드 복잡성 설정               | 암호 복잡성      | PW complexity     | パスワード複雑性    | 미설정 시 Insecure                  |
| 7   | W-49 | 패스워드 최소 길이                | 암호 길이       | PW length         | パスワード長      | 짧으면 Insecure                    |
| 8   | W-55 | 최근 암호 기억                  | 암호 이력       | PW history        | パスワード履歴     | 기록 안 하면 Insecure                |
| 9   | W-56 | 빈 암호 제한                   | 빈 암호        | Blank PW          | 空パスワード      | 허용 시 Insecure                   |
| 10  | W-09 | 불필요 서비스 제거                | 불필요 서비스     | Services          | 不要サービス      | 존재 시 Insecure                   |
| 11  | W-25 | FTP 서비스 점검                | FTP 서비스     | FTP service       | FTPサービス     | 동작 중이면 Insecure                 |
| 12  | W-65 | Telnet 보안                 | Telnet      | Telnet            | Telnet      | 켜져 있으면 Insecure                 |
| 13  | W-66 | 불필요 ODBC/OLE-DB 제거        | ODBC/OLE-DB | ODBC drivers      | ODBC/OLE-DB | 존재 시 Insecure                   |
| 14  | W-81 | 시작 프로그램 점검                | 시작프로그램      | Startup apps      | スタートアップ     | 불필요 등록 시 Insecure               |
| 15  | W-68 | 예약 작업 점검                  | 예약작업        | Scheduled tasks   | 予約タスク       | 악성 명령 있으면 Insecure              |
| 16  | W-34 | 로그 검토                     | 로그 검토       | Log review        | ログ確認        | 미수행 시 Insecure                  |
| 17  | W-41 | 감사 실패 종료 방지               | 감사 종료       | Audit shutdown    | 監査失敗終了      | 허용 시 Insecure                   |
| 18  | W-69 | 정책 로깅 설정                  | 정책 로깅       | Policy logging    | ポリシーログ      | 미설정 시 Insecure                  |
| 19  | W-70 | 이벤트 로그 관리                 | 이벤트 로그      | Event log mgmt    | イベントログ管理    | 미설정 시 Insecure                  |
| 20  | W-71 | 원격 로그 접근 차단               | 원격 로그       | Remote log access | リモートログアクセス  | 허용 시 Insecure                   |
| 21  | W-07 | 공유 권한 설정                  | 공유 권한       | Share perms       | 共有権限        | 과도 허용 시 Insecure                |
| 22  | W-08 | 디스크 기본 공유 제거              | 디스크 공유      | Disk shares       | ディスク共有      | 존재 시 Insecure                   |
| 23  | W-42 | SAM 계정/공유 익명 차단           | SAM 익명      | SAM anon          | SAM匿名列挙     | 허용 시 Insecure                   |
| 24  | W-54 | SID/이름 변환 차단              | SID 변환      | SID translation   | SID変換       | 허용 시 Insecure                   |
| 25  | W-57 | RDP 그룹 제한                 | RDP 그룹      | RDP groups        | RDPグループ     | 불필요 계정 있으면 Insecure             |
| 26  | W-67 | RDP 타임아웃 설정               | RDP 타임아웃    | RDP timeout       | RDPタイムアウト   | 미설정 시 Insecure                  |
| 27  | W-31 | 최신 서비스팩 적용                | 서비스팩        | Service pack      | サービスパック     | 미적용 시 Insecure                  |
| 28  | W-32 | 최신 Hotfix 적용              | 핫픽스         | Hotfix            | ホットフィックス    | 미적용 시 Insecure                  |
| 29  | W-33 | 백신 업데이트                   | 백신 업데이트     | AV update         | ウイルス対策更新    | 미적용 시 Insecure                  |
| 30  | W-36 | 백신 설치                     | 백신 설치       | AV installed      | ウイルス対策導入    | 미설치 시 Insecure                  |
| 31  | W-45 | 디스크 암호화                   | 디스크 암호화     | Disk encryption   | ディスク暗号化     | 미적용 시 Insecure                  |

---
