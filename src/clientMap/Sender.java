package clientMap;

import Common.*;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Ansvarar för att skicka meddelanden från klienten till servern.
 *
 */
public class Sender {

    private Client client;
    private ObjectOutputStream oos;

    /**
     * Skapar en ny Sender med en referens till klienten.
     *
     * @param client Referens till klienten som använder denna Sender.
     */
    public Sender(Client client) {
        this.client = client;
        this.oos = client.getOos();
    }

    /**
     * Skickar ett meddelande till servern.
     * Sparar även meddelandet i klientens konversationshistorik.
     * @param message Meddelandet som ska skickas.
     */
    public void sendMessage(Message message) {
        try {
            if (oos != null) {
                oos.writeObject(message);
                oos.flush();
                client.storeMessage(message);
            } else {
                System.out.println("Could not send message");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
