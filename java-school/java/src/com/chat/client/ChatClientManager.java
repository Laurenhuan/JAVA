package com.chat.client;

import com.chat.common.Message;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ChatClientManager {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean isRunning = false;
    private String currentNickname;

    private ClientNetworkListener listener;

    public void setNetworkListener(ClientNetworkListener listener) {
        this.listener = listener;
    }

    public boolean connect(String ip, int port, String nickname) {
        if (isRunning) {
            return true;
        }
        try {
            this.socket = new Socket(ip, port);
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
            this.currentNickname = nickname;

            Message loginMsg = new Message(Message.TYPE_LOGIN, nickname, null, null);
            sendMessage(loginMsg);

            this.isRunning = true;
            Thread receiveThread = new Thread(new ReceiveTask(), "ChatReceiveThread");
            receiveThread.setDaemon(true);
            receiveThread.start();

            return true;
        } catch (Exception e) {
            System.err.println("[网络异常] 无法连接到服务器: " + e.getMessage());
            closeResources();
            return false;
        }
    }

    public void sendMessage(Message msg) {
        try {
            if (oos != null && !socket.isClosed()) {
                oos.writeObject(msg);
                oos.flush();
            }
        } catch (Exception e) {
            System.err.println("[网络异常] 消息发送失败: " + e.getMessage());
            disconnect();
        }
    }

    public void sendGroupChat(String text) {
        Message msg = new Message(Message.TYPE_CHAT_ALL, this.currentNickname, "所有人", text);
        sendMessage(msg);
    }

    public void sendPrivateChat(String targetReceiver, String text) {
        Message msg = new Message(Message.TYPE_CHAT_PRI, this.currentNickname, targetReceiver, text);
        sendMessage(msg);
    }

    public void disconnect() {
        if (!isRunning) return;
        isRunning = false;
        try {
            if (oos != null && !socket.isClosed()) {
                Message logoutMsg = new Message(Message.TYPE_LOGOUT, currentNickname, null, null);
                oos.writeObject(logoutMsg);
                oos.flush();
            }
        } catch (Exception ignored) {
        } finally {
            closeResources();
            if (listener != null) {
                listener.onServerDisconnected();
            }
        }
    }

    private void closeResources() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception e) {
            System.err.println("[系统清理] 关闭流资源异常: " + e.getMessage());
        }
    }

    private class ReceiveTask implements Runnable {
        @Override
        public void run() {
            try {
                while (isRunning) {
                    Message incomingMsg = (Message) ois.readObject();

                    if (listener == null) continue;

                    switch (incomingMsg.getType()) {
                        case Message.TYPE_CHAT_ALL:
                        case Message.TYPE_CHAT_PRI:
                            listener.onMessageReceived(incomingMsg);
                            break;

                        case Message.TYPE_USER_LIST:
                            if (incomingMsg.getContent() != null && !incomingMsg.getContent().isEmpty()) {
                                String[] onlineUsers = incomingMsg.getContent().split(",");
                                listener.onUserListUpdated(onlineUsers);
                            } else {
                                listener.onUserListUpdated(new String[0]);
                            }
                            break;

                        case Message.TYPE_SYSTEM:
                            // 系统通知
                            listener.onSystemNotice(incomingMsg.getContent());
                            break;

                        case Message.TYPE_LOGIN_FAIL:
                            // 登录失败
                            listener.onLoginFailed(incomingMsg.getContent());
                            isRunning = false; // 停止接收线程
                            closeResources();
                            break;

                        default:
                            System.out.println("[未知协议] 收到未知类型的消息: " + incomingMsg.getType());
                            break;
                    }
                }
            } catch (Exception e) {
                if (isRunning) {
                    System.err.println("[网络异常] 与服务器的连接异常断开: " + e.getMessage());
                    disconnect();
                }
            }
        }
    }
}