package fr.dreamin.dreamapi.api.logger;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

/**
 * Log entry record representing a single log event.
 *
 * @param timestamp
 * @param level
 * @param pluginName
 * @param category
 * @param threadName
 * @param message
 *
 * @author Dreamin
 * @since 1.0.0
 */
public record LogEntry(
  @NotNull LocalDateTime timestamp,
  @NotNull LogLevel level,
  @NotNull String pluginName,
  @NotNull String category,
  @NotNull String threadName,
  @NotNull String message,
  @NotNull DebugMeta meta,
  @NotNull DebugContext context
) { }
