Windows 10および11環境において、**EDR（Endpoint Detection and Response）** の観点から必須であるチェック項目を、**一般サーバーセキュリティ項目リスト**（W-01〜W-82）より選定しました。クライアント（PC/ノート型PC）を中心としたEDR適用対象の特性、およびランサムウェア・ファイルレス攻撃・情報窃取などの実際の脅威に対する対応力を重視して判断しました。

---

## ✅ Windows 10/11 EDR観点における必須チェック項目（EDR Mandatory Check Items）

---

### 1. **アカウントおよび認証セキュリティ（Account & Authentication Security）**

| No | コード  | 説明                           | 英語説明                                        | 評価ランク                 | 備考（判断条件・説明）                           |
| -- | ---- | ---------------------------- | ------------------------------------------- | --------------------- | ------------------------------------- |
| 1  | W-01 | Administratorアカウントの名称変更または強化 | Rename or secure the Administrator account  | ✅ Secure / ❌ Insecure | アカウント名が "Administrator" でなければ Secure  |
| 2  | W-02 | Guestアカウントの無効化               | Disable the Guest account                   | ✅ Secure / ❌ Insecure | Guestアカウントが無効であれば Secure              |
| 3  | W-04 | アカウントロックアウトしきい値設定            | Set account lockout threshold               | ✅ Secure / ❌ Insecure | しきい値が3〜5の範囲であれば Secure、それ以外は Insecure |
| 4  | W-06 | 管理者グループの最小構成                 | Minimize users in the Administrators group  | ✅ Secure / ❌ Insecure | 管理者グループのユーザーが2人以下であれば Secure          |
| 5  | W-43 | Autologon機能の制御               | Disable or control Autologon                | ✅ Secure / ❌ Insecure | AutoAdminLogonが "0" であれば Secure       |
| 6  | W-48 | パスワードの複雑性設定                  | Enforce password complexity                 | ✅ Secure / ❌ Insecure | PasswordComplexity = 1 であれば Secure    |
| 7  | W-49 | パスワード最小長設定                   | Set minimum password length                 | ✅ Secure / ❌ Insecure | 8文字以上であれば Secure                      |
| 8  | W-55 | パスワード履歴設定                    | Configure password history                  | ✅ Secure / ❌ Insecure | 10回以上の履歴を保存していれば Secure               |
| 9  | W-56 | 空パスワードの禁止（ローカルアカウント）         | Prohibit blank passwords for local accounts | ✅ Secure / ❌ Insecure | LimitBlankPasswordUse = 1 であれば Secure |

> **理由：** アカウント乗っ取りの防止および初期侵入への対応に不可欠。

---

### 2. **実行およびサービス制御（Execution & Service Control）**

| No | コード  | 説明                      | 英語説明                                             | 評価ランク                 | 備考                       |
| -- | ---- | ----------------------- | ------------------------------------------------ | --------------------- | ------------------------ |
| 10 | W-09 | 不要なサービスの削除              | Remove unnecessary services                      | 📝 Manual Review      | 実行中サービスの一覧出力のみ、自動判断不可    |
| 11 | W-25 | FTPサービスの稼働確認            | Check if FTP service is running                  | ✅ Secure / ❌ Insecure | FTPサービスの稼働有無により自動評価可能    |
| 12 | W-65 | Telnetの無効化または制限         | Secure or disable Telnet                         | ✅ Secure / ❌ Insecure | Telnetの有効/無効で評価可能        |
| 13 | W-66 | 不要なODBC/OLE-DBの削除       | Remove unnecessary ODBC/OLE-DB components        | 📝 Manual Review      | ドライバ一覧のみ出力、要手動確認         |
| 14 | W-81 | スタートアッププログラムの分析         | Analyze startup program list                     | 📝 Manual Review      | リストアップのみ、悪性・不要の判定は手動     |
| 15 | W-68 | スケジュールタスクに不審なコマンドがあるか確認 | Check for suspicious commands in scheduled tasks | ✅ / ❌ / 📝 複合評価       | 疑わしいコマンドがあれば自動、なければ手動で確認 |

> **理由：** ファイルレス攻撃、フック・自動実行、C2通信の検出に有効。

---

### 3. **ログおよびイベント監視（Log & Event Monitoring）**

| No | コード  | 説明                 | 英語説明                                             | 評価ランク                   | 備考                                         |
| -- | ---- | ------------------ | ------------------------------------------------ | ----------------------- | ------------------------------------------ |
| 16 | W-34 | ログの定期確認・報告         | Review and report logs regularly                 | 📝 Manual Review        | 定期的な確認体制の有無を手動で確認                          |
| 17 | W-41 | 監査ログ記録不能時のシステム終了防止 | Prevent system shutdown when audit logging fails | ✅ Secure / ❌ Insecure   | CrashOnAuditFail = 1 であれば Secure           |
| 18 | W-69 | ポリシーに沿ったログ記録設定     | Configure logging according to policy            | 📝 Manual Review        | auditpol結果を手動で確認                           |
| 19 | W-70 | イベントログの管理設定        | Configure event log settings                     | ✅ Secure / 🟨 Recommend | 容量・保持期間が基準に満たない場合 Recommend                |
| 20 | W-71 | イベントログへのリモートアクセス制限 | Block remote access to event logs                | ✅ Secure / 🟨 Recommend | レジストリにリモートパスが含まれる場合は Recommend、なければ Secure |

> **理由：** EDRはログに基づく検知が基本。漏れがあると追跡不可。

---

### 4. **ネットワーク・ファイル共有およびアクセス制御（Network & Access Control）**

| No | コード  | 説明                    | 英語説明                                                | 評価ランク                 | 備考                                  |
| -- | ---- | --------------------- | --------------------------------------------------- | --------------------- | ----------------------------------- |
| 21 | W-07 | 共有権限およびユーザーグループ構成     | Configure share permissions and user groups         | 📝 Manual Review      | 手動での構成確認が必要                         |
| 22 | W-08 | デフォルトハードディスク共有の無効化    | Disable default hard disk sharing                   | ✅ Secure / ❌ Insecure | 共有（C\$, D\$など）の有無で判断                |
| 23 | W-42 | SAMアカウント/共有の匿名列挙のブロック | Block anonymous enumeration of SAM accounts/shares  | ✅ Secure / ❌ Insecure | RestrictAnonymous = 1 なら Secure     |
| 24 | W-54 | 匿名SID/名前の変換の無効化       | Disable anonymous SID/name translation              | ✅ Secure / ❌ Insecure | TurnOffAnonymousBlock = 0 なら Secure |
| 25 | W-57 | リモートデスクトップ接続ユーザーの制限   | Limit users allowed for remote terminal connections | 📝 Manual Review      | RDPユーザーの構成確認は手動                     |
| 26 | W-67 | RDPセッションのタイムアウト設定     | Set timeout for remote terminal sessions            | 📝 Manual Review      | グループポリシーの確認が必要                      |

> **理由：** 横展開・情報閲覧・RDP攻撃の経路遮断。

---

### 5. **セキュリティソフトウェア・更新・暗号化（Security Software, Updates & Encryption）**

| No | コード  | 説明                     | 英語説明                                            | 評価ランク                                        | 備考                                                |
| -- | ---- | ---------------------- | ----------------------------------------------- | -------------------------------------------- | ------------------------------------------------- |
| 27 | W-31 | 最新サービスパックの適用           | Apply latest service packs                      | ❌ Insecure / 📝 Manual Review                | OSインストール日から183日超過で Insecure、確認不可は Manual          |
| 28 | W-32 | 最新Hotfixの適用            | Apply latest hotfixes                           | ❌ Insecure / ✅ Secure / 📝 Manual Review     | Hotfix日から30日超過で Insecure、確認不可は Manual             |
| 29 | W-33 | ウイルス定義の更新状況            | Keep antivirus up to date                       | ❌ Insecure / ⚠️ Recommend / 📝 Manual Review | Defender存在時は手動で確認。他製品のみ→Recommend、なし→Insecure     |
| 30 | W-36 | ウイルス対策ソフトの導入           | Install antivirus software                      | ✅ Secure / ⚠️ Recommend / ❌ Insecure         | Defenderあり＋登録済→Secure、他製品のみ→Recommend、なし→Insecure |
| 31 | W-45 | ディスク暗号化の設定（BitLocker等） | Enable disk volume encryption (e.g., BitLocker) | ✅ Secure / ❌ Insecure / 📝 Manual Review     | BitLockerの有無と保護状態で判断。確認不可は Manual Review          |

> **理由：** ゼロデイ対応力・盗難やランサム被害の最小化のため。

---

## 📘 評価ランクの説明

| ランク                  | 意味                   | 例                                               |
| -------------------- | -------------------- | ----------------------------------------------- |
| ✅ **Secure**         | セキュリティポリシーに適合し変更不要   | `Telnet無効`, `PasswordComplexity = 1`            |
| ❌ **Insecure**       | セキュリティ的に不十分または明確なリスク | `CrashOnAuditFail = 0`, `MinPasswordLength = 5` |
| 🟨 **Recommend**     | 動作可能だがより良い設定が推奨される   | `LogMaxSize = 20MB`, `Retention = 0`            |
| 📝 **Manual Review** | 自動判断できず手動確認が必要       | `W-07: WinRM構成`, `W-67: ポリシー未設定` など             |

---

## 🟨 参考：Windows Server限定またはEDR適用度が低い項目

以下の項目は、一般的に**Windows ServerまたはIIS/DNS/FTPなどのサーバー系構成**に特化しており、Windows 10/11クライアントでは必須項目ではありません：

* `W-10`〜`W-23`: IIS関連設定
* `W-24`, `W-29`, `W-60`〜`W-63`: NetBIOS, DNS, SNMP 等
* `W-27`: Anonymous FTP（クライアントでは通常使用されない）

---

## ✳️ 結論

Windows 10/11環境で**EDRベースの脅威検出およびリアルタイム対応**を実施するには、アカウント・ログ・サービス・プロセス・ネットワーク制御を中心に**少なくとも40項目以上を重点的に確認する必要があります**。本リストは、PLURA-XDRやMicrosoft Defender for Endpoint、CrowdStrikeなどの主要EDR製品のガイドラインとも一致します。

---
