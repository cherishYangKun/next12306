package com.next.beans;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @ClassName : PageResult
 * @Description : 分页结果集封装
 * @Author : NathenYang
 * @Date: 2020-06-07 14:32
 */

@Getter
@Setter
@ToString
@Builder
public class PageResult<T> {


    private List<T> data = Lists.newArrayList();


    private Integer total = 0;
}
