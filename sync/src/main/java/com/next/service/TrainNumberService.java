package com.next.service;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.next.common.TrainEsConstant;
import com.next.dao.TrainNumberDetailMapper;
import com.next.dao.TrainNumberMapper;
import com.next.model.TrainNumber;
import com.next.model.TrainNumberDetail;
import com.next.util.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName : TrainNumberService
 * @Description :
 * @Author : NathenYang
 */
@Service
@Slf4j
public class TrainNumberService {

    @Resource
    private TrainNumberMapper trainNumberMapper;

    @Resource
    private TrainNumberDetailMapper trainNumberDetailMapper;

    @Resource
    private TrainCacheService trainCacheService;


    @Resource
    public EsClient esClient;

    public void handle(List<CanalEntry.Column> columns, CanalEntry.EventType eventType) throws Exception {
        if (eventType != CanalEntry.EventType.UPDATE) {
            log.info("not update , no need care");
            return;
        }
        int trainNumberId = 0;
        for (CanalEntry.Column column : columns) {
            if (column.getName().equals("id")) {
                trainNumberId = Integer.parseInt(column.getValue());
                break;
            }
        }
        TrainNumber trainNumber = trainNumberMapper.selectByPrimaryKey(trainNumberId);
        if (trainNumber == null) {
            log.error("not found trainNumber ,trainmNumberId:{}", trainNumberId);
            return;
        }
        List<TrainNumberDetail> detailList = trainNumberDetailMapper.getByTrainNumberId(trainNumberId);
        if (CollectionUtils.isEmpty(detailList)) {
            log.warn("no trainDetail,no need care trainNumber ：{}", trainNumber.getName());
        }
        //同步到redis 车次详情
        trainCacheService.set("TN_" + trainNumber.getName(), JsonMapper.obj2String(detailList));
        log.info("trainNumber:{} detailList updated redis ", trainNumber.getName());
        //同步到ES key :两车站 所有的车次信息
        saveEs(detailList, trainNumber);
        log.info("trainNumber:{} detailList updated ES ", trainNumber.getName());


    }


    public void saveEs(List<TrainNumberDetail> detailList, TrainNumber trainNumber) throws Exception {
        /**
         * A -> B fromStationId -> toStationId
         * trainNumber 车次  D123 : 北京 -> 锦州 ->大连 D222 北京 -> 烟台 -> 大连
         * 北京 -> 大连
         * D123 : 北京-锦州 锦州-大连  北京-大连
         * D222 : 北京-烟台 烟台-大连  北京-大连
         * ES key: fromStationId->toStationId value:trainNumberId,trainNumberId....
         */
        List<String> list = Lists.newArrayList();
        if (detailList.size() == 1) {
            Integer fromStationId = trainNumber.getFromStationId();
            Integer toStationId = trainNumber.getToStationId();
            list.add(fromStationId + "_" + toStationId);
        } else {
            for (int i = 0; i < detailList.size(); i++) {
                Integer fromStationId = detailList.get(i).getFromStationId();
                for (int j = i; j < detailList.size(); j++) {
                    Integer tmpStationId = detailList.get(j).getToStationId();
                    list.add(fromStationId + "_" + tmpStationId);
                }
            }
        }
        //wrong 经多次测试 性能很差
//        for (String item : list) {
//            GetRequest getRequest = new GetRequest(TrainEsConstant.INDEX, TrainEsConstant.TYPE, item);
//            GetResponse getResponse = esClient.get(getRequest);
//        }

        //批量组装请求 获取ES数据
        MultiGetRequest multiGetRequest = new MultiGetRequest();
        BulkRequest bulkRequest = new BulkRequest();
        for (String item : list) {
            multiGetRequest.add(new MultiGetRequest.Item(TrainEsConstant.INDEX, TrainEsConstant.TYPE, item));
        }
        //处理每一项响应
        MultiGetResponse multiGetItemResponses = esClient.muliGet(multiGetRequest);
        for (MultiGetItemResponse itemResponse : multiGetItemResponses.getResponses()) {
            if (itemResponse.isFailed()) {
                log.error("multiGet item faild ,itemResponse:{}", itemResponse);
                continue;
            }
            GetResponse response = itemResponse.getResponse();
            if (response == null) {
                log.error("mutiGet item response is null response:{}", response);
                continue;
            }
            //存储ES数据
            Map<Object, Object> dataMap = Maps.newHashMap();
            Map<String, Object> map = response.getSourceAsMap();
            if (!response.isExists() || map == null) {
                //add index
                dataMap.put(TrainEsConstant.COLUMN_TRAIN_NUMBER, trainNumber.getName());
                IndexRequest indexRequest = new IndexRequest(TrainEsConstant.INDEX, TrainEsConstant.TYPE, response.getId()).source(dataMap);
                bulkRequest.add(indexRequest);
                continue;

            }
            String origin = (String) map.get(TrainEsConstant.COLUMN_TRAIN_NUMBER);
            Set<String> set = Sets.newHashSet(Splitter.on(",").trimResults().omitEmptyStrings().split(origin));
            if (!set.contains(trainNumber.getName())) {
                //update index
                dataMap.put(TrainEsConstant.COLUMN_TRAIN_NUMBER, origin + "," + trainNumber.getName());
                UpdateRequest updateRequest = new UpdateRequest(TrainEsConstant.INDEX, TrainEsConstant.TYPE, response.getId()).doc(dataMap);
                bulkRequest.add(updateRequest);
            }
        }
        //批量更新ES数据
        BulkResponse bulkResponses = esClient.bulk(bulkRequest);
        log.info("es bulk ,bulkResponses:{}", JsonMapper.obj2String(bulkResponses));
        if (bulkResponses.hasFailures()) {
            throw new RuntimeException("es bulk failure");
        }
    }
}
