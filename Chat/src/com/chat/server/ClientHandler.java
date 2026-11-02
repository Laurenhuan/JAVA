package com.chat.server;

import java.io.*;
import java.net.Socket;
import com.chat.common.Message;

/**
 * 客户端连接处理线程
 * 每个在线客户端对应一个 ClientHandler 实例，负责：
 *   1. 接收并解析该客户端发来的 Message
 *   2. 根据消息类型调用服务器对应方法（群聊转发、私聊、登出等）
 *   3. 检测连接断开并清理资源
 */
public class ClientHandler implements Runnable {//这个类可以被线程执行

    private final Socket socket;//客户端连接
    private final ChatServer server;//服务器引用
    private ObjectInputStream in;//输入流
    private ObjectOutputStream out;//输出流
    private String nickname; // 当前客户端的昵称（登录成功后赋值）

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // 先建立输出流（必须先建立输出流，否则 ObjectInputStream 会阻塞）
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            // 循环读取客户端发来的消息
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
                        return; // 登出后结束线程
                    default:
                        System.err.println("未知消息类型: " + type);
                }
            }
        } catch (EOFException e) {
            // 客户端正常关闭（readObject 读到流末尾）
            System.out.println(" 客户端 " + nickname + " 已断开连接");
        } catch (IOException | ClassNotFoundException e) {
            // 客户端异常断开（如强制关闭窗口、网络中断）
            System.err.println(" 客户端 " + nickname + " 异常断开: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * 处理登录请求
     */
    private void handleLogin(Message msg) {
        String nick = msg.getSender();
        Message response = new Message();

        if (server.isNicknameTaken(nick)) {
            // 昵称已被占用，返回登录失败
            response.setType(Message.TYPE_LOGIN_FAIL);
            response.setContent("昵称 [" + nick + "] 已被占用，请更换昵称后重试");
            sendMessage(response);
            return;
        }

        // 注册用户
        boolean ok = server.addUser(nick, this);
        if (!ok) {
            response.setType(Message.TYPE_LOGIN_FAIL);
            response.setContent("昵称 [" + nick + "] 已被占用");
            sendMessage(response);
            return;
        }

        this.nickname = nick;

        // 1. 回复登录成功 + 当前在线用户列表
        response.setType(Message.TYPE_USER_LIST);
        response.setContent(String.join(",", server.getOnlineUserNames()));
        sendMessage(response);

        // 2. 广播上线通知给其他用户
        Message sysMsg = new Message(Message.TYPE_SYSTEM, "系统", null,
                " 用户 [" + nickname + "] 上线了！当前在线人数: " + server.getOnlineCount());
        server.broadcast(sysMsg, nickname);

        System.out.println(" 用户 [" + nickname + "] 登录成功，当前在线: " + server.getOnlineCount());
    }

    /**
     * 处理群聊消息
     */
    private void handleChatAll(Message msg) {
        System.out.println(" 群聊消息 [" + nickname + "]: " + msg.getContent());
        server.broadcast(msg, nickname);
    }

    /**
     * 处理私聊消息
     */
    private void handleChatPrivate(Message msg) {
        String receiver = msg.getReceiver();
        boolean sent = server.sendPrivate(msg, receiver);

        if (!sent) {
            // 接收者不在线，通知发送者
            Message errMsg = new Message(Message.TYPE_SYSTEM, "系统", nickname,
                    "用户 [" + receiver + "] 不在线，消息发送失败");
            sendMessage(errMsg);
        } else {
            System.out.println(" 私聊消息 [" + nickname + "] → [" + receiver + "]: " + msg.getContent());
        }
    }

    /**
     * 处理登出请求
     */
    private void handleLogout() {
        System.out.println(" 用户 [" + nickname + "] 请求下线");
        disconnect();
    }

    /**
     * 断开连接、清理资源、通知其他用户
     */
    private synchronized void disconnect() {
        if (nickname != null) {
            server.removeUser(nickname);
            // 广播下线通知
            Message sysMsg = new Message(Message.TYPE_SYSTEM, "系统", null,
                    " 用户 [" + nickname + "] 下线了！当前在线人数: " + server.getOnlineCount());
            server.broadcastToAll(sysMsg);
            nickname = null;
        }

        // 关闭流和 Socket
        try {
            if (in != null) in.close();
        } catch (IOException ignored) {}
        try {
            if (out != null) out.close();
        } catch (IOException ignored) {}
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    /**
     * 发送 Message 给该客户端
     */
    public void sendMessage(Message msg) {
        try {
            if (out != null) {
                out.writeObject(msg);
                out.flush();
            }
        } catch (IOException e) {
            System.err.println(" 发送消息给 [" + nickname + "] 失败: " + e.getMessage());
        }
    }
}
