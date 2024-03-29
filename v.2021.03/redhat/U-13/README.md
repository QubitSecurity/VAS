## 2. 파일 및 디렉토리 관리

### SUID, SGID, 설정 파일 점검

- `SUID(Special User ID) 및 SGID(Special Group ID)`: 이 설정은 실행 파일이 실행될 때, 파일의 소유자나 그룹의 권한으로 실행되도록 합니다. 이 기능은 특정 작업을 수행하기 위해 일시적으로 권한을 상승시킬 필요가 있을 때 유용하지만, 잘못 관리될 경우 보안 취약점이 될 수 있습니다. 따라서, 시스템에서 불필요하게 SUID/SGID 비트가 설정된 파일을 찾아 제거하거나, 필요한 경우에만 사용하도록 관리하는 점검이 필요합니다.

주기적인 감사 방법 (SUID/SGID 설정된 의심스러운 파일 확인)

```
find / -xdev -user root -type f \( -perm -04000 -o -perm -02000 \) -exec ls –al {} \;
```

<hr/>

### 부록 06. 불필요한 SUID/SGID 목록 설명 (154p)

- `/sbin/dump` : Backup/Restore
- `/sbin/restore` : Backup/Restore
- `/sbin/unix_chkpwd` : 사용자의 암호가 읽을 수 없는 장소에 보관되는 경우 사용자의 암호를 검사해주는 프로그램. 이 프로그램을 호출한 사용자의 암호를 검사해주는 역할만 함
- `/usr/bin/at` : 지정된 시간에 실행할 작업을 입력하고, 대기 목록을 확인하고, 제거하는 명령어
- `/usr/bin/lpq` : 라인프린터 작업 큐 조회 명령어
- `/usr/bin/lpq-lpd` : DAEMON
- `/usr/bin/lpr` : 콘솔환경에서 명시된 파일을 인쇄할 때 사용
- `/usr/bin/lpr-lpd` : DAEMON
- `/usr/bin/lprm` : lpq 명령어로 볼 수 있는 작업 큐를 살펴보고 해당하는 작업을 취소하거나 작업 번호를 지정하여 작업 번호에 해당하는 큐를 삭제
- `/usr/bin/lprm-lpd` : DAEMON
- `/usr/bin/newgrp` : 현재 세션의 사용자 그룹 변경(지정한 그룹의 쉘로 환경이 바로 변경)
- `/usr/sbin/lpc` : 커맨드 기반의 프린터 제어
- `/usr/sbin/lpc-lpd` : DAEMON
- `/usr/sbin/traceroute` : 네트워크 경로 출력

<hr/>

### nodev와 nosuid 설정 파일 점검

- `nodev 옵션` : 파일 시스템을 마운트할 때 이 옵션을 사용하면, 해당 파일 시스템에서 특수 장치 파일의 생성을 방지할 수 있습니다. 이는 시스템의 보안을 강화하는 데 도움이 됩니다.
- `nosuid 옵션` : 마찬가지로, 파일 시스템을 마운트할 때 이 옵션을 사용하면, 해당 파일 시스템에서 SUID 및 SGID 비트가 설정된 파일을 통한 권한 상승을 방지할 수 있습니다. 이는 SUID/SGID 설정 파일 점검과 직접적으로 관련이 있으며, 특히 공유 파일 시스템이나 사용자가 많이 접근하는 디렉토리에서 중요합니다.

nodev와 nosuid 옵션이 적절하게 설정되었다면 "S", 설정되지 않은 파일 시스템을 찾았을 때 "V" 출력
```
#!/bin/bash

# 점검할 파일 시스템 목록
check_filesystems=(
  "/dev/shm"
  "/tmp"
  "/var/tmp"
  "/home"
)

# 전체 점검 결과를 저장할 변수
overall_result="S"

# nodev와 nosuid 옵션 점검 시작
count=1
for fs in "${check_filesystems[@]}"; do
  echo "[$count] Checking $fs for nodev and nosuid options..."
  
  # 마운트된 파일 시스템의 옵션 확인
  mount_options=$(mount | grep " on $fs " | awk '{print $6}')
  fs_result="S"
  
  # nodev 옵션 점검
  if [[ $mount_options != *nodev* ]]; then
    echo "    F - $fs does not have 'nodev' option set."
    # ls -ld $fs
    fs_result="V"
    overall_result="V"
  else
    echo "    S - nodev option is set for $fs."
  fi
  
  # nosuid 옵션 점검
  if [[ $mount_options != *nosuid* ]]; then
    echo "    V - $fs does not have 'nosuid' option set."
    # ls -ld $fs
    fs_result="V"
    overall_result="V"
  else
    echo "    S - nosuid option is set for $fs."
  fi
  
  count=$((count + 1))
done

# 모든 파일 시스템이 적절하게 설정되었는지 최종 결과 출력
if [ "$overall_result" == "S" ]; then
  echo "[Status] S - All filesystems are correctly configured with 'nodev' and 'nosuid' options."
else
  echo "[Status] V - One or more filesystems are incorrectly configured."
fi
```

Example
```
[1] Checking /dev/shm for nodev and nosuid options...
    S - nodev option is set for /dev/shm.
    S - nosuid option is set for /dev/shm.
[2] Checking /tmp for nodev and nosuid options...
    V - /tmp does not have 'nodev' option set.
    V - /tmp does not have 'nosuid' option set.
[3] Checking /var/tmp for nodev and nosuid options...
    V - /var/tmp does not have 'nodev' option set.
    V - /var/tmp does not have 'nosuid' option set.
[4] Checking /home for nodev and nosuid options...
    V - /home does not have 'nodev' option set.
    V - /home does not have 'nosuid' option set.
[Status] V - One or more filesystems are incorrectly configured.
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
