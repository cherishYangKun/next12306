package com.next.param;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * @ClassName : TrainNumberParam
 * @Description : 车次参数
 * @Author : NathenYang
 * @Date: 2020-05-26 22:44
 */

@Getter
@Setter
public class TrainNumberParam {

    private Integer id;

    @NotBlank(message = "车次不可以为空")
    @Length(min = 2, max = 10, message = "车次名称长度需要在2-10个字之间")
    private String name;

    @NotBlank(message = "座位类型不可以为空")
    private String trainType;

    @Min(value = 1, message = "类型不合法")
    @Max(value = 4, message = "类型不合法")
    private Integer type;
}

