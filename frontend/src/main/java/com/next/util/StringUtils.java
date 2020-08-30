package com.next.util;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName : StringUtils
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-06-10 21:10
 */

public class StringUtils {


    public static List<Long> splitToListLong(String str) {
        List<String> strings = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(str);
        return strings.stream().map(e -> Long.valueOf(e)).collect(Collectors.toList());
    }

    public static List<Integer> splitToListInt(String str) {
        List<String> strings = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(str);
        return strings.stream().map(e -> Integer.parseInt(e)).collect(Collectors.toList());
    }
}
