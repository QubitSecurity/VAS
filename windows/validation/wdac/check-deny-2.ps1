# 테스트할 실행 파일 목록
$targetExes = @(
    "atbroker.exe",
    "bitsadmin.exe",
    "certutil.exe",
    "cmstp.exe",
    "computerdefaults.exe",
    "cscript.exe",
    "dfsvc.exe",
    "eudcedit.exe",
    "expand.exe",
    "forfiles.exe",
    "hh.exe",
    "makecab.exe",
    "mavinject.exe",
    "msconfig.exe",
    "mshta.exe",
    "msxsl.exe",
    "pcalua.exe",
    "pcwrun.exe",
    "presentationhost.exe",
    "provlaunch.exe",
    "rasautou.exe",
    "regasm.exe",
    "regsvcs.exe",
    "regsvr32.exe",
    "rundll32.exe",
    "scriptrunner.exe",
    "syncappvpublishingserver.exe",
    "unregmp2.exe",
    "verclsid.exe",
    "wscript.exe",
    "wsreset.exe",
    "wuauclt.exe"
)

Write-Host "WDAC 실행 차단 테스트를 시작합니다..." -ForegroundColor Cyan
Write-Host "========================================="

foreach ($exe in $targetExes) {
    Write-Host "[$exe] 테스트 중... " -NoNewline

    try {
        # 백그라운드(Hidden)에서 실행 시도, 오류 발생 시 즉시 catch 블록으로 이동
        $process = Start-Process -FilePath $exe -WindowStyle Hidden -PassThru -ErrorAction Stop
        
        # 에러 없이 이 줄에 도달했다면 WDAC가 차단하지 못한 것입니다.
        Write-Host "실행됨 (차단 실패!)" -ForegroundColor Red
        
        # 테스트를 위해 실행된 프로세스 즉시 종료
        if ($process) {
            Stop-Process -InputObject $process -Force -ErrorAction SilentlyContinue
        }
    }
    catch {
        $errMsg = $_.Exception.Message
        
        # WDAC에 의해 차단될 때 나타나는 일반적인 시스템 오류 메시지 패턴 확인 (국문/영문)
        if ($errMsg -match "그룹 정책" -or $errMsg -match "차단" -or $errMsg -match "blocked" -or $errMsg -match "액세스가 거부" -or $errMsg -match "Access is denied") {
            Write-Host "차단됨 (정상)" -ForegroundColor Green
        } 
        elseif ($errMsg -match "지정된 파일을 찾을 수 없습니다" -or $errMsg -match "cannot find the file") {
            Write-Host "파일 없음 (해당 OS에 파일이 존재하지 않음)" -ForegroundColor DarkGray
        }
        else {
            # 기타 다른 이유로 인한 실행 실패
            Write-Host "오류 발생 ($errMsg)" -ForegroundColor Yellow
        }
    }
}

Write-Host "========================================="
Write-Host "테스트가 완료되었습니다." -ForegroundColor Cyan
