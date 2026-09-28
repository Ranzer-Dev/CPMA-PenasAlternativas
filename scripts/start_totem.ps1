$ErrorActionPreference = "Continue"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   INICIANDO CPMA - TOTEM DE PRESENCA FACIAL (KIOSK)      " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$root = Split-Path -Parent $PSScriptRoot
$serverIp = $null

Write-Host "[1/3] Verificando conexao local em localhost:8080..." -ForegroundColor Yellow
try {
    $respLocal = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/totem/status" -TimeoutSec 1 -ErrorAction Stop
    if ($respLocal) {
        $serverIp = "localhost"
        Write-Host "Servidor encontrado localmente!" -ForegroundColor Green
    }
} catch {}

if (-not $serverIp) {
    $ipFile = Join-Path $root "cpma-server-ip.txt"
    if (Test-Path $ipFile) {
        $cachedIp = (Get-Content $ipFile -Raw).Trim()
        if ($cachedIp) {
            Write-Host "[2/3] Testando servidor em cache ($cachedIp)..." -ForegroundColor Yellow
            try {
                $respCached = Invoke-RestMethod -Uri "http://${cachedIp}:8080/api/v1/totem/status" -TimeoutSec 2 -ErrorAction Stop
                if ($respCached) {
                    $serverIp = $cachedIp
                    Write-Host "Servidor validado em $serverIp!" -ForegroundColor Green
                }
            } catch {}
        }
    }
}

if (-not $serverIp) {
    Write-Host "[2/3] Realizando auto-descoberta na rede local..." -ForegroundColor Yellow
    $localIpv4 = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { 
        $_.IPAddress -like "192.168.*" -or $_.IPAddress -like "10.*" 
    } | Select-Object -First 1).IPAddress

    if ($localIpv4) {
        $subnetParts = $localIpv4.Split(".")
        $subnetPrefix = "$($subnetParts[0]).$($subnetParts[1]).$($subnetParts[2])"
        Write-Host "Varrendo sub-rede $subnetPrefix.1-254 na porta 8080..." -ForegroundColor DarkGray

        $ips = 1..254 | ForEach-Object { "$subnetPrefix.$_" }
        
        $jobs = foreach ($ip in $ips) {
            [System.Net.HttpWebRequest]$req = [System.Net.WebRequest]::Create("http://${ip}:8080/api/v1/totem/status")
            $req.Timeout = 400
            [PSCustomObject]@{
                IP = $ip
                Request = $req
                AsyncResult = $req.BeginGetResponse($null, $null)
            }
        }

        Start-Sleep -Milliseconds 600

        foreach ($j in $jobs) {
            if ($j.AsyncResult.IsCompleted) {
                try {
                    $response = $j.Request.EndGetResponse($j.AsyncResult)
                    if ($response.StatusCode -eq 200) {
                        $serverIp = $j.IP
                        $response.Close()
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
    Write-Host "Nao foi possivel detectar o servidor automaticamente." -ForegroundColor Red
    $serverIp = Read-Host "Digite o IP do Computador A (ex: 192.168.0.3)"
}

if ($serverIp) {
    Set-Content -Path (Join-Path $root "cpma-server-ip.txt") -Value $serverIp -Force
    Write-Host ""
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host "   CONECTADO AO SERVIDOR CPMA EM: $serverIp:8080          " -ForegroundColor Green
    Write-Host "   MODO KIOSK EM TELA CHEIA ATIVADO                       " -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Green
    Write-Host ""

    Set-Location (Join-Path $root "cpma-desktop")
    D:\apache-maven-3.9.9\bin\mvn.cmd javafx:run -Dcpma.api.url="http://${serverIp}:8080/api/v1" -Djavafx.args="--kiosk"
}
