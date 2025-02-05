package View;

import clientMap.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Ett fönster för inloggning till chattapplikationen. Användare kan ange sitt användarnamn,
 * välja en profilbild och logga in.
 **/

public class LoginWindow extends JFrame implements ActionListener {
    private JTextField userTxtField;
    private JLabel showPicLbl;
    private JButton loginBtn;
    private JButton picBtn;

    private ImageIcon profilePic;
    private Client client;

    /**
     * Skapar ett nytt inloggningsfönster.
     *
     * @param client Klientinstansen som används för att ansluta till servern.
     */
    public LoginWindow(Client client) {
        this.client = client;

        initializeComponents();
    }

    /**
     * Initialiserar komponenterna i inloggningsfönstret.
     */
    private void initializeComponents()
    {
        setTitle("Chatt App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        showPicLbl = new JLabel();
        showPicLbl.setHorizontalAlignment(JLabel.CENTER);

        ImageIcon icon = new ImageIcon("/Users/tiffanyzon/Desktop/ChattApp2024/src/chattIcon/chattIcon.jpeg");
        showPicLbl.setIcon(icon);
        showPicLbl.setPreferredSize(new Dimension(300,200));
        add(showPicLbl, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(2,2,5,5));

        panel.add(new JLabel("Username"));
        userTxtField = new JTextField();
        panel.add(userTxtField);

        add(panel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1,2,5,5));

        loginBtn = new JButton("Logga in");
        loginBtn.addActionListener(this);
        btnPanel.add(loginBtn);

        picBtn = new JButton("Välj bild");
        picBtn.addActionListener(this);
        btnPanel.add(picBtn);



        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }


    /**
     * Hanterar händelser från användargränssnittet, som inloggning eller val av profilbild.
     *
     * @param e Händelsen som genererades av användargränssnittet.
     */

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == picBtn){
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("/Users/tiffanyzon/Desktop/ChattApp2024/src/profilePic"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("All pics", "png", "jpeg", "jpg");
            fileChooser.setFileFilter(filter);

            int option = fileChooser.showOpenDialog(this);

            if (option == JFileChooser.APPROVE_OPTION){
                File fileSelected = fileChooser.getSelectedFile();
               try{
                   BufferedImage bfrImage = ImageIO.read(fileSelected);
                   Image newScale = bfrImage.getScaledInstance(150,150, Image.SCALE_SMOOTH);
                   profilePic = new ImageIcon(newScale);
                   showPicLbl.setIcon(profilePic);
               } catch (Exception ex){
                   ex.printStackTrace();
               }
            }

        } else if (e.getSource() == loginBtn){
            String username = userTxtField.getText();

            if(username != null && !username.trim().isEmpty() && profilePic != null){
                client.tryLogin(username, profilePic);
            } else {
                JOptionPane.showMessageDialog(this,"Enter a username and select a profile picture before trying to connect to Chatt App.");
            }
        }


    }

}
