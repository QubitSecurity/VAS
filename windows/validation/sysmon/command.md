## Check

```text
PS C:\Users\admin\Downloads> certutil.exe -urlcache -split -f https://live.sysinternals.com/Procmon.exe $env:TEMP\procmon_test.exe
****  Online  ****
  000000  ...
  3ef018
CertUtil: -URLCache 명령이 성공적으로 완료되었습니다.
PS C:\Users\admin\Downloads>
```

```text
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
