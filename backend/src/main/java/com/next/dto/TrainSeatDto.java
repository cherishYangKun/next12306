package com.next.dto;

import com.next.model.TrainSeat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : TrainSeatDto
 * @Description : 座位前端数据组装
 * @Author : NathenYang
 */
@ToString
public class TrainSeatDto extends TrainSeat {

    @Getter
    @Setter
    @ApiModelProperty("车次名称")
    private String trainNumber;
    @Getter
    @Setter
    @ApiModelProperty("出发站")
    private String fromStation;
    @Getter
    @Setter
    @ApiModelProperty("到达站")
    private String toStation;
    @Getter
    @Setter
    @ApiModelProperty("出发时间")
    private String showStart;
    @Getter
    @Setter
    @ApiModelProperty("到站时间")
    private String showEnd;
}
