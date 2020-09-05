package com.next.dto;

import com.next.model.TrainOrder;
import com.next.model.TrainOrderDetail;
import lombok.*;

import java.util.List;

/**
 * @ClassName : TrainOrderDto
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-09-02 22:36
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrainOrderDto {

    public TrainOrder trainOrder;

    public List<TrainOrderDetail> trainOrderDetails;
}
