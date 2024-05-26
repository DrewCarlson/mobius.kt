package kt.mobius

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


class FireAtLeastOnceObserverTest {
    private lateinit var observed: MutableList<Int>
    private lateinit var observer: FireAtLeastOnceObserver<Int>

    @BeforeTest
    fun setUp() {
        observed = ArrayList()
        observer = FireAtLeastOnceObserver(observed::add)
    }

    @Test
    fun shouldForwardAcceptValuesNormally() {
        observer.accept(1)
        observer.accept(875)

        assertEquals(mutableListOf(1, 875), observed)
    }

    @Test
    fun shouldForwardAcceptFirstOnce() {
        observer.acceptIfFirst(98)

        assertEquals(mutableListOf(98), observed)
    }

    @Test
    fun shouldForwardAcceptNormallyAfterAcceptFirst() {
        observer.acceptIfFirst(87)
        observer.accept(87678)

        assertEquals(mutableListOf(87, 87678), observed)
    }

    @Test
    fun shouldNotForwardAcceptFirstTwice() {
        observer.acceptIfFirst(87)
        observer.acceptIfFirst(7767)

        assertEquals(mutableListOf(87), observed)
    }

    @Test
    fun shouldNotForwardAcceptFirstAfterNormalAccept() {
        observer.acceptIfFirst(987987)
        observer.acceptIfFirst(7767)

        assertEquals(mutableListOf(987987), observed)
    }
}
