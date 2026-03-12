# Windows

### 윈도우즈 서버 취약점 분석 · 평가 항목

#### [W-01](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-01%5D%20Administrator%20%EA%B3%84%EC%A0%95%20%EC%9D%B4%EB%A6%84%20%EB%B3%80%EA%B2%BD.md) `상` Administrator 계정 이름 변경 또는 보안성 강화

#### [W-02](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-02%5D%20Guest%20%EA%B3%84%EC%A0%95%20%EB%B9%84%ED%99%9C%EC%84%B1%ED%99%94.md) `상` Guest 계정 비활성화

#### [W-03](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-03%5D%20%EB%B6%88%ED%95%84%EC%9A%94%ED%95%9C%20%EA%B3%84%EC%A0%95%20%EC%A0%9C%EA%B1%B0.md) `상` 불필요한 계정 제거

#### [W-04](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-04%5D%20%EA%B3%84%EC%A0%95%20%EC%9E%A0%EA%B8%88%20%EC%9E%84%EA%B3%84%EA%B0%92%20%EC%84%A4%EC%A0%95.md) `상` 계정 잠금 임계값 설정

#### [W-05](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-05%5D%20%ED%95%B4%EB%8F%85%20%EA%B0%80%EB%8A%A5%ED%95%9C%20%EC%95%94%ED%98%B8%ED%99%94%EB%A1%9C%20%EC%95%94%ED%98%B8%20%EC%A0%80%EC%9E%A5%20%ED%95%B4%EC%A0%9C.md) `상` 해독 가능한 암호화를 사용하여 암호 저장 해제

#### [W-06](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-06%5D%20%EA%B4%80%EB%A6%AC%EC%9E%90%20%EA%B7%B8%EB%A3%B9%EC%97%90%20%EC%B5%9C%EC%86%8C%ED%95%9C%EC%9D%98%20%EC%82%AC%EC%9A%A9%EC%9E%90%20%ED%8F%AC%ED%95%A8.md) `상` 관리자 그룹에 최소한의 사용자 포함

#### [W-07](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-07%5D%20%EA%B3%B5%EC%9C%A0%20%EA%B6%8C%ED%95%9C%20%EB%B0%8F%20%EC%82%AC%EC%9A%A9%EC%9E%90%20%EA%B7%B8%EB%A3%B9%20%EC%84%A4%EC%A0%95.md) `상` 공유 권한 및 사용자 그룹 설정

#### `W-08` `상` 하드디스크 기본 공유 제거

#### `W-09` `상` 불필요한 서비스 제거

#### [W-10](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-10%5D%20IIS%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EA%B5%AC%EB%8F%99%20%EC%A0%90%EA%B2%80.md) `상` IIS 서비스 구동 점검

#### [W-11](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-11%5D%20IIS%20%EB%94%94%EB%A0%89%ED%86%A0%EB%A6%AC%20%EB%A6%AC%EC%8A%A4%ED%8C%85%20%EC%A0%9C%EA%B1%B0.md) `상` IIS 디렉토리 리스팅 제거

#### `W-12` `상` IIS CGI 실행 제한

#### [W-13](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-13%5D%20IIS%20%EC%83%81%EC%9C%84%20%EB%94%94%EB%A0%89%ED%86%A0%EB%A6%AC%20%EC%A0%91%EA%B7%BC%20%EA%B8%88%EC%A7%80.md) `상` IIS 상위 디렉토리 접근 금지

#### `W-14` `상` IIS 불필요한 파일 제거

#### `W-15` `상` IIS 웹프로세스 권한 제한

#### [W-16](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-16%5D%20IIS%20%EB%A7%81%ED%81%AC%20%EC%82%AC%EC%9A%A9%20%EA%B8%88%EC%A7%80.md) `상` IIS 링크 사용 금지

#### `W-17` `상` IIS 파일 업로드 및 다운로드 제한

#### `W-18` `상` IIS DB 연결 취약점 점검

#### `W-19` `상` IIS 가상 디렉토리 삭제

#### `W-20` `상` IIS 데이터파일 ACL 적용

#### `W-21` `상` IIS 미사용 스크립트 매핑 제거

#### `W-22` `상` IIS Exec 명령어 쉘 호출 진단

#### [W-23](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-23%5D%20IIS%20WebDAV%20%EB%B9%84%ED%99%9C%EC%84%B1%ED%99%94.md) `상` IIS WebDAV 비활성화

#### [W-24](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-24%5D%20NetBIOS%20%EB%B0%94%EC%9D%B8%EB%94%A9%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EA%B5%AC%EB%8F%99%20%EC%A0%90%EA%B2%80.md) `상` NetBIOS 바인딩 서비스 구동 점검

#### `W-25` `상` FTP 서비스 구동 점검

#### `W-26` `상` FTP 디렉토리 접근 권한 설정

#### `W-27` `상` Anonymous FTP 금지

#### `W-28` `상` FTP 접근 제어 설정

#### `W-29` `상` DNS Zone Transfer 설정

#### `W-30` `상` RDS(Remonte Data Services) 제거

#### [W-31](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-31%5D%20%EC%B5%9C%EC%8B%A0%20%EC%84%9C%EB%B9%84%EC%8A%A4%ED%8C%A9%20%EC%A0%81%EC%9A%A9.md) `상` 최신 서비스팩 적용

#### [W-32](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-32%5D%20%EC%B5%9C%EC%8B%A0%20HOT%20FIX%20%EC%A0%81%EC%9A%A9.md) `상` 최신 HOT FIX 적용

#### [W-33](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-33%5D%20%EB%B0%B1%EC%8B%A0%20%ED%94%84%EB%A1%9C%EA%B7%B8%EB%9E%A8%20%EC%97%85%EB%8D%B0%EC%9D%B4%ED%8A%B8.md) `상` 백신 프로그램 업데이트

#### `W-34` `상` 로그의 정기적 검토 및 보고

#### [W-35](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-35%5D%20%EC%9B%90%EA%B2%A9%EC%9C%BC%EB%A1%9C%20%EC%95%A1%EC%84%B8%EC%8A%A4%ED%95%A0%20%EC%88%98%20%EC%9E%88%EB%8A%94%20%EB%A0%88%EC%A7%80%EC%8A%A4%ED%8A%B8%EB%A6%AC%20%EA%B2%BD%EB%A1%9C.md) `상` 원격으로 액세스 할 수 있는 레지스트리 경로

#### [W-36](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-36%5D%20%EB%B0%B1%EC%8B%A0%20%ED%94%84%EB%A1%9C%EA%B7%B8%EB%9E%A8%20%EC%84%A4%EC%B9%98.md) `상` 백신 프로그램 설치

#### [W-37](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-37%5D%20SAM%20%ED%8C%8C%EC%9D%BC%20%EC%A0%91%EA%B7%BC%20%ED%86%B5%EC%A0%9C%20%EC%84%A4%EC%A0%95.md) `상` SAM 파일 접근 통제 설정

#### [W-38](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-38%5D%20%ED%99%94%EB%A9%B4%20%EB%B3%B4%ED%98%B8%EA%B8%B0%20%EC%84%A4%EC%A0%95.md)  `상` 화면보호기 설정

#### [W-39](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-39%5D%20%EB%A1%9C%EA%B7%B8%EC%98%A8%20%ED%95%98%EC%A7%80%20%EC%95%8A%EA%B3%A0%20%EC%8B%9C%EC%8A%A4%ED%85%9C%20%EC%A2%85%EB%A3%8C%20%ED%97%88%EC%9A%A9%20%ED%95%B4%EC%A0%9C.md) `상` 로그온 하지 않고 시스템 종료 허용 해제

#### `W-40` `상` 원격 시스템에서 강제로 시스템 종료

#### [W-41](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-41%5D%20%EB%B3%B4%EC%95%88%20%EA%B0%90%EC%82%AC%EB%A5%BC%20%EB%A1%9C%EA%B7%B8%ED%95%A0%20%EC%88%98%20%EC%97%86%EB%8A%94%20%EA%B2%BD%EC%9A%B0%20%EC%A6%89%EC%8B%9C%20%EC%8B%9C%EC%8A%A4%ED%85%9C%20%EC%A2%85%EB%A3%8C%20%ED%95%B4%EC%A0%9C.md) `상` 보안감사를 로그할 수 없는 경우 즉시 시스템 종료 해제

#### [W-42](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-42%5D%20SAM%20%EA%B3%84%EC%A0%95%EA%B3%BC%20%EA%B3%B5%EC%9C%A0%EC%9D%98%20%EC%9D%B5%EB%AA%85%20%EC%97%B4%EA%B1%B0%20%ED%97%88%EC%9A%A9%20%EC%95%88%20%ED%95%A8.md) `상` SAM 계정과 공유의 익명 열거 허용 안함


#### `W-43` `상` Autologon 기능 제어

#### `W-44` `상` 이동식 미디어 포맷 및 꺼내기 허용

#### [W-45](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-45%5D%20%EB%94%94%EC%8A%A4%ED%81%AC%20%EB%B3%BC%EB%A5%A8%20%EC%95%94%ED%98%B8%ED%99%94%20%EC%84%A4%EC%A0%95.md) `상` 디스크 볼륨 암호화 설정

#### `W-46` `중` Everyone 사용권한을 익명 사용자에 적용 해제

#### [W-47](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-47%5D%20%EA%B3%84%EC%A0%95%20%EC%9E%A0%EA%B8%88%20%EA%B8%B0%EA%B0%84%20%EC%84%A4%EC%A0%95.md) `중` 계정 잠금 기간 설정

#### `W-48` `중` 패스워드 복잡성 설정

#### [W-49](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-49%5D%20%ED%8C%A8%EC%8A%A4%EC%9B%8C%EB%93%9C%20%EC%B5%9C%EC%86%8C%20%EC%95%94%ED%98%B8%20%EA%B8%B8%EC%9D%B4.md) `중` 패스워드 최소 암호 길이

#### [W-50](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-50%5D%20%ED%8C%A8%EC%8A%A4%EC%9B%8C%EB%93%9C%20%EC%B5%9C%EB%8C%80%20%EC%82%AC%EC%9A%A9%20%EA%B8%B0%EA%B0%84.md) `중` 패스워드 최대 사용 기간

#### [W-51](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-51%5D%20%ED%8C%A8%EC%8A%A4%EC%9B%8C%EB%93%9C%20%EC%B5%9C%EC%86%8C%20%EC%82%AC%EC%9A%A9%20%EA%B8%B0%EA%B0%84.md) `중` 패스워드 최소 사용 기간

#### [W-52](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-52%5D%20%EB%A7%88%EC%A7%80%EB%A7%89%20%EC%82%AC%EC%9A%A9%EC%9E%90%20%EC%9D%B4%EB%A6%84%20%ED%91%9C%EC%8B%9C%20%EC%95%88%20%ED%95%A8.md) `중` 마지막 사용자 이름 표시 안함

#### [W-53](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-53%5D%20%EB%A1%9C%EC%BB%AC%20%EB%A1%9C%EA%B7%B8%EC%98%A8%20%ED%97%88%EC%9A%A9.md) `중` 로컬 로그온 허용

#### [W-54](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-54%5D%20%EC%9D%B5%EB%AA%85%20SID%20%EC%9D%B4%EB%A6%84%20%EB%B3%80%ED%99%98%20%ED%97%88%EC%9A%A9%20%ED%95%B4%EC%A0%9C.md) `중` 익명 SID/이름 변환 허용 해제

#### [W-55](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-55%5D%20%EC%B5%9C%EA%B7%BC%20%EC%95%94%ED%98%B8%20%EA%B8%B0%EC%96%B5.md) `중` 최근 암호 기억

#### [W-56](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-56%5D%20%EC%BD%98%EC%86%94%20%EB%A1%9C%EA%B7%B8%EC%98%A8%20%EC%8B%9C%20%EB%A1%9C%EC%BB%AC%20%EA%B3%84%EC%A0%95%EC%97%90%EC%84%9C%20%EB%B9%88%20%EC%95%94%ED%98%B8%20%EC%82%AC%EC%9A%A9%20%EC%A0%9C%ED%95%9C.md) `중` 콘솔 로그온 시 로컬 계정에서 빈 암호 사용 제한

#### [W-57](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-57%5D%20%EC%9B%90%EA%B2%A9%20%ED%84%B0%EB%AF%B8%EB%84%90%20%EC%A0%91%EC%86%8D%20%EA%B0%80%EB%8A%A5%ED%95%9C%20%EC%82%AC%EC%9A%A9%EC%9E%90%20%EA%B7%B8%EB%A3%B9%20%EC%A0%9C%ED%95%9C.md) `중` 원격터미널 접속 가능한 사용자 그룹 제한

#### `W-58` `중` 터미널 서비스 암호화 수준 설정

#### `W-59` `중` IIS 웹 서비스 정보 숨김

#### `W-60` `중` SNMP 서비스 구동 점검

#### `W-61` `중` SNMP 서비스 커뮤니티스트링의 복잡성 설정

#### `W-62` `중` SNMP Access control 설정

#### `W-63` `중` DNS 서비스 구동 점검

#### `W-64` `하` HTTP/FTP/SMTP 배너 차단

#### [W-65](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-65%5D%20Telnet%20%EB%B3%B4%EC%95%88%20%EC%84%A4%EC%A0%95.md) `중` Telnet 보안 설정

#### `W-66` `중` 불필요한 ODBC/OLE-DB 데이터소스와 드라이브 제거

#### [W-67](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-67%5D%20%EC%9B%90%EA%B2%A9%20%ED%84%B0%EB%AF%B8%EB%84%90%20%EC%A0%91%EC%86%8D%20%ED%83%80%EC%9E%84%EC%95%84%EC%9B%83%20%EC%84%A4%EC%A0%95.md) `중` 원격터미널 접속 타임아웃 설정

#### [W-68](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-68%5D%20%EC%98%88%EC%95%BD%EB%90%9C%20%EC%9E%91%EC%97%85%EC%97%90%20%EC%9D%98%EC%8B%AC%EC%8A%A4%EB%9F%AC%EC%9A%B4%20%EB%AA%85%EB%A0%B9%EC%9D%B4%20%EB%93%B1%EB%A1%9D%EB%90%98%EC%96%B4%20%EC%9E%88%EB%8A%94%EC%A7%80%20%EC%A0%90%EA%B2%80.md) `중` 예약된 작업에 의심스러운 명령이 등록되어 있는지 점검

#### [W-69](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-69%5D%20%EC%A0%95%EC%B1%85%EC%97%90%20%EB%94%B0%EB%A5%B8%20%EC%8B%9C%EC%8A%A4%ED%85%9C%20%EB%A1%9C%EA%B9%85%20%EC%84%A4%EC%A0%95.md) `중` 정책에 따른 시스템 로깅설정

#### [W-70](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-70%5D%20%EC%9D%B4%EB%B2%A4%ED%8A%B8%20%EB%A1%9C%EA%B7%B8%20%EA%B4%80%EB%A6%AC%20%EC%84%A4%EC%A0%95.md) `하` 이벤트 로그 관리 설정

#### [W-71](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-71%5D%20%EC%9B%90%EA%B2%A9%EC%97%90%EC%84%9C%20%EC%9D%B4%EB%B2%A4%ED%8A%B8%20%EB%A1%9C%EA%B7%B8%20%ED%8C%8C%EC%9D%BC%20%EC%A0%91%EA%B7%BC%20%EC%B0%A8%EB%8B%A8.md) `중` 원격에서 이벤트 로그파일 접근 차단


#### `W-72` `중` Dos 공격 방어 레지스트리 설정

#### `W-73` `중` 사용자가 프린터 드라이버를 설치할 수 없게 함

#### [W-74](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-74%5D%20%EC%84%B8%EC%85%98%20%EC%97%B0%EA%B2%B0%EC%9D%84%20%EC%A4%91%EB%8B%A8%ED%95%98%EA%B8%B0%20%EC%A0%84%EC%97%90%20%ED%95%84%EC%9A%94%ED%95%9C%20%EC%9C%A0%ED%9C%B4%20%EC%8B%9C%EA%B0%84.md) `중` 세션 연결을 중단하기 전에 필요한 유휴시간

#### [W-75](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-75%5D%20%EA%B2%BD%EA%B3%A0%20%EB%A9%94%EC%8B%9C%EC%A7%80%20%EC%84%A4%EC%A0%95.md) `하` 경고 메시지 설정

#### `W-76` `중` 사용자별 홈 디렉토리 권한 설정

#### [W-77](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-77%5D%20LAN%20Manager%20%EC%9D%B8%EC%A6%9D%20%EC%88%98%EC%A4%80.md) `중` LAN Manager 인증 수준

#### `W-78` `중` 보안 채널 데이터 디지털 암호화 또는 서명

#### `W-79` `중` 파일 및 디렉토리 보호

#### `W-80` `중` 컴퓨터 계정 암호 최대 사용 기간

#### [W-81](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-81%5D%20%EC%8B%9C%EC%9E%91%20%ED%94%84%EB%A1%9C%EA%B7%B8%EB%9E%A8%20%EB%AA%A9%EB%A1%9D%20%EB%B6%84%EC%84%9D.md) `중` 시작 프로그램 목록 분석

#### [W-82](https://github.com/qubitsec/VAS/blob/patch-1/windows/new_server/%5BW-82%5D%20Windows%20%EC%9D%B8%EC%A6%9D%20%EB%AA%A8%EB%93%9C%20%EC%82%AC%EC%9A%A9.md) `중` Windows 인증 모드 사용
