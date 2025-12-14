from setuptools import setup, find_packages

setup(
    name="dwcli",
    version="1.0.0",
    description="DataWorks CLI - Configuration-as-Code management tool for FlowSpec",
    author="Aliyun DataWorks",
    python_requires=">=3.8",
    packages=find_packages(where=".", exclude=["tests*"]),
    package_data={
        "dwcli": [
            "templates/*.template.json",
            "schemas/*.schema.json",
            "schemas/README.md",
        ],
    },
    include_package_data=True,
    install_requires=[
        "click>=8.1.0",
        "jsonpath-ng>=1.6.0",
        "jsonschema>=4.20.0",
        "jinja2>=3.1.0",
        "colorama>=0.4.6",
        "rich>=13.7.0",
        "pyyaml>=6.0",
    ],
    entry_points={
        "console_scripts": [
            "dwcli=dwcli.cli:main",
        ],
    },
    classifiers=[
        "Development Status :: 4 - Beta",
        "Intended Audience :: Developers",
        "Programming Language :: Python :: 3.10",
        "Programming Language :: Python :: 3.11",
        "Programming Language :: Python :: 3.12",
    ],
)