$ErrorActionPreference = "Continue"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   INICIANDO CPMA - SERVIDOR PRINCIPAL & ADMINISTRATIVO   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$root = Split-Path -Parent $PSScriptRoot

$lanIp = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { 
    $_.IPAddress -like "192.168.*" -or $_.IPAddress -like "10.*" 
} | Select-Object -First 1).IPAddress

if (-not $lanIp) {
    $lanIp = "127.0.0.1"
}

Set-Content -Path (Join-Path $root "cpma-server-ip.txt") -Value $lanIp -Force

Write-Host "[1/4] Verificando Banco de Dados PostgreSQL..." -ForegroundColor Yellow
$pgCheck = Test-NetConnection -ComputerName "127.0.0.1" -Port 5432 -InformationLevel Quiet -WarningAction SilentlyContinue
if (-not $pgCheck) {
    Write-Host "Iniciando container Docker cpma-postgres..." -ForegroundColor Yellow
    docker start cpma-postgres 2>$null | Out-Null
    Start-Sleep -Seconds 2
}

Write-Host "[2/4] Iniciando Servico Biometrico Facial (Python: 8000)..." -ForegroundColor Yellow
$facialBat = Join-Path $root "run_facial_service.bat"
Start-Process "cmd.exe" -ArgumentList "/c `"$facialBat`"" -WindowStyle Minimized

Write-Host "[3/4] Iniciando Backend REST API (Spring Boot: 8080)..." -ForegroundColor Yellow
$backendBat = Join-Path $root "run_backend.bat"
Start-Process "cmd.exe" -ArgumentList "/c `"$backendBat`"" -WindowStyle Minimized

Write-Host "[4/4] Aguardando inicializacao da API..." -ForegroundColor Yellow
$apiReady = $false
$tentativas = 0
while (-not $apiReady -and $tentativas -lt 30) {
    Start-Sleep -Seconds 1
    $tentativas++
    try {
        $resp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/totem/status" -TimeoutSec 1 -ErrorAction SilentlyContinue
        if ($resp) {
            $apiReady = $true
        }
    } catch {}
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "   SERVIDOR CPMA PRONTO E EM OPERACAO!                    " -ForegroundColor Green
Write-Host "   IP DA MAQUINA NA REDE LOCAL: $lanIp                    " -ForegroundColor Green
Write-Host "   PORTA DE ACESSO: 8080                                  " -ForegroundColor Green
Write-Host "                                                          " -ForegroundColor Green
Write-Host "   NO COMPUTADOR DO TOTEM (Computador B):                 " -ForegroundColor White
Write-Host "   Execute apenas: .\start_totem.bat                      " -ForegroundColor White
Write-Host "==========================================================" -ForegroundColor Green
Write-Host ""

Write-Host "Abrindo Painel Administrativo Desktop..." -ForegroundColor Cyan
Set-Location (Join-Path $root "cpma-desktop")
D:\apache-maven-3.9.9\bin\mvn.cmd javafx:run
