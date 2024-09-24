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

package com.aliyun.dataworks.migrationx.domain.dataworks.caiyunjian;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Datasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwDatasource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.connection.AbstractConnection;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.tenant.EnvType;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sam.liux
 * @date 2021/01/07
 */
public class DgDatasourceUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(DgDatasourceUtil.class);
    public static final String DATASOURCE_EXCEL = "datasource.xlsx";

    public static Map<String, DgDatasource> processDatasource(File datasourceExcelFile) throws IOException {
        Map<String, DgDatasource> datasources = new HashMap<>();
        if (!datasourceExcelFile.exists()) {
            return datasources;
        }

        Workbook workbook = new XSSFWorkbook(new FileInputStream(datasourceExcelFile));
        if (workbook.getNumberOfSheets() == 0) {
            return datasources;
        }

        Sheet sheet = workbook.getSheetAt(0);
        List<Row> rows = IteratorUtils.toList(sheet.rowIterator());
        rows.stream().parallel().forEach(row -> processRow(row, datasources));
        return datasources;
    }

    private static String getCellStringValue(Row row, String columnName) {
        Sheet sheet = row.getSheet();
        Row titleRow = sheet.getRow(0);

        List<Cell> columnCellList = Lists.newArrayList(titleRow.cellIterator());
        Integer index = columnCellList.stream()
            .filter(cell -> columnName.equalsIgnoreCase(StringUtils.strip(cell.getStringCellValue())))
            .map(Cell::getColumnIndex)
            .findFirst().orElseThrow(() -> new RuntimeException("column not found: " + columnName));
        Cell cell = row.getCell(index);
        if (cell != null) {
            cell.setCellType(CellType.STRING);
        }
        return cell == null ? "" : StringUtils.defaultIfBlank(cell.getStringCellValue(), "").trim();
    }

    private static void processRow(Row row, Map<String, DgDatasource> datasources) {
        if (row.getRowNum() == 0) {
            return;
        }

        DgDatasource dgDatasource = new DgDatasource();
        String id = getCellStringValue(row, "数据源ID");
        String type = getCellStringValue(row, "数据源类型");
        String name = getCellStringValue(row, "数据源名称");
        String host = getCellStringValue(row, "host");
        String port = getCellStringValue(row, "port");
        String rootPath = getCellStringValue(row, "路径");
        String username = getCellStringValue(row, "用户名");
        String password = getCellStringValue(row, "密码");
        dgDatasource.setId(StringUtils.isBlank(id) ? null : Long.valueOf(id));
        dgDatasource.setName(name);
        dgDatasource.setType(type);
        dgDatasource.setRootPath(rootPath);
        dgDatasource.setUsername(username);
        dgDatasource.setPassword(password);
        dgDatasource.setHost(host);
        dgDatasource.setPort(port);
        datasources.put(name, dgDatasource);
    }

    public static List<Datasource> convertDatasources(Map<String, DgDatasource> datasourceMap) {
        List<Datasource> datasourceList = new ArrayList<>();
        MapUtils.emptyIfNull(datasourceMap).forEach((name, dgDatasource) -> {
            DwDatasource dwDatasource = new DwDatasource();
            dwDatasource.setName(name);
            dwDatasource.setType(dgDatasource.getType());
            dwDatasource.setDescription("data go datasource id: " + dgDatasource.getId());
            if ("sftp".equalsIgnoreCase(dgDatasource.getType())) {
                dwDatasource.setType("ftp");
            }
            if ("oceanbase".equalsIgnoreCase(dgDatasource.getType())) {
                dwDatasource.setType("apsaradb_for_oceanbase");
            }
            AbstractConnection conn = dgDatasource.toConnection();
            dwDatasource.setConnection(GsonUtils.toJsonString(conn));
            dwDatasource.setEnvType(EnvType.PRD.name());
            if (datasourceList.stream().noneMatch(ds -> ds.getName().equalsIgnoreCase(name))) {
                datasourceList.add(dwDatasource);
            }
        });
        return datasourceList;
    }
}