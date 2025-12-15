package com.chat.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class UrlRewriteFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化方法
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        String path = req.getRequestURI().substring(req.getContextPath().length());
        
        // 重写URL，隐藏.jsp后缀
        if ("/".equals(path) || "/index".equals(path)) {
            req.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        } else if ("/login".equals(path) && "GET".equals(req.getMethod())) {
            req.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        } else if ("/chat".equals(path)) {
            req.getRequestDispatcher("/chat.jsp").forward(request, response);
            return;
        }
        
        // 继续执行其他过滤器或目标资源
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 销毁方法
    }
}