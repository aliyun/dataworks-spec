/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.datasource;

import java.util.Date;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.enums.DbType;

public class DataSource {
    /**
     * id
     */
    private int id;

    /**
     * user id
     */
    private int userId;

    /**
     * user name
     */
    private String userName;

    /**
     * data source name
     */
    private String name;

    /**
     * note
     */
    private String note;

    /**
     * data source type
     */
    private DbType type;

    /**
     * connection parameters
     */
    private String connectionParams;

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;

    public DataSource() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public DbType getType() {
        return type;
    }

    public void setType(DbType type) {
        this.type = type;
    }

    public String getConnectionParams() {
        return connectionParams;
    }

    public void setConnectionParams(String connectionParams) {
        this.connectionParams = connectionParams;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "DataSource{" +
            "id=" + id +
            ", userId=" + userId +
            ", userName='" + userName + '\'' +
            ", name='" + name + '\'' +
            ", note='" + note + '\'' +
            ", type=" + type +
            ", connectionParams='" + connectionParams + '\'' +
            ", createTime=" + createTime +
            ", updateTime=" + updateTime +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataSource that = (DataSource)o;

        if (id != that.id) {
            return false;
        }
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        return result;
    }
}
