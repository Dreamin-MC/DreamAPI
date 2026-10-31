package fr.dreamin.dreamapi.api.database.sql.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.dreamin.dreamapi.api.DreamAPI;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;

/**
 * Abstract base class for SQL database connections backed by HikariCP connection pool.
 * Provides thread-safe pooled connection management, safe resource-loaning execution,
 * and high-performance asynchronous querying.
 */
@Getter
public abstract class SqlManager {

  protected final @NotNull String label;
  protected final @NotNull String host;
  protected final int port;
  protected final @NotNull String database;
  protected final @NotNull String username;
  protected final @NotNull String password;

  /**
   * @deprecated Obsolete parameter. Connection health and reconnects are handled natively by HikariCP.
   */
  @Deprecated(forRemoval = true)
  @Setter
  protected boolean autoReconnect = true;

  // HikariCP Pool settings
  @Setter protected int maximumPoolSize = 10;
  @Setter protected int minimumIdle = 2;
  @Setter protected long idleTimeout = 30_000L;
  @Setter protected long connectionTimeout = 10_000L;
  @Setter protected long maxLifetime = 1_800_000L;
  @Setter protected @Nullable String connectionTestQuery = null;
  @Setter protected @NotNull Executor asyncExecutor = ForkJoinPool.commonPool();

  protected HikariDataSource dataSource;
  protected final Logger logger = DreamAPI.isInitialized() ? DreamAPI.getAPI().getLogger() : Logger.getLogger("DreamAPI-SqlManager");

  protected SqlManager(
    final @NotNull String label,
    final @NotNull String host,
    final int port,
    final @NotNull String database,
    final @NotNull String username,
    final @NotNull String password
  ) {
    this.label = label;
    this.host = host;
    this.port = port;
    this.database = database;
    this.username = username;
    this.password = password;
  }

  // ###############################################################
  // -------------------- ABSTRACT & HOOK METHODS ------------------
  // ###############################################################

  /**
   * Must build and return the database-specific JDBC URL.
   * Example: jdbc:mysql://localhost:3306/test?useSSL=false
   */
  protected abstract @NotNull String buildJdbcUrl();

  /**
   * Allows subclasses to add database-specific connection properties.
   */
  protected void applyProperties(final @NotNull Properties props) {
    props.setProperty("user", username);
    props.setProperty("password", password);
    props.setProperty("characterEncoding", "utf8");
    props.setProperty("useUnicode", "true");
  }

  /**
   * Hook for subclasses or custom implementations to alter HikariConfig before pool creation.
   */
  protected void configureHikari(final @NotNull HikariConfig config) {
    // Default no-op
  }

  // ###############################################################
  // ---------------------- CONNECTION LOGIC -----------------------
  // ###############################################################

  public synchronized boolean connect() {
    if (isOnline())
      return true;

    try {
      final var url = buildJdbcUrl();
      final var props = new Properties();
      applyProperties(props);

      final var config = new HikariConfig();
      config.setPoolName("DreamAPI-" + label);
      config.setJdbcUrl(url);
      config.setUsername(username);
      config.setPassword(password);
      config.setMaximumPoolSize(maximumPoolSize);
      config.setMinimumIdle(minimumIdle);
      config.setIdleTimeout(idleTimeout);
      config.setConnectionTimeout(connectionTimeout);
      config.setMaxLifetime(maxLifetime);

      if (connectionTestQuery != null && !connectionTestQuery.isBlank())
        config.setConnectionTestQuery(connectionTestQuery);
      config.setDataSourceProperties(props);

      configureHikari(config);

      this.dataSource = new HikariDataSource(config);
      logger.info(String.format("[%s] HikariCP pool initialized for %s:%d/%s (maxPool=%d, minIdle=%d)",
        label, host, port, database, maximumPoolSize, minimumIdle));
      return true;
    } catch (Exception e) {
      logger.severe(String.format("[%s] Failed to initialize HikariCP pool: %s", label, e.getMessage()));
      return false;
    }
  }

  public synchronized void disconnect() {
    if (dataSource != null) {
      try {
        if (!dataSource.isClosed())
          dataSource.close();
        logger.info(String.format("[%s] HikariCP pool closed", label));
      } catch (Exception e) {
        logger.severe(String.format("[%s] Failed to close HikariCP pool: %s", label, e.getMessage()));
      } finally {
        dataSource = null;
      }
    }
  }

  public boolean isOnline() {
    return dataSource != null && dataSource.isRunning() && !dataSource.isClosed();
  }

  public void reconnect() {
    disconnect();
    connect();
  }

  /**
   * Borrows a connection from the HikariCP pool.
   * <p>
   * <b>Important:</b> The returned {@link Connection} is a pool proxy and must be closed
   * (e.g. via try-with-resources) to return it back to the pool.
   */
  public Connection getConnection() {
    try {
      if (!isOnline() && !connect())
        return null;
      return dataSource.getConnection();
    } catch (Exception e) {
      logger.severe(String.format("[%s] Could not borrow connection from pool: %s", label, e.getMessage()));
      return null;
    }
  }

  public boolean testConnection() {
    try (final var conn = getConnection()) {
      if (conn == null)
        return false;
      try (final var stmt = conn.createStatement()) {
        return stmt.execute("SELECT 1");
      }
    } catch (SQLException e) {
      logger.warning(String.format("[%s] Test connection failed: %s", label, e.getMessage()));
      return false;
    }
  }

  // ###############################################################
  // ----------------- FUNCTIONAL EXECUTION HELPERS ----------------
  // ###############################################################

  @FunctionalInterface
  public interface ResultSetMapper<T> {
    T map(@NotNull ResultSet rs) throws SQLException;
  }

  @FunctionalInterface
  public interface ConnectionConsumer {
    void accept(@NotNull Connection conn) throws SQLException;
  }

  @FunctionalInterface
  public interface ConnectionFunction<T> {
    T apply(@NotNull Connection conn) throws SQLException;
  }

  public void withConnection(final @NotNull ConnectionConsumer consumer) throws SQLException {
    try (final var conn = getConnection()) {
      if (conn == null)
        throw new SQLException("Failed to borrow database connection from pool: " + label);
      consumer.accept(conn);
    }
  }

  public <T> T withConnectionResult(final @NotNull ConnectionFunction<T> function) throws SQLException {
    try (final var conn = getConnection()) {
      if (conn == null)
        throw new SQLException("Failed to borrow database connection from pool: " + label);
      return function.apply(conn);
    }
  }

  public int executeUpdate(final @NotNull String sql, final Object... params) throws SQLException {
    return withConnectionResult(conn -> {
      try (final var stmt = conn.prepareStatement(sql)) {
        setParameters(stmt, params);
        return stmt.executeUpdate();
      }
    });
  }

  public <T> Optional<T> queryFirst(
    final @NotNull String sql,
    final @NotNull ResultSetMapper<T> mapper,
    final Object... params
  ) throws SQLException {
    return withConnectionResult(conn -> {
      try (final var stmt = conn.prepareStatement(sql)) {
        setParameters(stmt, params);
        try (final var rs = stmt.executeQuery()) {
          if (rs.next())
            return Optional.ofNullable(mapper.map(rs));
          return Optional.empty();
        }
      }
    });
  }

  public <T> List<T> queryList(
    final @NotNull String sql,
    final @NotNull ResultSetMapper<T> mapper,
    final Object... params
  ) throws SQLException {
    return withConnectionResult(conn -> {
      try (final var stmt = conn.prepareStatement(sql)) {
        setParameters(stmt, params);
        try (final var rs = stmt.executeQuery()) {
          final var list = new ArrayList<T>();
          while (rs.next())
            list.add(mapper.map(rs));
          return list;
        }
      }
    });
  }

  private void setParameters(final @NotNull PreparedStatement stmt, final Object... params) throws SQLException {
    if (params != null)
      for (var i = 0; i < params.length; i++)
        stmt.setObject(i + 1, params[i]);
  }

  // ###############################################################
  // ------------------- ASYNCHRONOUS API --------------------------
  // ###############################################################

  public CompletableFuture<Void> withConnectionAsync(final @NotNull ConnectionConsumer consumer) {
    return CompletableFuture.runAsync(() -> {
      try {
        withConnection(consumer);
      } catch (SQLException e) {
        throw new RuntimeException("Async SQL operation failed on pool [" + label + "]: " + e.getMessage(), e);
      }
    }, asyncExecutor);
  }

  public <T> CompletableFuture<T> withConnectionResultAsync(final @NotNull ConnectionFunction<T> function) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return withConnectionResult(function);
      } catch (SQLException e) {
        throw new RuntimeException("Async SQL operation failed on pool [" + label + "]: " + e.getMessage(), e);
      }
    }, asyncExecutor);
  }

  public CompletableFuture<Integer> executeUpdateAsync(final @NotNull String sql, final Object... params) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return executeUpdate(sql, params);
      } catch (SQLException e) {
        throw new RuntimeException("Async executeUpdate failed on pool [" + label + "]: " + e.getMessage(), e);
      }
    }, asyncExecutor);
  }

  public <T> CompletableFuture<Optional<T>> queryFirstAsync(
    final @NotNull String sql,
    final @NotNull ResultSetMapper<T> mapper,
    final Object... params
  ) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return queryFirst(sql, mapper, params);
      } catch (SQLException e) {
        throw new RuntimeException("Async queryFirst failed on pool [" + label + "]: " + e.getMessage(), e);
      }
    }, asyncExecutor);
  }

  public <T> CompletableFuture<List<T>> queryListAsync(
    final @NotNull String sql,
    final @NotNull ResultSetMapper<T> mapper,
    final Object... params
  ) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return queryList(sql, mapper, params);
      } catch (SQLException e) {
        throw new RuntimeException("Async queryList failed on pool [" + label + "]: " + e.getMessage(), e);
      }
    }, asyncExecutor);
  }
}
