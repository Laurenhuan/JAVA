package com.chat.common;

import java.io.Serializable;

/**
 * 通信协议与消息实体类
 * 注意：由于要通过 Socket 在网络中传输对象，该类【必须】实现 Serializable 接口
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;//序列版本号

    // 消息类型常量（通信协议格式），使用常量
    public static final String TYPE_LOGIN = "LOGIN";
    public static final String TYPE_LOGOUT = "LOGOUT";
    public static final String TYPE_CHAT_ALL = "CHAT_ALL";
    public static final String TYPE_CHAT_PRI = "CHAT_PRI";
    public static final String TYPE_USER_LIST = "USER_LIST";
    public static final String TYPE_SYSTEM = "SYSTEM";       // 系统广播（上线/下线通知）
    public static final String TYPE_LOGIN_FAIL = "LOGIN_FAIL"; // 登录失败

    private String type;
    private String sender;
    private String receiver;
    private String content;

    public Message() {
    }

    public Message(String type, String sender, String receiver, String content) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
