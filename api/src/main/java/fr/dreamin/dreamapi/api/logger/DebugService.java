package fr.dreamin.dreamapi.api.logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing debug logging.
 * Provides methods to handle debug writers, categories, and log retention.
 *
 * @author Dreamin
 * @since 1.0.0
 */
public interface DebugService {

  /**
   * Get the list of registered debug writers.
   * @return An unmodifiable list of debug writers.
   */
  List<DebugWriter> getWriters();

  /**
   * Enable or disable global debug logging.
   * @param enabled True to enable, false to disable.
   */
  void setGlobalDebug(final boolean enabled);

  /**
   * Check if global debug logging is enabled.
   * @return True if enabled, false otherwise.
   */
  boolean isGlobalDebug();

  /**
   * Enable or disable a specific debug category.
   * @param category The category name.
   * @param enabled True to enable, false to disable.
   */
  void setCategory(final @NotNull String category, final boolean enabled);

  /**
   * Check if a specific debug category is enabled.
   * @param category The category name.
   * @return True if enabled, false otherwise.
   */
  boolean isCategoryEnabled(final @NotNull String category);

  /**
   * Get a map of all registered debug categories and their enabled status.
   * @return A map of category names to their enabled status.
   */
  Map<String, Boolean> getCategories();

  /**
   * Set the number of days to retain log files.
   * @param days Number of days to retain logs.
   */
  void setRetentionDays(final int days);

  /**
   * Get the number of days log files are retained.
   * @return Number of days logs are retained.
   */
  int getRetentionDays();

  /**
   * Cleanup old log files based on the retention policy.
   */
  void cleanupOldLogs();

  /**
   * Add a debug writer to the service.
   * @param writer The debug writer to add.
   */
  void addWriter(final @NotNull DebugWriter writer);

  void addWriter(final @NotNull DebugWriter writer, final boolean enabled);

  /**
   * Remove a debug writer from the service.
   * @param writer The debug writer to remove.
   */
  void removeWriter(final @NotNull DebugWriter writer) ;

  void setWriterEnabled(final @NotNull Class<? extends DebugWriter> clazz, final boolean enabled);

  boolean isWriterEnabled(Class<? extends DebugWriter> clazz);

  /**
   * Remove a debug writer from the service by its class.
   * @param writerClass The class of the debug writer to remove.
   */
  default void removeWriter(final @NotNull Class<? extends DebugWriter> writerClass) {

  }

  /**
   * Log a debug entry using all registered writers.
   * @param entry The log entry to log.
   */
  default void log(final @NotNull LogEntry entry) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

}
