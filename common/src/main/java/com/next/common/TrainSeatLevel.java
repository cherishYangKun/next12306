package com.next.common;

import lombok.Getter;

@Getter
public enum TrainSeatLevel {

    TOP_GRADE(0, "特等座,商务座"),
    GRADE_1(1, "一等座"),
    GRADE_2(2, "二等座");


    int level;
    String desc;

    TrainSeatLevel(int level, String desc) {
        this.level = level;
        this.desc = desc;
    }
}
