package com.chat.client.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import com.chat.common.Message;
import com.chat.client.ChatClientManager;
import com.chat.client.ClientNetworkListener;

public class ChatFrame extends JFrame implements ClientNetworkListener {
    private static final long serialVersionUID = 1L;

    private JEditorPane txtChatHistory;
    private JList<String> listOnlineUsers;
    private DefaultListModel<String> listModel;
    private JTextField txtInput;
    private JButton btnSend;
    private JButton btnSave;
    private JButton btnClear;
    private JLabel lblCurrentTarget;

    private String currentTarget = "所有人";
    private String myNickname = "";

    private ChatClientManager netManager;

    private final Color BG_MAIN = new Color(30, 31, 34);
    private final Color BG_PANEL = new Color(47, 49, 54);
    private final Color BG_INPUT = new Color(64, 68, 75);
    private final Color ACCENT = new Color(88, 101, 242);
    private final Color ACCENT_HOVER = new Color(71, 82, 196);
    private final Color TEXT_WHITE = new Color(255, 255, 255);
    private final Color TEXT_GRAY = new Color(187, 187, 187);
    private final Color SELF_BUBBLE = new Color(88, 101, 242);
    private final Color OTHER_BUBBLE = new Color(64, 68, 75);
    private final Color SYSTEM_BUBBLE = new Color(44, 47, 51);

    public ChatFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("局域网简易聊天室 - 客户端");
        setSize(950, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);

        // 消息展示区（支持 HTML 气泡）
        txtChatHistory = new JEditorPane();
        txtChatHistory.setEditable(false);
        txtChatHistory.setContentType("text/html");
        txtChatHistory.setBackground(BG_PANEL);
        txtChatHistory.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        txtChatHistory.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        txtChatHistory.setText("<html><body style='font-family:微软雅黑; margin:0; padding:12px;'></body></html>");

        JScrollPane scrollChat = new JScrollPane(txtChatHistory);
        scrollChat.setBorder(BorderFactory.createEmptyBorder());
        scrollChat.getViewport().setBackground(BG_PANEL);
        scrollChat.getVerticalScrollBar().setBackground(BG_PANEL);
        scrollChat.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollChat.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollChat, BorderLayout.CENTER);

        // 右侧用户列表
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG_MAIN);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(12, 5, 12, 10));

        JPanel userTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        userTitlePanel.setOpaque(false);
        JLabel userListTitle = new JLabel("👥 在线成员");
        userListTitle.setForeground(TEXT_WHITE);
        userListTitle.setFont(new Font("微软雅黑", Font.BOLD, 14));
        JLabel onlineCountLabel = new JLabel();
        onlineCountLabel.setForeground(TEXT_GRAY);
        onlineCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        userTitlePanel.add(userListTitle);
        userTitlePanel.add(onlineCountLabel);
        rightPanel.add(userTitlePanel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        listModel.addElement("所有人");
        listOnlineUsers = new JList<>(listModel);
        listOnlineUsers.setBackground(BG_PANEL);
        listOnlineUsers.setForeground(TEXT_WHITE);
        listOnlineUsers.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        listOnlineUsers.setSelectionBackground(ACCENT);
        listOnlineUsers.setSelectionForeground(TEXT_WHITE);
        listOnlineUsers.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        listOnlineUsers.setFixedCellHeight(36);
        listOnlineUsers.setCellRenderer(new UserListRenderer());
        JScrollPane scrollUsers = new JScrollPane(listOnlineUsers);
        scrollUsers.setBorder(BorderFactory.createEmptyBorder());
        scrollUsers.setPreferredSize(new Dimension(210, 0));
        scrollUsers.getViewport().setBackground(BG_PANEL);
        rightPanel.add(scrollUsers, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // 底部面板
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BG_MAIN);

        // 工具栏
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolBar.setOpaque(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 5, 12));
        lblCurrentTarget = new JLabel("💬 当前聊天：所有人");
        lblCurrentTarget.setForeground(TEXT_GRAY);
        lblCurrentTarget.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        btnSave = createStyledButton("💾 保存记录", ACCENT);
        btnClear = createStyledButton("🗑️ 清屏", new Color(120, 120, 120));
        JButton btnEmoji = createStyledButton("😊 表情", ACCENT);

        toolBar.add(lblCurrentTarget);
        toolBar.add(Box.createHorizontalStrut(15));
        toolBar.add(btnSave);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(btnClear);
        toolBar.add(Box.createHorizontalStrut(8));
        toolBar.add(btnEmoji);
        bottomPanel.add(toolBar, BorderLayout.NORTH);

        // 输入区域
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setOpaque(false);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 15, 12));
        txtInput = new JTextField();
        txtInput.setBackground(BG_INPUT);
        txtInput.setForeground(TEXT_WHITE);
        txtInput.setCaretColor(TEXT_WHITE);
        txtInput.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        txtInput.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(88, 101, 242, 150), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        // 输入框焦点边框高亮
        txtInput.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                txtInput.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACCENT, 1, true),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
            }
            public void focusLost(FocusEvent e) {
                txtInput.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(88, 101, 242, 150), 1, true),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
            }
        });

        btnSend = createStyledButton("发送", ACCENT);
        btnSend.setPreferredSize(new Dimension(80, 42));
        inputPanel.add(txtInput, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        bottomPanel.add(inputPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // 事件绑定
        btnSend.addActionListener(e -> performSend());
        txtInput.addActionListener(e -> performSend());
        btnSave.addActionListener(e -> saveHistoryToFile());
        btnClear.addActionListener(e -> clearChatHistory());
        btnEmoji.addActionListener(e -> showEmojiPicker());
        initListListener();

        // Ctrl+Enter 快捷发送
        txtInput.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "send");
        txtInput.getActionMap().put("send", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSend();
            }
        });

        // 更新在线人数显示
        updateOnlineCount();
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(TEXT_WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("微软雅黑", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        btn.setOpaque(true);
        // 悬停效果
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    // 用户列表渲染器（带在线绿点和头像占位符）
    private class UserListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            if (!isSelected) {
                label.setBackground(BG_PANEL);
            }
            String text = value.toString();
            if (text.equals("所有人")) {
                label.setText("🌐 " + text);
            } else {
                // 高亮当前私聊对象
                if (text.equals(currentTarget) && !currentTarget.equals("所有人")) {
                    label.setBackground(new Color(88, 101, 242, 80));
                }
                label.setText("🟢 " + text);
            }
            label.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            return label;
        }
    }

    private void updateOnlineCount() {
        int count = listModel.size() - 1; // 减去 "所有人"
        Component titleComp = ((JPanel)((JPanel)getContentPane().getComponent(1)).getComponent(0)).getComponent(0);
        if (titleComp instanceof JPanel) {
            JLabel countLabel = (JLabel)((JPanel)titleComp).getComponent(1);
            countLabel.setText(" (" + count + ")");
        }
    }

    private void showEmojiPicker() {
        JPopupMenu emojiMenu = new JPopupMenu();
        String[] emojis = {"😊", "😂", "😍", "😭", "😎", "😡", "👍", "❤️", "🎉", "🍺", "🔥", "✨", "💻", "🐱"};
        for (String emoji : emojis) {
            JMenuItem item = new JMenuItem(emoji);
            item.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            item.addActionListener(e -> {
                txtInput.setText(txtInput.getText() + emoji);
                txtInput.requestFocus();
            });
            emojiMenu.add(item);
        }
        emojiMenu.show(txtInput, 0, -emojiMenu.getPreferredSize().height);
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
        appendMessage(myNickname, text, false);
        txtInput.setText("");
    }

    private void clearChatHistory() {
        txtChatHistory.setText("<html><body style='font-family:微软雅黑; margin:0; padding:12px;'></body></html>");
    }

    public void appendMessage(String sender, String content, boolean isPrivate) {
        boolean isSelf = sender.equals(myNickname);
        String displayName = sender;
        if (isPrivate && !isSelf) {
            displayName = "🔒 [私聊] " + sender;
        }
        content = content.replace("[微笑]", "😊");
        String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        String align = isSelf ? "right" : "left";
        String bubbleColor = isSelf ? "#5865F2" : "#404249";
        String nameColor = isSelf ? "#B9BBFE" : "#B9BBBE";

        String bubbleHtml =
                "<div style='text-align:" + align + "; margin-bottom:12px;'>" +
                        "  <div style='display:inline-block; max-width:70%; background-color:" + bubbleColor + "; border-radius:18px; padding:8px 14px; box-shadow:0 1px 2px rgba(0,0,0,0.3);'>" +
                        "    <div style='font-size:11px; color:" + nameColor + "; margin-bottom:4px;'>" + escapeHtml(displayName) + " · " + time + "</div>" +
                        "    <div style='font-size:14px; color:#FFFFFF; word-wrap:break-word; line-height:1.4;'>" + escapeHtml(content) + "</div>" +
                        "  </div>" +
                        "</div>";

        String currentHtml = txtChatHistory.getText();
        int bodyEnd = currentHtml.lastIndexOf("</body>");
        if (bodyEnd != -1) {
            String newHtml = currentHtml.substring(0, bodyEnd) + bubbleHtml + currentHtml.substring(bodyEnd);
            txtChatHistory.setText(newHtml);
        } else {
            txtChatHistory.setText(currentHtml + bubbleHtml);
        }
        SwingUtilities.invokeLater(() -> txtChatHistory.setCaretPosition(txtChatHistory.getDocument().getLength()));
    }

    public void receiveSystemNotice(String notice) {
        String sysHtml =
                "<div style='text-align:center; margin:8px 0;'>" +
                        "  <span style='background-color:" + toHexColor(SYSTEM_BUBBLE) + "; color:#AAAAAA; font-size:12px; padding:4px 12px; border-radius:16px;'>📢 " + escapeHtml(notice) + "</span>" +
                        "</div>";
        String currentHtml = txtChatHistory.getText();
        int bodyEnd = currentHtml.lastIndexOf("</body>");
        if (bodyEnd != -1) {
            String newHtml = currentHtml.substring(0, bodyEnd) + sysHtml + currentHtml.substring(bodyEnd);
            txtChatHistory.setText(newHtml);
        } else {
            txtChatHistory.setText(currentHtml + sysHtml);
        }
        SwingUtilities.invokeLater(() -> txtChatHistory.setCaretPosition(txtChatHistory.getDocument().getLength()));
    }

    private String toHexColor(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void saveHistoryToFile() {
        String content = txtChatHistory.getText();
        String plainText = content.replaceAll("\\<.*?>", "").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");
        if (plainText.trim().isEmpty()) {
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
                writer.write(plainText);
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
                        lblCurrentTarget.setText("💬 当前聊天：" + currentTarget);
                        // 刷新列表高亮
                        listOnlineUsers.repaint();
                    } else if (selectedUser != null && selectedUser.equals("所有人")) {
                        currentTarget = "所有人";
                        lblCurrentTarget.setText("💬 当前聊天：所有人");
                        listOnlineUsers.repaint();
                    }
                }
            }
        });
    }

    @Override
    public void onMessageReceived(Message msg) {
        String sender = msg.getSender();
        String content = msg.getContent();
        boolean isPrivate = msg.getType().equals(Message.TYPE_CHAT_PRI);
        if (sender.equals(myNickname)) return;
        SwingUtilities.invokeLater(() -> appendMessage(sender, content, isPrivate));
    }

    @Override
    public void onUserListUpdated(String[] onlineUsers) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            listModel.addElement("所有人");
            for (String user : onlineUsers) {
                if (!user.equals(myNickname)) {
                    listModel.addElement(user);
                }
            }
            updateOnlineCount();
        });
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
        SwingUtilities.invokeLater(() -> receiveSystemNotice(notice));
    }

    @Override
    public void onLoginFailed(String reason) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "登录失败: " + reason, "错误", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        });
    }

    public void initNetwork(String serverIp, int port, String nickname) {
        this.myNickname = nickname;
        setTitle("局域网简易聊天室 - " + nickname);
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