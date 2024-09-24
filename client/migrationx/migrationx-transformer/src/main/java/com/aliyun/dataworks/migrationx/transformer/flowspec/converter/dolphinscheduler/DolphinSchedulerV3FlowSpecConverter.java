/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler;

import java.util.ArrayList;
import java.util.Objects;

import com.aliyun.dataworks.common.spec.domain.DataWorksWorkflowSpec;
import com.aliyun.dataworks.common.spec.domain.Specification;
import com.aliyun.dataworks.common.spec.domain.enums.SpecKind;
import com.aliyun.dataworks.common.spec.domain.enums.SpecVersion;
import com.aliyun.dataworks.common.spec.domain.ref.SpecWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320.DagDataSchedule;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.FlowSpecConverter;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.WorkflowConverter;
import com.aliyun.dataworks.migrationx.transformer.flowspec.converter.dolphinscheduler.common.context.DolphinSchedulerV3ConverterContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-06-04
 */
@Slf4j
public class DolphinSchedulerV3FlowSpecConverter implements FlowSpecConverter<DagDataSchedule> {

    private static final SpecVersion SPEC_VERSION = SpecVersion.V_1_2_0;

    private final DagDataSchedule dagDataSchedule;

    @Getter
    private final Specification<DataWorksWorkflowSpec> specification;

    private final DolphinSchedulerV3ConverterContext context;

    public DolphinSchedulerV3FlowSpecConverter(DagDataSchedule dagDataSchedule, DolphinSchedulerV3ConverterContext context) {
        this.dagDataSchedule = ObjectUtils.defaultIfNull(dagDataSchedule, new DagDataSchedule());
        this.context = ObjectUtils.defaultIfNull(context, new DolphinSchedulerV3ConverterContext());
        specification = new Specification<>();
        specification.setSpec(initSpec());
        checkContext();
    }

    private void checkContext() {
        MapUtils.emptyIfNull(context.getDataSourceMap()).values().forEach(datasource -> datasource.setIsRef(false));
        if (Objects.isNull(context.getSpecVersion())) {
            context.setSpecVersion(SPEC_VERSION.getLabel());
        }
    }

    /**
     * init spec instance
     *
     * @return spec
     */
    private DataWorksWorkflowSpec initSpec() {
        DataWorksWorkflowSpec spec = new DataWorksWorkflowSpec();
        spec.setVariables(new ArrayList<>());
        spec.setTriggers(new ArrayList<>());
        spec.setScripts(new ArrayList<>());
        spec.setFiles(new ArrayList<>());
        spec.setArtifacts(new ArrayList<>());
        spec.setDatasources(new ArrayList<>());
        spec.setDqcRules(new ArrayList<>());
        spec.setRuntimeResources(new ArrayList<>());
        spec.setFileResources(new ArrayList<>());
        spec.setFunctions(new ArrayList<>());
        spec.setNodes(new ArrayList<>());
        spec.setWorkflows(new ArrayList<>());
        spec.setComponents(new ArrayList<>());
        spec.setFlow(new ArrayList<>());

        return spec;
    }

    /**
     * Convert the T type to flowSpec
     *
     * @param from origin obj
     * @return flowSpec result
     */
    @Override
    public Specification<DataWorksWorkflowSpec> convert(DagDataSchedule from) {
        // judge process is manual or cycle workflow
        specification.setKind((Objects.isNull(from.getSchedule()) ? SpecKind.MANUAL_WORKFLOW : SpecKind.CYCLE_WORKFLOW).getLabel());
        specification.setVersion(context.getSpecVersion());

        // convert single process to workflow and add to spec
        log.info("====== start convert ======,version:{}", specification.getVersion());
        SpecWorkflow workflow = new WorkflowConverter(from, context).convert();
        DataWorksWorkflowSpec spec = specification.getSpec();
        spec.getWorkflows().add(workflow);
        log.info("====== finish convert ======");
        return specification;
    }

    /**
     * convert main method
     *
     * @return flowSpec result
     */
    public Specification<DataWorksWorkflowSpec> convert() {
        return convert(dagDataSchedule);
    }
}
