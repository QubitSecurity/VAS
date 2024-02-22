### 부록 01. find 명령어

```
# uname -a
Linux redhat-vas 3.10.0-1160.71.1.el7.x86_64 #1 SMP Tue Jun 28 15:37:28 UTC 2022 x86_64 x86_64 x86_64 GNU/Linux
```
`cat /etc/passwd`
- `lp:x:4:7:lp:/var/spool/lpd:/sbin/nologin` : 로컬 프린트 서버
- `sync:x:5:0:sync:/sbin:/bin/sync` : 원격지 서버 동기화
- `shutdown:x:6:0:shutdown:/sbin:/sbin/shutdown` : soft 시스템 종료
- `halt:x:7:0:halt:/sbin:/sbin/halt` : 강제 시스템 종료
- `mail:x:8:12:mail:/var/spool/mail:/sbin/nologin` : 메일 서비스 계정
- `operator:x:11:0:operator:/root:/sbin/nologin` : 설정에 따라 다르지만 /etc/syslog.conf 에 대해서 daemon.err operator라고 표기되어 있다면 데몬 관련 에러를 operator 계정을 이용해 출력하라는 의미임
- `games:x:12:100:games:/usr/games:/sbin/nologin`
- `ftp:x:14:50:FTP User:/var/ftp:/sbin/nologin` : ftp 사용 시 필요

<hr/>
