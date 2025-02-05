package clientMap;

import Common.Message;
import Common.User;
import View.ChatWindow;
import View.LoginWindow;
import View.UserInfoWindow;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * Hanterar klientens huvudlogik för anslutning, chattfönsterhantering,
 * kontaktlistan, och användarinteraktioner.
 *
 * */


public class Client {
    private User user;
    private int port = 2222;
    private LoginWindow loginWindow;
    private UserInfoWindow userInfoWindow;
    private Socket socket;
    private ObjectOutputStream oos;
    private Sender sender;
    private static final String CONTACTS_FILE_PREFIX = "contacts_";
    private static final String CONTACTS_FILE_SUFFIX = ".dat";
    private List<Contacts> contactsSaved = new ArrayList<>();
    private PropertyChangeSupport changeSupport;
    private Set<String> savedContactNames = new HashSet<>();
    private Receiver receiver;
    private Map <Client, User> savedContacts = new HashMap<>();
    private Map<String, ChatWindow> openChatWindows = new HashMap<>();
    private Map<String, List<Message>> conversationHistory = new HashMap<>();


    /**
     * Skapar en ny klient, initialiserar propertychange och visar inloggningsfönstret.
     */
    public Client() {
        changeSupport = new PropertyChangeSupport(this);
        loginWindow = new LoginWindow(this);
        loginWindow.setVisible(true);
    }

    /**
     * Lägger till en Listener.
     * @param pcl Den lyssnare som ska läggas till.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        changeSupport.addPropertyChangeListener(pcl);
    }

    /**
     * Skickar en notifiering om att en egenskap har ändrats.
     * @param propertyName Namnet på ändrad egenskapen.
     * @param oldValue Gamla värdet av egenskapen.
     * @param newValue Nya värdet av egenskapen.
     */
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Försöker logga in användaren och ansluta till servern.
     * @param username Användarens användarnamn.
     * @param image Användarens profilbild.
     */
    public void tryLogin(String username, ImageIcon image) {
        user = new User(username, image);
        user.setName(username);
        user.setProfilePic(image);

        userInfoWindow = new UserInfoWindow(user, this);
        loginWindow.setVisible(false);

        connectToServer(user, port);
    }

    /**
     * Öppnar ett chattfönster för en mottagare. Om ett chattfönster redan är öppet,
     * tar det befintliga fönstret fram. Annars skapas ett nytt chattfönster.
     * @param receiver Mottagaren av meddelanden.
     */
    public void openChat(User receiver) {
        ChatWindow chatWindow = openChatWindows.get(receiver.getName());
        if (chatWindow != null) {
            chatWindow.toFront();
            chatWindow.requestFocus();
        } else {
            String chatKey = getChatKey(user, receiver);
            System.out.println(chatKey);
            List<Message> history = conversationHistory.getOrDefault(chatKey, new ArrayList<>());
            chatWindow = new ChatWindow(user, receiver, this);
            chatWindow.displayMessages(history);
            openChatWindows.put(receiver.getName(), chatWindow);
        }
    }
    /**
     * Hanterar stängning av ett chattfönster genom att ta bort det från listan över öppna chattfönster.
     * @param receiver Användaren som har ett chattfönster som ska stängas.
     */
    public void chatWindowClosed(User receiver) {
        openChatWindows.remove(receiver.getName());
    }

    /**
     * Försöker ansluta till servern med användarens information och startar kommunikation.
     * @param user Användaren som försöker ansluta.
     * @param port Porten som används för anslutning.
     */
    public void connectToServer(User user, int port) {
        try {

            socket = new Socket("localhost", port);
            oos = new ObjectOutputStream(socket.getOutputStream());
            this.sender = new Sender(this);
            oos.writeObject(user);
            oos.flush();

            this.receiver = new Receiver(socket, this);
            Thread receiveInfoThread = new Thread(receiver);
            receiveInfoThread.start();
            //sender.requestSavedContacts();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadContactsFromFile();
    }


    /**
     * Sparar den aktuella användarens kontaktlista till en fil.
     */
    public void saveContactsToFile() {
        String userSpecificFileName = CONTACTS_FILE_PREFIX + user.getName() + CONTACTS_FILE_SUFFIX;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(userSpecificFileName))) {
            oos.writeObject(contactsSaved);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Läser in den aktuella användarens kontaktlista från en fil.
     */
    public void loadContactsFromFile() {
        String userSpecificFileName = CONTACTS_FILE_PREFIX + user.getName() + CONTACTS_FILE_SUFFIX;
        File file = new File(userSpecificFileName);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                contactsSaved = (List<Contacts>) ois.readObject();
                firePropertyChange("contactsLoaded", null, contactsSaved);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Lägger till en kontakt till den aktuella användarens sparade kontakter.
     * Om kontakten redan finns visas ett meddelande om detta.
     * @param contact Kontakten som ska läggas till.
     */
    public void addSavedContact(User contact) {
        if (!savedContactNames.contains(contact.getName())) {
            Contacts newContact = new Contacts(contact.getName());
            contactsSaved.add(newContact);
            savedContactNames.add(contact.getName()); // Lägg till namnet i Set
            saveContactsToFile();
            firePropertyChange("saveContact", null, newContact);
        } else {
            JOptionPane.showMessageDialog(null,"Contact already saved");
        }
    }

    /**
     * Skickar ett meddelande till servern som vidarebefordrar till mottagaren.
     * @param message Meddelandet som ska skickas.
     */
    public void sendMessage(Message message) {
        sender.sendMessage(message);

    }

    /**
     * Lagrar ett skickat eller mottaget meddelande i konversationshistoriken.
     * @param message Meddelandet som ska lagras.
     */
    public void storeMessage(Message message) {
        String chatKey = getChatKey(message.getSender(), message.getReceiver());
        conversationHistory.computeIfAbsent(chatKey, k -> new ArrayList<>()).add(message);
    }

    /**
     * Genererar en nyckel baserat på namnen på två användare för att hitta deras chattsession.
     * @param user1 Den ena användaren i konversationen.
     * @param user2 Den andra användaren i konversationen.
     * @return En sträng som representerar nyckeln för konversationen.
     */
    private String getChatKey(User user1, User user2) {
        String[] participants = { user1.getName(), user2.getName() };
        Arrays.sort(participants);
        return participants[0] + ":" + participants[1];
    }

    /**
     * Hämtar ObjectOutputStream associerad med denna klient.
     * @return ObjectOutputStream använd för att skicka objekt till servern.
     */
    public ObjectOutputStream getOos() {
        return oos;
    }

    /**
     * Hämtar en map av öppna chattfönster.
     */
    public Map<String, ChatWindow> getOpenChatWindows() {
        return openChatWindows;
    }

    /**
     * Hämtar socket för denna klient.
     * @return Socket som används för kommunikation med servern.
     */
    public Socket getSocket(){
        return socket;
    }

    /**
     * Hämtar användargränssnittet UserInfoWindow.
     * @return UserInfoWindow som visar den aktuella användarens sparade kontakter och online användare.
     */
    public UserInfoWindow getUserInfoWindow() {
        return userInfoWindow;
    }

    /**
     * Hämtar den aktuella användaren för denna klient.
     * @return Den aktuella användaren.
     */
    public User getUser() {
        return user;
    }

    /**
     * Stänger ner klientens anslutning.
     */
    public void closeSocket() {
        try {

            if (oos != null) {
                oos.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            loginWindow.setVisible(false);
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new Client();
    }
}

