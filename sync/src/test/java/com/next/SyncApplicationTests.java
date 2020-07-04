package com.next;

import com.google.common.collect.Lists;
import com.next.model.TrainNumberDetail;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest(classes = SyncApplication.class)
public class SyncApplicationTests {

    public static void main(String[] args) {
        // 1 5 7 10 11
        // 1-5 1-7 1-10  1-11
        List<TrainNumberDetail> list = Lists.newArrayList();
        TrainNumberDetail trainNumberDetail = new TrainNumberDetail();
        trainNumberDetail.setFromStationId(1);
        trainNumberDetail.setToStationId(5);
        TrainNumberDetail trainNumberDetail2 = new TrainNumberDetail();
        trainNumberDetail2.setFromStationId(5);
        trainNumberDetail2.setToStationId(7);
        TrainNumberDetail trainNumberDetail3 = new TrainNumberDetail();
        trainNumberDetail3.setFromStationId(7);
        trainNumberDetail3.setToStationId(10);
        TrainNumberDetail trainNumberDetail4 = new TrainNumberDetail();
        trainNumberDetail4.setFromStationId(10);
        trainNumberDetail4.setToStationId(11);
        list.add(trainNumberDetail);
        list.add(trainNumberDetail2);
        list.add(trainNumberDetail3);
        list.add(trainNumberDetail4);
        List<String> result = Lists.newArrayList();
        for (int i = 0; i < list.size(); i++) {
            Integer fromStationId = list.get(i).getFromStationId();
            for (int j = i; j < list.size(); j++) {
                Integer toStationId = list.get(j).getToStationId();
                result.add(fromStationId+"_"+toStationId);
            }

        }
        System.out.println(result);

    }


}
