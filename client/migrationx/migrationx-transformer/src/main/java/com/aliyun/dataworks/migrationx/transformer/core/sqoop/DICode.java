/*
 * Copyright (c) 2024, Alibaba Cloud;
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aliyun.dataworks.migrationx.transformer.core.sqoop;

import com.aliyun.dataworks.common.spec.domain.dw.types.CalcEngineType;
import com.aliyun.dataworks.migrationx.domain.dataworks.oozie.OozieConstants;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportRiskLevel;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DICode
 */
public class DICode {
    private static final Logger LOGGER = LoggerFactory.getLogger(DICode.class);
    private static final Pattern TARGET_DIR_PARTITION_PATTERN = Pattern.compile("([\\w-_]+=[\\w-_]+)");
    private static final String SQOOP_IMPORT = "import";
    private static final String SQOOP_EXPORT = "export";
    private static ThreadLocal<CalcEngineType> calcEngineType = ThreadLocal.withInitial(
        () -> CalcEngineType.ODPS);
    private static ThreadLocal<String> calcEngineDatasource = ThreadLocal.withInitial(() -> "odps_first");
    private static ThreadLocal<String> calcEngineDatasourceType = ThreadLocal.withInitial(() -> "odps");
    private String code;
    private String advice;
    private ReportRiskLevel riskLevel;
    private String description;
    private String message;
    private String exception;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public ReportRiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(ReportRiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public static DICode parseDiCode(Element e) {
        return parseDiCode(e, null, null, null);
    }

    public static DICode parseDiCode(String code, CalcEngineType engineType, String datasourceName,
        String datasourceType) {
        String diCodeTemplate = DIConfigTemplate.DI_CODE_TEMPLATE;
        calcEngineType.set(engineType);
        calcEngineDatasource.set(datasourceName);
        calcEngineDatasourceType.set(datasourceType);
        DIJsonProcessor diCodeJP = DIJsonProcessor.from(diCodeTemplate);
        DICode diCode = new DICode();
        diCode.setRiskLevel(ReportRiskLevel.OK);
        try {
            DICode.parseSqoop(diCode, diCodeJP, code);
        } catch (Exception ex) {
            diCode.setRiskLevel(ReportRiskLevel.ERROR);
            diCode.setMessage(ex.getMessage());
            diCode.setException(ExceptionUtils.getStackTrace(ex));
            diCode.setCode(null);
        }

        diCode.setCode(diCodeJP.toString());
        return diCode;
    }

    public static DICode parseDiCode(Element actionXml, CalcEngineType engineType, String datasourceName,
        String datasourceType) {
        String diCodeTemplate = DIConfigTemplate.DI_CODE_TEMPLATE;
        calcEngineType.set(engineType);
        calcEngineDatasource.set(datasourceName);
        calcEngineDatasourceType.set(datasourceType);
        DIJsonProcessor diCodeJP = DIJsonProcessor.from(diCodeTemplate);
        DICode diCode = new DICode();
        diCode.setRiskLevel(ReportRiskLevel.OK);
        try {
            DICode.parseSqoop(diCode, diCodeJP, actionXml);
        } catch (Exception ex) {
            diCode.setRiskLevel(ReportRiskLevel.ERROR);
            diCode.setMessage(ex.getMessage());
            diCode.setException(ExceptionUtils.getStackTrace(ex));
            diCode.setCode(null);
        }

        diCode.setCode(diCodeJP.toString());
        return diCode;
    }

    public static DICode parseDiCode(String sqoopCommands) {
        String diCodeTemplate = DIConfigTemplate.DI_CODE_TEMPLATE;

        DIJsonProcessor diCodeJP = DIJsonProcessor.from(diCodeTemplate);
        DICode diCode = new DICode();
        diCode.setRiskLevel(ReportRiskLevel.OK);
        try {
            DICode.parseSqoop(diCode, diCodeJP, sqoopCommands);
        } catch (Exception ex) {
            LOGGER.error("{}", ex);
            diCode.setRiskLevel(ReportRiskLevel.ERROR);
            diCode.setMessage(ex.getMessage());
            diCode.setException(ExceptionUtils.getStackTrace(ex));
            diCode.setCode(null);
        }

        diCode.setCode(diCodeJP.toString());
        return diCode;
    }

    private static void parseSqoop(DICode diCode, DIJsonProcessor diCodeJP, String sqoopCommands) {
        if (StringUtils.isBlank(sqoopCommands)) {
            diCode.setAdvice("ignore this sqoop node");
            diCode.setDescription("can not find sqoop config");
            diCode.setMessage("");
            diCode.setRiskLevel(ReportRiskLevel.WEEK_WARNINGS);
            diCode.setException(null);
        } else {
            DICode.doParseSqoop(diCode, diCodeJP, sqoopCommands);
        }
        return;
    }

    private static String[] parseSqoopArgs(String sqoopCommands) {
        AntCommandLine antCommandLine = new AntCommandLine(sqoopCommands);
        Iterator<AntCommandLine.Argument> itr = antCommandLine.iterator();
        List<String> args = new ArrayList<>();
        boolean propertiesMeet = false;
        while (itr.hasNext()) {
            AntCommandLine.Argument arg = itr.next();
            String[] parts = arg.getParts();
            if (parts == null) {
                continue;
            }

            if (parts != null && parts.length > 0) {
                if (parts[0].matches("-D$")) {
                    propertiesMeet = true;
                    continue;
                }

                if (parts[0].matches("^-D.+")) {
                    // -Dxxxx.xxxxx.xxx=yyyyy
                    // TODO: set properties
                    LOGGER.warn("ignore sqoop command -D: {}", parts[0]);
                    continue;
                }
            }

            if (parts != null && parts.length > 0 && propertiesMeet) {
                // TODO: set properties
                LOGGER.warn("ignore sqoop command -D: {}", parts);
                propertiesMeet = false;
                continue;
            }

            args.add(Joiner.on(" ").join(arg.getParts()));
        }

        String[] argArray = new String[args.size()];
        args.toArray(argArray);
        return argArray;
    }

    private static void parseSqoop(DICode diCode, DIJsonProcessor diCodeJP, Element e) {
        @SuppressWarnings("unchecked")
        List<Element> allElements = e.getChildren();
        List<Element> sqoopArg = new ArrayList<>();
        for (Element each : allElements) {
            if (StringUtils.equalsIgnoreCase(each.getName(), "arg")) {
                sqoopArg.add(each);
            }
        }

        if (null == sqoopArg || sqoopArg.isEmpty()) {
            Element command = e.getChild(OozieConstants.SQOOP_COMMAND, e.getNamespace());
            if (command != null && StringUtils.isNotBlank(command.getTextTrim())) {
                DICode.doParseSqoop(diCode, diCodeJP, command.getTextTrim());
            } else {
                diCode.setAdvice("ignore this sqoop node");
                diCode.setDescription("can not find sqoop config");
                diCode.setMessage("");
                diCode.setRiskLevel(ReportRiskLevel.WEEK_WARNINGS);
                diCode.setException(null);
            }
        } else {
            String[] sqArgs = new String[sqoopArg.size()];
            for (int i = 0; i < sqoopArg.size(); i++) {
                sqArgs[i] = sqoopArg.get(i).getValue();
            }
            DICode.doParseSqoop(diCode, diCodeJP, Joiner.on(" ").join(sqArgs));
        }

        return;
    }

    private static void doParseSqoop(DICode diCode, DIJsonProcessor diCodeJP, String sqoopCommands) {
        if (!sqoopCommands.startsWith("sqoop") && !sqoopCommands.startsWith("SQOOP")) {
            sqoopCommands = "sqoop " + sqoopCommands;
        }
        LOGGER.info("parse sqoop command: {}", sqoopCommands);
        DIJsonProcessor readerJp = diCodeJP.getConfiguration("steps[0]");
        DIJsonProcessor writerJp = diCodeJP.getConfiguration("steps[1]");
        String[] sqArgs = parseSqoopArgs(RegExUtils.replaceAll(sqoopCommands, "\n-", "\n -"));
        if (StringUtils.equalsIgnoreCase(StringUtils.trim(sqArgs[0]), SQOOP_IMPORT)) {
            ImportTool tool = new ImportTool();
            SqoopOptions importOptions;
            try {
                importOptions = tool.parseArguments(sqArgs, null, new SqoopOptions(), false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            setRmdbsParameters(readerJp, importOptions);
            setOdpsParameter(writerJp, importOptions);
        } else if (StringUtils.equalsIgnoreCase(StringUtils.trim(sqArgs[0]), SQOOP_EXPORT)) {
            ExportTool tool = new ExportTool();
            SqoopOptions exportOptions;
            try {
                exportOptions = tool.parseArguments(sqArgs, null, new SqoopOptions(), false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            setOdpsParameter(readerJp, exportOptions);
            setRmdbsParameters(writerJp, exportOptions);
        } else if (StringUtils.equalsIgnoreCase(StringUtils.trim(sqArgs[0]), SQOOP_EXPORT)) {
            // do nothing
        } else {
            diCode.setAdvice("not support this transport");
            diCode.setDescription("not support this transport");
            diCode.setMessage("not support this transport");
            diCode.setRiskLevel(ReportRiskLevel.ERROR);
        }
    }

    private static void setOdpsParameter(DIJsonProcessor writerJp, SqoopOptions sqoopOptions) {
        writerJp.set("stepType", calcEngineDatasourceType.get());
        writerJp.set("parameter.datasource", calcEngineDatasource.get());
        writerJp.set("parameter.column", new String[] {"*"});
        if (sqoopOptions.getColumns() != null) {
            writerJp.set("parameter.column", sqoopOptions.getColumns());
        }

        writerJp.set("parameter.table", sqoopOptions.getHiveTableName());
        if (StringUtils.isNotBlank(sqoopOptions.getHivePartitionKey())
            && StringUtils.isNotBlank(sqoopOptions.getHivePartitionValue())
        ) {
            String partition = Joiner.on("=").join(
                sqoopOptions.getHivePartitionKey(), sqoopOptions.getHivePartitionValue());
            writerJp.set("parameter.partition", partition);
        } else if (StringUtils.isNotBlank(sqoopOptions.getTargetDir())) {
            String partitions = getMatchedPartitionsByTargetDir(sqoopOptions.getTargetDir());
            writerJp.set("parameter.partition", partitions);
        }

        writerJp.set("parameter.truncate", "true");
    }

    private static String getMatchedPartitionsByTargetDir(String targetDir) {
        Matcher matcher = TARGET_DIR_PARTITION_PATTERN.matcher(targetDir);

        List<String> groups = new ArrayList<>();
        while (matcher.find()) {
            String group = matcher.group();
            groups.add(group);
        }

        return Joiner.on(",").join(groups);
    }

    private static void setRmdbsParameters(DIJsonProcessor readerJp, SqoopOptions sqoopOptions) {
        readerJp.set("stepType", "mysql");
        readerJp.set("parameter.connection[0].jdbcUrl", Arrays.asList(sqoopOptions.getConnectString()));
        if (StringUtils.isNotBlank(sqoopOptions.getTableName())) {
            readerJp.set("parameter.connection[0].table", Arrays.asList(sqoopOptions.getTableName()));
        }

        if (StringUtils.isNotBlank(sqoopOptions.getSqlQuery())) {
            readerJp.set("parameter.connection[0].querySql", Arrays.asList(sqoopOptions.getSqlQuery()));
        }

        readerJp.set("parameter.column", new String[] {"*"});
        if (sqoopOptions.getColumns() != null && sqoopOptions.getColumns().length > 0) {
            readerJp.set("parameter.column", sqoopOptions.getColumns());
        }

        String whereClause = processWhereClause(sqoopOptions);
        if (StringUtils.isNotBlank(whereClause)) {
            readerJp.set("parameter.where", whereClause);
        }
        readerJp.set("parameter.username", sqoopOptions.getUsername());
        readerJp.set("parameter.password", sqoopOptions.getPassword());
    }

    private static String processWhereClause(SqoopOptions sqoopOptions) {
        List<String> whereClauses = new ArrayList<>();
        if (StringUtils.isNotBlank(sqoopOptions.getWhereClause())) {
            whereClauses.add("(" + sqoopOptions.getWhereClause() + " )");
        }

        if (sqoopOptions.getIncrementalMode() != null) {
            switch (sqoopOptions.getIncrementalMode()) {
                case DateLastModified:
                    whereClauses.add("(" +
                        sqoopOptions.getIncrementalTestColumn() + " >= '" + sqoopOptions.getIncrementalLastValue()
                        + "' AND "
                        + sqoopOptions.getIncrementalTestColumn() + " < now()"
                        + ")");
                    break;
                case AppendRows:
                    whereClauses.add("(" +
                        sqoopOptions.getIncrementalTestColumn() + " > " + sqoopOptions.getIncrementalLastValue()
                        + ")");
                    break;
                default:
                    break;
            }
        }
        return Joiner.on(" AND ").join(whereClauses).replaceAll("^\\(", "").replaceAll("\\)$", "");
    }
}