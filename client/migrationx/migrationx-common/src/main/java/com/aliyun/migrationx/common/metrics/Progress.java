package com.aliyun.migrationx.common.metrics;

import lombok.Data;

@Data
public class Progress {
    private int total;
    private int middleSuccess;
    private int success;
    private int failed;
    private int skipped;
}
