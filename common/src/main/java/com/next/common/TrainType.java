package com.next.common;

import lombok.Getter;

/**
 * @ClassName : TrainType
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-05-26 22:50
 */

@Getter
public enum TrainType {


    CRH2(1220),
    CRH5(1224);

    TrainType(int count) {
        this.count = count;
    }

    int count;
}
