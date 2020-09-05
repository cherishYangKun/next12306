package com.next.common;

import com.next.model.TrainUser;
import com.next.util.JsonMapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName : LoginFilter
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-09-05 22:44
 */

public class LoginFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request1 = (HttpServletRequest) request;
        HttpServletResponse response1 = (HttpServletResponse) response;
        TrainUser user = (TrainUser) request1.getSession().getAttribute("user");
        if (user == null) {
            JsonData jsonData = JsonData.fail(ErrorCode.USER_NOT_LOGIN.getCode(), ErrorCode.USER_NOT_LOGIN.getDesc());
            response1.setHeader("content-type", "application/json;charset=utf-8");
            response1.getWriter().print(JsonMapper.obj2String(jsonData));
            return;
        }
        RequestHolder.add(user);
        chain.doFilter(request, response);


    }

    @Override
    public void destroy() {

    }
}
