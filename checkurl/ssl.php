<?php

$host = "www.plura.io"; // 여기에 SSL 인증서를 확인하고 싶은 호스트를 입력하세요.

// OpenSSL을 사용하여 인증서 만료일 가져오기
$certInfoCommand = "echo | openssl s_client -servername $host -connect $host:443 2>/dev/null | openssl x509 -noout -enddate";
$certInfo = shell_exec($certInfoCommand);

// 인증서 만료일 파싱
if (preg_match('/notAfter=(.*)/', $certInfo, $matches)) {
    $validTo = strtotime($matches[1]); // 만료일을 timestamp로 변환
    $today = time(); // 오늘 날짜를 timestamp로 변환
    $daysUntilExpiry = ceil(($validTo - $today) / (60 * 60 * 24)); // 남은 일수 계산, 올림하여 정수로 만듦
    
    // 남은 일수 표시
    echo "Remaining: " . $daysUntilExpiry . " day(s)";
} else {
    echo "Unable to retrieve the certificate's expiration date.";
}

?>
