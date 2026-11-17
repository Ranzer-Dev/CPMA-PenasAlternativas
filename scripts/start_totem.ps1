$ErrorActionPreference = "Continue"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   INICIANDO CPMA - TOTEM DE AUTOATENDIMENTO (MAQUINA B)  " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$root = Split-Path -Parent $PSScriptRoot
$serverIp = $null

Write-Host "[1/3] Verificando conexao local em 127.0.0.1:8080..." -ForegroundColor Yellow
try {
    $respLocal = Invoke-RestMethod -Uri "http://127.0.0.1:8080/api/v1/totem/status" -TimeoutSec 1 -ErrorAction Stop
    if ($respLocal) {
        $serverIp = "127.0.0.1"
        Write-Host "Servidor encontrado localmente (127.0.0.1)!" -ForegroundColor Green
    }
} catch {}

if (-not $serverIp) {
    $ipFile = Join-Path $root "cpma-server-ip.txt"
    if (Test-Path $ipFile) {
        $cachedIp = (Get-Content $ipFile -Raw).Trim()
        if ($cachedIp) {
            Write-Host "[2/3] Testando IP do PC do Delegado salvo ($cachedIp)..." -ForegroundColor Yellow
            try {
                $respCached = Invoke-RestMethod -Uri "http://${cachedIp}:8080/api/v1/totem/status" -TimeoutSec 2 -ErrorAction Stop
                if ($respCached) {
                    $serverIp = $cachedIp
                    Write-Host "Servidor validado via cabo de rede em $serverIp!" -ForegroundColor Green
                }
            } catch {}
        }
    }
}

if (-not $serverIp) {
    Write-Host "[2/3] Realizando auto-descoberta no cabo de rede (LAN)..." -ForegroundColor Yellow
    $localIpv4 = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { 
        $_.IPAddress -like "192.168.*" -or $_.IPAddress -like "10.*" -or $_.IPAddress -like "172.*"
    } | Select-Object -First 1).IPAddress

    if ($localIpv4) {
        $subnetParts = $localIpv4.Split(".")
        $subnetPrefix = "$($subnetParts[0]).$($subnetParts[1]).$($subnetParts[2])"
        Write-Host "Varrendo sub-rede $subnetPrefix.1-254 na porta 8080..." -ForegroundColor DarkGray

        $ips = 1..254 | ForEach-Object { "$subnetPrefix.$_" }
        
        $jobs = foreach ($ip in $ips) {
            [System.Net.HttpWebRequest]$req = [System.Net.WebRequest]::Create("http://${ip}:8080/api/v1/totem/status")
            $req.Timeout = 1000
            [PSCustomObject]@{
                IP = $ip
                Request = $req
                AsyncResult = $req.BeginGetResponse($null, $null)
            }
        }

        Start-Sleep -Milliseconds 1200

        foreach ($j in $jobs) {
            if ($j.AsyncResult.IsCompleted) {
                try {
                    $response = $j.Request.EndGetResponse($j.AsyncResult)
                    if ($response.StatusCode -eq 200) {
                        $serverIp = $j.IP
                        $response.Close()
                        Write-Host "PC do Delegado detectado automaticamente em $serverIp!" -ForegroundColor Green
                        break
                    }
                    $response.Close()
                } catch {}
            }
        }
    }
}

if (-not $serverIp) {
    Write-Host ""
    Write-Host "Nao foi possivel detectar o PC do Delegado automaticamente." -ForegroundColor Yellow
    $digitado = Read-Host "Digite o IP do PC do Delegado (ex: 192.168.0.10) ou pressione ENTER para abrir e configurar na tela do Totem"
    if ($digitado -and $digitado.Trim() -ne "") {
        $serverIp = $digitado.Trim()
    } else {
        $serverIp = "127.0.0.1"
    }
}

Set-Content -Path (Join-Path $root "cpma-server-ip.txt") -Value $serverIp -Force
Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "   TOTEM APONTANDO PARA O SERVIDOR: $serverIp:8080        " -ForegroundColor Green
Write-Host "   (Use Ctrl+Shift+C no Totem para alterar o IP na tela)  " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host ""

$totemDir = Join-Path $root "cpma-desktop-totem"
Set-Location $totemDir

if (-not (Test-Path (Join-Path $totemDir "node_modules"))) {
    Write-Host "Instalando dependencias do cpma-desktop-totem..." -ForegroundColor Yellow
    npm install
}

if (-not (Test-Path (Join-Path $totemDir "dist\index.html"))) {
    Write-Host "Compilando interface do cpma-desktop-totem..." -ForegroundColor Yellow
    npm run build
}

$env:CPMA_SERVER_IP = $serverIp
$env:CPMA_SERVER_PORT = "8080"
$env:CPMA_API_URL = "http://${serverIp}:8080/api/v1"
$env:CPMA_KIOSK = "true"
npm run electron
