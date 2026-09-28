#!/usr/bin/env bash
set -e

echo "=========================================================="
echo "   INICIANDO CPMA - TOTEM DE PRESENCA FACIAL (KIOSK)      "
echo "=========================================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

SERVER_IP=""

echo "[1/3] Verificando conexao local em localhost:8080..."
if curl -s -f -m 1 "http://localhost:8080/api/v1/totem/status" >/dev/null 2>&1; then
    SERVER_IP="localhost"
    echo "Servidor encontrado localmente!"
fi

if [ -z "$SERVER_IP" ]; then
    CACHE_FILE="$ROOT_DIR/cpma-server-ip.txt"
    if [ -f "$CACHE_FILE" ]; then
        CACHED_IP=$(tr -d ' \r\n' < "$CACHE_FILE")
        if [ -n "$CACHED_IP" ]; then
            echo "[2/3] Testando servidor em cache ($CACHED_IP)..."
            if curl -s -f -m 2 "http://${CACHED_IP}:8080/api/v1/totem/status" >/dev/null 2>&1; then
                SERVER_IP="$CACHED_IP"
                echo "Servidor validado em $SERVER_IP!"
            fi
        fi
    fi
fi

if [ -z "$SERVER_IP" ]; then
    echo "[2/3] Realizando auto-descoberta na rede local..."
    LOCAL_IP=""
    if command -v ip >/dev/null 2>&1; then
        LOCAL_IP=$(ip route get 1.1.1.1 2>/dev/null | awk '{print $7}' || true)
    fi
    if [ -z "$LOCAL_IP" ] && command -v ifconfig >/dev/null 2>&1; then
        LOCAL_IP=$(ifconfig 2>/dev/null | grep -E "inet (192\.168|10\.)" | awk '{print $2}' | head -n 1 || true)
    fi

    if [ -n "$LOCAL_IP" ]; then
        SUBNET=$(echo "$LOCAL_IP" | cut -d'.' -f1-3)
        echo "Varrendo sub-rede $SUBNET.1-254 na porta 8080..."
        for i in $(seq 1 254); do
            (
                if curl -s -f -m 1 "http://${SUBNET}.${i}:8080/api/v1/totem/status" >/dev/null 2>&1; then
                    echo "${SUBNET}.${i}" > "$ROOT_DIR/.discovered_ip"
                fi
            ) &
        done
        wait
        if [ -f "$ROOT_DIR/.discovered_ip" ]; then
            SERVER_IP=$(cat "$ROOT_DIR/.discovered_ip" | head -n 1)
            rm -f "$ROOT_DIR/.discovered_ip"
            echo "Servidor detectado automaticamente em $SERVER_IP!"
        fi
    fi
fi

while [ -z "$SERVER_IP" ]; do
    echo ""
    echo "Nao foi possivel detectar o servidor automaticamente."
    read -r -p "Digite o IP do Computador A (ex: 192.168.0.3): " DIGITADO
    DIGITADO=$(echo "$DIGITADO" | tr -d ' \r\n')
    if [ -n "$DIGITADO" ]; then
        echo "Validando conexao em http://${DIGITADO}:8080/api/v1/totem/status..."
        if curl -s -f -m 3 "http://${DIGITADO}:8080/api/v1/totem/status" >/dev/null 2>&1; then
            SERVER_IP="$DIGITADO"
            echo "Conexao estabelecida com sucesso com o servidor!"
        else
            echo "FALHA: Nao foi possivel conectar ao servidor em $DIGITADO:8080."
            echo "Verifique se digitou o IP correto (ex: 192.168.x.x) e se o Computador A esta ativo na rede."
        fi
    fi
done

echo "$SERVER_IP" > "$ROOT_DIR/cpma-server-ip.txt"

echo ""
echo "=========================================================="
echo "   CONECTADO AO SERVIDOR CPMA EM: $SERVER_IP:8080         "
echo "   MODO KIOSK EM TELA CHEIA ATIVADO                       "
echo "=========================================================="
echo ""

export CPMA_API_URL="http://${SERVER_IP}:8080/api/v1"
export CPMA_KIOSK="true"
mvn javafx:run -f "$ROOT_DIR/cpma-desktop/pom.xml"
