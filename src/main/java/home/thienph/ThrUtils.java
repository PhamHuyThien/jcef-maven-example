package home.thienph;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ThrUtils {
    private static final ConcurrentHashMap<String, AtomicInteger> THREAD_COUNTERS = new ConcurrentHashMap<>();

    public static Thread newNamedThread(String baseName, Runnable runnable) {
        AtomicInteger counter = THREAD_COUNTERS.computeIfAbsent(baseName, k -> new AtomicInteger(0));
        String threadName = baseName + "-" + counter.getAndIncrement();
        return new Thread(runnable, threadName);
    }

    public static void sleep(long mi) {
        try {
            Thread.sleep(mi);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    public static void sleepEnsureMinSecond(long startTime) {
        sleepEnsureMinDuration(startTime, 1000);
    }

    public static void sleepEnsureMinDuration(long startTime, long minDuration) {
        long duration = System.currentTimeMillis() - startTime;
        if (duration < minDuration) ThrUtils.sleep(minDuration - duration);
    }
}
