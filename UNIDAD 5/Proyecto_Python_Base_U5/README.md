# Unidad 5 CLN - Proyecto base

Ejecutar `pip install -r requirements.txt`, `pytest -v --cov=src --cov-fail-under=80` y `ruff check src tests`.

py -m venv .venv
.\.venv\Scripts\Activate.ps1
py -m pip install --upgrade pip
py -m pip install -r requirements.txt
py -m pytest -v
py -m pytest -v --cov=src --cov-report=term-missing 
py -m pytest -v --cov=src --cov-report=term-missing --cov-fail-under=80
ruff check src tests
py -m compileall -q src

**Importante:** El proyecto es un ejemplo de pedidos, no la solución de los 17 PIA. La regla de cancelación impide cancelar pedidos enviados incluso para administrador; esto resuelve explícitamente una ambigüedad del slide original. El pipeline realiza verificación de build (compileall), no despliegue productivo.
