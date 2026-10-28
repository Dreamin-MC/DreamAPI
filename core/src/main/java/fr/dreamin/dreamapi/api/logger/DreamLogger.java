package fr.dreamin.dreamapi.api.logger;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * DreamLogger interface for logging messages with various log levels.
 *
 * @author Dreamin
 * @since 1.0.0
 */
public interface DreamLogger {

  // ###############################################################
  // -----------------------  ------------------------
  // ###############################################################

  default void debug(@NotNull DebugMeta meta, @NotNull String msg, Object... args) {
    debug(msg, args);
  }

  default void info(@NotNull DebugMeta meta, @NotNull String msg, Object... args) {
    info(msg, args);
  }

  default void warn(@NotNull DebugMeta meta, @NotNull String msg, Object... args) {
    warn(msg, args);
  }

  default void error(@NotNull DebugMeta meta, @NotNull String msg, Object... args) {
    error(msg, args);
  }

  default void trace(@NotNull DebugMeta meta, @NotNull String msg, Object... args) {
    trace(msg, args);
  }

  // ###############################################################
  // -----------------------  ------------------------
  // ###############################################################

  /**
   * Log a debug level message.
   *
   * @param msg the message template
   * @param args the arguments for the message template
   *
   * @author Dreamin
   * @since 1.0.0
   */
  void debug(@NotNull String msg, Object... args);

  /**
   * Log an info level message.
   *
   * @param msg the message template
   * @param args the arguments for the message template
   *
   * @author Dreamin
   * @since 1.0.0
   */
  void info(final @NotNull String msg, Object... args);

  /**
   * Log a warning level message.
   * @param msg the message template
   * @param args the arguments for the message template
   *
   * @author Dreamin
   * @since 1.0.0
   */
  void warn(final @NotNull String msg, Object... args);

  /**
   * Log an error level message.
   *
   * @param msg the message template
   * @param args the arguments for the message template
   *
   * @author Dreamin
   * @since 1.0.0
   */
  void error(final @NotNull String msg, Object... args);

  /**
   * Log a trace level message.
   * By default, this method delegates to the debug method.
   *
   * @param msg the message template
   * @param args the arguments for the message template
   *
   * @author Dreamin
   * @since 1.0.0
   */
  default void trace(final @NotNull String msg, Object... args) {
    debug(msg, args);
  }

  default void debugFor(final @NotNull Player player, final @NotNull String msg, Object... args) {
    debug(DebugMeta.of(player), msg, args);
  }

  default void infoFor(final @NotNull Player player, final @NotNull String msg, Object... args) {
    info(DebugMeta.of(player), msg, args);
  }

  default void warnFor(final @NotNull Player player, final @NotNull String msg, Object... args) {
    warn(DebugMeta.of(player), msg, args);
  }

  default void errorFor(final @NotNull Player player, final @NotNull String msg, Object... args) {
    error(DebugMeta.of(player), msg, args);
  }

  /**
   * Check if debug logging is enabled for this logger.
   *
   * @return true if debug is enabled, false otherwise
   */
  boolean isDebugEnabled();

}
