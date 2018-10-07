package org.apache.storm.blobstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zhenghao on 2018/7/2.
 */
public class BlobSyncSegment {

    public static final Logger LOG = LoggerFactory.getLogger(BlobSyncSegment.class);
    private Map<String, Lock> segments = new HashMap<>();
    private Map<String, AtomicInteger> lockState = new HashMap<>();
    private final Object sync = new Object();

    public void lock(String key) {
        synchronized (sync) {
            if (segments.containsKey(key)) {
                lockState.get(key).incrementAndGet();
            } else {
                segments.put(key, new ReentrantLock());
                lockState.put(key, new AtomicInteger(1));
                LOG.debug("initialized lock for key: {}, lock {}, thread {}", key, segments.get(key), Thread.currentThread());
            }
        }

        segments.get(key).lock();
    }

    public void unlock(String key) {
        segments.get(key).unlock();
        lockState.get(key).decrementAndGet();
        LOG.debug("unlock for key: {}, lock {}, thread {}", key, segments.get(key), Thread.currentThread());
    }

    public void release(String key) {
        synchronized (sync) {
            if (lockState.get(key).get() == 0) {
                segments.remove(key);
                lockState.remove(key);
            }
        }
    }
}
