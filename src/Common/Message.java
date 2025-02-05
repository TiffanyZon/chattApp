package Common;

import javax.swing.*;
import java.io.Serializable;

/**
 * Representerar ett meddelande. Kan innehålla text, en bild eller båda.
 *
 */
public class Message implements Serializable {

    private User sender;
    private User receiver;

    private String message;
    private ImageIcon image;

    /**
     * Skapar ett textmeddelande.
     *
     * @param sender Avsändaren av meddelandet.
     * @param receiver Mottagaren av meddelandet.
     * @param message Text i meddelandet.
     */
    public Message(User sender, User receiver, String message){
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.image = null;
    }
    /**
     * Skapar ett meddelande som innehåller en bild.
     *
     * @param sender Avsändaren av meddelandet.
     * @param receiver Mottagaren av meddelandet.
     * @param image Bilden som skickas med meddelandet.
     */

    public Message (User sender, User receiver, ImageIcon image){
        this.sender = sender;
        this.receiver = receiver;
        this.image = image;
        this.message = null;

    }

    /**
     * Skapar ett meddelande som innehåller både text och bild.
     *
     * @param sender Avsändaren av meddelandet.
     * @param receiver Mottagaren av meddelandet.
     * @param message Text i meddelandet.
     * @param image Bilden som skickas med meddelandet.
     */
    public Message(User sender, User receiver, String message, ImageIcon image){
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.image = image;
    }

    /**
     * Hämtar avsändaren av meddelandet.
     *
     * @return Avsändaren.
     */
    public User getSender() {
        return sender;
    }

    /**
     * Hämtar mottagaren av meddelandet.
     *
     * @return Mottagaren.
     */

    public User getReceiver() {
        return receiver;
    }

    /**
     * Hämtar texten i meddelandet.
     *
     * @return Texten.
     */

    public String getMessage() {
        return message;
    }
    /**
     * Hämtar bilden som skickas med meddelandet.
     *
     * @return Bilden.
     */
    public ImageIcon getImage() {
        return image;
    }
}
