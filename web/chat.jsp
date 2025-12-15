<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // 1. 权限拦截：未登录直接跳转
    String currentUser = (String) session.getAttribute("user");
    if (currentUser == null) {
        request.setAttribute("error", "请先登录！");
        request.getRequestDispatcher("login.jsp").forward(request, response);
        return; // 必须return，停止执行后续代码
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>在线聊天室 - <%= currentUser %></title>
    <style>
        body {
            margin: 0;
            padding: 0;
            font-family: 'Segoe UI', sans-serif;
            background-color: #f0f2f5;
            height: 100vh;
            display: flex;
            flex-direction: column;
        }
        header {
            background: #fff;
            padding: 15px 30px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.05);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .main-container {
            flex: 1;
            display: flex;
            overflow: hidden; /* 防止双滚动条 */
            max-width: 1200px;
            width: 100%;
            margin: 20px auto;
            background: white;
            border-radius: 10px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }

        /* 左侧用户列表 */
        .sidebar {
            width: 250px;
            background: #f7f7f7;
            border-right: 1px solid #eee;
            display: flex;
            flex-direction: column;
        }
        .sidebar-header { padding: 15px; border-bottom: 1px solid #ddd; font-weight: bold; }
        .user-list { list-style: none; padding: 0; margin: 0; overflow-y: auto; flex: 1; }
        .user-list li {
            padding: 10px 15px;
            cursor: pointer;
            display: flex;
            align-items: center;
        }
        .user-list li:hover, .user-list li.active { background: #e6e6e6; }
        .status-dot { height: 8px; width: 8px; background: #4caf50; border-radius: 50%; margin-right: 10px; }

        /* 右侧聊天区域 */
        .chat-area { flex: 1; display: flex; flex-direction: column; }
        .messages-box {
            flex: 1;
            padding: 20px;
            overflow-y: auto;
            background: #fff;
        }

        /* 消息气泡 */
        .message { margin-bottom: 15px; display: flex; flex-direction: column; }
        .message.self { align-items: flex-end; }
        .message.other { align-items: flex-start; }

        .bubble {
            max-width: 60%;
            padding: 10px 15px;
            border-radius: 15px;
            position: relative;
            word-wrap: break-word;
        }
        .self .bubble { background: #0084ff; color: white; border-bottom-right-radius: 2px; }
        .other .bubble { background: #e4e6eb; color: black; border-bottom-left-radius: 2px; }
        .private-tag { font-size: 12px; color: #ffeb3b; margin-right: 5px; font-weight: bold; }

        .msg-info { font-size: 12px; color: #999; margin-top: 4px; }

        /* 输入框 */
        .input-area {
            padding: 20px;
            border-top: 1px solid #eee;
            background: #fff;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .target-label { font-size: 14px; color: #666; background: #f0f0f0; padding: 5px 10px; border-radius: 4px;}
        input[type="text"] { flex: 1; padding: 12px; border: 1px solid #ddd; border-radius: 20px; outline: none; }
        button {
            padding: 10px 25px;
            background: #0084ff;
            color: white;
            border: none;
            border-radius: 20px;
            cursor: pointer;
        }
        .logout-btn { background: #ff4757; text-decoration: none; color: white; padding: 8px 15px; border-radius: 5px; font-size: 14px; }
    </style>
</head>
<body>

<header>
    <div>
        <strong>Java聊天室</strong>
        <span style="font-size: 14px; color: #666; margin-left: 10px;">当前用户: <%= currentUser %></span>
    </div>
    <a href="logout" class="logout-btn">退出</a>
</header>

<div class="main-container">
    <!-- 侧边栏：用户列表 -->
    <div class="sidebar">
        <div class="sidebar-header">在线用户 (<span id="user-count">0</span>)</div>
        <ul class="user-list" id="user-list-ul">
            <!-- JS动态填充 -->
        </ul>
    </div>

    <!-- 主聊天区 -->
    <div class="chat-area">
        <div class="messages-box" id="msg-box">
            <!-- 消息列表 -->
        </div>

        <div class="input-area">
            <span class="target-label" id="target-display">发送给: 所有人</span>
            <input type="text" id="msg-input" placeholder="输入消息..." onkeypress="handleEnter(event)">
            <button onclick="sendMessage()">发送</button>
        </div>
    </div>
</div>

<script>
    const currentUser = "<%= currentUser %>";
    let currentTarget = "ALL"; // 默认群发
    let messageCount = 0;      // 记录消息数量

    // 1. 初始化：设置定时器每1秒拉取一次数据
    setInterval(fetchData, 1000);
    fetchData(); // 立即执行一次

    // 2. 拉取数据函数
    function fetchData() {
        fetch('chatAction')
            .then(response => {
                if (response.status === 401) {
                    window.location.href = "login.jsp";
                    return;
                }
                return response.json();
            })
            .then(data => {
                if (!data) return;
                // 调试用：可以先看看数据
                // console.log('data from server:', data);
                renderUsers(data.users, data.count);
                renderMessages(data.messages);
            })
            .catch(err => console.error('fetchData error:', err));
    }

    function renderUsers(users, count) {
        document.getElementById('user-count').innerText = count;
        const ul = document.getElementById('user-list-ul');
        let html = '';

        // 群聊入口
        html += "<li onclick=\"selectUser('ALL')\" class=\"" +
            (currentTarget === 'ALL' ? 'active' : '') +
            "\"><div class=\"status-dot\" style=\"background: orange;\"></div> 群聊 (所有人)</li>";

        // 其他在线用户
        users.forEach(function (u) {
            if (u !== currentUser) {
                html += "<li onclick=\"selectUser('" + u + "')\" class=\"" +
                    (currentTarget === u ? 'active' : '') +
                    "\"><div class=\"status-dot\"></div> " + u + "</li>";
            }
        });

        ul.innerHTML = html;
    }


    // 4. 渲染消息列表（完全不用模板字符串）
    function renderMessages(messages) {
        const box = document.getElementById('msg-box');
        if (!box) {
            console.error('#msg-box 元素未找到');
            return;
        }

        // 如果列表长度没有变化，就不重复渲染
        if (messages.length === messageCount) return;

        let html = '';
        messages.forEach(function (msg) {
            const isSelf = msg.sender === currentUser;
            const alignClass = isSelf ? 'self' : 'other';
            const privateBadge = msg.isPrivate ? '<span class="private-tag">[私聊]</span> ' : '';

            html += '<div class="message ' + alignClass + '">'
                +     '<div class="bubble">'
                +         privateBadge + escapeHtml(msg.content)
                +     '</div>'
                +     '<div class="msg-info">' + msg.sender + ' ' + msg.time + '</div>'
                + '</div>';
        });

        box.innerHTML = html;

        if (messages.length > messageCount) {
            box.scrollTop = box.scrollHeight; // 滚动到底部
        }
        messageCount = messages.length;
    }

    // 5. 选择私聊对象
    function selectUser(user) {
        currentTarget = user;
        const display = (user === 'ALL') ? '所有人' : user;
        document.getElementById('target-display').innerText = '发送给: ' + display;
        // 强制刷新一遍选中状态
        fetchData();
    }

    // 6. 发送消息
    function sendMessage() {
        const input = document.getElementById('msg-input');
        const content = input.value;
        if (!content || !content.trim()) return;

        const params = new URLSearchParams();
        params.append('content', content);
        params.append('receiver', currentTarget);

        fetch('chatAction', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
            },
            body: params
        }).then(function () {
            input.value = '';
            fetchData(); // 立即拉一次
        }).catch(function (err) {
            console.error('sendMessage error:', err);
        });
    }

    // 回车发送
    function handleEnter(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    }

    // 简单转义，防止内容里带有 < > 影响页面
    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
    }
</script>

</body>
</html>