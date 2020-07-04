package com.next.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @ClassName : EsClient
 * @Description : Es api客户端封装
 * @Author : NathenYang
 */

@Service
@Slf4j
public class EsClient implements ApplicationListener<ContextRefreshedEvent> {


    //容器初始化时 事件触发器 回调 onApplicationEvent();
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            initClient();
        } catch (Exception e) {
            log.error("es client init exception", e);
            try {
                Thread.sleep(1000);
            } catch (Exception e1) {

            }
            initClient(); //网络异常
        }
    }

    public static final int CONNECT_TIME_OUT = 100;
    public static final int SOCKET_TIME_OUT = 60 * 1000;
    public static final int REQUEST_TIMEOUT = SOCKET_TIME_OUT;
    public RestHighLevelClient restHighLevelClient;  //jdk8
    public BasicHeader[] basicHeaders;


    public void initClient() {
        log.info("es clinet is running");
        basicHeaders = new BasicHeader[]{new BasicHeader("Accept", "application/json;charset=utf8")};
        RestClientBuilder builder = RestClient.builder(new HttpHost("127.0.0.1", 9200, "http"));
        builder.setDefaultHeaders(basicHeaders)
                .setRequestConfigCallback((RequestConfig.Builder configBuilder) -> {
                    configBuilder.setConnectTimeout(CONNECT_TIME_OUT);
                    configBuilder.setSocketTimeout(SOCKET_TIME_OUT);
                    configBuilder.setConnectionRequestTimeout(REQUEST_TIMEOUT);
                    return configBuilder;
                });
        restHighLevelClient = new RestHighLevelClient(builder);
        log.info("es client is end");
    }

    public IndexResponse index(IndexRequest indexRequest) throws Exception {
        try {
            return restHighLevelClient.index(indexRequest, basicHeaders);
        } catch (IOException e) {
            log.error("es.index exception,indexRequest:{}", indexRequest);
            throw e;
        }
    }

    public UpdateResponse update(UpdateRequest updateRequest) throws Exception {
        try {
            return restHighLevelClient.update(updateRequest, basicHeaders);
        } catch (IOException e) {
            log.error("es.update exception ,updateRequest:{}", updateRequest);
            throw e;
        }
    }

    public GetResponse get(GetRequest getRequest) throws Exception {
        try {
            return restHighLevelClient.get(getRequest, basicHeaders);
        } catch (IOException e) {
            log.error("es.get exception ,getRequest:{}", getRequest);
            throw e;
        }
    }

    public MultiGetResponse muliGet(MultiGetRequest multiGetRequest) throws Exception {
        try {
            return restHighLevelClient.multiGet(multiGetRequest, basicHeaders);
        } catch (IOException e) {
            log.error("es.multiGet exception MultiGetRequest:{}", multiGetRequest);
            throw e;
        }
    }

    public BulkResponse bulk(BulkRequest bulkRequest) throws Exception {
        try {
            return restHighLevelClient.bulk(bulkRequest);
        } catch (IOException e) {
            log.error("es.bulk exception bulkRequest:{}", bulkRequest);
            throw e;
        }

    }

}
