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

    @Unroll
    def "should throw exception when not possible to make the rest"() {
        given:
        def kasjer = new Kasjer(envelopeInNZlotowka(cashRegister))

        when:
        kasjer.evaluateDenomination(rest)

        then:
        thrown Kasjer.NoRestException

        where:
        cashRegister   | rest
        [2, 5]         | 15
        [10, 10]       | 15
        [2, 2, 10, 10] | 15
        [20]           | 15
        [2, 2, 2, 2]   | 7
    }

    def "should return correct rest with empty cash register"() {
        given:
        def kasjer = new Kasjer()

        when:
        def result = kasjer.kup(convertToNZlotowkaFromListOfLIsts([[10, true], [5, false], [5, false]]), 20)

        then:
        that result.isEmpty(), equalTo(true)
        that convertToInts(kasjer.stanKasy()), equalTo([10, 5, 5])
        that convertToSimpleMap(kasjer.ileCzegoMamy()), equalTo([1: 1, 0: 1])
    }

    def "should return initial money when it is not possible to make rest with empty cash registry"() {
        given:
        def kasjer = new Kasjer()

        when:
        def result = kasjer.kup(convertToNZlotowkaFromListOfLIsts([[10, true], [5, false], [5, false]]), 17)

        then:
        that result.isEmpty(), equalTo(false)
        that result.collect { it -> it.wartosc }, equalTo([10, 5, 5])
        that result.collect { it -> it.zlotowkaNierozmienialna }, equalTo([true, false, false])
        that convertToInts(kasjer.stanKasy()), equalTo([])
        that convertToSimpleMap(kasjer.ileCzegoMamy()), equalTo([:])
    }

    def "should return correct rest with initial cash register not empty"() {
        given:
        def kasjer = new Kasjer(convertToNZlotowkaFromListOfLIsts([[10, true], [5, false], [5, false], [2, true], [2, true]]))

        when:
        def result = kasjer.kup(convertToNZlotowkaFromListOfLIsts([[10, true], [5, false], [5, true]]), 15)

        then:
        that result.isEmpty(), equalTo(false)
        that result.collect { it -> it.wartosc }, equalTo([5])
        that result.collect { it -> it.zlotowkaNierozmienialna }, equalTo([false])
        that kasjer.stanKasy().collect { it -> it.wartosc }, equalTo([10, 5, 2, 2, 10, 5, 5])
        that kasjer.stanKasy().collect { it -> it.zlotowkaNierozmienialna }, equalTo([true, false, true, true, true, true, false])
        that convertToSimpleMap(kasjer.ileCzegoMamy()), equalTo([2: 2, 0: 2, 7:1, 3: 2]) // 1 -> 10nr, 5 -> 5nr, 7 -> 5r, 3 -> 2nr
    }

    def "should return initial money when not enough was passed"() {
        given:
        def kasjer = new Kasjer(convertToNZlotowkaFromListOfLIsts([[10, true], [5, false], [5, false], [2, true], [2, true]]))

        when:
        def result = kasjer.kup(convertToNZlotowkaFromListOfLIsts([[10, true], [5, false], [5, true]]), 25)

        then:
        that result.isEmpty(), equalTo(false)
        that result.collect { it -> it.wartosc }, equalTo([10, 5, 5])
        that result.collect { it -> it.zlotowkaNierozmienialna }, equalTo([true, false, true])
        that kasjer.stanKasy().collect { it -> it.wartosc }, equalTo([10, 5, 5, 2, 2])
        that kasjer.stanKasy().collect { it -> it.zlotowkaNierozmienialna }, equalTo([ true, false, false, true, true])
        that convertToSimpleMap(kasjer.ileCzegoMamy()), equalTo([1: 2, 0: 1, 3: 2])
    }

    def "should return proper rest after changing unchangeable coin"() {
        given:
        def kasjer = new Kasjer(convertToNZlotowkaFromListOfLIsts([[10, true], [5, false], [5, true], [2, false], [1, true]]))

        when:
        def result = kasjer.kup(convertToNZlotowkaFromListOfLIsts([[10, true], [5, true], [5, true]]), 17)

        then:
        that result.isEmpty(), equalTo(false)
        that result.collect { it -> it.wartosc }, equalTo([5, 1, 2])
        that result.collect { it -> it.zlotowkaNierozmienialna }, equalTo([true, true, false])
        that kasjer.stanKasy().collect { it -> it.wartosc }, equalTo([10, 5, 5, 10, 5])
        that kasjer.stanKasy().collect { it -> it.zlotowkaNierozmienialna }, equalTo([true, false, true, true, true])
        that convertToSimpleMap(kasjer.ileCzegoMamy()), equalTo([1: 1, 0: 2, 2: 2])
    }

    def "should return proper rest after changing changeable coin"() {
        given:
        def kasjer = new Kasjer(convertToNZlotowkaFromListOfLIsts([[10, true], [5, false], [5, true], [2, false], [1, true]]))

        when:
        def result = kasjer.kup(convertToNZlotowkaFromListOfLIsts([[10, true], [5, false], [5, true]]), 17)

        then:
        that result.isEmpty(), equalTo(false)
        that result.collect { it -> it.wartosc }, equalTo([1, 2])
        that result.collect { it -> it.zlotowkaNierozmienialna }, equalTo([true, false])
        that kasjer.stanKasy().collect { it -> it.wartosc }, equalTo([10, 5, 5, 10, 5, 5])
        that kasjer.stanKasy().collect { it -> it.zlotowkaNierozmienialna }, equalTo([true, false, true, true, true, false])
        that convertToSimpleMap(kasjer.ileCzegoMamy()), equalTo([1: 2, 0: 2, 2: 2])
    }

    private static List<NZlotowka> envelopeInNZlotowka(List<Integer> values) {
        return values.collect {
            new NZlotowka(it, false)
        }
    }

    private static List<NZlotowka> convertToNZlotowkaFromListOfLIsts(List<List<Object>> values) {
        return values.collect {
            new NZlotowka(it[0], it[1])
        }
    }

    private static def convertToInts(List<NZlotowka> value) {
        return value.collect { it.getWartosc() }
    }

    private static def convertToSimpleMap(Map<NZlotowka, Integer> value) {
        return value.collectEntries { [(it.key.ID): it.value] }
    }
}
