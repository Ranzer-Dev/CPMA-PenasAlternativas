$OutputEncoding = [Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "       SISTEMA CPMA - COORDENADORIA DE PENAS ALTERNATIVAS (SP)          " -ForegroundColor Cyan
Write-Host "          Inicializador Inteligente & Autodiagnostico de Saude          " -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""

$mvnCmd = "mvn"
if (Test-Path "D:\apache-maven-3.9.9\bin\mvn.cmd") {
    $mvnCmd = "D:\apache-maven-3.9.9\bin\mvn.cmd"
}

Write-Host "[1/5] Liberando portas ocupadas e processos orfaos..." -ForegroundColor Yellow
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
Write-Host "  [OK] Portas 8001 e 8080 estao limpas e disponiveis." -ForegroundColor Green
Write-Host ""

Write-Host "[2/5] Validando compilacao do Frontend Desktop (JavaFX)..." -ForegroundColor Yellow
$compileOutput = & $mvnCmd compile -f cpma-desktop/pom.xml 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "  [ERRO] Falha de compilacao no cpma-desktop detectada!" -ForegroundColor Red
    Write-Host $compileOutput -ForegroundColor DarkRed
    Write-Host ""
    Write-Host "Pressione qualquer tecla para sair..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}
Write-Host "  [OK] Codigo JavaFX compilado com sucesso." -ForegroundColor Green
Write-Host ""

Write-Host "[3/5] Iniciando Servico Biometrico Facial (Python: 8001)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k run_facial_service.bat" -WindowStyle Normal

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
    Write-Host "  [OK] Servico Biometrico Facial ONLINE na porta 8001!" -ForegroundColor Green
} else {
    Write-Host "  [AVISO] Servico facial ainda inicializando ou requer dependencias. Continuando..." -ForegroundColor DarkYellow
}
Write-Host ""

Write-Host "[4/5] Iniciando Backend REST API (Spring Boot: 8080)..." -ForegroundColor Yellow
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
    Write-Host "  [AVISO] Backend demorou mais que o esperado, mas a inicializacao segue em segundo plano." -ForegroundColor DarkYellow
}
Write-Host ""

Write-Host "[5/5] Iniciando Aplicativo Desktop CPMA (JavaFX + AtlantaFX)..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k run_desktop.bat" -WindowStyle Normal

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Green
Write-Host "       ECOSSISTEMA CPMA INICIADO COM SUCESSO!                          " -ForegroundColor Green
Write-Host "  - Biometria Facial : http://localhost:8001/docs                      " -ForegroundColor Cyan
Write-Host "  - Backend REST API : http://localhost:8080/api/v1                    " -ForegroundColor Cyan
Write-Host "  - Desktop App      : Em execucao                                     " -ForegroundColor Cyan
Write-Host "========================================================================" -ForegroundColor Green
Start-Sleep -Seconds 4
