package com.next.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @ClassName : SearchLeftCountParam
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-08-30 21:50
 */
@Data
public class SearchLeftCountParam {

    public int fromStationId;

    public int toStationId;


    // 后 - > 前
    //@JsonFormat(pattern = "yyyyMMdd",timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyyMMdd")
    public String date;
}
