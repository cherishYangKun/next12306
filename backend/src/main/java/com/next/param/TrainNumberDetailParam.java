package com.next.param;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @ClassName : TrainNumberDetailParam
 * @Description : 车次详情参数
 * @Author : NathenYang
 * @Date: 2020-05-27 21:59
 */
@Data
public class TrainNumberDetailParam {


    @NotNull(message = "车次不能为空")
    private Integer trainNumberId;

    @NotNull(message = "出发站不能为空")
    private Integer fromStationId;

    @NotNull(message = "到达站不能为空")
    private Integer toStationId;

    @NotNull(message = "相对出发时间不能为空")
    private Integer relativeMinute;

    @NotNull(message = "等待时间不能为空")
    private Integer waitMinute;


    @NotBlank(message = "座位价钱不能为空")
    private String money;

    @Min(0) //代表该车次还有详情可以添加
    @Max(1) //代表车次详情全部添加完成
    private Integer end;
}
