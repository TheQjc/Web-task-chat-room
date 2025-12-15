package com.chat.listener;

import com.chat.util.DataStore;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebListener
public class OnlineUserListener implements ServletContextListener, HttpSessionListener {
    
    // 存储在线用户的会话
    private static final ConcurrentHashMap<String, String> onlineUsers = new ConcurrentHashMap<>();
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 应用启动时初始化
        sce.getServletContext().setAttribute("onlineUsers", onlineUsers);
    }
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // 会话创建时不需要特别处理
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // 会话销毁时，如果用户已经登录，则将其标记为离线
        String userId = (String) se.getSession().getAttribute("user");
        if (userId != null) {
            DataStore.logoutUser(userId);
        }
    }
}