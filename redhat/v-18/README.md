## 2. 파일 및 디렉토리 관리

### 2.14 접속 IP 및 포트 제한
RHEL 각 버전에 따라 제공되는 방화벽 관리 도구에 차이가 있습니다.

<hr/>

### 1. iptables 또는 firewalld

- `iptables`:RHEL 6 이하에서 사용되는 방화벽 관리 도구입니다. iptables는 패킷 필터링 규칙을 설정하여 특정 IP 주소나 포트로의 접근을 허용하거나 차단할 수 있습니다.
  - IP 주소 기반의 접근 제한 예: `iptables -A INPUT -s 192.168.1.100 -j ACCEPT`
  - 포트 기반의 접근 제한 예: `iptables -A INPUT -p tcp --dport 22 -j ACCEPT`
- `firewalld`: RHEL 7 이상에서 기본적으로 사용되는 동적 방화벽 관리 도구입니다. firewalld는 iptables보다 더 유연하고 사용하기 쉬운 구성을 제공합니다.
  - 특정 소스 IP에서 오는 접근 허용: `firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="192.168.1.100" accept'`
  - 특정 포트에 대한 접근 허용: `firewall-cmd --permanent --add-port=22/tcp`


### 2. /etc/hosts.allow 및 /etc/hosts.deny (TCP Wrappers)

- `TCP Wrappers`: 일부 서비스에 대해 IP 주소 기반의 접근 제어를 제공합니다. `/etc/hosts.allow`와 `/etc/hosts.deny` 파일을 통해 허용하거나 거부할 호스트를 지정할 수 있습니다.
  - 특정 IP에서의 SSH 접근만 허용하려면 `/etc/hosts.allow`에 다음과 같이 추가: `sshd: 192.168.1.100`
  - `/etc/hosts.deny`에는 모든 다른 접근을 거부하도록 설정 제한: `sshd: ALL`

<hr/>
