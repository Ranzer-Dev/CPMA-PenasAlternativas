#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

SERVER_IP="localhost"
CACHE_FILE="$ROOT_DIR/cpma-server-ip.txt"
if [ -f "$CACHE_FILE" ]; then
    CACHED_IP=$(tr -d ' \r\n' < "$CACHE_FILE")
    if [ -n "$CACHED_IP" ]; then
        SERVER_IP="$CACHED_IP"
    fi
fi

TOTEM_URL="http://${SERVER_IP}:8080/api/v1/totem/index.html"
KIOSK_DATA_DIR="/tmp/cpma_totem_kiosk_profile"
KIOSK_FLAGS="--kiosk --user-data-dir=${KIOSK_DATA_DIR} --no-first-run --no-default-browser-check --disable-infobars --disable-session-crashed-bubble --disable-features=Translate,FullscreenExitUI,EdgeSwipe --overscroll-history-navigation=0 --disable-pinch --noerrdialogs --hide-scrollbars --incognito ${TOTEM_URL}"

echo "=========================================================="
echo "   INICIANDO CPMA - TOTEM WEB KIOSK (NAVEGADOR)           "
echo "=========================================================="
echo "Acessando terminal em: $TOTEM_URL"
echo ""

if command -v chromium-browser >/dev/null 2>&1; then
    exec chromium-browser $KIOSK_FLAGS
elif command -v chromium >/dev/null 2>&1; then
    exec chromium $KIOSK_FLAGS
elif command -v google-chrome >/dev/null 2>&1; then
    exec google-chrome $KIOSK_FLAGS
elif command -v xdg-open >/dev/null 2>&1; then
    exec xdg-open "$TOTEM_URL"
elif command -v open >/dev/null 2>&1; then
    exec open "$TOTEM_URL"
else
    echo "Navegador compativel nao encontrado automaticamente."
    echo "Abra manualmente no navegador: $TOTEM_URL"
fi
