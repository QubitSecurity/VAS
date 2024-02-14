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

### x. "Chain INPUT (policy ACCEPT)" 검사의 필요성

`iptables -L | grep -qv "Chain INPUT (policy ACCEPT)"` 명령어의 핵심은 시스템의 iptables 방화벽 설정 중 INPUT 체인의 기본 정책(policy)이 ACCEPT로 설정되어 있는지 여부를 확인하는 것입니다. 여기서 `"Chain INPUT (policy ACCEPT)"` 문자열이 없다면 `(grep -qv)`, 이는 INPUT 체인의 기본 정책이 ACCEPT가 아님을 의미합니다.

- `보안 강화`:  iptables의 INPUT 체인에 대한 기본 정책을 ACCEPT로 설정하면, 명시적으로 차단하지 않은 모든 인바운드(들어오는) 트래픽이 시스템으로 허용됩니다. 이는 잠재적 보안 위험을 높일 수 있습니다. 따라서, 기본 정책을 ACCEPT가 아닌 DROP 또는 REJECT로 설정하는 것이 일반적으로 더 안전한 접근 방식입니다. 이렇게 설정하면, 명시적으로 허용된 규칙에 매칭되지 않는 모든 트래픽이 기본적으로 차단됩니다.
- `명시적 허용 규칙의 필요성`: 기본 정책이 ACCEPT가 아닐 경우, 시스템으로의 접근을 허용하려면 명시적인 허용 규칙을 설정해야 합니다. 이는 네트워크 보안을 강화하는 데 도움이 되며, 관리자가 네트워크 트래픽에 대해 더 세밀한 제어를 할 수 있게 합니다.
- `무분별한 접근 차단`: 기본 정책을 ACCEPT가 아니게 설정함으로써, 시스템에 대한 무분별한 접근을 기본적으로 차단할 수 있습니다. 이는 특히 공개적으로 접근 가능한 서버나, 보안이 중요한 환경에서 매우 중요합니다.
- `보안 감사 및 규정 준수`: 많은 보안 감사 및 규정 준수 요구 사항은 네트워크에 대한 기본적인 "거부(Deny)" 접근 정책을 요구합니다. "모든 것을 거부하고 필요한 것만 허용한다(Deny all, allow by exception)"는 네트워크 보안의 기본 원칙 중 하나입니다.
