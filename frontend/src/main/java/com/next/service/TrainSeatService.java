package com.next.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.next.common.TrainEsConstant;
import com.next.common.TrainSeatLevel;
import com.next.common.TrainType;
import com.next.common.TrainTypeSeatConstant;
import com.next.dto.RollBackSeatDto;
import com.next.dto.TrainNumberLeftDto;
import com.next.dto.TrainOrderDto;
import com.next.exception.BusinessException;
import com.next.model.*;
import com.next.mq.MessageBody;
import com.next.mq.QueueTopic;
import com.next.mq.RabbitMqClient;
import com.next.orderDao.TrainOrderDetailMapper;
import com.next.orderDao.TrainOrderMapper;
import com.next.param.GrabTicketParam;
import com.next.param.SearchLeftCountParam;
import com.next.seatDao.TrainSeatMapper;
import com.next.util.BeanValidator;
import com.next.util.JsonMapper;
import com.next.util.StringUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.jackson.type.TypeReference;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName : TrainSeatService
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-08-30 21:57
 */
@Service
@Slf4j
public class TrainSeatService {


    @Resource
    public EsClient esClient;

    @Resource
    public TrainCacheService trainCacheService;

    @Resource
    public TrainNumberService trainNumberService;

    @Resource
    public TrainSeatMapper trainSeatMapper;

    @Resource
    public RabbitMqClient rabbitMqClient;

    @Resource
    public TrainOrderMapper trainOrderMapper;

    @Resource
    public TrainOrderDetailMapper trainOrderDetailMapper;

    @Resource
    public TransactionService transactionService;

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 20, 2, TimeUnit.MINUTES, new ArrayBlockingQueue<>(200), new ThreadPoolExecutor.CallerRunsPolicy());

    public List<TrainNumberLeftDto> searchLeftCount(SearchLeftCountParam param) throws Exception {
        BeanValidator.check(param);
        List<TrainNumberLeftDto> dtoList = Lists.newArrayList();
        //从es中获取数据
        GetRequest getRequest = new GetRequest(TrainEsConstant.INDEX, TrainEsConstant.TYPE, param.getFromStationId() + "_" + param.getToStationId());
        GetResponse getResponse = esClient.get(getRequest);
        if (getResponse == null) {
            throw new BusinessException("数据查询异常,请重试");
        }
        Map<String, Object> map = getResponse.getSourceAsMap();
        if (MapUtils.isEmpty(map)) {
            return dtoList;
        }

        String trainNumbers = (String) map.get(TrainEsConstant.COLUMN_TRAIN_NUMBER);
        log.info("from es get data trainNumbers={}", JsonMapper.obj2String(trainNumbers));
        //拆分所有车次
        List<String> numberList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(trainNumbers);

        numberList.parallelStream().forEach(number -> {
            TrainNumber trainNumber = trainNumberService.findByNameFromCache(number);
            if (trainNumber == null) {
                return;
            }
            String detailStr = trainCacheService.get("TN_" + number);
            List<TrainNumberDetail> detailList = JsonMapper.string2Obj(detailStr, new TypeReference<List<TrainNumberDetail>>() {
            });
            Map<Integer, TrainNumberDetail> dataMap = Maps.newHashMap();
            detailList.stream().forEach(detail -> dataMap.put(detail.getFromStationId(), detail));
            /**
             * detailList : {1,2},{2,3},{3,4},{4,5},{5,6}
             * dataMap : 1 -> {1,2} 2 -> {2,3} ... 5->{5,6}
             * param: 2 -> 5
             * target : {2,3},{3,4},{4,5}
             * leftCount : {2,3} -> 5  {3,4}-> 3 {4,5} -> 10 min 3
             */
            int curFromStationId = param.getFromStationId();
            int targetToStationId = param.getToStationId();
            Long min = Long.MAX_VALUE;
            Boolean isSuccess = false;
            String redisKey = number + "_" + param.getDate() + "_Count";
            while (true) {
                TrainNumberDetail trainNumberDetail = dataMap.get(curFromStationId);
                if (trainNumberDetail == null) {
                    log.error("detail is null stationId={},number={}", curFromStationId, number);
                    break;
                }
                //从 redis中获取该车次详情剩余的座位数 取出最小值
                min = Math.min(min, NumberUtils.toLong(trainCacheService.hget(redisKey, trainNumberDetail.getFromStationId() + "_" + trainNumberDetail.getToStationId())));
                if (trainNumberDetail.getToStationId() == targetToStationId) {
                    isSuccess = true;
                    break;
                }
                //下次查询 起始站 从该车次详情的终点站
                curFromStationId = trainNumberDetail.getToStationId();
            }

            if (isSuccess) {
                dtoList.add(new TrainNumberLeftDto(trainNumber.getId(), number, min));
            }

        });
        return dtoList;
    }


    public TrainOrderDto grabTicket(GrabTicketParam param, TrainUser trainUser) {
        BeanValidator.check(param);
        //车厢详情数据准备
        List<Long> travellerIds = StringUtils.splitToListLong(param.getTravellerIds());
        if (CollectionUtils.isEmpty(travellerIds)) {
            throw new BusinessException("必须指定乘车人");
        }
        TrainNumber trainNumber = trainNumberService.findByNameFromCache(param.getNumber());
        if (trainNumber == null) {
            throw new BusinessException("车次不能为空");
        }
        String detailStr = trainCacheService.get("TN_" + param.getNumber());
        List<TrainNumberDetail> detailList = JsonMapper.string2Obj(detailStr, new TypeReference<List<TrainNumberDetail>>() {
        });
        Map<Integer, TrainNumberDetail> dataMap = Maps.newHashMap();
        detailList.stream().forEach(detail -> dataMap.put(detail.getFromStationId(), detail));
        //实际车次详情信息
        List<TrainNumberDetail> targetDetailLists = Lists.newArrayList();
        int curFromStationId = param.getFromStationId();
        int targetToStationId = param.getToStationId();
        String redisKey = param.getNumber() + "_" + param.getDate() + "_Count";
        while (true) {
            TrainNumberDetail trainNumberDetail = dataMap.get(curFromStationId);
            if (trainNumberDetail == null) {
                throw new BusinessException("实际车次详情不存在");
            }
            targetDetailLists.add(trainNumberDetail);
            if (trainNumberDetail.getToStationId() == targetToStationId) {
                break;
            }
            //下次查询 起始站 从该车次详情的终点站
            curFromStationId = trainNumberDetail.getToStationId();
        }
        //实际车次座位信息
        String seatRedisKey = param.getNumber() + "_" + param.getDate();
        Map<String, String> seatMap = trainCacheService.hgetAll(seatRedisKey);
        //指定车次座位布局
        TrainType trainType = TrainType.valueOf(trainNumber.getTrainType());
        Table<Integer, Integer, Pair<Integer, Integer>> seatTable = TrainTypeSeatConstant.getTable(trainType);

        String parentOrderId = UUID.randomUUID().toString(); //生成主订单号
        List<TrainOrderDetail> trainOrderDetails = Lists.newArrayList(); //订单详情列表信息
        List<TrainSeat> trainSeats = Lists.newArrayList();//最终抢到座位列表信息
        int totalMoney = 0;
        for (Long travellerId : travellerIds) {
            //已筛选出符合条件的座位 并且已占票
            TrainSeat tmpTrainSeat = selectOneMatchSeat(seatTable, targetDetailLists, seatMap, trainNumber, travellerId, trainUser.getId(), param.getDate());
            if (tmpTrainSeat == null) {
                break;
            }
            //初始化订单详情信息
            TrainSeatLevel seatLevel = TrainTypeSeatConstant.getSeatLevel(trainType, tmpTrainSeat.getCarriageNumber());
            TrainOrderDetail trainOrderDetail = TrainOrderDetail.builder()
                    .parentOrderId(parentOrderId)
                    .orderId(UUID.randomUUID().toString())
                    .carriageNumber(tmpTrainSeat.getCarriageNumber())
                    .rowNumber(tmpTrainSeat.getRowNumber())
                    .seatNumber(tmpTrainSeat.getSeatNumber())
                    .seatLevel(seatLevel.getLevel())
                    .userId(trainUser.getId())
                    .travellerId(travellerId)
                    .trainNumberId(trainNumber.getId())
                    .fromStationId(param.getFromStationId())
                    .toStationId(param.getToStationId())
                    .trainStart(tmpTrainSeat.getTrainStart())
                    .trainEnd(tmpTrainSeat.getTrainEnd())
                    .money(tmpTrainSeat.getMoney())
                    .ticket(param.getDate())
                    .createTime(new Date())
                    .updateTime(new Date())
                    .expireTime(DateUtils.addMinutes(new Date(), 30))
                    .showNumber("")
                    .build();
            totalMoney += tmpTrainSeat.getMoney();
            trainOrderDetails.add(trainOrderDetail);
            trainSeats.add(tmpTrainSeat);
        }
        if (trainSeats.size() < travellerIds.size()) {
            //回滚座位
            rollBackPlace(trainSeats, targetDetailLists);
            throw new BusinessException("座位不足");
        }
        //生成主订单信息
        TrainOrder trainOrder = TrainOrder.builder()
                .orderId(parentOrderId)
                .userId(trainUser.getId())
                .ticket(param.getDate())
                .trainNumberId(trainNumber.getId())
                .fromStationId(param.getFromStationId())
                .toStationId(param.getToStationId())
                .totalMoney(totalMoney)
                .status(10)
                .trainStart(trainOrderDetails.get(0).getTrainStart())
                .trainEnd(trainOrderDetails.get(0).getTrainEnd())
                .createTime(new Date())
                .updateTime(new Date())
                .expireTime(DateUtils.addMinutes(new Date(), 30))
                .build();

        //保存订单及详情信息(事务性保存)
        try {
            transactionService.saveOrder(trainOrder, trainOrderDetails);
        } catch (Exception e) {
            //注意需要回滚座位占座
            rollBackPlace(trainSeats, targetDetailLists);
            log.error("saveOrder exception trainOrder={} ,trainOrderList={}", trainOrder, JsonMapper.obj2String(trainOrderDetails));
            e.printStackTrace();
        }
        log.info("saveOrder success trainOrder={},trainOrderDetails={}", trainOrder, JsonMapper.obj2String(trainOrderDetails));

        //发送订单创建消息
        MessageBody messageBody1 = new MessageBody();
        messageBody1.setTopic(QueueTopic.ORDER_CREATE);
        messageBody1.setContent(JsonMapper.obj2String(trainOrder));
        rabbitMqClient.send(messageBody1);

        //发送延迟订单支付消息
        MessageBody messageBody2 = new MessageBody();
        messageBody2.setDelay(60 * 1000 * 30);
        messageBody2.setContent(JsonMapper.obj2String(trainOrder));
        messageBody2.setTopic(QueueTopic.ORDER_PAY_DELAY_CHECK);

        //返回核心订单数据
        return TrainOrderDto.builder().trainOrder(trainOrder).trainOrderDetails(trainOrderDetails).build();


    }

    //这种事务是错误的 已测试走坑
    @Transactional(rollbackFor = Exception.class)
    public void saveOrder(TrainOrder trainOrder, List<TrainOrderDetail> trainOrderDetails) {
        for (TrainOrderDetail trainOrderDetail : trainOrderDetails) {
            trainOrderDetailMapper.insertSelective(trainOrderDetail);
        }
        trainOrderMapper.insertSelective(trainOrder);
    }


    //筛选出符合条件的座位 carriage/row/seatNo 在指定车次详情里面 并且是空余座位  完成占座
    private TrainSeat selectOneMatchSeat(Table<Integer, Integer, Pair<Integer, Integer>> seatTable,
                                         List<TrainNumberDetail> targerDetailList,
                                         Map<String, String> seatMap,
                                         TrainNumber trainNumber,
                                         Long travellerId,
                                         Long userId,
                                         String ticket) {
        for (Table.Cell<Integer, Integer, Pair<Integer, Integer>> cell : seatTable.cellSet()) { //遍历每一节车厢
            Integer carriage = cell.getRowKey(); //车厢
            Integer row = cell.getColumnKey(); //排数
            Pair<Integer, Integer> seatNo = seatTable.get(carriage, row);//座位号 [1-4]
            for (int index = seatNo.getKey(); index < seatNo.getValue(); index++) { //遍历每一排座位号
                int cnt = 0;
                for (TrainNumberDetail detail : targerDetailList) {
                    String cacheKey = carriage + "_" + row + "_" + index + "_" + detail.getFromStationId() + "_" + detail.getToStationId();
                    if (!seatMap.containsKey(cacheKey) || NumberUtils.toInt(seatMap.get(cacheKey), 0) != 0) { //无座  或者是被占座
                        break;
                    }
                    cnt++;
                }
                if (cnt == targerDetailList.size()) { //这座位在本次需要的所有车次详情里面 都是空余的 可以占座
                    //数据检查通过 可以更新DB占座
                    TrainSeat trainSeat = TrainSeat.builder()
                            .trainNumberId(trainNumber.getId())
                            .travellerId(travellerId)
                            .userId(userId)
                            .carriageNumber(carriage)
                            .rowNumber(row)
                            .seatNumber(index)
                            .build();
                    try {
                        trainSeat = place(trainSeat, targerDetailList, ticket); //占座
                        if (trainSeat != null) {
                            log.info("place success trainSeat={}", trainSeat);
                            //更新该座位状态 --> 已被占座
                            for (TrainNumberDetail trainNumberDetail : targerDetailList) {
                                // 1 -> 被占座
                                seatMap.put(carriage + "_" + row + "_" + index + "_" + trainNumberDetail.getFromStationId() + "_" + trainNumberDetail.getToStationId(), "1");
                                return trainSeat;
                            }
                        }
                    } catch (BusinessException e) {
                        log.error("place BusinessException ,{} {}", trainSeat, e.getMessage());
                    } catch (Exception e2) {
                        log.error("place Exception ,{}", trainSeat, e2);
                    }
                }
            }
        }
        return null;

    }

    //占座
    //  1车厢/1排/座位1 D386  北京 -> 锦州 -> 唐山 ->大连
    // 实际占票 车次详情 北京 -> 唐山   这两段车次详情区间 开始时间 -> 到达时间  金额等等
    private TrainSeat place(TrainSeat seat, List<TrainNumberDetail> targetDetailList, String ticket) {
        List<Integer> fromStationIds = targetDetailList.stream().map(trainNumberDetail -> trainNumberDetail.getFromStationId()).collect(Collectors.toList());
        List<TrainSeat> updateTrainSeats = trainSeatMapper.getToPlaceSeatList(seat.getTrainNumberId(), seat.getCarriageNumber(), seat.getRowNumber(), seat.getSeatNumber(), fromStationIds, ticket);
        if (fromStationIds.size() != updateTrainSeats.size()) {
            //占座的位置不够 可能是某个车次详情 被占了
            return null;
        }
        List<Long> idList = updateTrainSeats.stream().map(seat1 -> seat.getId()).collect(Collectors.toList());
        //批量更新 占座
        int ret = trainSeatMapper.batchPlace(seat.getTrainNumberId(), idList, seat.getTravellerId(), seat.getUserId());
        if (ret != idList.size()) {
            //回滚座位
            rollBackPlace(updateTrainSeats, targetDetailList);
            throw new BusinessException("座位被占" + seat.toString());
        }
        //返回座位信息
        return TrainSeat.builder().trainNumberId(seat.getTrainNumberId())
                .carriageNumber(seat.getCarriageNumber())
                .rowNumber(seat.getRowNumber())
                .seatNumber(seat.getSeatNumber())
                .travellerId(seat.getTravellerId())
                .userId(seat.getUserId())
                .trainStart(updateTrainSeats.get(0).getTrainStart())
                .trainEnd(updateTrainSeats.get(updateTrainSeats.size() - 1).getTrainEnd())
                .money(updateTrainSeats.stream().collect(Collectors.summingInt(TrainSeat::getMoney)))
                .build();
    }

    //回滚占票座位信息
    private void rollBackPlace(List<TrainSeat> trainSeats, List<TrainNumberDetail> targetDetailLists) {
        //异步 优先解决用户快速线程  使用线程池
        executor.submit(() -> {
            for (TrainSeat trainSeat : trainSeats) {
                log.info("rollback seat ,seat={}", trainSeat);
                List<Integer> fromStationIds = targetDetailLists.stream().map(e -> e.getFromStationId()).collect(Collectors.toList());
                rollBackSeat(trainSeat, fromStationIds, 0);
            }
        });
    }

    /**
     * rollbackSeat 回滚 若发生网络异常 超时 断网 或者是内存不够 或者是获取不到DB连接 或者是数据服务挂了 等异常处理
     *
     * @param trainSeat
     * @param fromStationIds
     */
    public void rollBackSeat(TrainSeat trainSeat, List<Integer> fromStationIds, int delayMillSeconds) {
        try {
            trainSeatMapper.batchRollbackPlace(trainSeat, fromStationIds);
        } catch (Exception e) {
            log.error("rollbackSeat exception trainSeat={}", trainSeat, e);
            //通过延迟消息队列  捕获重试回滚座位
            RollBackSeatDto rollBackSeatDto = new RollBackSeatDto();
            rollBackSeatDto.setTrainSeat(trainSeat);
            rollBackSeatDto.setFromStationIds(fromStationIds);

            MessageBody messageBody = new MessageBody();
            messageBody.setTopic(QueueTopic.SEAT_PLACE_ROLLBACK);
            delayMillSeconds = delayMillSeconds * 2 + 1000;
            messageBody.setContent(JsonMapper.obj2String(rollBackSeatDto));
            messageBody.setDelay(delayMillSeconds);
            rabbitMqClient.sendDelay(messageBody, delayMillSeconds);
        }

    }
}
