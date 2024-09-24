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

package com.aliyun.migrationx.common.http;

import java.net.SocketException;

import com.aliyun.migrationx.common.utils.ExecuteUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;

/**
 * @author 聿剑
 * @date 2022/10/19
 */
@Slf4j
public class HttpClientUtil {
    private static final int TIMEOUT_SECONDS = 60;
    private HttpClient httpClient;

    public HttpClientUtil() {
        this.httpClient = HttpClientBuilder.create().build();
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public String executeAndGet(HttpRequestBase httpRequestBase, int retry, int retryInterval) throws Exception {
        return ExecuteUtils.executeWithRetry(new ExecuteUtils.ExecuteCommand<String>() {
            @Override
            public String run() throws Exception {
                return executeAndGet(httpRequestBase, getRetryCount() < retry);
            }

            @Override
            public boolean isRetry(Exception e) {
                return e instanceof SocketException;
            }
        }, retry, retryInterval);
    }

    public String executeAndGet(HttpRequestBase httpRequestBase) throws Exception {
        return executeAndGet(httpRequestBase, 3, 1000);
    }

    public String executeAndGet(HttpRequestBase httpRequestBase, boolean retry) throws Exception {
        if (httpRequestBase == null) {
            throw new IllegalArgumentException("'http Request' can't be null");
        }
        HttpResponse response;
        String responseString;
        Long start = System.currentTimeMillis();

        try {
            // timeout setting
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT_SECONDS * 1000)
                    .setConnectTimeout(TIMEOUT_SECONDS * 1000)
                    .setConnectionRequestTimeout(TIMEOUT_SECONDS * 1000)
                    .build();
            httpRequestBase.setConfig(requestConfig);

            log.info("request url: {}, method: {}", httpRequestBase.getURI(), httpRequestBase.getMethod());
            response = httpClient.execute(httpRequestBase);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.info("request url: {}, method: {}, status code: {}",
                        httpRequestBase.getURI(), httpRequestBase.getMethod(), response.getStatusLine().getStatusCode());

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        responseString = EntityUtils.toString(entity, Consts.UTF_8);
                    } else {
                        throw new Exception("Response Code Is 500, Response Entity Is Null");
                    }
                } else {
                    throw new Exception("Response Status Code : " + response.getStatusLine().getStatusCode());
                }
            } else {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    responseString = EntityUtils.toString(entity, Consts.UTF_8);
                } else {
                    throw new Exception("Response Code Is 200, Response Entity Is Null");
                }
            }
        } catch (Throwable e) {
            log.warn("HttpClientUtil executeAndGet error : ", e);
            throw e;
        } finally {
            Long end = System.currentTimeMillis();
            log.info("url: {}, time costed: {} seconds", httpRequestBase.getURI(), (end - start) / 1000);
            if (!retry) {
                httpRequestBase.releaseConnection();
                httpClient = null;
            }
        }
        return responseString;
    }

    public HttpResponse executeAndGetHttpResponse(HttpRequestBase httpRequestBase) throws Exception {
        if (httpRequestBase == null) {
            throw new IllegalArgumentException("'http Request' can't be null");
        }

        HttpResponse response;
        // timeout
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT_SECONDS * 1000)
                .setConnectTimeout(TIMEOUT_SECONDS * 1000)
                .setConnectionRequestTimeout(TIMEOUT_SECONDS * 1000)
                .build();
        httpRequestBase.setConfig(requestConfig);
        response = httpClient.execute(httpRequestBase);
        return response;
    }

}
