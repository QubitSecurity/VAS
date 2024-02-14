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

```
.bash_logout: Bash 셸 사용자가 로그아웃할 때 실행되는 스크립트입니다. 사용자 세션의 종료 시 정리 작업을 수행하는 데 사용될 수 있습니다.
.bash_profile: Bash 셸 사용자가 로그인할 때 실행되는 스크립트입니다. 사용자의 환경 변수, 시작 스크립트 등을 초기화합니다.
.bashrc: Bash 셸 사용자의 구성 파일로, 셸 세션 시작 시마다 실행됩니다. 별명(alias), 함수, 변수 등을 설정하는 데 사용됩니다.
.cshrc: C 셸(C Shell) 사용자의 구성 파일로, 셸 세션 시작 시마다 실행됩니다. C 셸 사용자의 환경 설정을 정의합니다.
.exrc: Vi 편집기의 구성 파일로, Vi 시작 시 실행되는 명령어를 포함합니다. Vi 사용자 환경을 사용자화하는 데 사용됩니다.
.kshrc: Korn 셸(K Shell) 사용자의 구성 파일로, 셸 세션 시작 시마다 실행됩니다. Korn 셸 사용자의 환경 설정을 정의합니다.
.login: C 셸(C Shell) 사용자가 로그인할 때 실행되는 스크립트입니다. 사용자의 로그인 환경을 초기화합니다.
.netrc: 네트워크 유틸리티 구성 파일로, FTP 같은 프로그램에서 사용자 인증 정보를 자동으로 처리하는 데 사용됩니다.
.pam_environment: 사용자의 PAM(Pluggable Authentication Modules) 환경 변수를 설정하는 파일입니다. 시스템 전반에 걸쳐 사용자 환경 변수를 관리하는 데 사용됩니다.
.profile: Bourne 셸(Sh) 및 호환 셸(Bash, Ksh 등) 사용자가 로그인할 때 실행되는 스크립트입니다. 사용자의 로그인 환경을 초기화합니다.
.zlogin: Z 셸(Zsh) 사용자가 로그인할 때 실행되는 스크립트입니다. 사용자의 로그인 환경을 초기화하는 데 사용됩니다.
.zlogout: Z 셸(Zsh) 사용자가 로그아웃할 때 실행되는 스크립트입니다. 로그아웃 시 정리 작업을 수행하는 데 사용될 수 있습니다.
.zprofile: Z 셸(Zsh) 사용자의 로그인 스크립트로, 로그인 시 실행됩니다. .profile과 유사한 역할을 하지만 Z 셸에 특화되어 있습니다.
.zshrc: Z 셸(Zsh) 사용자의 구성 파일로, 셸 세션 시작 시마다 실행됩니다. 별명(alias), 함수, 변수 등을 설정하는 데 사용됩니다.
```

Example
```
[1] Checking /dev/shm for nodev and nosuid options...
    T - nodev option is set for /dev/shm.
    T - nosuid option is set for /dev/shm.
[2] Checking /tmp for nodev and nosuid options...
    F - /tmp does not have 'nodev' option set.
    F - /tmp does not have 'nosuid' option set.
[3] Checking /var/tmp for nodev and nosuid options...
    F - /var/tmp does not have 'nodev' option set.
    F - /var/tmp does not have 'nosuid' option set.
[4] Checking /home for nodev and nosuid options...
    F - /home does not have 'nodev' option set.
    F - /home does not have 'nosuid' option set.
[Status] F - One or more filesystems are incorrectly configured.
```

<hr/>

### 점검

시스템에서 마운트된 모든 파일 시스템과 그 옵션을 확인하고자 할 때 /proc/mounts 파일을 조회할 수 있습니다. 
다음은 현재 마운트된 파일 시스템의 실시간 목록을 제공합니다:

nodev와 nosuid 설정이 없으면 출력 없음
```
cat /proc/mounts | grep /home
```

nodev와 nosuid 설정이 있으면 해당 내용 출력
```
cat /proc/mounts | grep /dev/shm
tmpfs /dev/shm tmpfs rw,seclabel,nosuid,nodev 0 0
```

<hr/>

```
ABC
```

<hr/>

```
curl -X POST -H "Content-type: application/json" --data-binary @schema_syslog_msg.json http://localhost:8983/solr/syslog/schema
```

<hr/>

```
c
```

<hr/>

```
d
```
