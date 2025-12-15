package com.chat.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private String sender;      // 发送者
    private String receiver;    // 接收者 (null 或 "ALL" 代表群发)
    private String content;     // 内容
    private String time;        // 时间字符串
    private boolean isPrivate;  // 是否私聊

    public Message(String sender, String receiver, String content, boolean isPrivate) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.isPrivate = isPrivate;
        // JDK 17 推荐的时间格式化方式
        this.time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    // 为系统消息提供的构造函数
    public Message(String sender, String receiver, String content, boolean isPrivate, String time) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.isPrivate = isPrivate;
        this.time = time;
    }

    // Getter methods
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getContent() { return content; }
    public String getTime() { return time; }
    public boolean isPrivate() { return isPrivate; }
}