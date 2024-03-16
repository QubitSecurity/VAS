<?php

// 도메인과 포트 설정
$domain = 'www.plura.io';
$port = 443;

// openssl s_client 명령어 실행
$command = "openssl s_client -connect $domain:$port -servername $domain < /dev/null 2> /dev/null | openssl x509 -text -noout";
$output = shell_exec($command);

// 필요한 정보를 추출하기 위한 정규 표현식 패턴들 정의
$patterns = [
    '/Version: (\d+ \(0x[0-9a-f]+\))/i',
    '/Signature Algorithm: (\w+With\w+)/i',
    '/Issuer:.*?C = (.*?),.*?O = (.*?),.*?CN = (.*?)\n/s',
    '/Validity\n\s+Not Before: (.*?)\n\s+Not After : (.*?)\n/s',
    '/Subject: (.*?)\n/s',
    '/Public Key Algorithm: (\w+).*?Public-Key: \((\d+ bit)\)/s',
    '/Signature Algorithm: (\w+With\w+)\n\s+([0-9a-f:\s]+)/is'
];

// 결과를 저장할 배열 초기화
$results = [];

// 정규 표현식을 사용하여 필요한 정보 추출
foreach ($patterns as $pattern) {
    if (preg_match($pattern, $output, $matches)) {
        array_shift($matches); // 첫 번째 요소(전체 매치) 제거
        $results[] = $matches;
    }
}

// 추출한 정보를 기반으로 축약된 형태의 인증서 정보 출력
echo "Certificate:\n";
echo "    Data:\n";
echo "        Version: " . $results[0][0] . "\n";
echo "        Signature Algorithm: " . $results[1][0] . "\n";
echo "        Issuer:\n";
echo "            C = " . $results[2][0] . ",\n";
echo "            O = " . $results[2][1] . ",\n";
echo "            CN = " . $results[2][2] . "\n";
echo "        Validity:\n";
echo "            Not Before: " . $results[3][0] . "\n";
echo "            Not After : " . $results[3][1] . "\n";
echo "        Subject: " . $results[4][0] . "\n";
echo "        Subject Public Key Info:\n";
echo "            Public Key Algorithm: " . $results[5][0] . "\n";
echo "                Public-Key: (" . $results[5][1] . ")\n";
echo "    Signature Algorithm: " . $results[6][0] . "\n";
?>
