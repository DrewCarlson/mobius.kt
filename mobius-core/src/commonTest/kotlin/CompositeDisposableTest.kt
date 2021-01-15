package kt.mobius

import kt.mobius.disposables.CompositeDisposable
import kt.mobius.disposables.Disposable
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompositeDisposableTest {

    @Test
    fun shouldDisposeAllIncludedDisposables() {
        val one = TestDisposable()
        val two = TestDisposable()
        val three = TestDisposable()

        val composite = CompositeDisposable.from(one, two, three)

        composite.dispose()

        assertTrue(one.disposed)
        assertTrue(two.disposed)
        assertTrue(three.disposed)
    }

    @Test
    fun changingArrayAfterCreatingHasNoEffect() {
        val one = TestDisposable()
        val two = TestDisposable()
        val three = TestDisposable()
        val four = TestDisposable()
        val five = TestDisposable()
        val six = TestDisposable()

        val disposables = arrayOf(one, two, three)

        val composite = CompositeDisposable.from(*disposables)

        disposables[0] = four
        disposables[1] = five
        disposables[2] = six

        composite.dispose()

        assertTrue(one.disposed)
        assertTrue(two.disposed)
        assertTrue(three.disposed)
        assertFalse(four.disposed)
        assertFalse(five.disposed)
        assertFalse(six.disposed)
    }

    private class TestDisposable : Disposable {
        var disposed = false
            private set

        override fun dispose() {
            disposed = true
        }
    }
}
