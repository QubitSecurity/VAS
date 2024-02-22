### 부록 01. find 명령어

* 1. `#find . -name '*.html'` :  . 은 현재 디렉터리에서 찾음, /usr와 같이 특정 위치에서 찾으려면 #find /usr –name '*.html’
- name은 파일 이름으로 찾으라는 조건으로 확장자가 .html 로 끝나는 파일만을 검색
* 2. `#find . -type d` : 디렉터리만 검색
* 3. `#find . -group admin -type l` : 그룹이 admin 이면서 심볼릭 링크만 조회

<hr/>
