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

package com.aliyun.migrationx.common.utils;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Function;

public class ExecuteUtils {


    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteUtils.class);

    /**
     * execution closure with retry
     * 
     * @param command command define
     * @param retryTimes retry times
     * @param retryInterval retry interval
     * @return result
     * @throws Exception exception
     */
    public static <T> T executeWithRetry(ExecuteCommand<T> command, int retryTimes, long retryInterval) throws Exception {
        T ret = null;
        int retryCount = 0;
        boolean ended = false;
        Exception caughtException = null;
        while (true) {
            try {
                command.setRetryCount(retryCount);
                ret = command.run();
                ended = true;
                break;
            } catch (Exception e) {
                LOGGER.warn("Got exception: {}", e.getMessage());
                caughtException = e;
                if (!command.isRetry(e)) {
                    break;
                }
            }
            if (++retryCount > retryTimes) {
                break;
            }
            LOGGER.warn("retrying...");
            Thread.sleep(retryInterval);
        }
        if (!ended) {
            LOGGER.warn("execute end, throw exception");
            throw caughtException;
        } else {
            LOGGER.info("execute end, success return");
            LOGGER.debug("return value: {}", ret);
            return ret;
        }
    }

    /**
     * 执行异常定义，当执行流程需要失败重试时可抛出该异常
     */
    public static class ExecuteException extends Exception {

        private static final long serialVersionUID = -7226665961824572062L;

        public ExecuteException() {
            super();
        }

        public ExecuteException(Exception e) {
            super(e);
        }

        public ExecuteException(String s) {
            super(s);
        }
    }

    /**
     * A Command is a closure used to pass a block of code from individual
     * functions to executeWithRetry, which centralizes connection error
     * handling. Command is parameterized on the return type of the function.
     * 
     * The general transformation is:
     * 
     * From: String foo(int a) throws ExceptionB { <block of code> }
     * 
     * To: String foo(final int a) throws ExceptionB { String ret = null; try {
     * ret = executeWithRetry(new Command<Boolean>() { String
     * run(ThriftConnector connector) { <block of code> } } } catch (ExceptionB
     * e) { throw e; } catch (Exception e) { // Since run is only supposed to
     * throw ExceptionB it could only // be a runtime exception throw
     * (RuntimeException)e; } }
     * 
     * The catch blocks are used to ensure that the exceptions thrown by the
     * <block of code> follow the function definition.
     */
    public static abstract class ExecuteCommand<T> {

        protected int retryCount;

        public int getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }

        /**
         * 执行流程，抛异常表示失败
         * 
         * @return
         * @throws Exception
         */
        public abstract T run() throws Exception;

        /**
         * 根据异常类型判断是否重试
         * 
         * @param e
         * @return
         */
        public abstract boolean isRetry(Exception e);
    }

    /**
     * 继承自ExecuteCommand，专用于http请求处理流程
     */
    public static abstract class HttpExecuteCommand<T> extends ExecuteCommand<T> {

        @Override
        public boolean isRetry(Exception e) {
            return true;
        }

        /**
         * 设置请求参数
         * 
         * @param request
         */
        public void setHttpParams(HttpRequestBase request) {
            // TODO Auto-generated method stub
        }

        /**
         * 设置请求内容实体
         * 
         * @param request
         */
        public void setHttpEntity(HttpEntityEnclosingRequestBase request) {
            // TODO Auto-generated method stub
        }

        /**
         * 设置请求配置
         * 
         * @param request
         */
        public void setHttpConf(HttpRequestBase request) {
            // TODO Auto-generated method stub
        }

        /**
         * 设置请求头部
         * 
         * @param request
         */
        public void setHttpHeader(HttpRequestBase request) {
            // TODO Auto-generated method stub
        }

        /**
         * 设置认证凭证
         * 
         * @param context
         */
        public void setHttpCredentials(HttpClientContext context) {
            // TODO Auto-generated method stub
        }
    }

    /**
     * 当命令过程不需要返回值的时候，返回VOID
     */
    public static class VOID {
        // TODO
    }

    @Data
    @ToString
    @Accessors(chain = true)
    public static class WaitForStateCommand<R> {
        private Callable<R> procedure;
        private R procedureResult;
        private Function<WaitForStateCommand<R>, Boolean> checkDoneFunc;
        private int intervalSeconds = 1;
        private int timeoutSeconds = 60;
        private boolean success = false;
        private Throwable exception;
    }

    public static <R> boolean waitForState(WaitForStateCommand<R> command) throws InterruptedException {
        int counter = command.getTimeoutSeconds();

        do {
            try {
                R procedureResult = command.getProcedure().call();
                command.setProcedureResult(procedureResult);
            } catch (Throwable throwable) {
                LOGGER.error("", throwable);
                command.setException(throwable);
                command.setSuccess(false);
            }

            try {
                Boolean done = command.getCheckDoneFunc().apply(command);
                if (done) {
                    LOGGER.info("procedure done");
                    command.setSuccess(true);
                    break;
                }
            } catch (Throwable e) {
                LOGGER.error("", e);
                command.setException(e);
                command.setSuccess(false);
                break;
            }

            counter--;
            LOGGER.info("sleeping for {} seconds", command.getIntervalSeconds());
            Thread.sleep(1000L * command.getIntervalSeconds());
        } while (counter > 0);

        String timeoutMsg = counter <= 0 ? "wait for procedure timeout after " + command.getTimeoutSeconds() + " seconds" : "";

        if (command.isSuccess() && counter > 0) {
            return true;
        } else {
            command.setException(new RuntimeException(timeoutMsg));
            throw new RuntimeException(command.getException());
        }
    }
}
