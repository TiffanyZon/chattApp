package View;

import serverMap.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * GUI för servern som visar servertrafik och användarinformation.
 * Tillåter även filtrering av serverloggar baserat på datum och tid.
 *
 */

public class ServerGUI extends JFrame {

    private JTextArea textArea;
    private JTextField dateField;
    private JTextField timeField;
    private JTextField secondTimeField;
    private JTextField secondDateTimeField;
    private JTextArea userListArea;
    private Server server;

    /**
     * Skapar ServerGUI och initierar dess komponenter.
     *
     * @param server Referensen till serverinstansen.
     */
    public ServerGUI(Server server) {
        this.server = server;
        initComponents();
    }

    /**
     * Initialiserar komponenterna.
     */
    public void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Chatt server");
        setResizable(false);
        setBackground(Color.GRAY);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        int frameWidth = screenWidth / 4;
        int frameHeight = screenHeight - 200;
        int frameX = (screenWidth - frameWidth) / 2;
        int frameY = (screenHeight - frameHeight) / 2;
        setBounds(frameX, frameY, frameWidth, frameHeight);

        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BorderLayout());
        upperPanel.setPreferredSize(new Dimension(getWidth(), getHeight() / 2));

        JPanel lowerPanel = new JPanel();
        lowerPanel.setLayout(new BorderLayout());
        lowerPanel.setPreferredSize(new Dimension(getWidth(), getHeight() / 2));

        JPanel timeSelect = new JPanel();
        timeSelect.setLayout(new FlowLayout());

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(dateFormatter);

        JLabel dateLabel = new JLabel("Date from: ");
        dateLabel.setFont(new Font("Copperplate", Font.BOLD,12));
        timeSelect.add(dateLabel);

        dateField = new JTextField(13);
        dateField.setText(formattedDate);
        timeSelect.add(dateField);

        JLabel secondDateLabel = new JLabel("Date to: ");
        secondDateLabel.setFont(new Font("Copperplate", Font.BOLD, 12));
        timeSelect.add(secondDateLabel);

        secondDateTimeField = new JTextField(13);
        secondDateTimeField.setText(formattedDate);
        timeSelect.add(secondDateTimeField);

        JLabel timeLabel = new JLabel("Time from (HH:mm:ss): ");
        timeLabel.setFont(new Font("Copperplate", Font.BOLD, 12));
        timeSelect.add(timeLabel);

        timeField = new JTextField(8);
        timeField.setText("00:00:00");
        timeSelect.add(timeField);

        JLabel secondTimeLabel = new JLabel("Time to (HH:mm:ss): ");
        secondTimeLabel.setFont(new Font("Copperplate", Font.BOLD, 12));
        timeSelect.add(secondTimeLabel);

        secondTimeField = new JTextField(8);
        secondTimeField.setText("23:59:59");
        timeSelect.add(secondTimeField);



        JButton selectButton = new JButton("Select: ");
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == selectButton){
                    readFile();
                }
            }
        });
        timeSelect.add(selectButton);

        getContentPane().add(timeSelect, BorderLayout.CENTER);

        JLabel upperLabel = new JLabel("ServerPack.serverMap.Server Traffic");
        upperLabel.setFont(new Font("Copperplate" ,Font.BOLD,18));
        upperLabel.setHorizontalAlignment(JLabel.CENTER);
        upperPanel.add(upperLabel, BorderLayout.NORTH);

        JLabel lowerLabel = new JLabel("Users");
        lowerLabel.setFont(new Font("Copperplate", Font.BOLD,18));
        lowerLabel.setHorizontalAlignment(JLabel.CENTER);
        lowerPanel.add(lowerLabel, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setBackground(Color.lightGray);
        textArea.setEditable(false);

        userListArea = new JTextArea();
        userListArea.setBackground(Color.lightGray);
        userListArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        upperPanel.add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(upperPanel, BorderLayout.NORTH);

        JScrollPane userListScrollPane = new JScrollPane(userListArea);
        lowerPanel.add(userListScrollPane, BorderLayout.CENTER);
        getContentPane().add(lowerPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    /**
     * Uppdaterar GUI med ett nytt meddelande och lagrar det i serverloggen.
     *
     * @param message Meddelandet som ska läggas till i textområdet och loggen.
     */

    public void updateServerGUI(String message) {
        textArea.append(message);
        server.storeInLog(message);
    }

    /**
     * Lägger till en användare i listan över anslutna användare och loggar händelsen.
     *
     * @param username Användarnamnet på den anslutna användaren.
     */
    public void addUser(String username) {
        LocalDateTime currentTime = LocalDateTime.now();
        String logMessage = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " + username + " Connected\n";
        userListArea.append(username + "\n");
        textArea.append(logMessage);
        server.storeInLog(logMessage);
    }

    /**
     * Tar bort en användare från listan över anslutna användare och loggar händelsen.
     *
     * @param username Användarnamnet på den användare som ska tas bort.
     */
    public void removeUser(String username) {
        LocalDateTime currentTime = LocalDateTime.now();
        String logMessage = currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " " + username + " Disconnected\n";

        String users = userListArea.getText();
        String removeUser = users.replace(username + "\n", "");

        userListArea.setText(removeUser);
        textArea.append(logMessage);
        server.storeInLog(logMessage);
    }


    /**
     * Läser och visar filtrerade logginlägg baserade på angivet datum och tid.
     * Använder textfälten för datum och tid för att bestämma vilka logginlägg som ska visas.
     */
    public void readFile() {
        String filePath = "server_log.txt";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime fromDateTime = LocalDateTime.parse(dateField.getText() + " " + timeField.getText(), formatter);
        LocalDateTime toDateTime = LocalDateTime.parse(secondDateTimeField.getText() + " " + secondTimeField.getText(), formatter);

        textArea.setText("");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String dateTimeString = line.substring(0, 19);
                    LocalDateTime messageDateTime = LocalDateTime.parse(dateTimeString, formatter);

                    if ((messageDateTime.isEqual(fromDateTime) || messageDateTime.isAfter(fromDateTime)) &&
                            (messageDateTime.isEqual(toDateTime) || messageDateTime.isBefore(toDateTime))) {
                        textArea.append(line + "\n");
                    }
                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this, "Error parsing log entry: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Log file not found: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading log file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


}

