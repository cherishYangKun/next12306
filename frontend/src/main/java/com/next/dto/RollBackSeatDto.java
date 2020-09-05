package com.next.dto;

import com.next.model.TrainSeat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName : RollBackSeatDto
 * @Description :座位回滚消息
 * @Author : NathenYang
 * @Date: 2020-09-02 21:13
 */
@Getter
@Setter
@ToString
public class RollBackSeatDto {

    private TrainSeat trainSeat;
    private List<Integer> fromStationIds;
}
