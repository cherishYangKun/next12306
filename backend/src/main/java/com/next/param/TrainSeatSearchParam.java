package com.next.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;


/**
 * @ClassName : TrainSeatSearchParam
 * @Description : 座位查询参数
 * @Author : NathenYang
 */

@Getter
@Setter
@ToString
@ApiModel(value = "座位查询参数")
public class TrainSeatSearchParam {


    @NotBlank(message = "车次查询不能为空")
    @Length(min = 2, max = 20, message = "车次长度必须在2-20子内")
    @ApiModelProperty(required = true, value = "车次名称")
    private String trainNumber;


    @NotBlank(message = "出发日期不能为空")
    @Length(min = 8, max = 8, message = "出发日期必须是yyyyMMdd")
    @ApiModelProperty(required = true, value = "日期")
    private String ticket;

    @ApiModelProperty(value = "状态", required = false)
    private Integer status;


    @ApiModelProperty(value = "车厢书", required = false)
    private Integer carriageNum;


    @ApiModelProperty(value = "排数", required = false)
    private Integer rowNum;

    @ApiModelProperty(value = "座位号", required = false)
    private Integer seatNum;

}
