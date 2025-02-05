package clientMap;

import java.io.Serializable;


/**
 * Representerar en kontakt i klientapplikationen.
 * Denna klass används för att hantera information om kontakter.
 *
 */
public class Contacts  implements Serializable {

    private String name;
    private String imagePath;

    /**
     * Skapar en ny kontakt.
     *
     * @param name Kontaktpersonens namn.
     */
    public Contacts(String name){
        this.name = name;

    }

    /**
     * Hämtar kontaktpersonens namn.
     *
     * @return Namnet på kontakten.
     */

    public String getName() {
        return name;
    }

    /**
     * Hämtar sökvägen till kontaktens profilbild.
     *
     * @return Sökvägen till profilbilden.
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Sätter namnet på kontaktpersonen.
     *
     * @param name Det nya namnet för kontaktpersonen.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sätter eller uppdaterar sökvägen till kontaktpersonens profilbild.
     *
     * @param imagePath Den nya sökvägen till kontaktpersonens profilbild.
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
