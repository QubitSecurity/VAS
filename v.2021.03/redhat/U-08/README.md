### 부록 05. 계정 설명

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

- `lp:x:4:7:lp:/var/spool/lpd:/sbin/nologin` : 로컬 프린트 서버
- `sync:x:5:0:sync:/sbin:/bin/sync` : 원격지 서버 동기화
- `shutdown:x:6:0:shutdown:/sbin:/sbin/shutdown` : soft 시스템 종료
- `halt:x:7:0:halt:/sbin:/sbin/halt` : 강제 시스템 종료
- `mail:x:8:12:mail:/var/spool/mail:/sbin/nologin` : 메일 서비스 계정
- `news:x:9:13:news:/etc/news:/sbin/nologin`
- `uucp:x:10:14:uucp:/var/spool/uucp:/sbin/nologin` : 유닉스 시스템 간 파일을 복사 프로토콜
- `operator:x:11:0:operator:/root:/sbin/nologin` : 설정에 따라 다르지만 /etc/syslog.conf 에 대해서 daemon.err operator라고 표기되어 있다면 데몬 관련 에러를 operator 계정을 이용해 출력하라는 의미임
- `games:x:12:100:games:/usr/games:/sbin/nologin`
- `gopher:x:13:30:gopher:/var/gopher:/sbin/nologin` : 웹(www)이 나오기 전 대표적인 서비스 중 하나로 gopher사이트 접속 후 잘 정리된 메뉴를 이용해서 웹 서핑을 즐기도록 한 서비스
- `ftp:x:14:50:FTP User:/var/ftp:/sbin/nologin` : ftp 사용 시 필요
- `squid:x:23:23::/var/spool/squid:/sbin/nologin` : 프록시 서버
- `named:x:25:25:Named:/var/named:/sbin/nologin` : 네임서비스 데몬 계정
- `mysql:x:27:27::/home/mysql:/bin/bash` : mysql 서비스 시작 시 사용하는 계정
- `nscd:x:28:28:NSCD Daemon:/:/sbin/nologin` : 네임서비스에 대한 캐시 기능 제공
- `rpcuser:x:29:29:RPC Service User:/var/lib/nfs:/sbin/nologin`
- `rpc:x:32:32:Portmapper RPC user:/:/sbin/nologin` : 원격 호출 관련 데몬
- `lp:x:4:7:lp:/var/spool/lpd:/sbin/nologin` : 로컬 프린트 서버
- `sync:x:5:0:sync:/sbin:/bin/sync` : 원격지 서버 동기화
- `shutdown:x:6:0:shutdown:/sbin:/sbin/shutdown` : soft 시스템 종료
- `halt:x:7:0:halt:/sbin:/sbin/halt` : 강제 시스템 종료
- `mail:x:8:12:mail:/var/spool/mail:/sbin/nologin` : 메일 서비스 계정
- `operator:x:11:0:operator:/root:/sbin/nologin` : 설정에 따라 다르지만 /etc/syslog.conf 에 대해서 daemon.err operator라고 표기되어 있다면 데몬 관련 에러를 operator 계정을 이용해 출력하라는 의미임
- `games:x:12:100:games:/usr/games:/sbin/nologin`
- `ftp:x:14:50:FTP User:/var/ftp:/sbin/nologin` : ftp 사용 시 필요
- `ntp:x:38:38::/etc/ntp:/sbin/nologin` : 컴퓨터 간 시간 동기화 Network Time Protocol
- `sshd:x:74:74:Privilege-separated SSH` : /var/empty/sshd:/sbin/nologin = 보안 쉘 계정
- `nobody:x:99:99:Nobody:/:/sbin/nologin` : 익명 연결 (웹 서비스 등 누구나 연결 가능한 서비스 사용 시)
- `nfsnobody:x:65534:65534:Anonymous` : NFS User:/var/lib/nfs:/sbin/nologin

* UID 100이하 또는 60000이상의 계정들은 시스템 계정으로 로그인이 필요없음
  
<hr/>
