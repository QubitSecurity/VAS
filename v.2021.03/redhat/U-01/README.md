### 부록 01. find 명령어

# Find 명령어 사용법

## 기본 사용법
1. `find . -name '*.html'`
   - 현재 디렉터리(`.`)에서 확장자가 `.html`로 끝나는 파일을 검색합니다. 특정 위치에서 검색하려면, 예를 들어 `/usr`, `find /usr –name '*.html'`과 같이 사용합니다.
   - `-name`은 파일 이름으로 검색하라는 조건입니다.

## 특정 타입 검색
2. `find . -type d`
   - 현재 디렉터리에서 디렉터리만 검색합니다.

3. `find . -group admin -type l`
   - 그룹이 `admin`이면서 심볼릭 링크만 조회합니다.

4. `find . -user icocoa -maxdepth 1 -type d`
   - 현재 디렉터리 내에서 소유자가 `icocoa`이며, 디렉터리인 것만을 검색합니다.

## 조합 검색
5. `find . -name '*.jpg' -o -name '*.html'`
   - `-o` 옵션은 OR 조건으로, 확장자가 `.jpg` 또는 `.html`인 파일을 검색합니다.

## 시간 기반 검색
6. `find . -atime -2`
   - 최근 2일 동안 액세스된 파일을 검색합니다.

7. `find . -atime +3`
   - 액세스한 지 3일이 지난 파일을 검색합니다.

8. `find . -mtime +7`
   - 7일 넘도록 변경되지 않은 파일을 검색합니다. (`m`: modification time)

9. `find . -mmin +30 -maxdepth 1 -type f`
   - 현재 디렉터리 내에서 변경된 지 30분이 지난 파일만을 검색합니다.

## 특수 검색
10. `find . -name '*.xml' -exec grep -l 'Version' {} \;`
    - 현재 디렉터리 내에서 `Version`이라는 단어가 포함된 `.xml` 확장자 파일을 검색합니다.

11. `find . \! -name "*.jpg"`
    - `.jpg`로 끝나지 않는 모든 파일을 검색합니다.

12. `find . -newermm test.txt`
    - `test.txt` 파일보다 더 최근에 수정된 파일을 검색합니다. (`-newermm`은 `-newer`와 동일)

13. `find . -size +100c`
    - 사이즈가 100바이트 이상인 파일을 검색합니다. (`c`: bytes)

<hr/>
