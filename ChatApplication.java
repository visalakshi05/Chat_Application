package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class ChatApplication {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton, exitButton;
    private File chatFile;
    private String userName;

    public ChatApplication(String userName, String filePath) {
        this.userName = userName;
        this.chatFile = new File(filePath);

        initializeGUI(); // Initialize GUI components first
        userAction("joined"); // Log the user's join action once
        startMessageMonitor(); // Start monitoring messages
    }

    // Method to initialize GUI components
    private void initializeGUI() {
        frame = new JFrame("Chat Room - " + userName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 400);
        frame.setLayout(new BorderLayout());

        // Header panel with title
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0x3A8EBA));
        JLabel titleLabel = new JLabel("Chat Room");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel);
        frame.add(headerPanel, BorderLayout.NORTH);

        // Chat area to display messages
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with message field and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        exitButton = new JButton("Exit");
        bottomPanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.add(exitButton, BorderLayout.WEST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Send button action
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText().trim();
                if (!message.isEmpty()) {
                    writeMessage(message);
                    messageField.setText("");
                }
            }
        });

        // Exit button action
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                userAction("exited");
                clearChatArea(); // Clear the chat area on exit
                chatFile.delete(); // Delete the chat file on exit
                System.exit(0);
            }
        });

        frame.setVisible(true);
    }

    // Method to append actions like join/exit to chat file and console
    private void userAction(String action) {
        String logMessage = userName + " " + action;
        try (BufferedWriter br = new BufferedWriter(new FileWriter(chatFile, true))) {
            br.write(logMessage + '\n');
            appendToChatArea(logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to write message to file
    private void writeMessage(String message) {
        String formattedMessage = userName + ": " + message; // Format the message
        try (BufferedWriter br = new BufferedWriter(new FileWriter(chatFile, true))) {
            br.write(formattedMessage + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to append text to chat area
    private void appendToChatArea(String message) {
        chatArea.append(message + "\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength()); // Auto-scroll to bottom
    }

    // Method to clear chat area
    private void clearChatArea() {
        chatArea.setText(""); // Clear the chat area
    }

    // Method to start monitoring the chat file for new messages
    private void startMessageMonitor() {
        Thread monitorThread = new Thread(() -> {
            try (RandomAccessFile raf = new RandomAccessFile(chatFile, "r")) {
                raf.seek(raf.length()); // Move to the end of the file
                String line;
                while (true) {
                    while ((line = raf.readLine()) != null) {
                        if (!line.trim().isEmpty()) { // Avoid empty lines
                            final String messageLine = line;
                            SwingUtilities.invokeLater(() -> {
                                appendToChatArea(messageLine);
                            });
                        }
                    }
                    Thread.sleep(1000); // Check for new messages every second
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    public static void main(String[] args) {
        String userName = JOptionPane.showInputDialog("Enter your username:");
        if (userName != null && !userName.trim().isEmpty()) {
            String file = "ChatRoom.txt"; // Chat file location
            new ChatApplication(userName, file);
        } else {
            System.out.println("Username cannot be empty. Exiting...");
            System.exit(0);
        }
    }
}
