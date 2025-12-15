package com.chat.servlet;

import com.chat.util.DataStore;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            String user = (String) session.getAttribute("user");
            if (user != null) {
                DataStore.logoutUser(user); // 从列表中移除
            }
            session.invalidate(); // 销毁 Session
        }
        resp.sendRedirect("login");
    }
}