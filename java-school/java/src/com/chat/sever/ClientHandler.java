package com.chat.server;

import java.io.*;
import java.net.Socket;
import com.chat.common.Message;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatServer server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String nickname;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Message msg = (Message) in.readObject();
                String type = msg.getType();
                System.out.println("收到消息 [" + type + "] from " + msg.getSender());

                switch (type) {
                    case Message.TYPE_LOGIN:
                        handleLogin(msg);
                        break;
                    case Message.TYPE_CHAT_ALL:
                        handleChatAll(msg);
                        break;
                    case Message.TYPE_CHAT_PRI:
                        handleChatPrivate(msg);
                        break;
                    case Message.TYPE_LOGOUT:
                        handleLogout();
                        return;
                    default:
                        System.err.println("未知消息类型: " + type);
                }
            }
        } catch (EOFException e) {
            System.out.println("客户端 " + nickname + " 已断开连接");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("客户端 " + nickname + " 异常断开: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void handleLogin(Message msg) {
        String nick = msg.getSender();
        if (server.isNicknameTaken(nick)) {
            Message response = new Message();
            response.setType(Message.TYPE_LOGIN_FAIL);
            response.setContent("昵称 [" + nick + "] 已被占用，请更换昵称后重试");
            sendMessage(response);
            return;
        }

        boolean ok = server.addUser(nick, this);
        if (!ok) {
            Message response = new Message();
            response.setType(Message.TYPE_LOGIN_FAIL);
            response.setContent("昵称 [" + nick + "] 已被占用");
            sendMessage(response);
            return;
        }

        this.nickname = nick;

        Message response = new Message();
        response.setType(Message.TYPE_USER_LIST);
        response.setContent(String.join(",", server.getOnlineUserNames()));
        sendMessage(response);

        Message sysMsg = new Message(Message.TYPE_SYSTEM, "系统", null,
                "用户 [" + nickname + "] 上线了！当前在线人数: " + server.getOnlineCount());
        server.broadcast(sysMsg, nickname);

        System.out.println("用户 [" + nickname + "] 登录成功，当前在线: " + server.getOnlineCount());
    }

    private void handleChatAll(Message msg) {
        System.out.println("群聊消息 [" + nickname + "]: " + msg.getContent());
        server.broadcast(msg, nickname);
    }

    private void handleChatPrivate(Message msg) {
        String receiver = msg.getReceiver();
        boolean sent = server.sendPrivate(msg, receiver);
        if (!sent) {
            Message errMsg = new Message(Message.TYPE_SYSTEM, "系统", nickname,
                    "用户 [" + receiver + "] 不在线，消息发送失败");
            sendMessage(errMsg);
        } else {
            System.out.println("私聊消息 [" + nickname + "] → [" + receiver + "]: " + msg.getContent());
        }
    }

    private void handleLogout() {
        System.out.println("用户 [" + nickname + "] 请求下线");
        disconnect();
    }

    private synchronized void disconnect() {
        if (nickname != null) {
            server.removeUser(nickname);
            Message sysMsg = new Message(Message.TYPE_SYSTEM, "系统", null,
                    "用户 [" + nickname + "] 下线了！当前在线人数: " + server.getOnlineCount());
            server.broadcastToAll(sysMsg);
            nickname = null;
        }
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (IOException ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
    }

    public void sendMessage(Message msg) {
        try {
            if (out != null) {
                out.writeObject(msg);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("发送消息给 [" + nickname + "] 失败: " + e.getMessage());
        }
    }
}