package com.aliyun.dataworks.migrationx.writer;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DataWorksMigrationAssistWriterTest {

    @Test
    public void test1() throws Exception {
        DataWorksMigrationAssistWriter writer = new DataWorksMigrationAssistWriter();
        String[] args = new String[]{
                "-e", "dataworks.cn-shenzhen.aliyuncs.com",  //endpoint
                "-i", "xxx",  //accessId
                "-k", "xxx",  //accessKey
                "-r", "cn-shenzhen",   //regionId
                "-p", "81780",   //projectId
                "-f", "../../temp/target3.zip",    //file
                "-t", "SPEC",   //spect
        };
        writer.run(args);
    }
}