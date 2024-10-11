# About

### 1. **시스템 청소(System Cleanup)**:
   - 시스템에서 불필요한 파일, 캐시, 로그 등을 정리하여 보안과 성능을 향상시키는 작업을 의미합니다.
   - 사용되지 않는 파일을 제거함으로써 시스템의 공격 표면을 줄이는 데 도움이 됩니다.
#### 1) **[LG Software의 URAlarm.exe 프로세스](https://github.com/QubitSecurity/VAS/blob/main/Prevention/FileCleanup/URAlarm.exe.md)**
    - Windows 작업 스케줄러에 등록된 "CriticalUpdate(LUR)" 작업을 강제로 삭제한 이벤트를 기록한 것입니다. 실행된 cmd.exe는 **높은 무결성 수준(관리자 권한)**에서 실행되었습니다.

### 2. **비활성화**:
   - 일반적으로 스크립트 기반의 자동화 작업이 필요하지 않은 환경에서는 비활성화하여 스크립트 실행을 제한합니다.
#### 1) **[Windows Script Host (`wscript.exe`, `cscript.exe`)](https://github.com/QubitSecurity/VAS/blob/main/Prevention/FileCleanup/URAlarm.exe.md)**
    - **기능:** VBScript 또는 JScript로 작성된 스크립트 실행.
    - **악용:** 다양한 유해 동작을 수행하는 악성 스크립트 실행.

### 3. [**안티바이러스 제품의 윈도우 이벤트 로그 연동**](https://github.com/QubitSecurity/VAS/blob/main/Prevention/AntiVirus.md)
