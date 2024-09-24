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

package com.aliyun.dataworks.migrationx.transformer.core;

import java.io.File;
import java.io.IOException;

import com.aliyun.dataworks.client.command.CommandApp;
import com.aliyun.dataworks.migrationx.domain.dataworks.standard.objects.Package;
import com.aliyun.dataworks.migrationx.transformer.core.checkpoint.file.LocalFileCheckPoint;
import com.aliyun.dataworks.migrationx.transformer.core.transformer.Transformer;
import com.aliyun.migrationx.common.context.TransformerContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 聿剑
 * @date 2023/02/15
 */
public abstract class BaseTransformerApp extends CommandApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTransformerApp.class);
    private static final String EXAMPLE = "python ./migrationx-transformer/bin/transformer.py"
        + " -a dataworks_transformer"
        + " -c dataworks-config.json"
        + " -s project_a.zip -t dw.zip";
    private static final String HEADER = "Transformer Command App";

    protected final Class<? extends Package> from;
    protected final Class<? extends Package> to;
    protected String optConfig;
    protected String optSourcePackage;
    protected String optTargetPackage;

    protected String checkpoint;

    protected String load;

    public BaseTransformerApp(Class<? extends Package> from, Class<? extends Package> to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void run(String[] args) {
        Options options = new Options();
        options.addRequiredOption("c", "config", true, "transform configuration file path");
        options.addRequiredOption("s", "sourcePackage", true, "source package file path");
        options.addRequiredOption("t", "targetPackage", true, "target package file path");
        options.addOption("ckpt", "checkpoint", true, "checkpoint dir");
        options.addOption("ld", "load", true, "resume dir");

        HelpFormatter helpFormatter = new HelpFormatter();
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(options, args);
            optConfig = commandLine.getOptionValue("c");
            optSourcePackage = commandLine.getOptionValue("s");
            optTargetPackage = commandLine.getOptionValue("t");
            checkpoint = commandLine.getOptionValue("ckpt");
            load = commandLine.getOptionValue("ld");

            doTransform();
        } catch (ParseException e) {
            LOGGER.error("parser command error: {}", e.getMessage());
            helpFormatter.printHelp("Options", HEADER, options, EXAMPLE);
            System.exit(-1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract Transformer createTransformer(File config, Package from, Package to);

    protected void doTransform() throws Exception {
        Package fromPackage = from.newInstance().setPackageFile(new File(new File(optSourcePackage).getAbsolutePath()));
        Package toPackage = to.newInstance().setPackageFile(new File(new File(optTargetPackage).getAbsolutePath()));
        LOGGER.info("start transform from: {}, to: {}", from, to);
        Transformer transformer = createTransformer(new File(optConfig), fromPackage, toPackage);
        initCollector();
        checkAndSetCheckpoint();
        transformer.init();
        transformer.load();
        transformer.transform();
        transformer.write();
        finishCollector();
        LOGGER.info("transform success");
    }

    /**
     * custom collector and metrics consumer, default consumer with logging
     */
    protected void initCollector() {
    }

    protected void checkAndSetCheckpoint() throws IOException {
        TransformerContext.getContext().setCheckpoint(checkpoint);
        TransformerContext.getContext().setLoad(load);
        if (TransformerContext.getContext().getCheckpoint() != null
            && TransformerContext.getContext().getLoad() != null) {
            if (TransformerContext.getContext().getCheckpoint().getCanonicalPath()
                .equals(TransformerContext.getContext().getLoad().getCanonicalPath())) {
                throw new RuntimeException("checkpoint path can not equals to load path");
            }
        }
        File file = TransformerContext.getContext().getCheckpoint();
        if (file != null && file.exists()) {
            for (File child : file.listFiles()) {
                if (child.getName().endsWith(LocalFileCheckPoint.SUFFIX)) {
                    child.delete();
                }
            }
        }
    }

    /**
     * custom metrics consumer with finishCollector(Consumer<Summary> c)
     */
    protected void finishCollector() {
        TransformerContext.getCollector().finishCollector();
        TransformerContext.clear();
    }
}
