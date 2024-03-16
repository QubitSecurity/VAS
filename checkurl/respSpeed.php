<?php

function getResponseTime($url) {
    // cURL 세션 초기화
    $ch = curl_init($url);
    
    // cURL 옵션 설정: 반환된 데이터를 변수에 저장
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    // 요청 실행 전의 시간
    $start = microtime(true);
    
    // 요청 실행
    curl_exec($ch);
    
    // 요청 완료 후의 시간
    $end = microtime(true);
    
    // cURL 세션 종료
    curl_close($ch);
    
    // 요청 실행 시간을 밀리초로 계산
    $responseTime = ($end - $start) * 1000; // 초 단위를 밀리초로 변환
    
    // 응답 시간 반환
    return $responseTime;
}

// 웹 사이트 응답 시간 확인 예제
$url = "https://www.plura.io"; // 여기에 확인하고 싶은 웹 사이트 주소를 입력하세요.
$responseTime = getResponseTime($url);
echo "Response Time: " . number_format($responseTime, 2) . " ms";

?>
