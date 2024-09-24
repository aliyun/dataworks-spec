package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.handler;

import java.util.Optional;

import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.component.SpecComponent;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * ComponentSqlNodeSpecHandler
 *
 * @author 聿剑
 * @date 2024/6/13
 */
@Slf4j
public class ComponentSqlNodeSpecHandler extends BasicNodeSpecHandler {
    @Override
    public boolean support(DwNodeEntity dwNode) {
        return matchNodeType(dwNode, CodeProgramType.COMPONENT_SQL);
    }

    @Override
    public SpecNode handle(DwNodeEntity dmNode) {
        SpecNode node = super.handle(dmNode);
        Optional.ofNullable(dmNode.getComponent()).ifPresent(com -> {
            SpecComponent specCom = new SpecComponent();
            specCom.setId(com.getId());
            specCom.setName(com.getName());
            specCom.setOwner(com.getOwner());
            specCom.setDescription(com.getDescription());
            specCom.setInputs(com.getInputs());
            specCom.setOutputs(com.getOutputs());
            specCom.setMetadata(com.getMetadata());
            node.setComponent(specCom);
        });
        return node;
    }
}
