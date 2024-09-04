package com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.handler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModel;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.CodeModelFactory;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ControllerBranchCode;
import com.aliyun.dataworks.common.spec.domain.dw.codemodel.ControllerBranchCode.Branch;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.common.spec.domain.noref.SpecBranch;
import com.aliyun.dataworks.common.spec.domain.noref.SpecBranches;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNode;
import com.aliyun.dataworks.common.spec.domain.ref.SpecNodeOutput;
import com.aliyun.dataworks.migrationx.domain.dataworks.service.spec.entity.DwNodeEntity;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

/**
 * 分支节点Spec处理器
 *
 * @author 聿剑
 * @date 2024/03/02
 */
@Slf4j
public class BranchNodeSpecHandler extends BasicNodeSpecHandler {
    @Override
    public boolean support(DwNodeEntity node) {
        return matchNodeType(node, CodeProgramType.CONTROLLER_BRANCH);
    }

    @Override
    public SpecNode handle(DwNodeEntity orcNode) {
        SpecNode specNode = super.handle(orcNode);
        specNode.setBranch(buildBranch(orcNode));
        return specNode;
    }

    private SpecBranch buildBranch(DwNodeEntity orcNode) {
        SpecBranch specBranch = new SpecBranch();

        CodeModel<ControllerBranchCode> codeModel
            = CodeModelFactory.getCodeModel(CodeProgramType.CONTROLLER_BRANCH.name(), orcNode.getCode());
        List<Branch> branches = ListUtils.emptyIfNull(Optional.ofNullable(codeModel.getCodeModel())
            .map(ControllerBranchCode::getBranchList).orElse(null));
        specBranch.setBranches(ListUtils.emptyIfNull(branches).stream().map(branch -> {
            SpecBranches b = new SpecBranches();
            b.setDesc(branch.getDescription());
            SpecNodeOutput output = new SpecNodeOutput();
            output.setData(branch.getNodeoutput());
            b.setOutput(output);
            b.setWhen(branch.getCondition());
            return b;
        }).collect(Collectors.toList()));
        return specBranch;
    }
}
