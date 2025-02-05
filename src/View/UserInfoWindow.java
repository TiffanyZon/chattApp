package View;

import Common.User;
import clientMap.Client;
import clientMap.Contacts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI som visar en användares information samt listor över sparade kontakter och
 * onlineanvändare. Tillåter användaren att initiera chattar och hantera sina kontakter.
 *
 */
public class UserInfoWindow extends JFrame implements ActionListener {
    private JLabel userImageLbl;
    private JLabel usernameLbl;
    private JList<String> savedContactsList;
    private JList<String> connectedClientsList;
    private DefaultListModel<String> savedContactsModel;
    private DefaultListModel<String> connectedClientsModel;
    private List<User> listOfUsers;
    private JButton saveContactBtn;
    private JButton openChatBtn;
    private JButton disconnectBtn;
    private User sender;
    private User receiver;
    private Client client;
    private PropertyChangeSupport changeSupport;
    private List<Contacts> loadedContacts = new ArrayList<>();


    /**
     * Skapar ett nytt UserInfoWindow som visar den inloggade användarens information och listor över kontakter.
     *
     * @param sender Den inloggade användaren.
     * @param client Klienten som hanterar anslutningen.
     */
    public UserInfoWindow(User sender, Client client) {
        this.sender = sender;
        this.client = client;

        client.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("saveContact".equals(evt.getPropertyName())) {
                    Contacts savedContact = (Contacts) evt.getNewValue();
                    updateSavedContactsList(savedContact);
                } else if ("contactsLoaded".equals(evt.getPropertyName())) {
                    loadedContacts = (List<Contacts>) evt.getNewValue();

                    updateSavedContactsList(loadedContacts);
                }
            }
        });
        listOfUsers = new ArrayList<>();


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sendLogoutMessage();
            }
        });

        initializeComponents();


    }

    /**
     * Initialiserar komponenterna i användarfönstret.
     */
    private void initializeComponents(){
        setTitle("Pick a user and start chatting!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userImageLbl = new JLabel();
        usernameLbl = new JLabel(sender.getName());
        userImageLbl.setIcon(sender.getProfilePic());
        userPanel.add(userImageLbl);
        userPanel.add(usernameLbl);
        add(userPanel, BorderLayout.NORTH);

        JPanel contactsPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        savedContactsModel = new DefaultListModel<>();
        savedContactsList = new JList<>(savedContactsModel);
        savedContactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel savedContactsPanel = new JPanel(new BorderLayout());
        JLabel savedContactsLabel = new JLabel("Saved Contacts");
        savedContactsPanel.add(savedContactsLabel, BorderLayout.NORTH);
        savedContactsPanel.add(new JScrollPane(savedContactsList), BorderLayout.CENTER);


        connectedClientsModel = new DefaultListModel<>();
        connectedClientsList = new JList<>(connectedClientsModel);
        connectedClientsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel connectedClientsPanel = new JPanel(new BorderLayout());
        JLabel connectedClientsLabel = new JLabel("Online Users");
        connectedClientsPanel.add(connectedClientsLabel, BorderLayout.NORTH);
        connectedClientsPanel.add(new JScrollPane(connectedClientsList), BorderLayout.CENTER);

        contactsPanel.add(savedContactsPanel);
        contactsPanel.add(connectedClientsPanel);

        JPanel buttonPanel = new JPanel();
        saveContactBtn = new JButton("Save contact");
        saveContactBtn.addActionListener(this);
        buttonPanel.add(saveContactBtn);
        openChatBtn = new JButton("Open chat");
        openChatBtn.addActionListener(this);
        buttonPanel.add(openChatBtn);

        disconnectBtn = new JButton("Disconnect");  // Skapa knappen
        disconnectBtn.addActionListener(this);      // Lyssnare för knappen
        buttonPanel.add(disconnectBtn);



        add(contactsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        add(contactsPanel, BorderLayout.CENTER);

        setSize(450, 500);
        setLocationRelativeTo(null);

        setVisible(true);

    }

    /**
     * Lägger till en Listener.
     * @param pcl Den lyssnare som ska läggas till.
     */

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        changeSupport.addPropertyChangeListener(pcl);
    }

    /**
     * Uppdaterar listan över sparade kontakter i användargränssnittet.
     *
     * @param loadedContacts Lista över kontakter att uppdatera med.
     */
    private void updateSavedContactsList(List<Contacts> loadedContacts) {
        savedContactsModel.clear();

        for (Contacts loadedContact : loadedContacts) {
            savedContactsModel.addElement(loadedContact.getName());
        }
    }
    /**
     * Uppdaterar listan över sparade kontakter med en ny kontakt.
     *
     * @param savedContact Den kontakt som ska läggas till i listan.
     */
    private void updateSavedContactsList(Contacts savedContact) {
        savedContactsModel.addElement(savedContact.getName());
    }

    /**
     * Uppdaterar listan över onlineanvändare i användargränssnittet.
     *
     * @param connectedClients Lista över onlineanvändare.
     * @param currentUser Den nuvarande inloggade användaren.
     */
    public void updateConnectedClientsList(List<User> connectedClients, User currentUser) {
        connectedClientsModel.clear();
        listOfUsers.clear();
        listOfUsers = connectedClients;
        sender = currentUser;

        for (User connectedUser : connectedClients) {
            if (!connectedUser.equals(currentUser)) {
                connectedClientsModel.addElement(connectedUser.getName());
            }
        }

    }

    /**
     * Öppnar ett chattfönster med den valda användaren.
     */

    private void openChatWindow() {
        String selectedUsername = connectedClientsList.getSelectedValue();
        if (selectedUsername == null) {
            selectedUsername = savedContactsList.getSelectedValue();
        }
        if (selectedUsername == null) {
            JOptionPane.showMessageDialog(this, "No user selected.");
            return;
        }

        receiver = findUserInLoadedContacts(selectedUsername);

        if (receiver == null) {
            receiver = findUserByUsername(selectedUsername);
        }

        if (receiver != null) {
            client.openChat(receiver);
        } else {
            JOptionPane.showMessageDialog(this, "Could not find user: " + selectedUsername);
        }
    }

    /**
     * Hitta en användare i listan över sparade kontakter baserat på användarnamn.
     *
     * @param username Användarnamnet att söka efter.
     * @return Användaren om den finns, annars null.
     */
    private User findUserInLoadedContacts(String username) {
        for (Contacts loadedContact : loadedContacts) {
            if (loadedContact.getName().equals(username)) {
                return new User(username, null);
            }
        }
        return null;
    }


    /**
     * Hanterar händelser från användargränssnittet, som att spara kontakter, öppna chattar eller logga ut.
     *
     * @param e Händelsen som genererades av användargränssnittet.
     */

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveContactBtn){
            saveContact();
        } else if (e.getSource() == openChatBtn) {
            openChatWindow();
        } else if (e.getSource() == disconnectBtn) {
            sendLogoutMessage();
        }
    }

    /**
     * Skickar ett meddelande till servern för att logga ut den nuvarande användaren.
     */
    private void sendLogoutMessage() {
        client.closeSocket();
    }

    /**
     * Sparar den valda onlineanvändaren som en kontakt.
     */
    private void saveContact() {
        String selectedUsername = connectedClientsList.getSelectedValue();

        if (selectedUsername != null && !selectedUsername.equals(sender.getName())) {
            if (!savedContactsModel.contains(selectedUsername)) {
                User savedUser = findUserByUsername(selectedUsername);

                if (savedUser != null) {
                    client.addSavedContact(savedUser);
                } else {
                    JOptionPane.showMessageDialog(this, "Could not find user: " + selectedUsername);
                }
            } else {
                JOptionPane.showMessageDialog(this, selectedUsername + " is already saved.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No user selected or can't save current user.");
        }
    }

    /**
     * Hitta en användare baserat på användarnamn.
     *
     * @param username Användarnamnet att söka efter.
     * @return Användaren om den finns i listan över användare, annars null.
     */
    private User findUserByUsername(String username) {
        for (User user : listOfUsers) {
            if (user.getName().equals(username)) {
                return user;
            }
        }

        return null;
    }
}

