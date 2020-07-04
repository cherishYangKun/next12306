package com.next.common;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import javafx.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * @ClassName : TrainTypeSeatConstant
 * @Description : 初始化车次 车厢 排数 和座位基础数据
 * @Author : NathenYang
 * @Date: 2020-05-30 11:07
 */

public class TrainTypeSeatConstant {


    // <车厢 carriage 排row 座位 seat pair>
    private static Table<Integer, Integer, Pair<Integer, Integer>> ch2Table = HashBasedTable.create();
    // <车厢 carriage 排row 座位 seat pair> 默认座位5  不等于5时才会存储内存
    private static Table<Integer, Integer, Integer> ch2SpecialTable = HashBasedTable.create();
    //车厢数与下角表对应 每一节车厢数
    private static List<Integer> ch2CarriageRowTotal = Lists.newArrayList(0, 11, 20, 17, 20, 11, 20, 13, 13, 11, 20, 17, 20, 11, 20, 13, 13);


    // <车厢 carriage 排row 座位 seat pair>
    private static Table<Integer, Integer, Pair<Integer, Integer>> ch5Table = HashBasedTable.create();
    // <车厢 carriage 排row 座位 seat pair> 默认座位5  不等于5时才会存储内存
    private static Table<Integer, Integer, Integer> ch5SpecialTable = HashBasedTable.create();
    //车厢数与下角表对应 每一节车厢数
    private static List<Integer> ch5CarriageRowTotal = Lists.newArrayList(0, 15, 19, 19, 19, 19, 9, 16, 15, 15, 19, 19, 19, 19, 9, 16, 15);


    private static Map<TrainType, Table<Integer, Integer, Pair<Integer, Integer>>> carriageMap = Maps.newHashMap();

    //车厢座位等级 <类型,车厢,座位等级>
    private static Table<TrainType, Integer, TrainSeatLevel> seatCarriageLevelTable = HashBasedTable.create();


    static {
        //ch2初始化数据
        for (int row = 1; row <= 12; row++) {
            ch2SpecialTable.put(7, row, 4);
            ch2SpecialTable.put(15, row, 4);
        }
        ch2SpecialTable.put(7, 13, 3);
        ch2SpecialTable.put(7, 13, 3);
        ch2SpecialTable.put(8, 1, 4);
        ch2SpecialTable.put(16, 1, 4);
        for (int carriage = 1; carriage < ch2CarriageRowTotal.size(); carriage++) { //遍历车厢
            int order = 0;
            for (int row = 1; row < ch2CarriageRowTotal.get(carriage); row++) {    //遍历车厢每一排
                int count = 5;
                if (ch2SpecialTable.contains(carriage, row)) { //是否包含已初始化 特殊座位
                    count = ch2SpecialTable.get(carriage, row);
                }
                ch2Table.put(carriage, row, new Pair<>(order + 1, order + count));
                order += count;
            }
        }

        //初始化ch5数据

        for (int row = 1; row <= 15; row++) {
            ch5SpecialTable.put(8, row, 4);
            ch5SpecialTable.put(16, row, 4);
        }
        ch5SpecialTable.put(1, 1, 4);
        ch5SpecialTable.put(2, 1, 3);
        ch5SpecialTable.put(3, 1, 3);
        ch5SpecialTable.put(4, 1, 3);
        ch5SpecialTable.put(5, 1, 3);
        ch5SpecialTable.put(6, 1, 3);
        ch5SpecialTable.put(6, 9, 4);
        ch5SpecialTable.put(7, 1, 3);
        ch5SpecialTable.put(7, 16, 1);


        ch5SpecialTable.put(9, 1, 4);
        ch5SpecialTable.put(10, 1, 3);
        ch5SpecialTable.put(11, 1, 3);
        ch5SpecialTable.put(12, 1, 3);
        ch5SpecialTable.put(13, 1, 3);
        ch5SpecialTable.put(14, 1, 3);
        ch5SpecialTable.put(14, 9, 4);
        ch5SpecialTable.put(15, 1, 3);
        ch5SpecialTable.put(15, 16, 1);


        for (int carriage = 1; carriage < ch5CarriageRowTotal.size(); carriage++) { //遍历车厢
            int order = 0;
            for (int row = 1; row < ch5CarriageRowTotal.get(carriage); row++) {    //遍历车厢的每一排
                int count = 5;
                if (ch5SpecialTable.contains(carriage, row)) { //是否包含已初始化 特殊座位
                    count = ch5SpecialTable.get(carriage, row);
                }
                ch5Table.put(carriage, row, new Pair<>(order + 1, order + count));
                order += count;
            }
        }

        carriageMap.put(TrainType.CRH2, ch2Table);
        carriageMap.put(TrainType.CRH5, ch5Table);

        seatCarriageLevelTable.put(TrainType.CRH2, 1, TrainSeatLevel.TOP_GRADE);
        seatCarriageLevelTable.put(TrainType.CRH2, 2, TrainSeatLevel.GRADE_1);


        seatCarriageLevelTable.put(TrainType.CRH5, ch5CarriageRowTotal.size() - 1, TrainSeatLevel.TOP_GRADE);
        seatCarriageLevelTable.put(TrainType.CRH5, ch5CarriageRowTotal.size() - 2, TrainSeatLevel.GRADE_1);

    }


    public static Table<Integer, Integer, Pair<Integer, Integer>> getTable(TrainType type) {
        return carriageMap.get(type);
    }

    public static TrainSeatLevel getSeatLevel(TrainType type, Integer carriage) {
        if (seatCarriageLevelTable.contains(type, carriage)) {
            return seatCarriageLevelTable.get(type, carriage);
        }
        return TrainSeatLevel.GRADE_2;
    }


}
