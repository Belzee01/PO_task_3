import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class Kasjer {

    private List<NZlotowka> cashRegister;
    private Stack<NZlotowka> stack;

    public Kasjer() {
        this.cashRegister = new ArrayList<>();
        this.stack = new Stack<>();
    }

    public Kasjer(List<NZlotowka> cashRegister) {
        this.cashRegister = cashRegister;
        this.stack = new Stack<>();
    }

    public Collection<NZlotowka> kup(Collection<NZlotowka> pieniadze, int kwotaDoZaplaty) {

        int sum = pieniadze.stream().mapToInt(NZlotowka::getWartosc).sum();
        if (sum < kwotaDoZaplaty)
            return pieniadze;

        List<NZlotowka> wynikPracy = new ArrayList<>();

        List<NZlotowka> unchangeable = pieniadze.stream().filter(NZlotowka::isZlotowkaNierozmienialna).collect(Collectors.toList());
        List<NZlotowka> normal = pieniadze.stream().filter(m -> !m.isZlotowkaNierozmienialna()).collect(Collectors.toList());

        sort(unchangeable);
        sort(normal);

        for (int i = unchangeable.size() - 1; i >= 0; i--) { // try to take as much of unchangeable coins as possible
            if (kwotaDoZaplaty >= unchangeable.get(i).getWartosc()) {
                kwotaDoZaplaty = kwotaDoZaplaty - unchangeable.get(i).getWartosc();
            }
        }

        for (int i = normal.size() - 1; i >= 0; i--) { // try to take as much of changeable coins as possible
            kwotaDoZaplaty = kwotaDoZaplaty - normal.get(i).getWartosc();
            if (kwotaDoZaplaty <= 0)
                break;
        }

        if (kwotaDoZaplaty > 0) { // if there is still not enough money, then try to take another unchangeable coins
            for (NZlotowka anUnchangeable : unchangeable) {
                kwotaDoZaplaty = kwotaDoZaplaty - anUnchangeable.getWartosc();

                if (kwotaDoZaplaty < 0) {
                    wynikPracy.add(anUnchangeable);
                    break;
                }
            }
        }

        int rest = kwotaDoZaplaty * (-1);
        int cashRegisterSum = getSumOfCashRegister();
        if (cashRegisterSum < rest) // check whether there is enough money in cash register
            return pieniadze;
        else if (cashRegisterSum == rest) // if cash register has the equal amount of money as that we want to return, then return all
            wynikPracy.addAll(this.cashRegister);
        else { // if there is more than we want to return, then we have to check if it possible to return the rest
            try {
                wynikPracy.addAll(evaluateDenomination(rest));
            } catch (NoRestException e) {
                return pieniadze;
            } finally {
                stack.removeAllElements();
            }
        }

        return wynikPracy;
    }

    private int getSumOfCashRegister() {
        return this.cashRegister.stream().mapToInt(NZlotowka::getWartosc).sum();
    }

    public List<NZlotowka> evaluateDenomination(int cost) throws NoRestException {
        checkIfItIsPossibleToGetTheRest(cost, this.cashRegister);
        if (stack.empty())
            throw new NoRestException("No suitable denomination in cash registry to give the rest");
        return new ArrayList<>(stack);
    }

    private boolean checkIfItIsPossibleToGetTheRest(int rest, List<NZlotowka> currentCash) {
        if (rest == 0)
            return true;

        if (rest < 0)
            return false;

        for (int i = 0; i < currentCash.size(); i++) {
            NZlotowka temp = currentCash.get(i);
            currentCash.remove(i);
            boolean found = checkIfItIsPossibleToGetTheRest(rest - temp.getWartosc(), currentCash);
            if (found) {
                stack.push(temp);
                return true;
            }
            currentCash.add(temp);
        }

        return false;
    }


    public List<NZlotowka> stanKasy() {
        return this.cashRegister;
    }

    public Map<NZlotowka, Integer> ileCzegoMamy() {
        return this.cashRegister.stream().collect(Collectors.toMap(n -> n, n -> 1, Integer::sum)); //przeciez nie bede zliczal jak zwierze
    }

    //bubble sort
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

    private List<Integer> getCurrentDenominations() {
        return this.ileCzegoMamy().entrySet().stream().mapToInt(e -> e.getKey().getWartosc()).boxed().collect(Collectors.toList());
    }

    //Exception thrown when it is not possible to produce the rest
    public static class NoRestException extends Exception {
        public NoRestException(String message) {
            super(message);
        }
    }
}