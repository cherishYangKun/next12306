package com.next.param;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;


/**
 * @ClassName : PublishTicketParam
 * @Description : 放票参数
 * @Author : NathenYang
 */
@Data
public class PublishTicketParam {


    @NotBlank(message = "车次不能为空")
    public String trainNumber;


    @NotBlank(message = "座位id不能为空")
    public String trainSeatIds;


}
