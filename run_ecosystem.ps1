$OutputEncoding = [Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "       SISTEMA CPMA - COORDENADORIA DE PENAS ALTERNATIVAS (SP)          " -ForegroundColor Cyan
Write-Host "   Inicializador Completo (Facial + Backend + Delegado + Totem)         " -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1/5] Liberando portas ocupadas (8001 e 8080)..." -ForegroundColor Yellow
$portas = @(8001, 8080)
foreach ($porta in $portas) {
    try {
        $conns = Get-NetTCPConnection -LocalPort $porta -State Listen -ErrorAction SilentlyContinue
        if ($conns) {
            foreach ($c in $conns) {
                Write-Host "  -> Encerrando processo na porta $porta (PID: $($c.OwningProcess))..." -ForegroundColor DarkYellow
                Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue
            }
        }
    } catch {
    }
}
Start-Sleep -Seconds 1
Write-Host "  [OK] Portas 8001 e 8080 disponiveis." -ForegroundColor Green
Write-Host ""

Write-Host "[2/5] Compilando Frontends Electron (Delegado e Totem)..." -ForegroundColor Yellow
Push-Location "cpma-desktop-delegado"
if (-not (Test-Path "node_modules")) { npm install }
npm run build
Pop-Location

Push-Location "cpma-desktop-totem"
if (-not (Test-Path "node_modules")) { npm install }
npm run build
Pop-Location
Write-Host "  [OK] Frontends cpma-desktop-delegado e cpma-desktop-totem compilados." -ForegroundColor Green
Write-Host ""

Write-Host "[3/5] Iniciando Servico Biometrico Facial (Python: 127.0.0.1:8001)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k run_facial_service.bat" -WindowStyle Minimized

$facialOnline = $false
$tentativas = 0
while (-not $facialOnline -and $tentativas -lt 15) {
    Start-Sleep -Seconds 1
    $tentativas++
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $client.Connect("127.0.0.1", 8001)
        if ($client.Connected) {
            $facialOnline = $true
            $client.Close()
        }
    } catch {
        Write-Host "  ... aguardando inicializacao facial ($tentativas/15s)" -ForegroundColor DarkGray
    }
}

if ($facialOnline) {
    Write-Host "  [OK] Servico Biometrico Facial ONLINE em 127.0.0.1:8001!" -ForegroundColor Green
} else {
    Write-Host "  [AVISO] Servico facial ainda inicializando em segundo plano..." -ForegroundColor DarkYellow
}
Write-Host ""

Write-Host "[4/5] Iniciando Backend REST API (Spring Boot: 0.0.0.0:8080)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k run_backend.bat" -WindowStyle Normal

$backendOnline = $false
$tentativas = 0
while (-not $backendOnline -and $tentativas -lt 35) {
    Start-Sleep -Seconds 1
    $tentativas++
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $client.Connect("127.0.0.1", 8080)
        if ($client.Connected) {
            $backendOnline = $true
            $client.Close()
        }
    } catch {
        Write-Host "  ... aguardando inicializacao do backend ($tentativas/35s)" -ForegroundColor DarkGray
    }
}

if ($backendOnline) {
    Write-Host "  [OK] Backend REST API ONLINE na porta 8080!" -ForegroundColor Green
} else {
    Write-Host "  [AVISO] Backend ainda inicializando em segundo plano." -ForegroundColor DarkYellow
}
Write-Host ""

Write-Host "[5/5] Iniciando Painel do Delegado (Electron)..." -ForegroundColor Yellow
Start-Process powershell.exe -ArgumentList "-NoExit -Command `"Set-Location '$PSScriptRoot\cpma-desktop-delegado'; npm run electron`"" -WindowStyle Normal

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Green
Write-Host "       ECOSSISTEMA CPMA INICIADO COM SUCESSO!                          " -ForegroundColor Green
Write-Host "  - Biometria Facial : http://127.0.0.1:8001/docs (Local)              " -ForegroundColor Cyan
Write-Host "  - Backend REST API : http://0.0.0.0:8080/api/v1 (LAN + Local)        " -ForegroundColor Cyan
Write-Host "  - App Delegado     : Em execucao (cpma-desktop-delegado)             " -ForegroundColor Cyan
Write-Host "  - Para abrir Totem : Execute .\start_totem.bat                       " -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Green
Start-Sleep -Seconds 3
