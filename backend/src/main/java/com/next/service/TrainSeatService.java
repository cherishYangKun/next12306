package com.next.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.next.beans.PageQuery;
import com.next.common.TrainSeatLevel;
import com.next.common.TrainType;
import com.next.common.TrainTypeSeatConstant;
import com.next.dao.TrainNumberDetailMapper;
import com.next.dao.TrainNumberMapper;
import com.next.exception.BusinessException;
import com.next.model.TrainNumber;
import com.next.model.TrainNumberDetail;
import com.next.model.TrainSeat;
import com.next.param.GenerateParam;
import com.next.param.PublishTicketParam;
import com.next.param.TrainSeatSearchParam;
import com.next.seatDao.TrainSeatMapper;
import com.next.utils.BeanValidator;
import com.next.utils.GsonUtil;
import com.next.utils.JsonMapper;
import com.next.utils.StringUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @ClassName : TrainSeatService
 * @Description :
 * @Author : NathenYang
 */
@Service
@Slf4j
public class TrainSeatService {

    @Resource
    private TrainNumberMapper trainNumberMapper;

    @Resource
    private TrainNumberDetailMapper trainNumberDetailMapper;

    @Resource
    private TrainSeatMapper trainSeatMapper;


    public List<TrainSeat> searchList(TrainSeatSearchParam param, PageQuery pageQuery) {
        BeanValidator.check(param);
        BeanValidator.check(pageQuery);
        TrainNumber trainNumber = trainNumberMapper.findByName(param.getTrainNumber());
        if (trainNumber == null) {
            throw new BusinessException("待查询车次不存在");
        }
        return trainSeatMapper.searchList(trainNumber.getId(), param.getTicket(), param.getStatus(), param.getCarriageNum(), param.getRowNum(), param.getSeatNum(), pageQuery.getOffSet(), pageQuery.getPageSize());
    }

    public Integer countList(TrainSeatSearchParam param) {
        BeanValidator.check(param);
        TrainNumber trainNumber = trainNumberMapper.findByName(param.getTrainNumber());
        if (trainNumber == null) {
            throw new BusinessException("待查询车次不存在");
        }
        return trainSeatMapper.countList(trainNumber.getId(), param.getTicket(), param.getStatus(), param.getCarriageNum(), param.getRowNum(), param.getSeatNum());
    }


    public void generate(GenerateParam param) {
        BeanValidator.check(param);
        String fromTime = param.getFromTime();
//        boolean matches = Pattern.compile("yyyy-MM-dd HH:mm").matcher(fromTime).matches();
//        if (!matches){
//            throw new BusinessException("出发日期格式不正确");
//
//        }
        //车次
        TrainNumber trainNumber = trainNumberMapper.selectByPrimaryKey(param.getTrainNumberId());
        if (trainNumber == null) {
            throw new BusinessException("当前车次不存在");
        }
        //车次详情
        List<TrainNumberDetail> trainNumberDetails = trainNumberDetailMapper.getByTrainNumberId(param.getTrainNumberId());
        if (CollectionUtils.isEmpty(trainNumberDetails)) {
            throw new BusinessException("当前车次详情不存在");
        }
        //对车次详情顺序排序
        Collections.sort(trainNumberDetails, Comparator.comparing(TrainNumberDetail::getStationIndex));
        //座位配置基础数据
        TrainType trainType = TrainType.valueOf(trainNumber.getTrainType());
        log.info("generate ticket trainType={}", trainNumber.getType());
        Table<Integer, Integer, Pair<Integer, Integer>> seatTable = TrainTypeSeatConstant.getTable(trainType);
        System.err.println(trainType + "类型车厢 排数 座位内存数据是:" + JsonMapper.obj2String(seatTable.toString()));
        log.info(trainType + "类型车厢 排数 座位内存数据是:"+ GsonUtil.toJson(seatTable.toString()));

        //时间
        //TODO 校验出发时间应该在未来
        ZoneId zoneId = ZoneId.systemDefault();
        log.info("generate ticket zoneId={}", zoneId);
        LocalDateTime fromLocalDate = LocalDateTime.parse(param.getFromTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        log.info("generate ticket fromLocalDate={}", fromLocalDate);
        String ticket = fromLocalDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        log.info("generate ticket ticket={}", ticket);
        List<TrainSeat> list = Lists.newArrayList();
        for (TrainNumberDetail trainNumberDetail : trainNumberDetails) { //遍历车次的每一段
            //每一段的发车时间
            Date fromDate = Date.from(fromLocalDate.atZone(zoneId).toInstant());
            //每一段的到站时间
            Date toDate = Date.from(fromLocalDate.plusMinutes(trainNumberDetail.getRelativeMinute().longValue()).atZone(zoneId).toInstant());
            Map<Integer, Integer> seatMapMoney = splitSeatMoney(trainNumberDetail.getMoney());
            //遍历每一节车厢  每一排
            for (Table.Cell<Integer, Integer, Pair<Integer, Integer>> cell : seatTable.cellSet()) {
                //当前车厢 获取座位数及座位等级
                Integer carriage = cell.getRowKey();
                //当前排数 锁定座位数
                Integer row = cell.getColumnKey();
                //座位等级
                TrainSeatLevel seatLevel = TrainTypeSeatConstant.getSeatLevel(trainType, carriage);
                //座位价钱
                Integer money = seatMapMoney.get(seatLevel.getLevel());
                Pair<Integer, Integer> rowSeatRange = seatTable.get(carriage, row);
                for (int index = rowSeatRange.getKey(); index <= rowSeatRange.getValue(); index++) { //遍历每一排的座位
                    TrainSeat trainSeat = TrainSeat.builder()
                            .carriageNumber(carriage)
                            .rowNumber(row)
                            .seatNumber(index)
                            .ticket(ticket)
                            .seatLevel(seatLevel.getLevel())
                            .trainStart(fromDate)
                            .trainEnd(toDate)
                            .trainNumberId(trainNumber.getId())
                            .showNumber("第" + carriage + "车厢" + row + "排" + index)
                            .fromStationId(trainNumberDetail.getFromStationId())
                            .toStationId(trainNumberDetail.getToStationId())
                            .status(0)
                            .money(money)
                            .build();
                    list.add(trainSeat);
                }
            }
            //遍历下一段出发时间 需要手动维护
            fromLocalDate = fromLocalDate.plusMinutes(trainNumberDetail.getRelativeMinute() + trainNumberDetail.getWaitMinute());
        }

        //批量保存db
        batchInsert(list);
    }


    @Transactional(rollbackFor = Exception.class)
    public void batchInsert(List<TrainSeat> list) {
        List<List<TrainSeat>> listPartition = Lists.partition(list, 1000);
        listPartition.parallelStream().forEach(partition -> {
            trainSeatMapper.batchInsert(partition);
        });
    }


    private Map<Integer, Integer> splitSeatMoney(String money) {
        //money 0:50,1:100,2:200
        Map<Integer, Integer> map = Maps.newHashMap();
        try {
            List<String> list = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(money);
            list.stream().forEach(str -> {
                String[] split = str.split(":");
                map.put(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            });
            return map;
        } catch (Exception e) {
            throw new BusinessException("价钱解析出错");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public void publish(PublishTicketParam param) {
        BeanValidator.check(param);
        TrainNumber trainNumber = trainNumberMapper.findByName(param.getTrainNumber());
        if (trainNumber == null) {
            throw new BusinessException("车次不存在");
        }
        List<Long> trainSeats = StringUtils.splitToListLong(param.getTrainSeatIds());
        List<List<Long>> partitionList = Lists.partition(trainSeats, 1000);
        for (List<Long> partition : partitionList) {
            trainSeatMapper.batchPublish(trainNumber.getId(), partition);
        }

    }


}
