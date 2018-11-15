import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PMO_Test {
    //////////////////////////////////////////////////////////////////////////
    private static final Map<String, Double> tariff = new HashMap<>();
    private static final List<Integer> correctDenominations = Arrays.asList(1, 2, 5, 10, 20, 50, 100, 200, 500);

    static {
        tariff.put("notEnoughMoney", 1.0);
        tariff.put("noChange", 1.0);
        tariff.put("simplePurchase", 1.0);
        tariff.put("change", 1.6 );
        tariff.put("change2", 1.6 );
    }

    private Kasjer kasjer;
    private Set<Integer> correctIDs;
    //////////////////////////////////////////////////////////////////////////

    public static double getTariff(String testName) {
        return tariff.get(testName);
    }

    private static void showException(Exception e, String txt) {
        e.printStackTrace();
        fail(txt + " " + e.toString());
    }

    private static NZlotowka generate(int value) {
        return generate(value, false);
    }

    private static NZlotowka generate(int value, boolean flagaNierozmienialnosci) {
        NZlotowka result = new NZlotowka(value, flagaNierozmienialnosci);
        return result;
    }

    private static void testDenomination(NZlotowka zloty) {
        assertTrue(correctDenominations.contains(zloty.getWartosc()), zloty + " ma niedozwolony nominaĹ");
    }

    private static void testDenomination(Collection<NZlotowka> zlotys) {
        zlotys.stream().forEach(z -> testDenomination(z));
    }

    private static void testNull(Collection<NZlotowka> zlotys) {
        assertNotNull(zlotys, "Zamiast kolekcji bÄdÄcej wynikiem pracy metody kup pojawiĹ siÄ null");
        zlotys.forEach(zloty -> assertNotNull(zloty, "W wynikowej kolekcji odnaleziono null "));
    }

    // obliczenie wartoĹci pieniedzy w tablicy zlotys
    private static int calcValue(Collection<NZlotowka> zlotys, boolean countUnchangable) {
        testDenomination(zlotys);

        if (countUnchangable) {
            return zlotys.stream().mapToInt(zloty -> zloty.getWartosc()).sum();
        } else {
            return zlotys.stream().filter(zloty -> !zloty.isZlotowkaNierozmienialna())
                    .mapToInt(zloty -> zloty.getWartosc()).sum();
        }
    }

    private Collection<NZlotowka> executeKup(Collection<NZlotowka> money, int toPay) {
        Collection<NZlotowka> result = null;
        try {
            result = assertTimeoutPreemptively(java.time.Duration.ofSeconds(1000), () -> {
                return kasjer.kup(new ArrayList<>(money), toPay);
            });
        } catch (Exception e) {
            showException(e, "Niespodziewany bĹÄd w trakcie pracy kup");
            fail("W trakcie wykonania metody kup doszĹo do bĹÄdu");
        }
        return result;
    }

    private Collection<NZlotowka> executeStanKasy() {
        Collection<NZlotowka> result = null;
        try {
            result = assertTimeoutPreemptively(java.time.Duration.ofSeconds(1), () -> {
                return kasjer.stanKasy();
            });
        } catch (Exception e) {
            showException(e, "Niespodziewany bĹÄd w trakcie pracy stanKasy");
            fail("W trakcie wykonania metody stanKasy doszĹo do bĹÄdu");
        }
        return result;
    }

    private Map<NZlotowka, Integer> executeIleCzegoMamy() {
        Map<NZlotowka, Integer> result = null;
        try {
            result = assertTimeoutPreemptively(java.time.Duration.ofSeconds(1), () -> {
                return kasjer.ileCzegoMamy();
            });
        } catch (Exception e) {
            showException(e, "Niespodziewany bĹÄd w trakcie pracy ileCzegoMamy");
            fail("W trakcie wykonania metody ileCzegoMamy doszĹo do bĹÄdu");
        }
        return result;
    }

    @BeforeEach
    public void createSimpleCalculations() {
        kasjer = new Kasjer();
        correctIDs = new HashSet<>();
    }

    private void addIDs(Collection<NZlotowka> zlotys) {
        zlotys.forEach(z -> correctIDs.add(z.getID()));
    }

    private boolean testIDs(Collection<NZlotowka> zlotys) {
        return zlotys.stream().allMatch(z -> correctIDs.contains(z.getID()));
    }

    private void removeIDs(Collection<NZlotowka> zlotys) {
        zlotys.forEach(z -> correctIDs.remove(z.getID()));
    }

    private static Stream<Arguments> simplePurchaseTestDataProvider() {
        return Stream.of(Arguments.of(new NZlotowka[] { generate(10), generate(2), generate(20) }, 31, 1),
                Arguments.of(new NZlotowka[] { generate(10), generate(200), generate(20) }, 31, 199));
    }

    private static boolean testEquivalence(Collection<NZlotowka> n1, Collection<NZlotowka> n2) {
        return n1.containsAll(n2) && n2.containsAll(n1);
    }

    private static String toString(Collection<NZlotowka> collection) {
        StringBuffer sb = new StringBuffer();
        collection.forEach(z -> sb.append(z.toString() + " "));
        return sb.toString();
    }

    private void assertEqualMap(Map<NZlotowka, Integer> expected, Map<NZlotowka, Integer> actual) {
        assertTrue(testEquivalence(expected.keySet(), actual.keySet()),
                "Mapa powinna mieÄ inny zestaw kluczy, oczekiwano " + toString(expected.keySet()) + " a jest "
                        + toString(actual.keySet()));
        for (Map.Entry<NZlotowka, Integer> entry : expected.entrySet()) {
            assertEquals(entry.getValue(), actual.get(entry.getKey()),
                    "Oczekiwano innej liczby pieniÄdzy typu " + entry.getKey());
        }
    }

    @Test
    @DisplayName("Test zakupu za zbyt maĹo pieniÄdzy")
    public void notEnoughMoney() {
        Collection<NZlotowka> money = List.of(generate(10), generate(20), generate(1), generate(1));
        Collection<NZlotowka> result = executeKup(money, 33);
        testNull(result);
        if (!testEquivalence(money, result))
            fail("Kasjer dostaĹ za maĹo pieniÄdzy - oczekiwano ich zwrotu, powinno byÄ " + toString(money) + " jest "
                    + toString(result));
    }

    @Test
    @DisplayName("Test braku reszty")
    public void noChange() {
        Collection<NZlotowka> money = List.of(generate(10), generate(20), generate(10), generate(1));
        Collection<NZlotowka> result = executeKup(money, 32);
        testNull(result);
        if (!testEquivalence(money, result))
            fail("Kasjer nie moĹźe wydaÄ reszty - oczekiwano zwrotu, powinno byÄ " + toString(money) + " jest "
                    + toString(result));
    }

    @Test
    @DisplayName("Test zakupĂłw za odliczonÄ kwotÄ")
    public void simplePurchase() {
        Collection<NZlotowka> money1 = List.of(generate(10), generate(20), generate(1), generate(1));
        Collection<NZlotowka> money2 = List.of(generate(100), generate(200), generate(1), generate(1));
        addIDs(money1);
        addIDs(money2);
        Collection<NZlotowka> result1 = executeKup(money1, 32);
        Collection<NZlotowka> result2 = executeKup(money2, 302);
        testNull(result1);
        testNull(result2);
        assertEquals(0, result1.size(), "Zakup za odliczonÄ kwotÄ nie powinien daÄ reszty");
        assertEquals(0, result2.size(), "Zakup za odliczonÄ kwotÄ nie powinien daÄ reszty");

        Collection<NZlotowka> cash = executeStanKasy();

        if (!testIDs(cash)) {
            fail("Po zakupach w kasie powinny byÄ pieniÄdze zostawione przez klientĂłw. Metoda stanKasy ich nie ujawnia."
                    + "\n Powinno byÄ: " + correctIDs + " a jest " + cash);
        }

        Map<NZlotowka, Integer> expectedMap = Map.ofEntries(Map.entry(generate(1), 4), Map.entry(generate(10), 1),
                Map.entry(generate(20), 1), Map.entry(generate(100), 1), Map.entry(generate(200), 1));
        assertEqualMap(expectedMap, executeIleCzegoMamy());
    }

    @Test
    @DisplayName("Test wydawania pieniÄdzy")
    public void change() {

        NZlotowka nz5 = generate(5);
        NZlotowka nz10 = generate(10);
        NZlotowka nz10n = generate(10,true);
        NZlotowka nz100 = generate(100);

        // najpierw dodajemy trochÄ pieniÄdzy do kasy
        Collection<NZlotowka> money = List.of(nz10, generate(20));
        addIDs(money);
        Collection<NZlotowka> result = executeKup(money, 30);
        testNull(result);
        assertEquals(0, result.size(), "Zakup za odliczonÄ kwotÄ nie powinien daÄ reszty");
        money = List.of(nz5, generate(2), generate(1));
        addIDs(money);
        result = executeKup(money, 8);
        testNull(result);
        assertEquals(0, result.size(), "Zakup za odliczonÄ kwotÄ nie powinien daÄ reszty");

        purchaseWithChange(List.of(nz100), 90, List.of( nz10 ) );
        purchaseWithChange(List.of(generate(200)), 100, List.of( nz100 ) );
        purchaseWithChange(List.of(nz10n), 5, List.of( nz10n, nz5 ) ); // tu pojawia sie nierozmienialna 10zĹ.
    }

    private void purchaseWithChange(Collection<NZlotowka> money, int price, Collection<NZlotowka> changeExpected) {

        Collection<NZlotowka> result;
        addIDs(money);
        result = executeKup(money, price);
        testNull(result);

        assertEquals(changeExpected.size(), result.size(), "Oczekiwano innej liczby NZlotowek w reszcie");
        assertEquals(calcValue(changeExpected, true), calcValue(result, true), "Oczekiwano innej kwoty reszty");

        if (! testIDs(changeExpected)) {
            fail("Wydano resztÄ obiektem NZlotowka, ktĂłrego nie przekazaĹ klient." );
        }
        removeIDs(result);
    }

    @Test
    @DisplayName("Test wydawania pieniÄdzy + stanKasy + ileCzegoMamy")
    public void change2() {

        NZlotowka nz2a = generate(2);
        NZlotowka nz2b = generate(2);
        NZlotowka nz5 = generate(5);
        NZlotowka nz10n = generate(10,true);
        NZlotowka nz100 = generate(100);

        // najpierw dodajemy trochÄ pieniÄdzy do kasy
        Collection<NZlotowka> money = List.of(nz2a, nz2b, nz5, nz100 );
        addIDs(money);
        Collection<NZlotowka> result = executeKup(money, 109 );
        testNull(result);
        assertEquals(0, result.size(), "Zakup za odliczonÄ kwotÄ nie powinien daÄ reszty");

        purchaseWithChange(List.of(nz10n), 1, List.of( nz2a, nz2b, nz5, nz10n ) );

        Collection<NZlotowka> cash = executeStanKasy();

        if (!testIDs(cash)) {
            fail("Po zakupach w kasie powinny byÄ pieniÄdze zostawione przez klientĂłw. Metoda stanKasy ich nie ujawnia."
                    + "\n Powinno byÄ: " + correctIDs + " a jest " + cash);
        }

        Map<NZlotowka, Integer> expectedMap = Map.ofEntries(Map.entry(generate(100), 1 ));
        assertEqualMap(expectedMap, executeIleCzegoMamy());
    }

}