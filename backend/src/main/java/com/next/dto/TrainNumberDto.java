package com.next.dto;

import com.next.model.TrainNumber;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : TrainNumberDto
 * @Description : 车次数据传输
 * @Author : NathenYang
 * @Date: 2020-05-24 22:05
 */
@ToString
public class TrainNumberDto extends TrainNumber {


    @Getter
    @Setter
    @ApiModelProperty("始发车站")
    public String fromStation;

    @Getter
    @Setter
    @ApiModelProperty("终点车站")
    public String toStation;
}
