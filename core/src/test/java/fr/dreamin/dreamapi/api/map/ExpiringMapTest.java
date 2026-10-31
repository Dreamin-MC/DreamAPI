package fr.dreamin.dreamapi.api.map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExpiringMap Caffeine Migration Tests (Feature F1)")
class ExpiringMapTest {

  @Test
  @DisplayName("Basic CRUD operations and presence checks")
  void testBasicCrud() {
    final var map = new ExpiringMap<String, String>();

    map.put("k1", "v1", Duration.ofSeconds(10));
    assertTrue(map.containsKey("k1"));
    assertEquals("v1", map.get("k1"));
    assertEquals(1, map.size());
    assertFalse(map.isEmpty());

    final var existing = map.putIfAbsent("k1", "v2", Duration.ofSeconds(10));
    assertEquals("v1", existing);
    assertEquals("v1", map.get("k1"));

    final var absent = map.putIfAbsent("k2", "v2", Duration.ofSeconds(10));
    assertNull(absent);
    assertEquals("v2", map.get("k2"));

    final var removed = map.remove("k1");
    assertEquals("v1", removed);
    assertNull(map.get("k1"));
    assertFalse(map.containsKey("k1"));

    map.clear();
    assertEquals(0, map.size());
    assertTrue(map.isEmpty());
    map.shutdown();
  }

  @Test
  @DisplayName("Entry expiration with TTL eviction")
  void testExpiration() throws InterruptedException {
    final var map = new ExpiringMap<String, String>(Duration.ofMillis(20));

    map.put("fast", "value", Duration.ofMillis(50));
    assertEquals("value", map.get("fast"));

    Thread.sleep(120);

    assertNull(map.get("fast"));
    assertFalse(map.containsKey("fast"));
    assertEquals(0, map.size());
    map.shutdown();
  }

  @Test
  @DisplayName("Expiry listener receives callback upon expiration")
  void testExpiryListener() throws InterruptedException {
    final var expiredCount = new AtomicInteger();
    final var map = new ExpiringMap<String, String>(Duration.ofMillis(20), (k, v) -> expiredCount.incrementAndGet());

    map.put("item1", "val1", Duration.ofMillis(50));
    map.put("item2", "val2", Duration.ofMillis(50));

    Thread.sleep(120);
    map.cleanupExpiredNow();

    assertEquals(2, expiredCount.get());
    map.shutdown();
  }

  @Test
  @DisplayName("Renew expiry extends entry lifetime")
  void testRenewExpiry() throws InterruptedException {
    final var map = new ExpiringMap<String, String>();

    map.put("renewMe", "persisted", Duration.ofMillis(100));
    Thread.sleep(50);

    final var renewed = map.renewExpiry("renewMe", Duration.ofMillis(200));
    assertTrue(renewed);

    Thread.sleep(80);
    // At 130ms, original 100ms TTL would have expired, but renewed gives extra 200ms from t=50ms
    assertEquals("persisted", map.get("renewMe"));

    final var falseRenew = map.renewExpiry("nonExistent", Duration.ofMillis(100));
    assertFalse(falseRenew);
    map.shutdown();
  }

  @Test
  @DisplayName("Snapshot, keySet, and values exclude expired entries")
  void testSnapshotViews() throws InterruptedException {
    final var map = new ExpiringMap<String, String>();

    map.put("alive", "yes", Duration.ofSeconds(5));
    map.put("dying", "no", Duration.ofMillis(40));

    Thread.sleep(80);
    map.cleanupExpiredNow();

    final var snapshot = map.snapshot();
    assertEquals(1, snapshot.size());
    assertTrue(snapshot.containsKey("alive"));
    assertFalse(snapshot.containsKey("dying"));

    final var keys = map.keySet();
    assertTrue(keys.contains("alive"));
    assertFalse(keys.contains("dying"));

    final var values = map.values();
    assertTrue(values.contains("yes"));
    assertFalse(values.contains("no"));
    map.shutdown();
  }

  @Test
  @DisplayName("Concurrent multi-threaded access without corruption")
  void testConcurrentAccess() throws InterruptedException {
    final var map = new ExpiringMap<Integer, String>();
    final var threads = 8;
    final var iterations = 500;
    final var executor = Executors.newFixedThreadPool(threads);
    final var latch = new CountDownLatch(threads);

    for (var t = 0; t < threads; t++) {
      final var threadId = t;
      executor.submit(() -> {
        try {
          for (var i = 0; i < iterations; i++) {
            final var key = (threadId * iterations) + i;
            map.put(key, "val-" + key, Duration.ofMillis(200));
            map.get(key);
            if (i % 2 == 0)
              map.renewExpiry(key, Duration.ofMillis(300));
            if (i % 5 == 0)
              map.remove(key);
          }
        } finally {
          latch.countDown();
        }
      });
    }

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    executor.shutdown();
    map.shutdown();
  }
}
