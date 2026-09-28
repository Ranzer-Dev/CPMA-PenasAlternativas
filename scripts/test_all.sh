#!/usr/bin/env bash
set -e

echo "========================================================"
echo "  CPMA - EXECUTANDO BATERIA COMPLETA DE TESTES (POSIX) "
echo "========================================================"
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "[1/3] Testando cpma-backend (Java Spring Boot 3 + MockMvc)..."
mvn test -f "$ROOT_DIR/cpma-backend/pom.xml"

echo ""
echo "[2/3] Testando cpma-facial-service (Python FastAPI + Biometria)..."
export PYTHONPATH="$ROOT_DIR/cpma-facial-service"
PYTHON_BIN="python3"
if ! command -v python3 >/dev/null 2>&1; then
    PYTHON_BIN="python"
fi

"$PYTHON_BIN" -m pytest -c "$ROOT_DIR/cpma-facial-service/pytest.ini" "$ROOT_DIR/cpma-facial-service/tests"

echo ""
echo "[3/3] Testando cpma-desktop (JavaFX + AtlantaFX + ApiClient)..."
mvn test -f "$ROOT_DIR/cpma-desktop/pom.xml"

echo ""
echo "========================================================"
echo "  TODOS OS TESTES FORAM APROVADOS COM SUCESSO! (100%)   "
echo "========================================================"
