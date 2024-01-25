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

package com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity;

import com.aliyun.dataworks.migrationx.domain.dataworks.objects.serialize.CodeControlCharEscapeConverter;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.serialize.CodeControlCharUnEscapeConverter;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.DmObjectType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.google.common.base.Joiner;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * @author 聿剑
 * @date 2021/10/18
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString(callSuper = true)
public class DqcRule extends DmObject {

    @JacksonXmlProperty(isAttribute = true, localName = "ruleName")
    private String ruleName;

    /**
     * 计算引擎类型
     */
    @JacksonXmlProperty(localName = "calcEngineType", isAttribute = true)
    private String calcEngineType;

    /**
     * 规则表名
     */
    @JacksonXmlProperty(localName = "ruleTable", isAttribute = true)
    private String ruleTable;

    /**
     * 分区表达式
     */
    @JacksonXmlProperty(localName = "partition", isAttribute = true)
    private String partition;
    /**
     * 引擎实例名称
     */
    @JacksonXmlProperty(localName = "calcEngineInstance", isAttribute = true)
    private String calcEngineInstance;
    /**
     * 责任人
     */
    @JacksonXmlProperty(localName = "owner", isAttribute = true)
    private String owner;
    /**
     * 调度节点ID
     */
    @JacksonXmlProperty(localName = "schedulerNodeOutput", isAttribute = true)
    private String schedulerNodeOutput;
    /**
     * 创建时间
     */
    @JacksonXmlProperty(localName = "createTime", isAttribute = true)
    private Date createTime;
    /**
     * 修改时间
     */
    @JacksonXmlProperty(localName = "modifyTime", isAttribute = true)
    private Date modifyTime;
    /**
     * 规则配置
     */
    @JacksonXmlProperty(localName = "RuleConfig")
    @JacksonXmlCData
    @JacksonXmlElementWrapper(localName = "RuleConfig")
    @JsonSerialize(converter = CodeControlCharEscapeConverter.class)
    @JsonDeserialize(converter = CodeControlCharUnEscapeConverter.class)
    private String ruleConfig;

    public DqcRule() {
        super(DmObjectType.DQC_RULE);
    }

    @Override
    public String getUniqueKey() {
        String str = Joiner.on("@").join(
            getCalcEngineType(), StringUtils.defaultIfBlank(getCalcEngineInstance(), ""),
            getRuleTable(), getPartition(), getRuleName());
        return UUID.nameUUIDFromBytes(str.getBytes(StandardCharsets.UTF_8)).toString();
    }

    public String getRuleName() {
        return ruleName;
    }

    public DqcRule setRuleName(String ruleName) {
        this.ruleName = ruleName;
        return this;
    }

    public String getCalcEngineType() {
        return calcEngineType;
    }

    public DqcRule setCalcEngineType(String calcEngineType) {
        this.calcEngineType = calcEngineType;
        return this;
    }

    public String getRuleTable() {
        return ruleTable;
    }

    public DqcRule setRuleTable(String ruleTable) {
        this.ruleTable = ruleTable;
        return this;
    }

    public String getPartition() {
        return partition;
    }

    public DqcRule setPartition(String partition) {
        this.partition = partition;
        return this;
    }

    public String getCalcEngineInstance() {
        return calcEngineInstance;
    }

    public DqcRule setCalcEngineInstance(String calcEngineInstance) {
        this.calcEngineInstance = calcEngineInstance;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public DqcRule setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getSchedulerNodeOutput() {
        return schedulerNodeOutput;
    }

    public DqcRule setSchedulerNodeOutput(String schedulerNodeOutput) {
        this.schedulerNodeOutput = schedulerNodeOutput;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public DqcRule setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public DqcRule setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
        return this;
    }

    public String getRuleConfig() {
        return ruleConfig;
    }

    public DqcRule setRuleConfig(String ruleConfig) {
        this.ruleConfig = ruleConfig;
        return this;
    }
}
