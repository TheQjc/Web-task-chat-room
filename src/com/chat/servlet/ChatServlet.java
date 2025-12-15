package com.chat.servlet;

import com.chat.entity.Message;
import com.chat.util.DataStore;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@WebServlet("/chatAction")
public class ChatServlet extends HttpServlet {

    // GET 请求用于前端轮询获取最新数据 (JSON格式)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(401);
            return;
        }

        String currentUser = (String) session.getAttribute("user");

        // 获取在线用户
        Set<String> users = DataStore.getOnlineUsers();

        // 获取消息（过滤逻辑：只显示 群聊 OR 发给我的 OR 我发的）
        List<Message> allMessages = DataStore.getMessages();
        List<Message> visibleMessages = allMessages.stream()
                .filter(m -> !m.isPrivate() ||
                        m.getSender().equals(currentUser) ||
                        m.getReceiver().equals(currentUser) ||
                        m.getSender().equals("系统")) // 系统消息对所有人都可见
                .collect(Collectors.toList());

        // 简单手动构建 JSON，避免依赖第三方库（如果你没有Gson jar包）
        // 格式: {"count": 5, "users": ["A","B"], "messages": [...]}
        PrintWriter out = resp.getWriter();
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"count\":").append(users.size()).append(",");

        // 构建用户列表 JSON
        json.append("\"users\":[");
        int u = 0;
        for (String user : users) {
            json.append("\"").append(user).append("\"");
            if (++u < users.size()) json.append(",");
        }
        json.append("],");

        // 构建消息列表 JSON
        json.append("\"messages\":[");
        for (int i = 0; i < visibleMessages.size(); i++) {
            Message m = visibleMessages.get(i);
            json.append("{");
            json.append("\"sender\":\"").append(m.getSender()).append("\",");
            json.append("\"content\":\"").append(escapeJson(m.getContent())).append("\",");
            json.append("\"time\":\"").append(m.getTime()).append("\",");
            json.append("\"isPrivate\":").append(m.isPrivate()).append(",");
            json.append("\"receiver\":\"").append(m.getReceiver() == null ? "" : m.getReceiver()).append("\"");
            json.append("}");
            if (i < visibleMessages.size() - 1) json.append(",");
        }
        json.append("]");
        json.append("}");

        out.print(json.toString());
        out.flush();
    }

    // POST 请求用于发送消息
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(401); // 未登录
            return;
        }

        String sender = (String) session.getAttribute("user");
        String content = req.getParameter("content");
        String receiver = req.getParameter("receiver"); // "ALL" 或 具体用户名

        if (content != null && !content.trim().isEmpty()) {
            boolean isPrivate = receiver != null && !receiver.equals("ALL");
            Message msg = new Message(sender, receiver, content, isPrivate);
            DataStore.addMessage(msg);
        }
        resp.setStatus(200);
    }

    // 简单的特殊字符转义，防止JSON破坏
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}