package fr.dreamin.dreamapi.test.challenge;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dreamin.dreamapi.api.map.ExpiringMap;
import fr.dreamin.dreamapi.api.util.math.Interpolation;
import fr.dreamin.dreamapi.api.util.math.InterpolationType;
import fr.dreamin.dreamapi.core.bukkit.module.BukkitItemStackModule;
import fr.dreamin.dreamapi.core.logger.PlayerDebugServiceImpl;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Core Milestone 1 Adversarial Challenge & Stress Tests")
public class CoreMilestone1ChallengeTest {

  private Player createMockPlayer(final UUID uuid) {
    return (Player) Proxy.newProxyInstance(
      Player.class.getClassLoader(),
      new Class<?>[]{Player.class},
      (proxy, method, args) -> {
        if ("getUniqueId".equals(method.getName()))
          return uuid;
        if ("equals".equals(method.getName()) && args != null && args.length == 1)
          return proxy == args[0];
        if ("hashCode".equals(method.getName()))
          return uuid.hashCode();
        return null;
      }
    );
  }

  // =========================================================================
  // 1. ExpiringMap Empirical Stress & Concurrency Challenges
  // =========================================================================

  @Test
  @DisplayName("CHALLENGE 1.1: ExpiringMap.putIfAbsent atomicity under concurrent execution")
  void challengeExpiringMapPutIfAbsentAtomicity() throws InterruptedException {
    final var map = new ExpiringMap<String, String>();
    final var key = "atomicity-test-key";
    final var threadCount = 10;
    final var executor = Executors.newFixedThreadPool(threadCount);
    final var startBarrier = new CyclicBarrier(threadCount);
    final var results = new CopyOnWriteArrayList<String>();
    final var nullReturns = new AtomicInteger();

    for (var i = 0; i < threadCount; i++) {
      final var val = "val-" + i;
      executor.submit(() -> {
        try {
          startBarrier.await();
          final var res = map.putIfAbsent(key, val, Duration.ofSeconds(10));
          if (res == null)
            nullReturns.incrementAndGet();
          else
            results.add(res);
        } catch (Exception ignored) {
        }
      });
    }

    executor.shutdown();
    assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

    final var nullCount = nullReturns.get();
    System.out.printf("[EMPIRICAL] ExpiringMap.putIfAbsent null returns across %d concurrent threads: %d%n", threadCount, nullCount);

    map.shutdown();
  }

  @Test
  @DisplayName("CHALLENGE 1.2: ExpiringMap high-throughput concurrent stress test")
  void challengeExpiringMapConcurrentStress() throws InterruptedException {
    final var map = new ExpiringMap<Integer, String>(Duration.ofMillis(50));
    final var threadCount = 16;
    final var opsPerThread = 1_000;
    final var executor = Executors.newFixedThreadPool(threadCount);
    final var errorCount = new AtomicInteger();

    for (var t = 0; t < threadCount; t++) {
      final var threadId = t;
      executor.submit(() -> {
        try {
          for (var i = 0; i < opsPerThread; i++) {
            final var key = (threadId * opsPerThread) + (i % 50);
            map.put(key, "data-" + key, Duration.ofMillis(100));
            final var v = map.get(key);
            if (v != null && !v.startsWith("data-"))
              errorCount.incrementAndGet();
            if (i % 3 == 0)
              map.renewExpiry(key, Duration.ofMillis(200));
            if (i % 7 == 0)
              map.remove(key);
          }
        } catch (Throwable t1) {
          errorCount.incrementAndGet();
        }
      });
    }

    executor.shutdown();
    assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
    assertEquals(0, errorCount.get(), "No errors or data corruption during concurrent operations");
    map.shutdown();
  }

  @Test
  @DisplayName("CHALLENGE 1.3: ExpiringMap sub-second expiry without explicit maintenance")
  void challengeExpiringMapSubSecondPassiveExpiry() throws InterruptedException {
    final var map = new ExpiringMap<String, String>();
    final var expiredFired = new AtomicBoolean(false);
    map.setExpiryListener((k, v) -> expiredFired.set(true));

    map.put("quickKey", "quickVal", Duration.ofMillis(60));

    // Sleep 150ms: TTL is 60ms.
    Thread.sleep(150);

    // Reading the key should trigger passive expiry eviction
    final var val = map.get("quickKey");
    assertNull(val, "Key must be expired and return null on get()");
    assertTrue(expiredFired.get(), "Expiry listener should have fired upon get()");
    map.shutdown();
  }

  // =========================================================================
  // 2. PlayerDebugServiceImpl Concurrency Race Conditions
  // =========================================================================

  @Test
  @DisplayName("CHALLENGE 2.1: PlayerDebugServiceImpl startDebug and stopDebug race condition")
  void challengePlayerDebugServiceStartStopRace() throws InterruptedException {
    final var service = new PlayerDebugServiceImpl();
    final var target = createMockPlayer(UUID.randomUUID());

    final var lostExecutors = new AtomicInteger();
    final var iterations = 500;
    final var executor = Executors.newFixedThreadPool(4);

    for (var iter = 0; iter < iterations; iter++) {
      final var e1 = createMockPlayer(UUID.randomUUID());
      final var e2 = createMockPlayer(UUID.randomUUID());

      // Start with E1 present
      service.startDebug(target, e1);

      final var barrier = new CyclicBarrier(2);

      // Thread A: stopDebug(target, E1)
      final var f1 = executor.submit(() -> {
        try {
          barrier.await();
          service.stopDebug(target, e1);
        } catch (Exception ignored) {
        }
      });

      // Thread B: startDebug(target, E2)
      final var f2 = executor.submit(() -> {
        try {
          barrier.await();
          service.startDebug(target, e2);
        } catch (Exception ignored) {
        }
      });

      try {
        f1.get(2, TimeUnit.SECONDS);
        f2.get(2, TimeUnit.SECONDS);
      } catch (Exception e) {
        fail("Execution timed out in iteration " + iter);
      }

      // In every iteration, E2 was started. Therefore, isDebugging(target, e2) MUST be true!
      if (!service.isDebugging(target, e2))
        lostExecutors.incrementAndGet();

      // Clean up for next iteration
      service.stopDebug(target, e2);
    }

    executor.shutdown();
    assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

    System.out.printf("[EMPIRICAL] PlayerDebugService race condition: lost %d executors out of %d iterations%n",
      lostExecutors.get(), iterations);
  }

  @Test
  @DisplayName("CHALLENGE 2.2: PlayerDebugServiceImpl high concurrency multi-player stress")
  void challengePlayerDebugServiceMultiPlayerStress() throws InterruptedException {
    final var service = new PlayerDebugServiceImpl();
    final var threadCount = 12;
    final var playersCount = 20;
    final var players = new ArrayList<Player>();

    for (var i = 0; i < playersCount; i++)
      players.add(createMockPlayer(UUID.randomUUID()));

    final var executor = Executors.newFixedThreadPool(threadCount);
    final var errorCount = new AtomicInteger();

    for (var t = 0; t < threadCount; t++) {
      final var threadIdx = t;
      executor.submit(() -> {
        try {
          for (var i = 0; i < 200; i++) {
            final var target = players.get((threadIdx + i) % playersCount);
            final var exec = players.get((threadIdx + i + 1) % playersCount);
            service.startDebug(target, exec);
            service.isDebugging(target, exec);
            if (i % 2 == 0)
              service.stopDebug(target, exec);
          }
        } catch (Throwable t2) {
          errorCount.incrementAndGet();
        }
      });
    }

    executor.shutdown();
    assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
    assertEquals(0, errorCount.get(), "No exceptions during concurrent multi-player debug operations");
  }

  // =========================================================================
  // 3. BukkitItemStackModule Deserialization Edge Cases
  // =========================================================================

  @Test
  @DisplayName("CHALLENGE 3.1: BukkitItemStackModule corrupted Base64 and invalid tokens")
  void challengeBukkitItemStackModuleCorruptedInput() {
    final var mapper = new ObjectMapper().registerModule(new BukkitItemStackModule());

    // Corrupted Base64 string that cannot be decoded
    assertThrows(JsonParseException.class, () -> mapper.readValue("\"!!!NotBase64!!!\"", ItemStack.class));

    // Numeric token
    assertThrows(Exception.class, () -> mapper.readValue("12345", ItemStack.class));

    // Boolean token
    assertThrows(Exception.class, () -> mapper.readValue("true", ItemStack.class));

    // Array token
    assertThrows(Exception.class, () -> mapper.readValue("[1, 2, 3]", ItemStack.class));
  }

  // =========================================================================
  // 4. Interpolation Boundary & Mathematical Edge Cases
  // =========================================================================

  @Test
  @DisplayName("CHALLENGE 4.1: Interpolation boundary values and curve invariants")
  void challengeInterpolationCurves() {
    for (final var type : InterpolationType.values()) {
      // Progress 0.0 must always yield start
      final var atZero = Interpolation.interpolate(10.0, 50.0, 0.0, type);
      assertEquals(10.0, atZero, 0.0001, "Progress 0.0 must equal start for " + type);

      // Progress 1.0 must always yield end
      final var atOne = Interpolation.interpolate(10.0, 50.0, 1.0, type);
      assertEquals(50.0, atOne, 0.0001, "Progress 1.0 must equal end for " + type);

      // Progress 0.5 must be strictly between start and end
      final var atHalf = Interpolation.interpolate(0.0, 100.0, 0.5, type);
      assertTrue(atHalf > 0.0 && atHalf < 100.0, "Progress 0.5 must be between 0 and 100 for " + type);
    }
  }

  @Test
  @DisplayName("CHALLENGE 4.2: Interpolation generate edge case steps = 0 and negative steps")
  void challengeInterpolationGenerateZeroSteps() {
    // When steps is 0, (double) i / steps produces 0.0 / 0.0 = NaN!
    final var listZero = Interpolation.generate(0.0, 10.0, 0, InterpolationType.LINEAR);
    System.out.println("[EMPIRICAL] Interpolation.generate with steps=0 produced: " + listZero);

    // Verify negative steps handling
    assertThrows(IllegalArgumentException.class, () -> Interpolation.generate(0.0, 10.0, -2, InterpolationType.LINEAR));
  }

  @Test
  @DisplayName("CHALLENGE 4.3: Interpolation fluent builder execution")
  void challengeInterpolationBuilder() {
    final var doubles = Interpolation.between(0.0, 100.0)
      .over(10)
      .ease(InterpolationType.EASE_IN_OUT)
      .asDoubles();

    assertEquals(11, doubles.size());
    assertEquals(0.0, doubles.get(0), 0.0001);
    assertEquals(100.0, doubles.get(doubles.size() - 1), 0.0001);

    // Verify monotonicity for EASE_IN_OUT
    for (var i = 1; i < doubles.size(); i++)
      assertTrue(doubles.get(i) >= doubles.get(i - 1), "Sequence must be monotonically non-decreasing");
  }
}
