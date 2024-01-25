# 开发文档

## 版本控制

统一修改项目版本号，运行maven命令:

```shell
mvn versions:set -DnewVersion={major.minor.patch}
```

## 构建代码

构建环境：

```
Maven: apache-maven-3.6.3
Java version: 1.8.0_312
Python: 3.10
```

项目根目录执行代码构建语句：

```shell
mvn clean install -DskipTests=true && mvn package -DskipTests=true
```

在目录`client/target/migrationx`会生成migrationx的整体的可执行程序包目录结构：

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
