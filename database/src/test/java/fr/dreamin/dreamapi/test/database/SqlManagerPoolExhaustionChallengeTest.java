package fr.dreamin.dreamapi.test.database;

import com.zaxxer.hikari.HikariConfig;
import fr.dreamin.dreamapi.api.database.sql.core.SqlManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SqlManager Pool Exhaustion & Legacy Compatibility Challenge Tests (Feature F7)")
class SqlManagerPoolExhaustionChallengeTest {

  private static class ExhaustibleH2Manager extends SqlManager {
    final AtomicBoolean configureHookCalled = new AtomicBoolean(false);

    ExhaustibleH2Manager(final String dbName) {
      super("exhaust-pool", "localhost", 0, dbName, "sa", "");
      setMaximumPoolSize(2);
      setMinimumIdle(1);
      setConnectionTimeout(400); // 400ms timeout for rapid testing
    }

    @Override
    protected @NotNull String buildJdbcUrl() {
      return "jdbc:h2:mem:" + database + ";DB_CLOSE_DELAY=-1";
    }

    @Override
    protected void configureHikari(final @NotNull HikariConfig config) {
      super.configureHikari(config);
      configureHookCalled.set(true);
    }
  }

  private ExhaustibleH2Manager sqlManager;

  @BeforeEach
  void setUp() {
    sqlManager = new ExhaustibleH2Manager("exdb_" + UUID.randomUUID().toString().replace("-", ""));
    assertTrue(sqlManager.connect());
    assertTrue(sqlManager.isOnline());
    assertTrue(sqlManager.configureHookCalled.get());
  }

  @AfterEach
  void tearDown() {
    if (sqlManager != null)
      sqlManager.disconnect();
  }

  @Test
  @DisplayName("Empirically verify Hikari pool exhaustion, null fallback, and recovery after release")
  void testPoolExhaustionAndRecovery() throws SQLException {
    final var conn1 = sqlManager.getConnection();
    final var conn2 = sqlManager.getConnection();

    assertNotNull(conn1);
    assertNotNull(conn2);
    assertFalse(conn1.isClosed());
    assertFalse(conn2.isClosed());

    // Both connections in the pool of max 2 are checked out.
    // 3rd attempt must timeout after ~400ms and return null.
    final var start = System.currentTimeMillis();
    final var conn3 = sqlManager.getConnection();
    final var elapsed = System.currentTimeMillis() - start;

    assertNull(conn3, "Exhausted pool must return null when connection cannot be borrowed");
    assertTrue(elapsed >= 350, "Should have waited for connection timeout (expected >= 350ms, took " + elapsed + "ms)");

    // withConnection must throw SQLException when pool is exhausted
    final var ex = assertThrows(SQLException.class, () -> sqlManager.withConnection(c -> {
      fail("Consumer should not be invoked when pool is exhausted");
    }));
    assertTrue(ex.getMessage().contains("Failed to borrow database connection from pool"));

    // withConnectionResult must throw SQLException
    assertThrows(SQLException.class, () -> sqlManager.withConnectionResult(c -> 42));

    // Release conn1 back to the pool
    conn1.close();
    assertTrue(conn1.isClosed());

    // Immediately after releasing conn1, borrowing must succeed without blocking
    final var recoveredConn = sqlManager.getConnection();
    assertNotNull(recoveredConn, "Connection borrow must succeed after returning connection to pool");
    assertFalse(recoveredConn.isClosed());

    // Close recoveredConn so pool has 1 free connection available for withConnection
    recoveredConn.close();

    // withConnection must now succeed
    final var verified = new AtomicBoolean(false);
    sqlManager.withConnection(c -> verified.set(true));
    assertTrue(verified.get());

    conn2.close();
  }

  @Test
  @DisplayName("Empirically verify async operations under pool exhaustion fail exceptionally")
  void testAsyncPoolExhaustion() throws SQLException, InterruptedException, TimeoutException {
    final var conn1 = sqlManager.getConnection();
    final var conn2 = sqlManager.getConnection();
    assertNotNull(conn1);
    assertNotNull(conn2);

    final var asyncFuture = sqlManager.withConnectionAsync(c -> {
      fail("Async operation must not run when pool is exhausted");
    });

    final var ex = assertThrows(ExecutionException.class, () -> asyncFuture.get(2, TimeUnit.SECONDS));
    assertTrue(ex.getCause() instanceof RuntimeException);
    assertTrue(ex.getCause().getMessage().contains("Failed to borrow database connection from pool"));

    conn1.close();
    conn2.close();
  }

  @Test
  @DisplayName("Empirically verify legacy lifecycle methods: reconnect, testConnection, lazy auto-connect")
  void testLegacyLifecycle() {
    assertTrue(sqlManager.isOnline());
    assertTrue(sqlManager.testConnection());

    sqlManager.disconnect();
    assertFalse(sqlManager.isOnline());

    // Calling getConnection() or testConnection() when offline triggers lazy auto-reconnect
    assertTrue(sqlManager.testConnection());
    assertTrue(sqlManager.isOnline());

    sqlManager.reconnect();
    assertTrue(sqlManager.isOnline());
    assertTrue(sqlManager.testConnection());
  }
}
