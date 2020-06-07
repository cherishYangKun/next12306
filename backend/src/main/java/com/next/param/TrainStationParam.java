package com.next.param;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @ClassName : TrainStationParam
 * @Description : 车站参数
 * @Author : NathenYang
 * @Date: 2020-05-26 21:47
 */
@Data
public class TrainStationParam {

    private Integer id;

    @NotBlank(message = "站点名称不能为空")
    @Length(min = 2, max = 20, message = "站点名称不能超过20字")
    private String name;

    @NotNull
    @Min(value = 1, message = "城市不合法参数")
    private Integer cityId;
}
