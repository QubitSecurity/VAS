# 🚀 WinGet Policy Control & Verification Guide

이 프로젝트는 Windows 전역에서 **Windows Package Manager(winget)**의 사용 가능 여부를 제어하고, 정책이 실제 시스템에 올바르게 반영되었는지 검증하는 도구를 제공합니다.

## 📌 개요
Microsoft의 `EnableAppInstaller` 그룹 정책을 활용하여 winget 기능을 차단하거나 허용합니다. 정책이 비활성화(`OFF`)되면 `winget -?`와 같은 기본 도움말은 실행될 수 있으나, 실제 네트워크 통신이나 설치가 필요한 명령은 차단됩니다.



---

## 🛠 사용 방법

### 1. 정책 제어 스크립트 실행
`wd-winget-c.ps1` 스크립트를 사용하여 winget 상태를 전환합니다.

* **winget 차단 (활성화 상태 OFF):**
    ```powershell
    powershell -ExecutionPolicy Bypass -File .\wd-winget-c.ps1 OFF
    ```
* **winget 허용 (활성화 상태 ON):**
    ```powershell
    powershell -ExecutionPolicy Bypass -File .\wd-winget-c.ps1 ON
    ```
* **현재 상태 확인:**
    ```powershell
    powershell -ExecutionPolicy Bypass -File .\wd-winget-c.ps1 RELOAD
    ```

---

## 🔍 검증 시나리오 (Verification)

정책 변경 후 시스템이 실제로 어떻게 반응하는지 확인하기 위해 다음 단계별 테스트를 권장합니다.

### Step 1. 비파괴 기능 테스트 (Smoke Test)
`winget search` 명령은 실제 설치를 진행하지 않으면서 정책 차단 여부를 확인할 수 있는 가장 안전한 방법입니다.

```powershell
# 결과 기대값: 
# OFF 상태 -> "그룹 정책에 의해 차단되었습니다" 메시지와 함께 실패
# ON 상태 -> 검색 결과 리스트 출력
winget search vscode
```

### Step 2. 다운로드 차단 테스트
실제 파일 바이너리가 내려받아지는지 확인하여 차단의 실효성을 검증합니다.

```powershell
# 임시 폴더 생성
$testPath = "$env:TEMP\wgtest"
Remove-Item $testPath -Recurse -Force -ErrorAction SilentlyContinue
New-Item $testPath -ItemType Directory | Out-Null

# 다운로드 시도
winget download --id Git.Git -e -d $testPath

# 파일 존재 여부 확인 (차단 상태라면 파일이 없어야 함)
Get-ChildItem $testPath
```

---

## 📝 주요 참고 사항
* **도움말 명령:** `winget -?`는 정책 차단 상태에서도 동작할 수 있습니다. 이는 시스템에 CLI 도구 자체가 남아 있기 때문이며 정상적인 현상입니다.
* **Windows 11 24H2 이상:** `EnableWindowsPackageManagerCommandLineInterfaces` 정책이 추가로 존재할 수 있으나, 본 스크립트가 제어하는 `EnableAppInstaller` 정책이 우선적인 차단 역할을 수행합니다.
* **정확한 상태 값:** 스크립트에서 반환하는 `Winget.Enabled` 값이 `OFF`라면 사용자의 다운로드가 금지된 상태임을 의미합니다.

---

## 🧪 자동 검증 스크립트
위의 모든 과정을 한 번에 수행하려면 `t-wd-winget.ps1` (작성 예정)을 실행하십시오. 이 스크립트는 정책 적용 상태와 실제 동작의 일치 여부를 양방향으로 비교 검증합니다.

---
