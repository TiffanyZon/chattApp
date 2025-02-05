package View;

import Common.*;
import clientMap.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ett fönster för chatt mellan användare i applikationen.
 *
 */
public class ChatWindow extends JFrame implements ActionListener {
    private User sender;
    private User receiver;
    private Client client;

    private File selectedImage;
    private JTextPane chatDisplayArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton choosePic;


    /**
     * Skapar en ny ChatWindow för chatt mellan två användare.
     *
     * @param sender Avsändaren av chattmeddelandena.
     * @param receiver Mottagaren av chattmeddelandena.
     * @param client Klientinstansen som används för att skicka meddelanden.
     */

    public ChatWindow(User sender, User receiver, Client client) {
        this.sender = sender;
        this.receiver = receiver;
        this.client = client;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.chatWindowClosed(receiver);
            }
        });

        initializeComponents();
        updateChatWindow();
    }

    /**
     * Initialiserar komponenterna i chattfönstret.
     */
    private void initializeComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        chatDisplayArea = new JTextPane();
        chatDisplayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatDisplayArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
        messageField = new JTextField();
        sendButton = new JButton("Send Message");
        choosePic = new JButton("Choose Picture");

        sendButton.addActionListener(this);
        choosePic.addActionListener(this);

        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        messagePanel.add(choosePic, BorderLayout.WEST);
        add(messagePanel, BorderLayout.SOUTH);

        setSize(500, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Hanterar händelser från användargränssnittet, som att skicka meddelanden eller välja bilder.
     *
     * @param e Händelsen som genererades av användargränssnittet.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            String messageText = messageField.getText();
            Message message = null;

            if (selectedImage != null) {
                ImageIcon imageIcon = new ImageIcon(selectedImage.getAbsolutePath());
                if (!messageText.isEmpty()) {
                    message = new Message(sender, receiver, messageText, imageIcon);
                } else {
                    message = new Message(sender, receiver, null, imageIcon);
                }
                sendMessage(message);
            } else if (!messageText.isEmpty()) {
                message = new Message(sender, receiver, messageText,null);
                messageField.setText("");
                sendMessage(message);
            }
            messageField.setText("");
        }
        else if (e.getSource() == choosePic) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("/Users/tiffanyzon/Documents/intellij_projekt/ChattApp2024.V2/src/pics"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("All pics", "png", "jpeg", "jpg");
            fileChooser.setFileFilter(filter);

            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImage = fileChooser.getSelectedFile();
            }
        }
    }

    /**
     * Uppdaterar titeln på chattfönstret baserat på de deltagande användarna.
     */
    public void updateChatWindow() {
        if (receiver != null) {
            setTitle(sender.getName() + ": Chat with " + receiver.getName());
        } else {
            setTitle("Default Chat: " + sender.getName());
        }
    }

    /**
     * Skickar ett meddelande till servern för vidarebefordran till mottagaren och visar meddelandet i chattfönstret.
     *
     * @param message Meddelandet som ska skickas och visas.
     */
    public void sendMessage(Message message) {
        client.sendMessage(message);
        displayMessage(message);
    }

    /**
     * Visar ett meddelande i chattfönstret.
     *
     * @param message Meddelandet som ska visas.
     */
    public void displayMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            addMessageToChat(message);
        });
    }

    /**
     * Visar en lista av meddelanden i chattfönstret. Används vid initialisering av chattfönstret för att visa tidigare meddelanden.
     *
     * @param messages Listan av meddelanden som ska visas.
     */
    public void displayMessages(List<Message> messages) {
        chatDisplayArea.setText("");
        for (Message message : messages) {
            addMessageToChat(message);
        }
    }

    /**
     * Lägger till ett meddelande i dokumentet för chattfönstret. Hanterar både text- och bildmeddelanden.
     *
     * @param message Meddelandet som ska läggas till i chattfönstret.
     */
    private void addMessageToChat(Message message) {
        try {
            Document doc = chatDisplayArea.getDocument();
            String senderName;
            if (message.getSender().equals(sender)) {
                senderName = "You";
            } else {
                senderName = message.getSender().getName();
            }

            if (message.getImage() != null) {
                ImageIcon imageIcon = message.getImage();
                doc.insertString(doc.getLength(), senderName + ": (Image)\n", null);
                doc.insertString(doc.getLength(), "\n", null);
                int imageEnd = doc.getLength();
                chatDisplayArea.setCaretPosition(imageEnd);
                chatDisplayArea.insertIcon(imageIcon);
                doc.insertString(doc.getLength(), "\n", null);
            }

            if (message.getMessage() != null) {
                String messageText = message.getMessage();
                doc.insertString(doc.getLength(), senderName + ": " + messageText + "\n", null);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }


}
