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

package com.aliyun.dataworks.migrationx.transformer.core.translator;

import com.aliyun.dataworks.migrationx.domain.dataworks.datago.DataGoConstants;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwNode;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwResource;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.DwWorkflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Resource;
import com.aliyun.dataworks.common.spec.domain.dw.types.CodeProgramType;
import com.aliyun.dataworks.migrationx.transformer.core.report.ReportItem;
import com.aliyun.dataworks.migrationx.transformer.core.report.Reportable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author sam.liux
 * @date 2020/07/16
 */
public class Perl2ShellTranslator implements Reportable, NodePropertyTranslator {
    private Properties properties;

    @Override
    public List<ReportItem> getReport() {
        return null;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public boolean match(DwWorkflow workflow, DwNode node) {
        List<CodeProgramType> perlTypes = Arrays.asList(CodeProgramType.PERL, CodeProgramType.ODPS_PERL);
        boolean perl2Shell = perlTypes.stream().anyMatch(t -> t.name().equalsIgnoreCase(node.getType())) &&
            BooleanUtils.toBoolean(properties.getProperty(DataGoConstants.PROPERTIES_CONVERTER_PERL2SHELL_ENABLE, "true"));

        if (!perl2Shell) {
            return false;
        }
        return true;
    }

    @Override
    public boolean translate(DwWorkflow dwWorkflow, DwNode dwNode) throws Exception {
        if (!match(dwWorkflow, dwNode)) {
            return false;
        }

        String code = dwNode.getCode();
        Resource perlResource = new DwResource();
        perlResource.setName(dwNode.getName() + "__script.pl");
        perlResource.setType(CodeProgramType.ODPS_FILE.name());
        perlResource.setOdps(false);
        perlResource.setConnection(dwNode.getConnection());
        perlResource.setFolder(dwNode.getFolder());
        ((DwResource)perlResource).setLocalPath(writePerlCodeFile(code));
        ((DwResource)perlResource).setWorkflowRef(dwWorkflow);
        dwWorkflow.getResources().add(perlResource);

        String perlBin = properties.getProperty(DataGoConstants.PROPERTIES_CONVERTER_PERL2SHELL_PERL_BIN, "/usr/bin/perl");
        String perlIncludePath = properties.getProperty(DataGoConstants.PROPERTIES_CONVERTER_PERL2SHELL_PERL_INCLUDE_PATHS,
            "/usr/lib64/perl5/5.8.8/x86_64-linux-thread-multi");
        String perlIncludeOptions = Arrays.asList(perlIncludePath.split(","))
            .stream().map(p -> "-I " + p).collect(Collectors.joining(" "));

        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(
            "file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init();
        Template template = velocityEngine.getTemplate("res/perl2shell.sh", "UTF-8");
        VelocityContext context = new VelocityContext();
        context.put("perlBin", perlBin);
        context.put("perlIncludePathOptions", perlIncludeOptions);
        context.put("perlScriptResourceName", perlResource.getName());

        StringWriter stringWriter = new StringWriter();
        template.merge(context, stringWriter);
        dwNode.setCode(stringWriter.getBuffer().toString());
        dwNode.setType(CodeProgramType.DIDE_SHELL.name());
        dwNode.setConnection("odps_first");
        return true;
    }

    private String writePerlCodeFile(String code) throws IOException {
        File tmpFile = File.createTempFile("tmp", "perl");
        FileUtils.writeStringToFile(tmpFile, code, "utf-8");
        return tmpFile.getAbsolutePath();
    }
}
