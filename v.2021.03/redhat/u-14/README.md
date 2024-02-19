## 2. 파일 및 디렉토리 관리

### 2.10 사용자, 시스템 시작파일 및 환경파일 소유자 및 권한 설정

- 각 파일은 사용자의 셸 환경과 편집기 설정에 대한 개인화를 가능하게 합니다. 보안 관점에서, 이 파일들의 쓰기 권한을 적절히 관리하는 것은 중요합니다.

```
files=(
  ".bash_logout"
  ".bash_profile"
  ".bashrc"
  ".cshrc"
  ".exrc"
  ".kshrc"
  ".login"
  ".netrc"
  ".pam_environment"
  ".profile"
  ".zlogin"
  ".zlogout"
  ".zprofile"
  ".zshrc"
)
```

<hr/>

### 환경 변수 설명

- `.bash_logout`: Bash 셸 사용자가 로그아웃할 때 실행되는 스크립트입니다.
- `.bash_profile`: Bash 셸 사용자가 로그인할 때 실행되는 스크립트입니다.
- `.bashrc`: Bash 셸 사용자의 구성 파일로, 셸 세션 시작 시마다 실행됩니다.
- `.cshrc`: C 셸(C Shell) 사용자의 구성 파일로, 셸 세션 시작 시마다 실행됩니다.
- `.exrc`: Vi 편집기의 구성 파일로, Vi 시작 시 실행되는 명령어를 포함합니다.
- `.kshrc`: Korn 셸(K Shell) 사용자의 구성 파일로, 셸 세션 시작 시마다 실행됩니다.
- `.login`: C 셸(C Shell) 사용자가 로그인할 때 실행되는 스크립트입니다.
- `.netrc`: 네트워크 유틸리티 구성 파일로, FTP 같은 프로그램에서 사용자 인증 정보를 자동으로 처리하는 데 사용됩니다.
- `.pam_environment`: 사용자의 PAM(Pluggable Authentication Modules) 환경 변수를 설정하는 파일입니다.
- `.profile`: Bourne 셸(Sh) 및 호환 셸(Bash, Ksh 등) 사용자가 로그인할 때 실행되는 스크립트입니다.
- `.zlogin`: Z 셸(Zsh) 사용자가 로그인할 때 실행되는 스크립트입니다.
- `.zlogout`: Z 셸(Zsh) 사용자가 로그아웃할 때 실행되는 스크립트입니다.
- `.zprofile`: Z 셸(Zsh) 사용자의 로그인 스크립트로, 로그인 시 실행됩니다.
- `.zshrc`: Z 셸(Zsh) 사용자의 구성 파일로, 셸 세션 시작 시마다 실행됩니다.

<hr/>
