package serverMap;

import Common.Message;
import Common.User;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * Hanterar kommunikationen med klienter genom att skicka meddelanden.
 *
 */
public class ClientCommunicationHandler {

    private Map<String,ObjectOutputStream> clientStreams;
    private Map<ObjectOutputStream, User> oosAndUserMap;

    /**
     * Skapar en ny instans av ClientCommunicationHandler.
     *
     * @param clientStreams En mappning mellan klienternas namn och deras oos.
     */
    public ClientCommunicationHandler(Map<String,ObjectOutputStream> clientStreams) {
        this.clientStreams = clientStreams;
        this.oosAndUserMap = new HashMap<>();
    }

    /**
     * Lägger till en ny användarström.
     *
     * @param user Den användare som strömmen tillhör.
     * @param oos Den oos som ska associeras med användaren.
     */
    public void addUserStream(User user, ObjectOutputStream oos) {
        oosAndUserMap.put(oos, user);
    }

    /**
     * Skickar ett meddelande till den angivna mottagaren.
     * @param message Meddelandet som ska skickas.
     */
    public void sendMessage(Message message) {
        User receiver = message.getReceiver();
        ObjectOutputStream oos = clientStreams.get(receiver.getName());

        if (oos != null) {
            try {
                oos.writeObject(message);
                oos.flush();
                System.out.println("Send message: " + message + " to " + receiver.getName());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error sending message to " + receiver.getName());
            }
        } else {
            System.out.println("No OutputStream found for " + receiver.getName());
        }
    }

}
