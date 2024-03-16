<?php

function getDomainExpirationDate($domain) {
    $whoisServer = "whois.kr"; // .kr 도메인을 위한 WHOIS 서버
    $port = 43;
    
    $timeout = 10;
    $fp = @fsockopen($whoisServer, $port, $errno, $errstr, $timeout);
    
    if (!$fp) {
        return "Error: Could not connect to WHOIS server. $errstr ($errno)";
    } else {
        // 도메인 정보 요청
        fputs($fp, "$domain\r\n");
        
        // 응답 읽기
        $response = '';
        while (!feof($fp)) {
            $response .= fgets($fp, 128);
        }
        fclose($fp);
        
        // 만료 날짜 찾기
        $lines = explode("\n", $response);
        foreach ($lines as $line) {
            if (strpos($line, "Registry Expiry Date:") !== false) {
                // 만료 날짜 추출
                $expiryDate = trim(substr($line, strpos($line, ":") + 1));
                // 만료 날짜를 DateTime 객체로 변환
                $expiryDate = new DateTime($expiryDate);
                // 현재 날짜와 비교
                $today = new DateTime();
                $interval = $today->diff($expiryDate);
                // 남은 일수 반환
                return $interval->format('%a day(s)');
            }
        }
        
        return "Expiration date not found";
    }
}

// 도메인 만료 날짜 조회 예제
$domain = "plura.io";
$remainingDays = getDomainExpirationDate($domain);
echo "Remaining: " . $remainingDays;

?>
