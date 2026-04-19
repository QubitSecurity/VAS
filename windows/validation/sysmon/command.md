## Deny

```powershell
PS C:\Users\admin\Downloads> certutil.exe -urlcache -split -f https://live.sysinternals.com/Procmon.exe $env:TEMP\procmon_test.exe
****  Online  ****
  000000  ...
  3ef018
CertUtil: -URLCache 명령이 성공적으로 완료되었습니다.
PS C:\Users\admin\Downloads>
```

```powershell
PS C:\Users\admin\Downloads> certutil.exe -urlcache -split -f https://live.sysinternals.com/Procmon.exe C:\Windows\Temp\procmon_test.exe
'certutil.exe' 프로그램을 실행하지 못했습니다. 액세스가 거부되었습니다위치 줄:1 문자:1
+ certutil.exe -urlcache -split -f https://live.sysinternals.com/Procmo ...
+ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~.
위치 줄:1 문자:1
+ certutil.exe -urlcache -split -f https://live.sysinternals.com/Procmo ...
+ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    + CategoryInfo          : ResourceUnavailable: (:) [], ApplicationFailedException
    + FullyQualifiedErrorId : NativeCommandFailed
```

---


```powershell
PS C:\Users\admin\Downloads> copy C:\Windows\System32\calc.exe C:\Users\Public\test_block.exe
PS C:\Users\admin\Downloads>

PS C:\Users\admin\Downloads> dir C:\Windows\Temp\
PS C:\Users\admin\Downloads>
```

---

### Allow

```powershell
certutil.exe -urlcache -split -f https://live.sysinternals.com/Procmon.exe $env:TEMP\procmon_test.exe
```

---

이유 : PLURA 룰셋에 일반적인 프로그램 설치나 정상 동작이 오탐(False Positive)으로 차단되는 것을 막기 위한 **예외 처리(Whitelist)가 되어 있었기 때문**입니다.

### 🕵️‍♂️ 이전 테스트가 차단되지 않은 이유
설정 파일의 `<FileBlockExecutable onmatch="exclude">` (예외 처리) 섹션을 보면 아래와 같은 룰이 있습니다.

```xml
<Rule groupRelation="and">
  <TargetFilename condition="contains">\AppData\Local\Temp\</TargetFilename>
  <TargetFilename condition="end with">.exe</TargetFilename>
</Rule>
```
이 룰은 **"`\AppData\Local\Temp\` 경로에 생성되는 `.exe` 파일은 차단하지 말고 허용하라"**는 뜻입니다. 
우리가 이전에 사용했던 `$env:TEMP` 환경 변수가 바로 저 경로를 가리키고, `.exe` 확장자를 사용했기 때문에 Sysmon이 룰에 따라 정상적으로 "통과" 시켜준 것입니다. (보안 솔루션이 아주 정교하게 잘 설정되어 있네요!)

---

