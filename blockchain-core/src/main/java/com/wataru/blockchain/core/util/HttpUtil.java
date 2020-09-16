package com.wataru.blockchain.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class HttpUtil {
    /**
     * 创建get请求
     */
    public static String get(String url, Integer timeout, Map<String, String> header) {
        String result = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            RequestConfig requestConfig = RequestConfig.custom()
                    // 设置连接超时时间(单位毫秒)
                    .setConnectTimeout(timeout)
                    // 设置请求超时时间(单位毫秒)
                    .setConnectionRequestTimeout(timeout)
                    // socket读写超时时间(单位毫秒)
                    .setSocketTimeout(timeout)
                    // 设置是否允许重定向(默认为true)
                    .setRedirectsEnabled(false).build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            if (header != null) {
                header.forEach(httpGet::setHeader);
            }
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity responseEntity = response.getEntity();
                if (response.getStatusLine().getStatusCode() == 200) {
                    if (responseEntity != null) {
                        result = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                    }
                }
            }
        } catch (IOException e) {
            log.error("", e);
        }
        return result;
    }

    /**
     * 创建get请求
     */
    public static String post(String url, Integer timeout, Map<String, String> header, String body) {
        String result = null;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            RequestConfig requestConfig = RequestConfig.custom()
                    // 设置连接超时时间(单位毫秒)
                    .setConnectTimeout(timeout)
                    // 设置请求超时时间(单位毫秒)
                    .setConnectionRequestTimeout(timeout)
                    // socket读写超时时间(单位毫秒)
                    .setSocketTimeout(timeout)
                    // 设置是否允许重定向(默认为true)
                    .setRedirectsEnabled(false).build();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            StringEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json;charset=utf8");
            if (header != null) {
                header.forEach(httpPost::setHeader);
            }
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                if (response.getStatusLine().getStatusCode() == 200) {
                    if (responseEntity != null) {
                        result = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                    }
                }
            }
        } catch (IOException e) {
            log.error("", e);
        }
        return result;
    }
}
