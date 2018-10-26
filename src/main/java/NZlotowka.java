/**
 * Klasa reprezentuje monetÄ lub banknot o wartoĹci N zĹotych. W zaleĹźnoĹci od
 * stanu flagi reprezentowana jest zwykĹad lub nierozmienialna jednostka
 * pĹatnicza.
 */
public class NZlotowka {
    /**
     * NominaĹ NZĹotĂłwki.
     */
    private final int wartosc;
    /**
     * Flaga nierozmienialnoĹci. JeĹli true - mamy do czynienia z nierozmienialnÄ
     * NZĹotĂłwkÄ.
     */
    private final boolean zlotowkaNierozmienialna;
    /**
     * Unikalny numer seryjny
     */
    private final int id;
    private static int counter;

    public NZlotowka(int wartosc, boolean zlotowkaNierozmienialna) {
        this.wartosc = wartosc;
        this.zlotowkaNierozmienialna = zlotowkaNierozmienialna;
        id = counter++;
    }

    /**
     * Metoda zwraca nominaĹ NZlotowka.
     *
     * @return wartoĹÄ (nominaĹ) obiektu NZlotowka
     */
    public int getWartosc() {
        return wartosc;
    }

    /**
     * Metoda pozwala na sprawdzenie czy NZlotowka jest nierozmienialna.
     *
     * @return true - zlotowka nierozmienialna, false - zwykla zlotowka.
     */
    public boolean isZlotowkaNierozmienialna() {
        return zlotowkaNierozmienialna;
    }

    /**
     * Metoda zwraca numer seryjny obiektu NZlotowka.
     *
     * @return numer seryjny NZlotowka.
     */
    public int getID() {
        return id;
    }

    @Override
    public String toString() {
        return "NZlotowka [id=" + id + ", wartosc=" + wartosc + ", zlotowkaNierozmienialna=" + zlotowkaNierozmienialna
                + "]";
    }

    /**
     * Metoda generuje hashCode dla obiektu. Uwaga metoda nie uwzglÄdnia id czyli
     * dwa obiekty o tej samej wartoĹci i ustawieniu flagi zlotowkaNierozmienialna
     * dadzÄ tÄ samÄ wartoĹÄ hashCode.
     *
     * @return hashCode dla obiektu
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + wartosc;
        result = prime * result + (zlotowkaNierozmienialna ? 1231 : 1237);
        return result;
    }

    /**
     * Metoda sprawdza czy dwa obiekty NZlotowka sÄ identyczne. Metoda nie
     * uwzglÄdnia pola id czyli do wykazania zgodnoĹci wystarczy zgodnoĹÄ wartoĹci i
     * ustawienia flagi zlotowkaNierozmienialna.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NZlotowka other = (NZlotowka) obj;
        if (wartosc != other.wartosc)
            return false;
        if (zlotowkaNierozmienialna != other.zlotowkaNierozmienialna)
            return false;
        return true;
    }

}