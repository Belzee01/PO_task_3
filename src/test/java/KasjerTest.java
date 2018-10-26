import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

public class KasjerTest {

    @DataProvider
    public Object[][] correctRestProvider() {
        return new Object[][] {
                {}
        };
    }

    @Test(dataProvider = "correctRestProvider")
    public void shouldReturnCorrectRest(List<NZlotowka> money) {

    }

    @Test
    public void shouldReturnMoneyWhenNotEnoughWasPassed() {

    }

    @Test
    public void shouldReturnMoneyWhenNoOptionToChange() {

    }

}