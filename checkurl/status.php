<?php

function checkWebServerStatus($url) {
    // cURL 세션 초기화
    $ch = curl_init($url);
    
    // cURL 옵션 설정
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HEADER, true);
    curl_setopt($ch, CURLOPT_NOBODY, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);
    
    // HTTP 요청 실행
    curl_exec($ch);
    // 응답 상태 코드 가져오기
    $httpStatusCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    // 상태 코드에 따른 메시지 반환
    if ($httpStatusCode == 200) {
        return "OK";
    } else {
        return "WARNING ($httpStatusCode)";
    }
}

// 웹 서버 상태 확인 예제
$url = "https://www.plura.io"; // 여기에 확인하고 싶은 URL을 입력하세요.
$statusMessage = checkWebServerStatus($url);
echo $statusMessage;

?>
