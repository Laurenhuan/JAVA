package com.chat.server;//属于服务器模块

import java.io.IOException;//处理输入输出错误？？
import java.net.ServerSocket;//服务器专用收件口？？
import java.net.Socket;//网络连接通道？？
import java.util.Map;//存储键值对的数据结构？？
import java.util.concurrent.ConcurrentHashMap;//线程安全的map？？
import com.chat.common.Message;//消息类

/**
 * 聊天服务器主类
 *  功能：监听端口、接收客户端连接、管理在线用户、转发消息
 */
public class ChatServer {

    private final int port;
    private ServerSocket serverSocket;//服务器接收口，用来接受客户端的连接请求？？
    private volatile boolean running = false;//volatile表示这个变量可以被多个线程同时修改，running表示服务器是否正在运行

    // 在线用户列表：昵称 → 客户端处理线程（线程安全）
    private final Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    //存储所有的在线用户，String是昵称，ClientHandler是处理这个用户的分拣员
    public ChatServer(int port) {
        this.port = port;
    }

    /**
     * 启动服务器：创建 ServerSocket 并循环接收客户端连接
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println(" 服务器已启动，监听端口: " + port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    // 每个客户端分配一个独立线程处理
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    new Thread(handler).start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println(" 接受客户端连接时出错: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(" 服务器启动失败（端口 " + port + " 可能已被占用）: " + e.getMessage());
        }
    }

    /**
     * 停止服务器
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println(" 关闭服务器时出错: " + e.getMessage());
        }
    }

    // ==================== 用户管理 ====================

    /**
     * 注册用户上线（由 ClientHandler 在登录成功后调用）
     * @return true 注册成功，false 昵称已存在
     */
    public synchronized boolean addUser(String nickname, ClientHandler handler) {
        if (onlineUsers.containsKey(nickname)) {
            return false;
        }
        onlineUsers.put(nickname, handler);
        return true;
    }

    /**
     * 移除用户（由 ClientHandler 在断开时调用）
     */
    public void removeUser(String nickname) {
        onlineUsers.remove(nickname);
    }

    /**
     * 检查昵称是否已被占用
     */
    public synchronized boolean isNicknameTaken(String nickname) {
        return onlineUsers.containsKey(nickname);
    }

    /**
     * 获取在线用户列表（用于发送 USER_LIST 消息）
     */
    public String[] getOnlineUserNames() {
        return onlineUsers.keySet().toArray(new String[0]);
    }

    // ==================== 消息转发 ====================

    /**
     * 群聊消息：转发给所有在线用户（除发送者外）
     */
    public void broadcast(Message msg, String excludeUser) {
        for (Map.Entry<String, ClientHandler> entry : onlineUsers.entrySet()) {
            if (!entry.getKey().equals(excludeUser)) {
                entry.getValue().sendMessage(msg);
            }
        }
    }

    /**
     * 广播给所有人（包括发送者，用于系统消息）
     */
    public void broadcastToAll(Message msg) {
        for (ClientHandler handler : onlineUsers.values()) {
            handler.sendMessage(msg);
        }
    }

    /**
     * 私聊消息：仅发送给指定接收者
     * @return true 发送成功，false 接收者不在线
     */
    public boolean sendPrivate(Message msg, String receiver) {
        ClientHandler handler = onlineUsers.get(receiver);
        if (handler != null) {
            handler.sendMessage(msg);
            return true;
        }
        return false;
    }

    // ==================== 工具方法 ====================

    public int getOnlineCount() {
        return onlineUsers.size();
    }

    public static void main(String[] args) {
        int port = 8888;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("端口号格式错误，使用默认端口 8888");
            }
        }
        new ChatServer(port).start();
    }
}
