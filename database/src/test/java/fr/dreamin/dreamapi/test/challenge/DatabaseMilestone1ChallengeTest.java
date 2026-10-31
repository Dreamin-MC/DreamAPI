package fr.dreamin.dreamapi.test.challenge;

import fr.dreamin.dreamapi.api.database.sql.core.SqlManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Database Milestone 1 Adversarial Challenge & Stress Tests")
public class DatabaseMilestone1ChallengeTest {

  private static class H2ChallengeSqlManager extends SqlManager {
    H2ChallengeSqlManager(final String dbName, final int maxPool) {
      super("challenge-h2", "localhost", 0, dbName, "sa", "");
      setMaximumPoolSize(maxPool);
      setMinimumIdle(1);
      setConnectionTimeout(1000L);
    }

    @Override
    protected @NotNull String buildJdbcUrl() {
      return "jdbc:h2:mem:" + database + ";DB_CLOSE_DELAY=-1";
    }
  }

  private H2ChallengeSqlManager sqlManager;

  @BeforeEach
  void setUp() throws SQLException {
    sqlManager = new H2ChallengeSqlManager("challengedb_" + UUID.randomUUID().toString().replace("-", ""), 3);
    assertTrue(sqlManager.connect());
    assertTrue(sqlManager.isOnline());

    sqlManager.executeUpdate("""
      CREATE TABLE stress_records (
        id INT PRIMARY KEY AUTO_INCREMENT,
        val VARCHAR(128) NOT NULL
      )
    """);
  }

  @AfterEach
  void tearDown() {
    if (sqlManager != null)
      sqlManager.disconnect();
  }

  @Test
  @DisplayName("CHALLENGE 7.1: Pool contention under high load with small pool size (3 connections vs 24 threads)")
  void challengePoolContention() throws InterruptedException {
    final var threads = 24;
    final var queriesPerThread = 20;
    final var executor = Executors.newFixedThreadPool(threads);
    final var successCount = new AtomicInteger();
    final var failureCount = new AtomicInteger();
    final var latch = new CountDownLatch(threads);

    for (var t = 0; t < threads; t++) {
      final var threadId = t;
      executor.submit(() -> {
        try {
          for (var i = 0; i < queriesPerThread; i++) {
            sqlManager.executeUpdate("INSERT INTO stress_records (val) VALUES (?)", "thread-" + threadId + "-" + i);
            successCount.incrementAndGet();
          }
        } catch (SQLException e) {
          failureCount.incrementAndGet();
        } finally {
          latch.countDown();
        }
      });
    }

    assertTrue(latch.await(15, TimeUnit.SECONDS));
    executor.shutdown();

    assertEquals(0, failureCount.get(), "Zero pool acquisition failures under queued load");
    assertEquals(threads * queriesPerThread, successCount.get());
  }

  @Test
  @DisplayName("CHALLENGE 7.2: Connection leak verification after exceptions inside withConnection")
  void challengeConnectionLeakOnException() throws Exception {
    // Deliberately trigger multiple SQL errors
    for (var i = 0; i < 20; i++) {
      assertThrows(SQLException.class, () -> sqlManager.withConnection(conn -> {
        try (final var stmt = conn.createStatement()) {
          stmt.execute("SELECT * FROM non_existent_table_xyz");
        }
      }));
    }

    // Verify all 3 pool connections are still healthy and usable
    for (var i = 0; i < 3; i++) {
      final var conn = sqlManager.getConnection();
      assertNotNull(conn, "Connection must not be leaked after SQLException");
      assertFalse(conn.isClosed());
      try {
        conn.close();
      } catch (SQLException ignored) {
      }
    }
  }

  @Test
  @DisplayName("CHALLENGE 7.3: Pool exhaustion timeout when all connections are held")
  void challengePoolExhaustionTimeout() throws Exception {
    sqlManager.setConnectionTimeout(300L); // Fast 300ms timeout

    // Borrow all 3 connections
    final var c1 = sqlManager.getConnection();
    final var c2 = sqlManager.getConnection();
    final var c3 = sqlManager.getConnection();

    assertNotNull(c1);
    assertNotNull(c2);
    assertNotNull(c3);

    // 4th request must time out and return null
    final var start = System.currentTimeMillis();
    final var c4 = sqlManager.getConnection();
    final var elapsed = System.currentTimeMillis() - start;

    assertNull(c4, "Exhausted pool must return null after timeout");
    assertTrue(elapsed >= 250, "Must have waited at least near the timeout period");

    c1.close();
    c2.close();
    c3.close();

    // After closing, pool should immediately be usable again
    final var cReacquired = sqlManager.getConnection();
    assertNotNull(cReacquired, "Should re-acquire connection after pool releases");
    cReacquired.close();
  }
}
