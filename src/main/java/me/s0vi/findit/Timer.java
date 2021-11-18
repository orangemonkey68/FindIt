package me.s0vi.findit;

/**
 * A simple single interval timer. This means that calling #start() will reset
 * the time stored.
 */
public class Timer {
    private long startTime = 0;
    private long endTime = 0;

    public Timer start() {
        startTime = System.nanoTime();
        return this;
    }

    public Timer stop() {
        endTime = System.nanoTime();
        return this;
    }

    public long getTimeNanos() {
        return endTime - startTime;
    }

    public long getTimeMillis() {
        return getTimeNanos() / 1000000;
    }
}
