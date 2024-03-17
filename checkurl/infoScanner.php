<?php

function getScanner($url) {
    // cURL 세션 초기화
    $ch = curl_init($url);
    
    // cURL 옵션 설정: 반환된 데이터를 변수에 저장
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    // 헤더를 포함한 전체 응답을 받기 위해 TRUE 로 설정
    curl_setopt($ch, CURLOPT_HEADER, true);
    // 리다이렉션을 따르도록 설정
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);

    // 응답 시간 측정을 위한 시작 시간
    $start = microtime(true);
    
    // 첫 번째 기능: 응답 크기 및 시간 측정
    $response = curl_exec($ch);
    $end = microtime(true); // 응답 시간 측정을 위한 종료 시간
    
    $responseSize = strlen($response); // 바이트 단위
    $responseTime = ($end - $start) * 1000; // 응답 시간 (밀리초 단위)
    
    // 두 번째 기능: 상태 코드 확인
    $httpStatusCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    
    // cURL 세션 종료
    curl_close($ch);
    
    // 상태 코드에 따른 메시지 반환
    if ($httpStatusCode == 200) {
        $statusMessage = "OK";
    } else {
        $statusMessage = "WARNING ($httpStatusCode)";
    }
    
    // 결과 출력
    echo "Web Server Status: " . $statusMessage. "\n";
    echo "Response Size: " . $responseSize . " bytes\n";
    echo "Response Time: " . number_format($responseTime, 2) . " ms\n";
}

// 웹 사이트의 응답 크기, 응답 시간, 상태 확인 예제
$url = "https://www.plura.io"; // 여기에 확인하고 싶은 웹 사이트 주소를 입력하세요.
getScanner($url);

?>
