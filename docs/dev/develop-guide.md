# Develop guide

## Version Controller

To increase the project version number consistently, run maven command below:

```shell
mvn versions:set -DnewVersion={major.minor.patch}
```

## Build

Build requirements:

```
Maven: apache-maven-3.6.3
Java version: 1.8.0_312
Python: 3.10
```

run maven command at root directory of repository：

```shell
mvn clean install -DskipTests=true && mvn package -DskipTests=true
```

the target directory `client/target/migrationx` will look like below：

```
.
├── bin
│   ├── common.py
│   ├── local.py
│   ├── migrationx.py
│   ├── reader.py
│   ├── transformer.py
│   └── writer.py
├── conf
│   ├── apps.json
│   ├── dataworks-transformer-config-emr-sample.json
│   ├── dataworks-transformer-config-hologres-sample.json
│   ├── dataworks-transformer-config-maxcompute-sample.json
│   ├── dataworks-transformer-config.json
│   ├── logback.xml
│   └── migrationx.json
├── lib
│   ├── common
│   ├── reader
│   ├── transformer
│   └── writer
├── logs
└── python
    ├── airflow-exporter
    └── airflow-exporter.tgz
```
