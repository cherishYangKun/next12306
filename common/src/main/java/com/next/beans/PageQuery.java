package com.next.beans;


import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;

/**
 * @ClassName : PageQuery
 * @Description : 分页查询
 * @Author : NathenYang
 * @Date: 2020-06-07 14:28
 */

public class PageQuery {

    @Getter
    @Setter
    @Min(value = 1, message = "当前页码不合法")
    private Integer pageNo;


    @Getter
    @Setter
    @Min(value = 1, message = "每页展示数量不合法")
    private Integer pageSize;

    //偏移量
    @Setter
    private Integer offSet;


    public Integer getOffSet() {
        return (pageNo - 1) * pageSize;
    }
}
