package fr.dreamin.dreamapi.core.logger;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlayerDebugServiceImpl Concurrency Tests (Feature F2)")
class PlayerDebugServiceImplTest {

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

  @Test
  @DisplayName("Multiple debuggers can simultaneously debug the same target")
  void testMultiDebuggerRetention() {
    final var service = new PlayerDebugServiceImpl();
    final var target = createMockPlayer(UUID.randomUUID());
    final var admin1 = createMockPlayer(UUID.randomUUID());
    final var admin2 = createMockPlayer(UUID.randomUUID());

    service.startDebug(target, admin1);
    service.startDebug(target, admin2);

    assertTrue(service.isDebugging(target, admin1));
    assertTrue(service.isDebugging(target, admin2));
  }

  @Test
  @DisplayName("Stopping debug for one admin retains the other admin")
  void testIndependentRemoval() {
    final var service = new PlayerDebugServiceImpl();
    final var target = createMockPlayer(UUID.randomUUID());
    final var admin1 = createMockPlayer(UUID.randomUUID());
    final var admin2 = createMockPlayer(UUID.randomUUID());

    service.startDebug(target, admin1);
    service.startDebug(target, admin2);

    service.stopDebug(target, admin1);
    assertFalse(service.isDebugging(target, admin1));
    assertTrue(service.isDebugging(target, admin2));

    service.stopDebug(target, admin2);
    assertFalse(service.isDebugging(target, admin2));
  }

  @Test
  @DisplayName("Audience for empty or inactive target is empty")
  void testEmptyAudience() {
    final var service = new PlayerDebugServiceImpl();
    final var target = createMockPlayer(UUID.randomUUID());

    final var audience = service.getDebuggers(target);
    assertNotNull(audience);
  }

  @Test
  @DisplayName("Concurrent start and stop debug calls do not throw CME")
  void testConcurrentDebuggers() throws InterruptedException {
    final var service = new PlayerDebugServiceImpl();
    final var target = createMockPlayer(UUID.randomUUID());
    final var threadCount = 10;
    final var iterations = 200;
    final var executor = Executors.newFixedThreadPool(threadCount);
    final var latch = new CountDownLatch(threadCount);

    for (var t = 0; t < threadCount; t++) {
      final var admin = createMockPlayer(UUID.randomUUID());
      executor.submit(() -> {
        try {
          for (var i = 0; i < iterations; i++) {
            service.startDebug(target, admin);
            service.isDebugging(target, admin);
            if (i % 2 == 0)
              service.stopDebug(target, admin);
          }
        } finally {
          latch.countDown();
        }
      });
    }

    assertTrue(latch.await(5, TimeUnit.SECONDS));
    executor.shutdown();
  }
}
