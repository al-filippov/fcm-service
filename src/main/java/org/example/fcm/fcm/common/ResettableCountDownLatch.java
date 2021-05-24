package org.example.fcm.fcm.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

class ResettableCountDownLatch {
    private final Sync sync;

    public ResettableCountDownLatch(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count < 0");
        } else {
            this.sync = new Sync(count);
        }
    }

    public void await() throws InterruptedException {
        this.sync.acquireSharedInterruptibly(1);
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return this.sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    public void countDown() {
        this.sync.releaseShared(1);
    }

    public int getCount() {
        return this.sync.getCount();
    }

    public void reset(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count < 0");
        }
        this.sync.reset(count);
    }

    public String toString() {
        return super.toString() + "[Count = " + this.sync.getCount() + "]";
    }

    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            this.setState(count);
        }

        int getCount() {
            return this.getState();
        }

        protected int tryAcquireShared(int acquires) {
            return this.getState() == 0 ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            int c;
            int nextc;
            do {
                c = this.getState();
                if (c == 0) {
                    return false;
                }

                nextc = c - 1;
            } while (!this.compareAndSetState(c, nextc));

            return nextc == 0;
        }

        protected void reset(int count) {
            this.setState(count);
        }
    }
}
