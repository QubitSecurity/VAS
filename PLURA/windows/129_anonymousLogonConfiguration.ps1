# Set registry path (Local Security Policy)
$regPath = "HKLM:\SYSTEM\CurrentControlSet\Control\Lsa"

# Set the value of RestrictAnonymous to 2 (Completely block anonymous logon)
Set-ItemProperty -Path $regPath -Name "RestrictAnonymous" -Value 2

Write-Host "Anonymous logon has been blocked in NTLM authentication."
