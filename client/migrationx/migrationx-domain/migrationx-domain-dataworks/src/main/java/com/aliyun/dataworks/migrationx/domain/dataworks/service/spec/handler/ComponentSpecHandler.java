package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.handler;

import com.aliyun.dataworks.common.spec.adapter.handler.AbstractEntityHandler;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * Component
 *
 * @author 聿剑
 * @date 2024/6/13
 */
@Slf4j
public class ComponentSpecHandler extends AbstractEntityHandler<DwNodeEntity, SpecComponent> {
    @Override
    public boolean support(DwNodeEntity dwNode) {
        return CodeProgramType.SQL_COMPONENT.name().equalsIgnoreCase(dwNode.getType());
    }

    @Override
    public SpecComponent handle(DwNodeEntity orcComponent) {
        return orcComponent.getComponent();
    }
}
