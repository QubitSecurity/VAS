# LOLBAS Tunnel (2) 탐지용 정규식 정책표
작성일: 2026-03-14

이 문서는 앞서 재분류한 **사용자 정의 Tunnel 그룹 2개**를 기준으로, **CLI 출력 / Process CommandLine 문자열 기반 탐지 정책**을 설계하기 위한 운영용 정리본입니다.

> **중요:** 여기의 `Tunnel (2)`은 LOLBAS 공식 분류를 그대로 옮긴 것이 아닙니다. `devtunnel.exe`는 LOLBAS 공식 페이지에서 `Download`로 소개되고, `ssh.exe`는 `Execute (CMD)`로 소개됩니다. 그러나 운영 관점에서는 `devtunnel host/connect`와 `ssh -L/-R/-D/-w`가 **포트포워딩·중계 채널 생성**이라는 공통 성격을 가지므로 별도 Tunnel 그룹으로 분리하는 편이 더 실용적입니다.

## 설계 원칙
- 기본 전제: **도구명 + 터널링 신호(host/connect/anonymous access/token scope/forwarding flags/local port)** 를 함께 봅니다.
- 정규식은 모두 **대소문자 무시 `(?i)`** 기준입니다.
- `ssh.exe`는 **일반 원격 셸 접속**과 **포트포워딩/동적 SOCKS/터널 디바이스 포워딩**을 분리해야 합니다. 일반 접속은 `Remote` 문서, 포워딩은 `Tunnel` 문서가 더 적합합니다.
- `devtunnel.exe`는 단순 실행만으로는 의미가 약하고, **`host` / `connect` / `--allow-anonymous` / `--scopes connect`** 같은 운영 신호가 훨씬 중요합니다.
- 가능하면 **Process CommandLine + Network telemetry + Local Listening Port + Parent/Child process**를 함께 봅니다.
- 개발자 PC, 점프 서버, 운영 자동화 호스트는 allowlist 또는 baseline 분리가 필요합니다.

## 권장 운영 구조
1. **Broad prefilter**: 터널 계열 후보 도구명을 먼저 좁힙니다.  
   - <code>(?i)\b(?:devtunnel|ssh)(?:\.exe)?\b</code>
2. **그룹 규칙**: `Dev Tunnels Relay / Public Exposure` 와 `SSH Port Forward / SOCKS / TUN` 으로 나눠 적용합니다.
3. **상관분석 규칙**
   - `devtunnel.exe`: `host`/`connect`, `--allow-anonymous`, `--scopes connect`, `*.devtunnels.ms`, temp 경로 실행, 로컬 포트 노출
   - `ssh.exe`: `-L/-R/-D/-w`, `-N`, `-f`, `-g`, outbound connection, 로컬 listening port, jump host 사용 여부
4. **환경 allowlist**
   - Visual Studio / VS Code / 웹개발 환경
   - 관리자 점프호스트
   - 네트워크 운영/원격지원 시스템
   - CI/CD, 테스트 랩, 임시 디버깅 환경

## 그룹 요약
| Group | Count | 운영 의미 |
|---|---:|---|
| Dev Tunnels Relay / Public Exposure | 1 | 로컬 서비스나 포트를 relay/forwarding 채널로 외부에 노출시키는 계열 |
| SSH Port Forward / SOCKS / TUN | 1 | `-L/-R/-D/-w` 로 TCP·SOCKS·터널 디바이스를 중계하는 계열 |

## 그룹 공통 정규식
- **Dev Tunnels Relay / Public Exposure**: <code>(?i)\bdevtunnel(?:\.exe)?\b.*?(?:\bhost\b(?:.*?\b-p\b\s+\d{1,5}\b)?(?:.*?\b--allow-anonymous\b)?|\bconnect\b\s+[A-Za-z0-9][A-Za-z0-9-]*\b|\baccess\s+create\b.*?\b--anonymous\b|\btoken\b.*?\b--scopes\b\s+connect\b)</code>
- **SSH Port Forward / SOCKS / TUN**: <code>(?i)\bssh(?:\.exe)?\b.*?(?:\s-L\b\s*(?:\[[^\]]+\]:|[^:\s]+:)?\d{1,5}:[^:\s]+:\d{1,5}\b|\s-R\b\s*(?:\[[^\]]+\]:|[^:\s]+:)?\d{1,5}:[^:\s]+:\d{1,5}\b|\s-D\b\s*(?:\[[^\]]+\]:|[^:\s]+:)?\d{1,5}\b|\s-w\b\s*(?:any|\d+)(?::(?:any|\d+))?\b)</code>

## CLI 출력 기반 보조 패턴
> 아래 패턴은 **stdout/stderr 또는 콘솔 캡처가 가능한 환경**에서만 보조로 사용하는 것을 권장합니다. 특히 `devtunnel`은 콘솔 출력이 매우 강한 시그널입니다.

- **DevTunnel host 출력**: <code>(?i)\bHosting port \d{1,5} at https://[A-Za-z0-9.-]+\.devtunnels\.ms/?\b</code>
- **DevTunnel connect 출력**: <code>(?i)\bSSH:\s+Forwarding from (?:127\.0\.0\.1|\[::1\]):\d{1,5}\s+to host port \d{1,5}\b</code>

## 특기사항
- `devtunnel.exe`는 LOLBAS 공식 분류상 `Download` 이지만, 운영상 핵심은 **중계 채널 생성과 포트 노출**입니다. 따라서 `Tunnel` 태그가 더 직관적입니다.
- `ssh.exe`는 LOLBAS 공식 분류상 `Execute (CMD)` 이지만, **`-L/-R/-D/-w`** 는 원격 명령 실행보다 **중계/프록시/터널** 의미가 더 큽니다.
- `ssh.exe`의 일반 세션 접속(`ssh user@host`)은 이 문서의 대상이 아닙니다. 그 패턴은 `Remote` 문서에서 관리하는 편이 정확합니다.
- `ssh.exe`는 `-N`, `-f`, `-g` 가 함께 붙으면 **비대화형 터널/백그라운드 프록시** 가능성이 커집니다.
- `devtunnel.exe`는 사용자 temp 경로에서 실행될 수 있으므로, **실행 경로 + 네트워크 목적지(`*.devtunnels.ms`)** 를 결합하면 정확도가 올라갑니다.
- `ssh.exe`는 명령행만으로는 실제 목적지와 로컬 리스닝 상태를 모두 확인하기 어렵습니다. **네트워크 세션, 방화벽 로그, 포트 리스너 이벤트**를 함께 보는 것이 좋습니다.
- `ssh -W host:port`, Unix socket forwarding, SSH config alias 기반 포워딩은 기본 정규식에서 보수적으로 제외했습니다. 필요하면 별도 확장 규칙을 추가합니다.

## 정책표
| No | Tool | Group | Regex | FP 주의 | FN 주의 | 권장 정책명 |
|---:|---|---|---|---|---|---|
| 1 | devtunnel.exe | Dev Tunnels Relay / Public Exposure | <code>(?i)\bdevtunnel(?:\.exe)?\b.*?(?:\bhost\b(?:.*?\b-p\b\s+\d{1,5}\b)?(?:.*?\b--allow-anonymous\b)?|\bconnect\b\s+[A-Za-z0-9][A-Za-z0-9-]*\b|\baccess\s+create\b.*?\b--anonymous\b|\btoken\b.*?\b--scopes\b\s+connect\b)</code> | 정상 개발/디버깅 환경, VS Code/Visual Studio 포트 공유, 원격 협업 시나리오와 겹칠 수 있습니다. 특히 개발자 워크스테이션은 baseline 분리가 필요합니다. | `devtunnel create`, `port create`, `update`, 브라우저 기반 relay 사용, 또는 config/cache 에 의존하는 흐름은 기본 규칙에서 일부 놓칠 수 있습니다. stdout 캡처가 있으면 `Hosting port ... at https://...devtunnels.ms/` 패턴을 함께 쓰는 것이 좋습니다. | `LOLBAS.Tunnel.DevTunnel.HostOrConnect` |
| 2 | ssh.exe | SSH Port Forward / SOCKS / TUN | <code>(?i)\bssh(?:\.exe)?\b.*?(?:\s-L\b\s*(?:\[[^\]]+\]:|[^:\s]+:)?\d{1,5}:[^:\s]+:\d{1,5}\b|\s-R\b\s*(?:\[[^\]]+\]:|[^:\s]+:)?\d{1,5}:[^:\s]+:\d{1,5}\b|\s-D\b\s*(?:\[[^\]]+\]:|[^:\s]+:)?\d{1,5}\b|\s-w\b\s*(?:any|\d+)(?::(?:any|\d+))?\b)</code> | 정상 관리자 포트포워딩, 개발자 로컬 포트 공유, 데이터베이스/웹 관리, 프록시 점프 환경과 겹칠 수 있습니다. `-N`, `-f`, `-g` 가 동반되면 우선순위를 높이는 방식이 좋습니다. | `ssh -W host:port`, Unix socket forwarding, SSH config alias 만으로 포워딩 대상이 숨는 경우, 또는 별도 스크립트가 옵션을 조립하는 경우는 일부 누락될 수 있습니다. 로컬 리스너 이벤트와 원격 세션 로그를 결합하는 편이 안전합니다. | `LOLBAS.Tunnel.SSH.PortForwardOrTUN` |

## 운영 권장안
- **1차**: <code>(?i)\b(?:devtunnel|ssh)(?:\.exe)?\b</code> 로 prefilter
- **2차**: 위 정책표의 개별 정규식 적용
- **3차 상관분석**
  - `devtunnel.exe` + `*.devtunnels.ms`
  - `devtunnel.exe` + stdout `Hosting port`
  - `ssh.exe` + `-L/-R/-D/-w`
  - `ssh.exe` + `-N/-f/-g`
  - `ssh.exe` + 로컬 listening port 생성
- **4차 환경 예외**
  - 개발자 PC
  - 점프박스
  - 네트워크 운영 서버
  - 승인된 원격지원 도구 운용 구간

## 운영 메모
- Tunnel 그룹은 **도구 수는 적지만 위험도는 높습니다**. 특히 원격 공격자가 이미 foothold 를 확보한 뒤 **내부 자산 노출, NAT 우회, SOCKS 프록시, 임시 relay** 용도로 자주 사용합니다.
- 따라서 이 그룹은 개수보다 **정확한 시나리오 분리**가 중요합니다.
- `devtunnel.exe`는 Download 분류에 묻히면 운영적 의미가 약해지므로, 별도 Tunnel 그룹 유지가 적절합니다.
- `ssh.exe`는 Remote 와 Tunnel 을 분리해야 탐지 품질이 올라갑니다. 일반 접속과 포트포워딩을 같은 정책으로 묶으면 오탐이 급증합니다.
