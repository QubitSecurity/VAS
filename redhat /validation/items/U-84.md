## [U-84] 불필요한 HTTP Method(PUT, DELETE, TRACE 등) 차단

### 개요

| 항목 | 내용 |
|------|------|
| 점검 코드 | U-84 |
| 항목명 | 불필요한 HTTP Method 차단 |
| 위험도 | 상 (High) |
| 범주 | 서비스 관리 |

### 점검 목적

`PUT`, `DELETE`, `TRACE`, `CONNECT` 등의 HTTP 메서드는 일반 웹 서비스에서 필요하지 않습니다. 특히 `TRACE`는 XST(Cross-Site Tracing) 공격에 악용될 수 있으며, `PUT`/`DELETE`는 파일 시스템 직접 조작에 사용될 수 있습니다.

### 점검 기준

| 구분 | 기준 | 설명 |
|------|------|------|
| 안전 | GET/POST/HEAD 이외의 HTTP 메서드가 차단된 경우 | 불필요한 HTTP 메서드를 통한 공격 차단 |
| 취약 | PUT, DELETE, TRACE 등의 메서드가 허용된 경우 | XST, 파일 조작 공격 가능 |

## 베이스라인 기준

**안전(양호)**

Apache:
```apache
TraceEnable Off
<LimitExcept GET POST HEAD>
    Require all denied
</LimitExcept>
```

Nginx:
```nginx
if ($request_method !~ ^(GET|POST|HEAD)$) {
    return 405;
}
```

**취약**

- `TraceEnable On` 또는 미설정
- `LimitExcept` 설정 없음 (모든 메서드 허용)

### 참고 문서

- KISA 주요정보통신기반시설 기술적 취약점 분석·평가 방법 상세가이드 (Linux/Unix)
