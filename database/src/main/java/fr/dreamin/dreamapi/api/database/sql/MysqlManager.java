package fr.dreamin.dreamapi.api.database.sql;

import fr.dreamin.dreamapi.api.database.sql.core.SqlManager;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * Represents a MySQL database connection pool managed via HikariCP.
 */
@Getter
@Setter
public final class MysqlManager extends SqlManager {

  public MysqlManager(
    final @NotNull String label,
    final @NotNull String host,
    final int port,
    final @NotNull String database,
    final @NotNull String username,
    final @NotNull String password
  ) {
    super(label, host, port, database, username, password);
  }

  /**
   * @deprecated autoReconnect is obsolete and ignored by HikariCP. Use standard constructor.
   */
  @Deprecated(forRemoval = true)
  public MysqlManager(
    final @NotNull String label,
    final @NotNull String host,
    final int port,
    final @NotNull String database,
    final @NotNull String username,
    final @NotNull String password,
    final boolean autoReconnect
  ) {
    super(label, host, port, database, username, password);
    this.autoReconnect = autoReconnect;
  }

  @Override
  protected @NotNull String buildJdbcUrl() {
    return String.format(
      "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC",
      host, port, database
    );
  }

  @Override
  protected void applyProperties(final @NotNull Properties props) {
    super.applyProperties(props);
    props.setProperty("cachePrepStmts", "true");
    props.setProperty("prepStmtCacheSize", "250");
    props.setProperty("prepStmtCacheSqlLimit", "2048");
    props.setProperty("useServerPrepStmts", "true");
    props.setProperty("useLocalSessionState", "true");
    props.setProperty("rewriteBatchedStatements", "true");
    props.setProperty("cacheResultSetMetadata", "true");
    props.setProperty("cacheServerConfiguration", "true");
    props.setProperty("elideSetAutoCommits", "true");
    props.setProperty("maintainTimeStats", "false");
  }
}