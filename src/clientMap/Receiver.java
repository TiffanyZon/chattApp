package clientMap;

import Common.Message;
import Common.User;
import View.ChatWindow;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;

/**
 * Ansvarar för att lyssna efter och hantera inkommande meddelanden från servern.
 *
 */
public class Receiver implements Runnable {

    private Client client;
    private ObjectInputStream ois;
    private Socket socket;

    /**
     * Skapar en ny Receiver för att hantera inkommande meddelanden.
     *
     * @param socket Socketsanslutningen till servern.
     * @param client Referensen till klienten som skapar denna instans.
     */
    public Receiver(Socket socket, Client client) {
        this.client = client;
        this.socket = socket;

        try {
            this.ois = new ObjectInputStream(client.getSocket().getInputStream());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Lyssnar kontinuerligt efter meddelanden från servern och hanterar de.
     */
    public void run() {
        try {
            while (!client.getSocket().isClosed()) {
                try {
                    Object serverMessage = ois.readObject();
                    if (serverMessage instanceof List<?>) {
                        List<User> connectedUsers = (List<User>) serverMessage;
                        SwingUtilities.invokeLater(() -> client.getUserInfoWindow().updateConnectedClientsList(connectedUsers, client.getUser()));

                    } else if (serverMessage instanceof Message) {
                        Message userMessage = (Message) serverMessage;
                        client.storeMessage(userMessage);
                        SwingUtilities.invokeLater(() -> {
                            ChatWindow chatWindow = client.getOpenChatWindows().get(userMessage.getSender().getName());
                            if (chatWindow != null) {
                                chatWindow.displayMessage(userMessage);
                            }
                        });
                    }
                } catch (IOException e) {
                    if (client.getSocket().isClosed()) {
                        System.out.println("Socket has been closed, exiting receiver thread.");
                        break;
                    }
                    e.printStackTrace();
                }
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
