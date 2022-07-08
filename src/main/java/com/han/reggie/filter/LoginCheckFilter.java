package com.han.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.han.reggie.common.R;
import com.han.reggie.config.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
//登录过滤器

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    // 路径匹配器，支持通配符

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
//        1、获取本次请求的URI
        String uri = request.getRequestURI();
        // 定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                //移动端发送短信
                "/user/sendMsg",
                //移动端登录
                "/user/login"
        };
//        2、判断本次请求是否需要处理
        boolean check = check(urls, uri);
//        3、如果不需要处理，则直接放行
        if (check) {
            filterChain.doFilter(request, response);
            return;
        }
//        4-1、判断后台员工登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("employee") != null && request.getSession().getAttribute("employee") != "") {
//            获取id
            Long empId = (Long) request.getSession().getAttribute("employee");
//            存储id到线程中
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request, response);
            return;
        }
//       4-2、判断前台用户登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("user") != null && request.getSession().getAttribute("user") != "") {
//            获取id
            Long userId = (Long) request.getSession().getAttribute("user");
//            存储id到线程中
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request, response);
            return;
        }
//        5、如果未登录则返回未登录结果，通过输出流的方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     *
     * @param urls
     * @param uri
     * @return
     */
    public boolean check(String[] urls, String uri) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, uri);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
