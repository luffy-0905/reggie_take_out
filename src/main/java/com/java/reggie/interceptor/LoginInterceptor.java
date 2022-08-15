package com.java.reggie.interceptor;

import com.alibaba.fastjson.JSON;
import com.java.reggie.common.BaseContext;
import com.java.reggie.common.Contants;
import com.java.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("拦截的请求路径:{}",request.getRequestURI());
        //登录检查
        HttpSession session = request.getSession();
        if(session.getAttribute(Contants.SESSION_EMPLOYEE)!=null){
            //动态设置id
            Long empId = (Long) session.getAttribute(Contants.SESSION_EMPLOYEE);
            BaseContext.setCurrentId(empId);
            //放行
            return true;
        }
        //这是移动端判断登录
        if(session.getAttribute("user")!=null){
            //动态设置id
            Long userId = (Long) session.getAttribute("user");
            BaseContext.setCurrentId(userId);
            //放行
            return true;
        }
        //没有登录就返回未登录结果，通过输出流向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
