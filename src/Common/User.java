package Common;

import javax.swing.*;
import java.io.Serializable;

/**
 * Representerar en användare i systemet. Denna klass håller information om användarens namn
 * och profilbild.
 *
 */
public class User implements Serializable {

    private String name;
    private ImageIcon profilePic;

    /**
     * Skapar en ny användare med namn och profilbild.
     *
     * @param name Användarens namn.
     * @param profilePic Användarens profilbild.
     */
    public User(String name, ImageIcon profilePic){
        this.name = name;
        this.profilePic = profilePic;
    }

    /**
     * Jämför denna användare med ett annat objekt för att avgöra om de är lika.
     * Användare anses vara lika om de har samma namn.
     *
     * @param obj Objektet som denna användare ska jämföras med.
     * @return true om objektet är en instans av User och har samma namn, annars false.
     */
    public boolean equals(Object obj){
        if(obj!=null && obj instanceof User)
            return name.equals(((User)obj).getName());
        return false;

    }

    /**
     * Genererar en hashkod för denna användare. Hashkoden baseras på användarens namn.
     *
     * @return En hashkod som representerar användaren.
     */

    public int hashCode(){
        return name.hashCode();
    }

    /**
     * Hämtar användarens namn.
     *
     * @return Användarens namn.
     */
    public String getName() {
        return name;
    }
    /**
     * Sätter användarens namn.
     *
     * @param name Det nya namnet för användaren.
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Hämtar användarens profilbild.
     *
     * @return Användarens profilbild.
     */
    public ImageIcon getProfilePic() {
        return profilePic;
    }
    /**
     * Sätter användarens profilbild.
     *
     * @param profilePic Den nya profilbilden för användaren.
     */
    public void setProfilePic(ImageIcon profilePic) {
        this.profilePic = profilePic;
    }
}
