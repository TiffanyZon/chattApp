package serverMap;

import Common.Message;
import Common.User;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Hanterar lagring och samt hämtar meddelanden som inte kunnat skickas till mottagaren.
 * Säkerställer att meddelanden som inte kan levereras direkt,
 * pga att mottagaren är offline lagras tillfälligt tills mottagaren är online.
 *
 */
public class UnsentMessageHandler {

    private HashMap<User, ArrayList<Message>> unsent = new HashMap<>();

    /**
     * Lägger till ett meddelande i listan av osända meddelanden för en specifik användare.
     * Om det inte finns någon lista för användaren skapas en ny lista.
     *
     * @param user Användaren som meddelandet ska levereras till.
     * @param message Meddelandet som inte kunde levereras.
     */
    public synchronized void put (User user, Message message){
        ArrayList <Message> userMessages = unsent.computeIfAbsent(user, k -> new ArrayList<>());

        userMessages.add(message);
    }

    /**
     * Hämtar listan av osända meddelanden för en specifik användare.
     * Om det inte finns några osända meddelanden för användaren returneras en tom lista.
     *
     * @param user Användaren vars osända meddelanden ska hämtas.
     * @return En lista av osända meddelanden för den angivna användaren.
     */

    public synchronized  ArrayList<Message> get (User user){
        return unsent.getOrDefault(user, new ArrayList<>());

    }

    /**
     * Rensar listan av osända meddelanden för en specifik användare.
     *
     * @param user Användaren vars osända meddelanden ska rensas bort.
     */
    public synchronized void clear (User user){
        unsent.remove(user);
    }
}
