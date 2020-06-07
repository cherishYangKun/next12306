package com.next.dto;

import com.next.model.TrainStation;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName : TrainStationDto
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-05-24 21:22
 */
@ToString
public class TrainStationDto extends TrainStation {

    @Getter
    @Setter
    @ApiModelProperty("城市名称")
    public String cityName;
}
