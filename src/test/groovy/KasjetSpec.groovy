import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.CoreMatchers.equalTo
import static spock.util.matcher.HamcrestSupport.that

class KasjetSpec extends Specification {

    @Unroll
    def "should return proper rest based on cash register"() {
        given:
        def kasjer = new Kasjer(envelopeInNZlotowka(cashRegister))

        when:
        def result = kasjer.evaluateDenomination(rest)
        def convertedToInts = convertToInts(result)
        convertedToInts.removeAll(expectedRest)

        then:
        that result.sum { i -> i.getWartosc() }, equalTo(rest)
        that convertedToInts.isEmpty(), equalTo(true)
        that convertToInts(kasjer.stanKasy()), equalTo(expectedCashRegister)
        that convertToSimpleMap(kasjer.ileCzegoMamy()), equalTo(expectedCounts)

        where:
        cashRegister                    | rest | expectedRest  | expectedCashRegister | expectedCounts
        [1, 2, 5, 5, 10]                | 17   | [2, 5, 10]    | [5, 1]               | [5: 1, 1: 1]
        [1, 2, 5, 5, 20, 10, 10]        | 17   | [2, 5, 10]    | [10, 20, 5, 1]       | [1: 1, 5: 1, 20: 1, 10: 1]
        [1, 2, 5, 1, 5, 20, 10, 10, 10] | 17   | [1, 1, 5, 10] | [5, 10, 20, 10, 2]   | [5: 1, 20: 1, 10: 2, 2: 1]
        [2, 5, 10]                      | 17   | [2, 5, 10]    | []                   | [:]
    }

    def "should throw exception when not enough money passed"() {
        given:
        def kasjer = new Kasjer(envelopeInNZlotowka(cashRegister))

        when:
        kasjer.evaluateDenomination(rest)

        then:
        thrown Kasjer.NoRestException

        where:
        cashRegister | rest
        [2, 5]       | 15
    }

    private static List<NZlotowka> envelopeInNZlotowka(List<Integer> values) {
        return values.collect {
            new NZlotowka(it, false)
        }
    }

    private static def convertToInts(List<NZlotowka> value) {
        return value.collect { it.getWartosc() }
    }

    private static def convertToSimpleMap(Map<NZlotowka, Integer> value) {
        return value.collectEntries { [(it.key.getWartosc()): it.value] }
    }
}
