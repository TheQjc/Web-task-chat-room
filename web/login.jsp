<%@page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>进入聊天室</title>
    <style>
        body {
            background: linear-gradient(135deg,#667eea 0%, #764ba2 100%);
            height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            margin: 0;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        .card {
            background: white;
            padding: 40px;
            border-radius: 15px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            width: 350px;
            text-align: center;
        }
        h2 { color: #333; margin-bottom: 20px; }
        input[type="text"] {
            width: 100%;
            padding: 12px;
            margin: 10px 0;
           border: 1px solid #ddd;
            border-radius: 6px;
            box-sizing: border-box; /* 关键：防止padding撑破宽度 */
        }
        button {
            width: 100%;
            padding: 12px;
            background-color: #764ba2;
            color: white;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 16px;
            transition: background 0.3s;
        }
        button:hover { background-color: #5a387e; }
        .error {color: red; font-size: 14px; margin-bottom: 10px; }
    </style>
</head>
<body>
<div class="card">
    <h2>👋 欢迎来到聊天室</h2>
    <!-- 显示后端转发来的错误信息 -->
    <%
        String error = (String) request.getAttribute("error");
        if (error != null) {
    %>
    <div class="error"><%= error %></div>
    <% } %>

    <form action="${pageContext.request.contextPath}/login" method="post">
        <!-- 如果有Cookie，自动填入 -->
        <%
            String lastUser = "";
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("lastUser".equals(c.getName())) lastUser = c.getValue();
                }
            }
        %>
        <input type="text" name="username" placeholder="请输入你的昵称" value="<%=lastUser%>" required autocomplete="off">
        <button type="submit">进入聊天</button>
    </form>
</div>
</body>
</html>