<?php

function getResponseSize($url) {
    // cURL 세션 초기화
    $ch = curl_init($url);
    
    // cURL 옵션 설정: 반환된 데이터를 변수에 저장
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    // 헤더를 포함한 전체 응답을 받기 위해 TRUE 로 설정
    curl_setopt($ch, CURLOPT_HEADER, true);
    // 리다이렉션을 따르도록 설정
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    
    // 요청 실행
    $response = curl_exec($ch);
    
    // 응답 크기 측정
    $responseSize = strlen($response); // 바이트 단위
    
    // cURL 세션 종료
    curl_close($ch);
    
    // 응답 크기 반환
    return $responseSize;
}

// 웹 사이트 응답 크기 확인 예제
$url = "https://www.plura.io"; // 여기에 확인하고 싶은 웹 사이트 주소를 입력하세요.
$responseSize = getResponseSize($url);
echo "Response Size: " . $responseSize . " bytes";

?>
