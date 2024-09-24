package com.aliyun.dataworks.migrationx.transformer.dataworks.apps;

import com.aliyun.dataworks.migrationx.transformer.core.BaseTransformerApp;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DataWorksDolphinschedulerTransformerAppTest {

    @Test
    public void test1() {
        BaseTransformerApp transformerApp = new DataWorksDolphinschedulerTransformerApp();
        String[] args = new String[]{
                "-c", "../../temp/conf/transformer.json",
                //"-s", "../../temp/13666515015680/.tmp",
                "-s", "../../temp/123456/.tmp",
                //"-s", "../../temp/datax",
                "-t", "../../temp/target3.zip"
        };
        transformerApp.run(args);
    }
}