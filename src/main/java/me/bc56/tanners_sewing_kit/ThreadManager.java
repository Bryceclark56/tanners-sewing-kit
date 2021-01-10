package me.bc56.tanners_sewing_kit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadManager {
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(6, new TannersThreadFactory()); // TODO: Determine best way of managing threads

    public static class TannersThreadFactory implements ThreadFactory {

        private static final String prefix = "TannerKit-Worker-";

        private AtomicInteger threadCount = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + threadCount.incrementAndGet());
            t.setDaemon(true);
            return t;
        }

    }
}
