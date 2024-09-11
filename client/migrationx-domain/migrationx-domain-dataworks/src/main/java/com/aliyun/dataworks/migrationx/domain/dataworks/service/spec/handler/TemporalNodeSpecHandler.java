package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.handler;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.TemporalSpecNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntity;
import com.aliyun.migrationx.common.context.TransformerContext;

import com.google.common.base.Preconditions;

public class TemporalNodeSpecHandler extends BasicNodeSpecHandler {
    @Override
    public boolean support(DwNodeEntity dwNode) {
        return matchNodeType(dwNode, CodeProgramType.CUSTOM);
    }

    @Override
    public SpecNode handle(DwNodeEntity orcNode) {
        Preconditions.checkNotNull(orcNode, "node is null");
        SpecNode specNode = super.handle(orcNode, new TemporalSpecNode());
        TemporalSpecNode temporalSpecNode = (TemporalSpecNode) specNode;
        temporalSpecNode.setOriginSource(orcNode.getOrigin());
        TransformerContext.getCollector().markTempSpecProcess(orcNode.getWorkflowName(), orcNode.getName());
        return temporalSpecNode;
    }
}
