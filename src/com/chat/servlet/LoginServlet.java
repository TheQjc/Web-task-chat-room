package com.chat.servlet;

import com.chat.util.DataStore;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置编码
        req.setCharacterEncoding("UTF-8");
        String username = req.getParameter("username");

        if (username == null || username.trim().isEmpty()) {
            req.setAttribute("error", "用户名不能为空");
            // 【请求转发】带错误信息回 login.jsp
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        // 尝试登录（检查重名）
        boolean success = DataStore.loginUser(username);
        if (success) {
            // 【Session】记录登录状态
            HttpSession session = req.getSession();
            session.setAttribute("user", username);
            session.setMaxInactiveInterval(30 * 60); // 30分钟过期

            // 【Cookie】可选：记住用户名方便下次输入
            Cookie userCookie = new Cookie("lastUser", username);
            userCookie.setMaxAge(60 * 60 * 24); // 1天
            resp.addCookie(userCookie);

            // 【重定向】跳转到聊天室
            resp.sendRedirect(req.getContextPath() + "/chat.jsp");
        } else {
            req.setAttribute("error", "用户名已存在，请换一个");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}