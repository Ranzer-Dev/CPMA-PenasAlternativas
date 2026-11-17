$ErrorActionPreference = "Continue"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   INICIANDO CPMA - PC DO DELEGADO (SERVIDOR + PAINEL)    " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$root = Split-Path -Parent $PSScriptRoot

$lanIp = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { 
    $_.IPAddress -like "192.168.*" -or $_.IPAddress -like "10.*" -or $_.IPAddress -like "172.*" -or $_.IPAddress -like "169.254.*"
} | Select-Object -First 1).IPAddress

if (-not $lanIp) {
    $lanIp = "127.0.0.1"
}

Set-Content -Path (Join-Path $root "cpma-server-ip.txt") -Value $lanIp -Force

# Tenta garantir regra de Firewall no Windows apenas para a porta 8080 (API REST) na rede local (Cabo),
# mantendo a porta do banco (5432) e facial (8001) estritamente fechadas para a rede externa.
try {
    $ruleExists = Get-NetFirewallRule -DisplayName "CPMA Backend API (LAN 8080)" -ErrorAction SilentlyContinue
    if (-not $ruleExists) {
        New-NetFirewallRule -DisplayName "CPMA Backend API (LAN 8080)" `
            -Direction Inbound -Protocol TCP -LocalPort 8080 `
            -Action Allow -Profile Private,Domain -ErrorAction SilentlyContinue | Out-Null
        Write-Host "[Firewall] Porta 8080 liberada para o cabo de rede (Banco 5432 permanece blindado em 127.0.0.1)." -ForegroundColor Green
    }
} catch {
    # Caso não esteja rodando como Administrador, segue normalmente
}

Write-Host "[1/4] Verificando Banco de Dados Local (127.0.0.1)..." -ForegroundColor Yellow
$pgCheck = Test-NetConnection -ComputerName "127.0.0.1" -Port 5432 -InformationLevel Quiet -WarningAction SilentlyContinue
if (-not $pgCheck) {
    Write-Host "  -> PostgreSQL nao detectado em 127.0.0.1:5432. Tentando iniciar container cpma-postgres (ou usando SQLite local)..." -ForegroundColor DarkYellow
    docker start cpma-postgres 2>$null | Out-Null
    Start-Sleep -Seconds 2
} else {
    Write-Host "  -> PostgreSQL ativo localmente em 127.0.0.1:5432 (porta isolada da LAN)." -ForegroundColor Green
}

Write-Host "[2/4] Iniciando Servico Biometrico Facial (127.0.0.1:8001)..." -ForegroundColor Yellow
$facialBat = Join-Path $root "run_facial_service.bat"
if (Test-Path $facialBat) {
    Start-Process "cmd.exe" -ArgumentList "/c `"$facialBat`"" -WindowStyle Minimized
}

Write-Host "[3/4] Iniciando Backend REST API (Spring Boot: 0.0.0.0:8080)..." -ForegroundColor Yellow
$backendBat = Join-Path $root "run_backend.bat"
if (Test-Path $backendBat) {
    Start-Process "cmd.exe" -ArgumentList "/c `"$backendBat`"" -WindowStyle Minimized
}

Write-Host "[4/4] Aguardando inicializacao da API em http://127.0.0.1:8080/api/v1/totem/status..." -ForegroundColor Yellow
$apiReady = $false
$tentativas = 0
while (-not $apiReady -and $tentativas -lt 35) {
    Start-Sleep -Seconds 1
    $tentativas++
    try {
        $resp = Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/v1/totem/status" -TimeoutSec 1 -ErrorAction SilentlyContinue
        if ($resp) {
            $apiReady = $true
        }
    } catch {}
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "   SERVIDOR CPMA PRONTO E EM OPERACAO!                    " -ForegroundColor Green
Write-Host "   IP DO PC DO DELEGADO (CABO LAN): $lanIp                " -ForegroundColor Green
Write-Host "   PORTA DA API PARA O TOTEM      : 8080                  " -ForegroundColor Green
Write-Host "   BANCO DE DADOS                 : Blindado (127.0.0.1)  " -ForegroundColor Green
Write-Host "                                                          " -ForegroundColor Green
Write-Host "   NO COMPUTADOR DO TOTEM (Maquina B via cabo de rede):   " -ForegroundColor White
Write-Host "   Execute: .\start_totem.bat (ou informe o IP $lanIp)    " -ForegroundColor White
Write-Host "==========================================================" -ForegroundColor Green
Write-Host ""

Write-Host "Abrindo Sistema do Delegado (cpma-desktop-delegado)..." -ForegroundColor Cyan
$delegadoDir = Join-Path $root "cpma-desktop-delegado"
Set-Location $delegadoDir

if (-not (Test-Path (Join-Path $delegadoDir "node_modules"))) {
    Write-Host "Instalando dependencias do cpma-desktop-delegado..." -ForegroundColor Yellow
    npm install
}

if (-not (Test-Path (Join-Path $delegadoDir "dist\index.html"))) {
    Write-Host "Compilando interface do cpma-desktop-delegado..." -ForegroundColor Yellow
    npm run build
}

$env:CPMA_API_URL = "http://127.0.0.1:8080/api/v1"
npm run electron
