package com.aliyun.migrationx.common.metrics;

import java.util.function.Consumer;

import com.aliyun.migrationx.common.metrics.DolphinMetricsCollector.Summary;

/**
 * default implement
 * @author 聿剑
 * @date 2024/9/12
 */
public class DefaultMetricCollector implements MetricsCollector{
    @Override
    public void setMetricsConsumer(Consumer<Metrics> consumer) {

    }

    @Override
    public void setTransformerType(String type) {

    }

    @Override
    public void setTotalTasks(int total) {

    }

    @Override
    public void markSuccessMiddleProcess(Metrics metrics) {

    }

    @Override
    public void markFailedSpecProcess(String workflowName, String nodeName) {

    }

    @Override
    public void markFailedMiddleProcess(Metrics metrics) {

    }

    @Override
    public void markSkippedProcess(Metrics metrics) {

    }

    @Override
    public void markTempSpecProcess(String workflowName, String nodeName) {

    }

    @Override
    public void markTempSpecProcess(Metrics metrics) {

    }

    @Override
    public void markSuccessSpecProcess(String workflowName, String nodeName) {

    }

    @Override
    public void markSuccessSpecProcess(Metrics metrics) {

    }

    @Override
    public void markSuccessSpecProcess(Metrics tmp, Consumer<Metrics> c) {

    }

    @Override
    public void markFailedSpecProcess(Metrics metrics) {

    }

    @Override
    public void finishCollector() {

    }

    @Override
    public void finishCollector(Consumer<Summary> c) {

    }

    @Override
    public Progress progress() {
        return null;
    }
}
