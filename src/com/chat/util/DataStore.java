package com.chat.util;

import com.chat.entity.Message;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class DataStore {
    // 使用线程安全的集合，防止多人并发操作报错
    private static final List<Message> MESSAGE_HISTORY = new CopyOnWriteArrayList<>();
    private static final Set<String> ONLINE_USERS = new CopyOnWriteArraySet<>();

    // 添加消息
    public static void addMessage(Message msg) {
        MESSAGE_HISTORY.add(msg);
        // 限制内存占用，只保留最近100条
        if (MESSAGE_HISTORY.size() > 100) {
            MESSAGE_HISTORY.remove(0);
        }
    }

    // 获取所有消息
    public static List<Message> getMessages() {
        return MESSAGE_HISTORY;
    }

    // 用户上线
    public static boolean loginUser(String username) {
        if (ONLINE_USERS.contains(username)) {
            return false; // 用户名已存在
        }
        ONLINE_USERS.add(username);
        return true;
    }

    // 用户下线
    public static void logoutUser(String username) {
        ONLINE_USERS.remove(username);
    }

    // 获取在线用户列表
    public static Set<String> getOnlineUsers() {
        return ONLINE_USERS;
    }
}