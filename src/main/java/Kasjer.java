import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Kasjer {

    private static final List<Integer> DENOMINATIONS = Arrays.asList(500, 200, 100, 50, 20, 10, 5, 2, 1);

    private List<NZlotowka> cashRegister = new ArrayList<>();

    public Collection<NZlotowka> kup(Collection<NZlotowka> pieniadze, int kwotaDoZaplaty) {

        int sum = pieniadze.stream().mapToInt(NZlotowka::getWartosc).sum();
        if (sum < kwotaDoZaplaty)
            return pieniadze;

        List<NZlotowka> wynikPracy = new ArrayList<>();

        List<NZlotowka> unchangeable = pieniadze.stream().filter(NZlotowka::isZlotowkaNierozmienialna).collect(Collectors.toList());
        List<NZlotowka> normal = pieniadze.stream().filter(m -> !m.isZlotowkaNierozmienialna()).collect(Collectors.toList());

        sort(unchangeable);
        sort(normal);

        for (int i = unchangeable.size() - 1; i >= 0; i--) {
            if (kwotaDoZaplaty >= unchangeable.get(i).getWartosc()) {
                kwotaDoZaplaty = kwotaDoZaplaty - unchangeable.get(i).getWartosc();
            }
        }

        for (int i = normal.size() - 1; i >= 0; i--) {
            kwotaDoZaplaty = kwotaDoZaplaty - normal.get(i).getWartosc();
            if (kwotaDoZaplaty <= 0)
                break;
        }

        if (kwotaDoZaplaty > 0) {
            for (NZlotowka anUnchangeable : unchangeable) {
                kwotaDoZaplaty = kwotaDoZaplaty - anUnchangeable.getWartosc();

                if (kwotaDoZaplaty <= 0) {
                    wynikPracy.add(anUnchangeable);
                    break;
                }
            }
        }


        return wynikPracy;
    }


    /**
     * Metoda zwraca stan kasy sklepowej czyli, lista wszystkich obietkĂłw NZlotowka,
     * ktĂłre od poczÄtku pracy kasjera wpĹynÄĹy za zakupy a nie zostaĹy przekazane
     * klientowi (jako reszta za zakupy i nierozmienialne NZlotowki stracone z
     * powodu koniecznoĹci ich rozmiany).
     *
     * @return aktualny stan gotĂłwki w kasie.
     */
    public List<NZlotowka> stanKasy() {
        return this.cashRegister;
    }

    /**
     * Metoda zwraca mapÄ, ktĂłrej kluczem jest NZĹotowka a wartoĹcia liczba takich
     * samych NZĹotĂłwek w kasie (porĂłwnujÄc uwzglÄdniamy wyĹÄcznie nominaĹ oraz
     * flagÄ nierozmienialnoĹci). PrzykĹadowo jeĹli w kasie znajdujÄ siÄ: 10zĹ,
     * 20zĹ, 10zĹ, 10zĹNierozmienialne, 1zĹ, 20zĹ, 10zĹ to wynikiem jest:
     *
     * <pre>
     * 10zĹ -&gt; 3
     * 20zĹ -&gt; 2
     * 10zĹNierozmienialne -&gt; 1
     * 1zĹ -&gt; 1
     * </pre>
     *
     * @return mapa zawierajÄca informacjÄ o iloĹci rĂłĹźnych rodzajĂłw NZĹotĂłwek.
     */
    public Map<NZlotowka, Integer> ileCzegoMamy() {
        return this.cashRegister.stream().collect(Collectors.toMap(n -> n, n -> 1, Integer::sum)); //przeciez nie bede zliczal jak zwierze
    }

    private static void sort(List<NZlotowka> nZlotowkas) {
        NZlotowka temp;
        int change = 1;
        while (change > 0) {
            change = 0;
            for (int i = 0; i < nZlotowkas.size() - 1; i++) {
                if (nZlotowkas.get(i).getWartosc() > nZlotowkas.get(i + 1).getWartosc()) {
                    temp = nZlotowkas.get(i + 1);
                    nZlotowkas.set(i + 1, nZlotowkas.get(i));
                    nZlotowkas.set(i, temp);
                    change++;
                }
            }
        }
    }
}