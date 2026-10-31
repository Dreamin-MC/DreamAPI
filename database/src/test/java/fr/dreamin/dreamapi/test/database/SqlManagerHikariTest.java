package fr.dreamin.dreamapi.test.database;

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

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SqlManager HikariCP Integration Tests (Feature F7)")
class SqlManagerHikariTest {

  private static class H2SqlManager extends SqlManager {
    H2SqlManager(final String dbName) {
      super("test-h2", "localhost", 0, dbName, "sa", "");
      setMaximumPoolSize(5);
      setMinimumIdle(1);
    }

    @Override
    protected @NotNull String buildJdbcUrl() {
      return "jdbc:h2:mem:" + database + ";DB_CLOSE_DELAY=-1";
    }
  }

  private H2SqlManager sqlManager;

  @BeforeEach
  void setUp() throws SQLException {
    sqlManager = new H2SqlManager("testdb_" + UUID.randomUUID().toString().replace("-", ""));
    assertTrue(sqlManager.connect());
    assertTrue(sqlManager.isOnline());

    sqlManager.executeUpdate("""
      CREATE TABLE test_users (
        id INT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(64) NOT NULL,
        score INT NOT NULL
      )
    """);
  }

  @AfterEach
  void tearDown() {
    if (sqlManager != null)
      sqlManager.disconnect();
  }

  @Test
  @DisplayName("Pool lifecycle and testConnection")
  void testPoolLifecycle() {
    assertTrue(sqlManager.isOnline());
    assertTrue(sqlManager.testConnection());

    sqlManager.disconnect();
    assertFalse(sqlManager.isOnline());

    sqlManager.reconnect();
    assertTrue(sqlManager.isOnline());
  }

  @Test
  @DisplayName("Synchronous query and executeUpdate operations")
  void testSyncOperations() throws SQLException {
    final var affected = sqlManager.executeUpdate(
      "INSERT INTO test_users (name, score) VALUES (?, ?)",
      "Alice", 100
    );
    assertEquals(1, affected);

    sqlManager.executeUpdate(
      "INSERT INTO test_users (name, score) VALUES (?, ?)",
      "Bob", 200
    );

    final var first = sqlManager.queryFirst(
      "SELECT name, score FROM test_users WHERE name = ?",
      rs -> rs.getInt("score"),
      "Alice"
    );
    assertTrue(first.isPresent());
    assertEquals(100, first.get());

    final var list = sqlManager.queryList(
      "SELECT name FROM test_users ORDER BY score ASC",
      rs -> rs.getString("name")
    );
    assertEquals(2, list.size());
    assertEquals("Alice", list.get(0));
    assertEquals("Bob", list.get(1));
  }

  @Test
  @DisplayName("Asynchronous query execution pipelines")
  void testAsyncOperations() throws Exception {
    final var insertFuture = sqlManager.executeUpdateAsync(
      "INSERT INTO test_users (name, score) VALUES (?, ?)",
      "Charlie", 300
    );
    assertEquals(1, insertFuture.get(5, TimeUnit.SECONDS));

    final var queryFuture = sqlManager.queryFirstAsync(
      "SELECT score FROM test_users WHERE name = ?",
      rs -> rs.getInt("score"),
      "Charlie"
    );
    final var score = queryFuture.get(5, TimeUnit.SECONDS);
    assertTrue(score.isPresent());
    assertEquals(300, score.get());

    final var listFuture = sqlManager.queryListAsync(
      "SELECT name FROM test_users WHERE score >= ?",
      rs -> rs.getString("name"),
      300
    );
    final var names = listFuture.get(5, TimeUnit.SECONDS);
    assertEquals(1, names.size());
    assertEquals("Charlie", names.get(0));
  }

  @Test
  @DisplayName("Safe connection loaning helpers")
  void testConnectionLoaning() throws SQLException {
    sqlManager.withConnection(conn -> {
      assertNotNull(conn);
      assertFalse(conn.isClosed());
    });

    final var count = sqlManager.withConnectionResult(conn -> {
      try (final var stmt = conn.createStatement();
           final var rs = stmt.executeQuery("SELECT COUNT(*) FROM test_users")) {
        assertTrue(rs.next());
        return rs.getInt(1);
      }
    });
    assertEquals(0, count);
  }

  @Test
  @DisplayName("Concurrent database operations borrow from pool safely")
  void testConcurrentPoolBorrowing() throws InterruptedException {
    final var threads = 6;
    final var iterations = 50;
    final var executor = Executors.newFixedThreadPool(threads);
    final var latch = new CountDownLatch(threads);

    for (var t = 0; t < threads; t++) {
      final var threadId = t;
      executor.submit(() -> {
        try {
          for (var i = 0; i < iterations; i++)
            sqlManager.executeUpdate(
              "INSERT INTO test_users (name, score) VALUES (?, ?)",
              "User_" + threadId + "_" + i, i * 10
            );
        } catch (SQLException e) {
          fail("Concurrent query failed: " + e.getMessage());
        } finally {
          latch.countDown();
        }
      });
    }

    assertTrue(latch.await(10, TimeUnit.SECONDS));
    executor.shutdown();

    final var total = assertDoesNotThrow(() -> sqlManager.queryFirst(
      "SELECT COUNT(*) FROM test_users",
      rs -> rs.getInt(1)
    ));
    assertTrue(total.isPresent());
    assertEquals(threads * iterations, total.get());
  }

  @Test
  @DisplayName("Deprecated autoReconnect compatibility")
  void testDeprecatedAutoReconnect() {
    assertTrue(sqlManager.isAutoReconnect());
    sqlManager.setAutoReconnect(false);
    assertFalse(sqlManager.isAutoReconnect());
    sqlManager.setAutoReconnect(true);
  }
}
