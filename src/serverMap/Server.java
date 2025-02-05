package serverMap;

import Common.Message;
import Common.User;
import View.ServerGUI;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hanterar serverlogiken för chattapplikationen.
 * Den accepterar anslutningar från klienter, hanterar inkommande meddelanden och skickar vidare de till rätt mottagare.
 *
 */

public class Server {
    private ServerSocket serverSocket;
    private ServerGUI serverGUI;
    private Map<String, ObjectOutputStream> clientStream = new HashMap<>();
    private ClientCommunicationHandler clientCommunicationHandler;
    private List<User> connectedClients = new ArrayList<>();
    private UnsentMessageHandler unsentMessageHandler = new UnsentMessageHandler();
    private Map<User, List<Message>> unsentMessages = new HashMap<>();

    /**
     * Skapar en ny serverinstans och initialiserar serversocketen med den angivna porten.
     *
     * @param port Porten servern ska lyssna på.
     */
    public Server(int port){
        serverGUI = new ServerGUI(this);
        try {
            serverSocket = new ServerSocket(port);
            clientCommunicationHandler = new ClientCommunicationHandler(clientStream);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Startar servern och lyssnar efter nya klientanslutningar.
     * För varje ny anslutning startas en ny tråd för att hantera klientens kommunikation.
     */
    public void openServer(){
        try {
            while (true){
                Socket clientSocket = serverSocket.accept();


                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

                User user = (User) ois.readObject();

                if (!connectedClients.contains(user)) {
                    new Thread(new ClientHandler(clientSocket, serverGUI, clientCommunicationHandler, user, ois)).start();

                    clientCommunicationHandler.addUserStream(user,oos);
                    clientStream.put(user.getName(),oos);
                } else {
                    ois.close();
                    oos.close();
                    clientSocket.close();
                }
            }
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    /**
     * Skickar en lista över alla anslutna klienter till alla anslutna klienter.
     * Används för att uppdatera klienternas användarlistor.
     *
     * @param connectedClients Lista över anslutna klienter.
     */
    public void broadcastList(List<User> connectedClients) {
        List<User> connectedClientsCopy = new ArrayList<>(connectedClients);
        Map<String, ObjectOutputStream> failedStreams = new HashMap<>();

        for (Map.Entry<String, ObjectOutputStream> entry : clientStream.entrySet()) {
            try {
                entry.getValue().writeObject(connectedClientsCopy);
                entry.getValue().flush();
            } catch (IOException e) {
                failedStreams.put(entry.getKey(), entry.getValue());
            }
        }

        for (String key : failedStreams.keySet()) {
            clientStream.remove(key);
        }
    }

    /**
     * Kontrollerar om en användare är online.
     *
     * @param user Användaren som ska kontrolleras.
     * @return true om användaren är online, annars false.
     */

    public boolean isUserOnline(User user) {
        return connectedClients.contains(user);
    }

    /**
     * Kopplar från en användare och uppdaterar alla anslutna klienters användarlistor.
     *
     * @param user Användaren som ska kopplas från.
     */
    public void disconnectUser(User user) {
        serverGUI.removeUser(user.getName());

        connectedClients.remove(user);
        clientStream.remove(user);

        broadcastList(connectedClients);

        if (connectedClients.isEmpty()) {
            System.out.println("No users connected");

        } else {
            System.out.println("ClientHandler for " + user.getName() + " terminated.");
        }

    }

    /**
     * Lagrar ett meddelande i serverns loggfil.
     *
     * @param message Meddelandet som ska lagras.
     */
    public void storeInLog(String message) {
        try {
            String logFilePath = "server_log.txt";

            File logFile = new File(logFilePath);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(logFile, true);

            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(message);
            bufferedWriter.newLine();

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Hanterar inloggningen för en användare och skickar eventuella osända meddelanden till den användaren.
     *
     * @param user Användaren som loggar in.
     */
    public void userLoggedIn(User user) {

        List<Message> unsentMessages = unsentMessageHandler.get(user);
        if (unsentMessages != null && !unsentMessages.isEmpty()) {

            for (Message message : unsentMessages) {
                LocalDateTime currentTime = LocalDateTime.now();
                serverGUI.updateServerGUI(currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " "+message.getReceiver().getName() + " received message from: " + message.getSender().getName() + " Message text: " + message.getMessage() + " Image: " + message.getImage() + "\n");
                clientCommunicationHandler.sendMessage(message);
            }
            unsentMessageHandler.clear(user);
        }
    }

    public static void main(String[] args) {
        Server server = new Server(2222);
        server.openServer();
    }


    /**
     * Inre klass som hanterar kommunikation med en specifik klient. Tar emot meddelanden från klienten
     * och vidarebefordrar de till mottagaren eller lagrar de om mottagaren inte är online.
     */
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ClientCommunicationHandler clientCommunicationHandler;
        private ServerGUI serverGUI;
        private User user;
        private ObjectInputStream ois;

        /**
         * Skapar en ny ClientHandler för att hantera kommunikation med en specifik klient.
         *
         * @param clientSocket Klientens socket.
         * @param serverGUI Serverns GUI.
         * @param clientCommunicationHandler Kommunikationshanterare.
         * @param user Användaren associerad med denna klient.
         * @param ois Ois för att läsa meddelanden från klienten.
         */
        public ClientHandler(Socket clientSocket, ServerGUI serverGUI, ClientCommunicationHandler clientCommunicationHandler, User user, ObjectInputStream ois) throws IOException {
            this.clientSocket = clientSocket;
            this.serverGUI = serverGUI;
            this.user = user;
            this.ois = ois;
            this.clientCommunicationHandler = clientCommunicationHandler;

        }

        /**
         * Lyssnar på meddelanden från klienten och hanterar de.
         */
        @Override
        public void run() {
            try {

                String username = user.getName();
                serverGUI.addUser(username);
                connectedClients.add(user);
                broadcastList(connectedClients);
                userLoggedIn(user);

                while (!clientSocket.isClosed()) {
                    try {
                        Object obj = ois.readObject();
                         if (obj instanceof Message) {
                            Message userMessage = (Message) obj;
                            if (isUserOnline(userMessage.getReceiver())) {
                                LocalDateTime currentTime = LocalDateTime.now();
                                String serverGUImessageOnline = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " +userMessage.getSender().getName() + " sent a message to online user: " + userMessage.getReceiver().getName() + " Messagetext: " + userMessage.getMessage() + " Image sent: " + userMessage.getImage() + "\n";
                                serverGUI.updateServerGUI(serverGUImessageOnline);
                                clientCommunicationHandler.sendMessage(userMessage);
                            } else {
                                LocalDateTime currentTime = LocalDateTime.now();
                                String serverGUImessageOffline = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " +userMessage.getSender().getName() + " message to offline user: " + userMessage.getReceiver().getName() + " Messagetext: " + userMessage.getMessage() + " Image sent: " + userMessage.getImage() + "\n";
                                serverGUI.updateServerGUI(serverGUImessageOffline);
                                unsentMessageHandler.put(userMessage.getReceiver(),userMessage);
                            }                        }
                    }
                    catch (IOException e) {
                        break;

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (ois != null) ois.close();
                    if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                disconnectUser(user);
            }
        }

    }
}

