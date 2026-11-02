package com.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.chat.common.Message;

public class ChatServer {

    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private final Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("服务器已启动，监听端口: " + port);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    new Thread(handler).start();
                } catch (IOException e) {
                    if (running) {
                        System.err.println("接受客户端连接时出错: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("服务器启动失败（端口 " + port + " 可能已被占用）: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("关闭服务器时出错: " + e.getMessage());
        }
    }

    public synchronized boolean addUser(String nickname, ClientHandler handler) {
        if (onlineUsers.containsKey(nickname)) {
            return false;
        }
        onlineUsers.put(nickname, handler);
        return true;
    }

    public void removeUser(String nickname) {
        onlineUsers.remove(nickname);
    }

    public synchronized boolean isNicknameTaken(String nickname) {
        return onlineUsers.containsKey(nickname);
    }

    public String[] getOnlineUserNames() {
        return onlineUsers.keySet().toArray(new String[0]);
    }

    public void broadcast(Message msg, String excludeUser) {
        for (Map.Entry<String, ClientHandler> entry : onlineUsers.entrySet()) {
            if (!entry.getKey().equals(excludeUser)) {
                entry.getValue().sendMessage(msg);
            }
        }
    }

    public void broadcastToAll(Message msg) {
        for (ClientHandler handler : onlineUsers.values()) {
            handler.sendMessage(msg);
        }
    }

    public boolean sendPrivate(Message msg, String receiver) {
        ClientHandler handler = onlineUsers.get(receiver);
        if (handler != null) {
            handler.sendMessage(msg);
            return true;
        }
        return false;
    }

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