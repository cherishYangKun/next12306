package com.next.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @ClassName : TrainCityParam
 * @Description : 车站参数
 * @Author : NathenYang
 * @Date: 2020-05-24 22:30
 */
@Data
@Builder
public class TrainCityParam {

    @ApiModelProperty("城市id")
    private Integer id;


    @ApiModelProperty("城市名称")
    @NotBlank(message = "车站名称不能为空")
    @Length(min = 2, max = 20, message = "城市名称长度需要在2-20个字之间")
    private String cityName;
}
