#!/usr/bin/env bash
set -e

echo "=========================================================="
echo "   INICIANDO CPMA - SERVIDOR PRINCIPAL & ADMINISTRATIVO   "
echo "=========================================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

LAN_IP=""
if command -v ip >/dev/null 2>&1; then
    LAN_IP=$(ip route get 1.1.1.1 2>/dev/null | awk '{print $7}' || true)
fi

if [ -z "$LAN_IP" ] && command -v ifconfig >/dev/null 2>&1; then
    LAN_IP=$(ifconfig 2>/dev/null | grep -E "inet (192\.168|10\.)" | awk '{print $2}' | head -n 1 || true)
fi

if [ -z "$LAN_IP" ]; then
    LAN_IP="127.0.0.1"
fi

echo "$LAN_IP" > "$ROOT_DIR/cpma-server-ip.txt"

echo "[1/4] Verificando Banco de Dados PostgreSQL..."
PG_UP=false
if command -v nc >/dev/null 2>&1; then
    if nc -z 127.0.0.1 5432 2>/dev/null; then
        PG_UP=true
    fi
fi

if [ "$PG_UP" = false ] && command -v docker >/dev/null 2>&1; then
    echo "Iniciando container Docker cpma-postgres..."
    docker start cpma-postgres 2>/dev/null || true
    sleep 2
fi

echo "[2/4] Iniciando Servico Biometrico Facial (Python: 8000)..."
export PYTHONPATH="$ROOT_DIR/cpma-facial-service"
PYTHON_BIN="python3"
if ! command -v python3 >/dev/null 2>&1; then
    PYTHON_BIN="python"
fi

nohup "$PYTHON_BIN" "$ROOT_DIR/cpma-facial-service/app/main.py" > "$ROOT_DIR/facial_service.log" 2>&1 &

echo "[3/4] Iniciando Backend REST API (Spring Boot: 8080)..."
nohup mvn spring-boot:run -f "$ROOT_DIR/cpma-backend/pom.xml" > "$ROOT_DIR/backend.log" 2>&1 &

echo "[4/4] Aguardando inicializacao da API..."
API_READY=false
TENTATIVAS=0
while [ "$API_READY" = false ] && [ "$TENTATIVAS" -lt 35 ]; do
    sleep 1
    TENTATIVAS=$((TENTATIVAS + 1))
    if curl -s -f -m 1 "http://localhost:8080/api/v1/totem/status" >/dev/null 2>&1; then
        API_READY=true
    fi
done

echo ""
echo "=========================================================="
echo "   SERVIDOR CPMA PRONTO E EM OPERACAO!                    "
echo "   IP DA MAQUINA NA REDE LOCAL: $LAN_IP                   "
echo "   PORTA DE ACESSO: 8080                                  "
echo "                                                          "
echo "   NO COMPUTADOR DO TOTEM (Computador B):                 "
echo "   Execute apenas: ./start_totem.sh                       "
echo "=========================================================="
echo ""

echo "Abrindo Painel Administrativo Desktop..."
mvn javafx:run -f "$ROOT_DIR/cpma-desktop/pom.xml"
