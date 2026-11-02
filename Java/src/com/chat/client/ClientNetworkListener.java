package com.chat.client;

import com.chat.common.Message;

public interface ClientNetworkListener {

    void onMessageReceived(Message message);

    void onUserListUpdated(String[] onlineUsers);

    void onServerDisconnected();

    // 新增：处理系统通知（上线、下线、私聊失败等）
    void onSystemNotice(String notice);

    // 新增：处理登录失败
    void onLoginFailed(String reason);
}