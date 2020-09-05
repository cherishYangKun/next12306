package com.next.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;


/**
 * @ClassName : GrabTicketParam
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-09-02 00:21
 */
@Getter
@Setter
@ToString
public class GrabTicketParam {

    public int fromStationId;

    public int toStationId;

    // 后 - > 前
    //@JsonFormat(pattern = "yyyyMMdd",timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyyMMdd")
    public String date;

    @NotBlank(message = "车次不能为空")
    public String number;

    @NotBlank(message = "乘客信息不能为空")
    public String travellerIds;
}
