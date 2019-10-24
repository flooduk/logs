import java.util.concurrent.atomic.AtomicBoolean;

final class AtomicLock extends AtomicBoolean {
    AtomicLock() {
        super(false);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    void lock() {
        while (!compareAndSet(false, true)) ;
    }

    void unlock() {
        set(false);
    }
}
