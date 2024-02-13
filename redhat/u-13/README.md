## 2. 파일 및 디렉토리 관리

### SUID, SGID, 설정 파일점검

주기적인 감사 방법 (SUID/SGID 설정된 의심스러운 파일 확인)
```
find / -xdev -user root -type f \( -perm -04000 -o -perm -02000 \) -exec ls –al {} \;
```

### nodev와 nosuid 설정 파일점검

nodev와 nosuid 옵션이 적절하게 설정되지 않은 파일 시스템을 찾았을 때 "F"를 출력
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
overall_result="T"

# nodev와 nosuid 옵션 점검 시작
count=1
for fs in "${check_filesystems[@]}"; do
  echo "[$count] Checking $fs for nodev and nosuid options..."
  
  # 마운트된 파일 시스템의 옵션 확인
  mount_options=$(mount | grep " on $fs " | awk '{print $6}')
  fs_result="T"
  
  # nodev 옵션 점검
  if [[ $mount_options != *nodev* ]]; then
    echo "    F - $fs does not have 'nodev' option set."
    # ls -ld $fs
    fs_result="F"
    overall_result="F"
  else
    echo "    T - nodev option is set for $fs."
  fi
  
  # nosuid 옵션 점검
  if [[ $mount_options != *nosuid* ]]; then
    echo "    F - $fs does not have 'nosuid' option set."
    # ls -ld $fs
    fs_result="F"
    overall_result="F"
  else
    echo "    T - nosuid option is set for $fs."
  fi
  
  count=$((count + 1))
done

# 모든 파일 시스템이 적절하게 설정되었는지 최종 결과 출력
if [ "$overall_result" == "T" ]; then
  echo "[Status] T - All filesystems are correctly configured with 'nodev' and 'nosuid' options."
else
  echo "[Status] F - One or more filesystems are incorrectly configured."
fi
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

### 2.2 Replace-field

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
