package com.next.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : TrainNumberLeftDto
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-08-30 21:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainNumberLeftDto {

    public int id;

    public String number;


    public long leftCount;
}
