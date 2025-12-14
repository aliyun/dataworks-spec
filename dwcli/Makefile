.PHONY: install test clean build

install:
	pip install -e .

test:
	pytest tests/ -v --cov=dwcli --cov-report=term-missing

clean:
	rm -rf build/ dist/ *.egg-info .pytest_cache .coverage
	find . -type d -name __pycache__ -exec rm -rf {} +

build:
	python setup.py sdist bdist_wheel

lint:
	ruff check dwcli/
	black --check dwcli/

format:
	black dwcli/
	ruff check --fix dwcli/
