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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.sql;

import java.util.ArrayList;
import java.util.List;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.entity.ResourceInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.enums.DbType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.task.AbstractParameters;

import org.apache.commons.lang.StringUtils;

/**
 * Sql/Hql parameter.
 */
public class SqlParameters extends AbstractParameters {
    /**
     * data source type，eg  MYSQL, POSTGRES, HIVE ...
     */
    private DbType type;

    /**
     * datasource id
     */
    private int datasource;

    /**
     * sql
     */
    private String sql;

    /**
     * sql type 0 query 1 NON_QUERY
     */
    private int sqlType;

    /**
     * send email
     */
    private Boolean sendEmail;

    /**
     * display rows
     */
    private int displayRows;

    /**
     * udf list
     */
    private String udfs;

    /**
     * show type 0 TABLE 1 TEXT 2 attachment 3 TABLE+attachment
     */
    private String showType;

    /**
     * SQL connection parameters
     */
    private String connParams;

    /**
     * Pre Statements
     */
    private List<String> preStatements;

    /**
     * Post Statements
     */
    private List<String> postStatements;

    /**
     * title
     */
    private String title;

    /**
     * receivers
     */
    private String receivers;

    /**
     * receivers cc
     */
    private String receiversCc;

    /**
     * query result limit
     */
    private int limit;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public DbType getType() {
        return type;
    }

    public void setType(DbType type) {
        this.type = type;
    }

    public int getDatasource() {
        return datasource;
    }

    public void setDatasource(int datasource) {
        this.datasource = datasource;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getUdfs() {
        return udfs;
    }

    public void setUdfs(String udfs) {
        this.udfs = udfs;
    }

    public int getSqlType() {
        return sqlType;
    }

    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    public Boolean getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public int getDisplayRows() {
        return displayRows;
    }

    public void setDisplayRows(int displayRows) {
        this.displayRows = displayRows;
    }

    public String getShowType() {
        return showType;
    }

    public void setShowType(String showType) {
        this.showType = showType;
    }

    public String getConnParams() {
        return connParams;
    }

    public void setConnParams(String connParams) {
        this.connParams = connParams;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReceivers() {
        return receivers;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public String getReceiversCc() {
        return receiversCc;
    }

    public void setReceiversCc(String receiversCc) {
        this.receiversCc = receiversCc;
    }

    public List<String> getPreStatements() {
        return preStatements;
    }

    public void setPreStatements(List<String> preStatements) {
        this.preStatements = preStatements;
    }

    public List<String> getPostStatements() {
        return postStatements;
    }

    public void setPostStatements(List<String> postStatements) {
        this.postStatements = postStatements;
    }

    @Override
    public boolean checkParameters() {
        return datasource != 0 && type != null && StringUtils.isNotEmpty(sql);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "SqlParameters{"
                + "type='" + type + '\''
                + ", datasource=" + datasource
                + ", sql='" + sql + '\''
                + ", sqlType=" + sqlType
                + ", sendEmail=" + sendEmail
                + ", displayRows=" + displayRows
                + ", limit=" + limit
                + ", udfs='" + udfs + '\''
                + ", showType='" + showType + '\''
                + ", connParams='" + connParams + '\''
                + ", title='" + title + '\''
                + ", receivers='" + receivers + '\''
                + ", receiversCc='" + receiversCc + '\''
                + ", preStatements=" + preStatements
                + ", postStatements=" + postStatements
                + '}';
    }
}
