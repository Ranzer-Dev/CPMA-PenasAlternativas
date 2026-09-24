@echo off
echo Iniciando Servico Biometrico Facial CPMA na porta 8001...
set PYTHONPATH=cpma-facial-service
python cpma-facial-service/app/main.py
