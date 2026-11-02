package com.chat.client;

import com.chat.common.Message;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 🚀 客户端网络模块核心管理器
 * 负责 Socket 的生命周期、对象的序列化发送与反序列化接收
 */
public class ChatClientManager {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean isRunning = false;
    private String currentNickname;

    // 持有监听器，用于将网络消息回调给界面层
    private ClientNetworkListener listener;

    /**
     * 设置网络事件监听器（供角色 C 调用）
     */
    public void setNetworkListener(ClientNetworkListener listener) {
        this.listener = listener;
    }

    /**
     * 建立与服务器的连接并发送登录请求
     * @param ip 服务器IP
     * @param port 服务器端口
     * @param nickname 用户昵称
     * @return 是否连接并登录成功
     */
    public boolean connect(String ip, int port, String nickname) {
        if (isRunning) {
            return true; // 已经连接则不重复连接
        }
        try {
            // 1. 创建 Socket 连接
            this.socket = new Socket(ip, port);

            // 2. 初始化对象输入输出流（注意：ObjectOutputStream 必须在 ObjectInputStream 之前创建，否则两端会死锁）
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());
            this.currentNickname = nickname;

            // 3. 组装登录消息并立即发送给服务器
            Message loginMsg = new Message(Message.TYPE_LOGIN, nickname, null, null);
            sendMessage(loginMsg);

            // 4. 启动后台独立线程，开始死循环监听服务器推送的消息（满足多线程优化要求）
            this.isRunning = true;
            Thread receiveThread = new Thread(new ReceiveTask(), "ChatReceiveThread");
            receiveThread.setDaemon(true); // 设置为守护线程，主程序关闭时自动结束
            receiveThread.start();

            return true;
        } catch (Exception e) {
            System.err.println("[网络异常] 无法连接到服务器: " + e.getMessage());
            closeResources();
            return false;
        }
    }

    /**
     * 向服务器发送一条 Message 对象
     * @param msg 构造好的 Message 对象
     */
    public void sendMessage(Message msg) {
        try {
            if (oos != null && !socket.isClosed()) {
                oos.writeObject(msg);
                oos.flush(); // 强制刷新缓冲区，确保数据立刻发出
            }
        } catch (Exception e) {
            System.err.println("[网络异常] 消息发送失败: " + e.getMessage());
            disconnect(); // 发送失败说明连接断了，执行断开清理
        }
    }

    /**
     * 发送普通群聊文本（供 C 界面层直接调用的快捷方法）
     * @param text 聊天内容
     */
    public void sendGroupChat(String text) {
        Message msg = new Message(Message.TYPE_CHAT_ALL, this.currentNickname, "所有人", text);
        sendMessage(msg);
    }

    /**
     * 发送私聊文本（供 C 界面层直接调用）
     * @param targetReceiver 接收方昵称
     * @param text 聊天内容
     */
    public void sendPrivateChat(String targetReceiver, String text) {
        Message msg = new Message(Message.TYPE_CHAT_PRI, this.currentNickname, targetReceiver, text);
        sendMessage(msg);
    }

    /**
     * 主动断开连接（用户点击退出或关闭窗口时调用）
     */
    public void disconnect() {
        if (!isRunning) return;
        isRunning = false;
        try {
            // 尝试优雅地向服务器发送下线通知
            if (oos != null && !socket.isClosed()) {
                Message logoutMsg = new Message(Message.TYPE_LOGOUT, currentNickname, null, null);
                oos.writeObject(logoutMsg);
                oos.flush();
            }
        } catch (Exception ignored) {
            // 忽略断开时的发送异常
        } finally {
            closeResources();
            // 触发回调，通知界面层做后续处理（如置灰按钮、弹窗提示）
            if (listener != null) {
                listener.onServerDisconnected();
            }
        }
    }

    /**
     * 私有方法：彻底关闭底层物理流资源
     */
    private void closeResources() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception e) {
            System.err.println("[系统清理] 关闭流资源异常: " + e.getMessage());
        }
    }

    /**
     * 👥 内部类：接收服务器消息的独立线程任务
     */
    private class ReceiveTask implements Runnable {
        @Override
        public void run() {
            try {
                while (isRunning) {
                    // 阻塞式读取：死守输入流，直到服务器发来一个 Message 对象
                    Message incomingMsg = (Message) ois.readObject();

                    // 如果界面层没有注册监听器，则不处理消息
                    if (listener == null) continue;

                    // 根据消息类型，精准分发给界面的不同回调方法
                    switch (incomingMsg.getType()) {
                        case Message.TYPE_CHAT_ALL:
                        case Message.TYPE_CHAT_PRI:
                            // 无论是群聊还是私聊文本，直接丢给界面展示
                            listener.onMessageReceived(incomingMsg);
                            break;

                        case Message.TYPE_USER_LIST:
                            // 服务器发来了最新的在线用户列表
                            // 约定：服务器将用户昵称用逗号拼接在 content 字段中，如 "张三,李四,王五"
                            if (incomingMsg.getContent() != null && !incomingMsg.getContent().isEmpty()) {
                                String[] onlineUsers = incomingMsg.getContent().split(",");
                                listener.onUserListUpdated(onlineUsers);
                            } else {
                                listener.onUserListUpdated(new String[0]);
                            }
                            break;

                        default:
                            System.out.println("[未知协议] 收到未知类型的消息: " + incomingMsg.getType());
                            break;
                    }
                }
            } catch (Exception e) {
                // 当服务器突然关闭，或者底层 Socket 被 close 时，readObject 会抛出异常
                if (isRunning) {
                    System.err.println("[网络异常] 与服务器的连接异常断开: " + e.getMessage());
                    disconnect();
                }
            }
        }
    }
}