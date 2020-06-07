package com.next.dto;

import com.next.model.TrainNumberDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : TrainNumberDetailDto
 * @Description : 车次详情数据封装
 * @Author : NathenYang
 * @Date: 2020-05-24 22:17
 */

@ToString
public class TrainNumberDetailDto extends TrainNumberDetail {


    @Getter
    @Setter
    @ApiModelProperty("始发车站")
    private String fromStation;

    @Getter
    @Setter
    @ApiModelProperty("终点车站")
    private String toStation;

    @Getter
    @Setter
    @ApiModelProperty("车次名称")
    private String trainNumber;
}
