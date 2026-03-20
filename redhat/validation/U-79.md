## [U-79] 웹 설정 파일 및 DB 연결 정보 접근 통제

### 개요

| 항목 | 내용 |
|------|------|
| 점검 코드 | U-79 |
| 항목명 | 웹 설정 파일 및 DB 연결 정보 접근 통제 |
| 위험도 | 상 (High) |
| 범주 | 서비스 관리 |

### 점검 목적

`.htaccess`, `.htpasswd`, `.env`, DB 설정 파일, 백업 파일 등이 웹을 통해 직접 접근 가능하면 DB 접속 정보, 패스워드 해시 등 민감 정보가 노출될 수 있습니다. 해당 파일 유형에 대한 접근을 웹 서버 레벨에서 차단해야 합니다.

### 점검 기준

| 구분 | 기준 | 설명 |
|------|------|------|
| 안전 | .ht*, .env, 백업 파일 등에 대한 웹 접근 차단 설정이 있는 경우 | 민감 파일 외부 노출 불가 |
| 취약 | 차단 설정이 없어 민감 파일이 웹으로 접근 가능한 경우 | DB 정보, 패스워드 등 즉시 노출 위험 |

## 베이스라인 기준

**안전(양호)**

Apache:
```apache
<FilesMatch "^\.ht">
    Require all denied
</FilesMatch>
<FilesMatch "\.(env|bak|inc|sql|old|dump)$">
    Require all denied
</FilesMatch>
```

Nginx:
```nginx
location ~ /\.ht { deny all; }
location ~ \.(env|bak|sql|inc|old)$ { deny all; }
```

**취약**

- 위 차단 설정 없음
- `.htaccess`에 `Satisfy Any` 등 우회 설정 존재

### 참고 문서

- KISA 주요정보통신기반시설 기술적 취약점 분석·평가 방법 상세가이드 (Linux/Unix)
