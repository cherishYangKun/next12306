package com.next.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @ClassName : GenerateParam
 * @Description : 生成座位参数
 * @Author : NathenYang
 * @Date: 2020-05-31 17:19
 */
@Getter
@Setter
@ToString
public class GenerateParam {

    @NotNull(message = "车次不能为空")
    private Integer trainNumberId;

    @NotBlank(message = "必须有出发时间")
    private String fromTime;
}
