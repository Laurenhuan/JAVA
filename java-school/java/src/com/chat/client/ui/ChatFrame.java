package com.chat.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.chat.common.Message;
import com.chat.client.ChatClientManager;
import com.chat.client.ClientNetworkListener;

public class ChatFrame extends JFrame implements ClientNetworkListener {
    private static final long serialVersionUID = 1L;

    private JTextArea txtChatHistory;
    private JList<String> listOnlineUsers;
    private DefaultListModel<String> listModel;
    private JTextField txtInput;
    private JButton btnSend;
    private JButton btnSave;
    private JLabel lblCurrentTarget;

    private String currentTarget = "所有人";
    private String myNickname = "";

    private ChatClientManager netManager;

    public ChatFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("局域网简易聊天室 - 客户端");
        setSize(750, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        txtChatHistory = new JTextArea();
        txtChatHistory.setEditable(false);
        txtChatHistory.setLineWrap(true);
        JScrollPane scrollChat = new JScrollPane(txtChatHistory);
        add(scrollChat, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("在线用户列表"));
        listModel = new DefaultListModel<>();
        listModel.addElement("所有人");
        listOnlineUsers = new JList<>(listModel);
        JScrollPane scrollUsers = new JScrollPane(listOnlineUsers);
        scrollUsers.setPreferredSize(new Dimension(180, 0));
        rightPanel.add(scrollUsers, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblCurrentTarget = new JLabel("当前聊天对象：所有人");
        lblCurrentTarget.setForeground(Color.BLUE);
        btnSave = new JButton("💾 保存记录");
        JButton btnEmoji = new JButton("😊 [微笑]");
        toolBar.add(lblCurrentTarget);
        toolBar.add(new JToolBar.Separator());
        toolBar.add(btnSave);
        toolBar.add(btnEmoji);
        bottomPanel.add(toolBar, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new BorderLayout());
        txtInput = new JTextField();
        btnSend = new JButton("发送");
        inputPanel.add(txtInput, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        bottomPanel.add(inputPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        btnSend.addActionListener(e -> performSend());
        txtInput.addActionListener(e -> performSend());
        btnSave.addActionListener(e -> saveHistoryToFile());
        btnEmoji.addActionListener(e -> insertEmoji());
        initListListener();
    }

    private void performSend() {
        String text = txtInput.getText().trim();
        if (text.isEmpty()) return;

        if (netManager == null) {
            JOptionPane.showMessageDialog(this, "网络未连接", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (currentTarget.equals("所有人")) {
            netManager.sendGroupChat(text);
        } else {
            netManager.sendPrivateChat(currentTarget, text);
        }

        // 本地立即显示自己发出的消息（避免等待服务器回显的延迟）
        appendMessage(myNickname, text, false);
        txtInput.setText("");
    }

    public void appendMessage(String sender, String content, boolean isPrivate) {
        if (content.contains("[微笑]")) {
            content = content.replace("[微笑]", "😊");
        }
        String prefix = isPrivate ? "【私聊】" : "";
        txtChatHistory.append(prefix + "[" + sender + "]: " + content + "\n");
        txtChatHistory.setCaretPosition(txtChatHistory.getDocument().getLength());
    }

    private void insertEmoji() {
        txtInput.setText(txtInput.getText() + "[微笑]");
        txtInput.requestFocus();
    }

    private void saveHistoryToFile() {
        String content = txtChatHistory.getText();
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "当前没有聊天记录可以保存！");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择聊天记录保存路径");
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                file = new java.io.File(file.getAbsolutePath() + ".txt");
            }
            try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                    new java.io.FileWriter(file, java.nio.charset.StandardCharsets.UTF_8))) {
                writer.write(content);
                JOptionPane.showMessageDialog(this, "聊天记录已成功保存至: " + file.getName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "文件保存失败: " + e.getMessage());
            }
        }
    }

    private void initListListener() {
        listOnlineUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    String selectedUser = listOnlineUsers.getSelectedValue();
                    if (selectedUser != null && !selectedUser.equals("所有人")) {
                        currentTarget = selectedUser;
                        lblCurrentTarget.setText("当前聊天对象：" + currentTarget);
                    }
                }
            }
        });
    }

    // ========== 实现 ClientNetworkListener 接口 ==========
    @Override
    public void onMessageReceived(Message msg) {
        String sender = msg.getSender();
        String content = msg.getContent();
        boolean isPrivate = msg.getType().equals(Message.TYPE_CHAT_PRI);
        // 如果是自己发送的消息，服务器也会转发回来，为避免重复，可以过滤掉自己
        if (sender.equals(myNickname)) {
            return;
        }
        appendMessage(sender, content, isPrivate);
    }

    @Override
    public void onUserListUpdated(String[] onlineUsers) {
        listModel.clear();
        listModel.addElement("所有人");
        for (String user : onlineUsers) {
            if (!user.equals(myNickname)) {
                listModel.addElement(user);
            }
        }
    }

    @Override
    public void onServerDisconnected() {
        SwingUtilities.invokeLater(() -> {
            receiveSystemNotice("与服务器断开连接");
            btnSend.setEnabled(false);
            txtInput.setEnabled(false);
        });
    }

    @Override
    public void onSystemNotice(String notice) {
        receiveSystemNotice(notice);
    }

    @Override
    public void onLoginFailed(String reason) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "登录失败: " + reason, "错误", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        });
    }

    public void receiveSystemNotice(String notice) {
        txtChatHistory.append("📣 【系统提示】" + notice + "\n");
        txtChatHistory.setCaretPosition(txtChatHistory.getDocument().getLength());
    }

    public void initNetwork(String serverIp, int port, String nickname) {
        this.myNickname = nickname;
        netManager = new ChatClientManager();
        netManager.setNetworkListener(this);
        boolean ok = netManager.connect(serverIp, port, nickname);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "连接服务器失败，请检查服务器是否启动", "错误", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String ip = JOptionPane.showInputDialog("请输入服务器IP（默认127.0.0.1）");
            if (ip == null) System.exit(0);
            ip = ip.trim().isEmpty() ? "127.0.0.1" : ip;
            String portStr = JOptionPane.showInputDialog("端口（默认8888）");
            int port = (portStr == null || portStr.trim().isEmpty()) ? 8888 : Integer.parseInt(portStr);
            String nickname = JOptionPane.showInputDialog("请输入你的昵称");
            if (nickname == null || nickname.trim().isEmpty()) System.exit(0);
            ChatFrame frame = new ChatFrame();
            frame.initNetwork(ip, port, nickname.trim());
            frame.setVisible(true);
        });
    }
}