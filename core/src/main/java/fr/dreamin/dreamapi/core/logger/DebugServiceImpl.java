package fr.dreamin.dreamapi.core.logger;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.logger.*;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.logger.writer.BroadcastDebugWriter;
import fr.dreamin.dreamapi.core.logger.writer.ConsoleDebugWriter;
import fr.dreamin.dreamapi.core.logger.writer.DailyFileDebugWriter;
import fr.dreamin.dreamapi.core.logger.writer.PlayerDebugWriter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Implementation of the DebugService interface for managing debug logging.
 *
 * @author Dreamin
 * @since 1.0.0
 */
@DreamAutoService(value = DebugService.class, dependencies = {PlayerDebugServiceImpl.class})
public final class DebugServiceImpl implements DreamService, DebugService {

  private final @NotNull Plugin plugin;

  private final Map<String, Boolean> categories = new ConcurrentHashMap<>();
  private final CopyOnWriteArrayList<DebugWriter> writers = new CopyOnWriteArrayList<>();

  private final Map<Class<? extends DebugWriter>, Boolean> writerStates = new ConcurrentHashMap<>();

  private final BlockingDeque<LogEntry> queue = new LinkedBlockingDeque<>();
  private final Thread workerThread;

  private final File debugFolder;

  private volatile boolean globalDebug = false;
  private volatile int retentionDays = 0;

  public DebugServiceImpl(final @NotNull Plugin plugin) {
    this.plugin = plugin;

    this.debugFolder = new File(this.plugin.getDataFolder(), "debug");

    addWriter(new BroadcastDebugWriter(this.plugin), false);
    addWriter(new ConsoleDebugWriter(this.plugin), false);
    addWriter(new DailyFileDebugWriter(this.debugFolder), false);
    addWriter(new PlayerDebugWriter(DreamAPI.getAPI().getService(PlayerDebugService.class)), false);

    this.workerThread = new Thread(this::processQueue, "DreamAPI-Debug-Logger");
    this.workerThread.setDaemon(true);
    this.workerThread.start();
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public List<DebugWriter> getWriters() {
    return Collections.unmodifiableList(this.writers);
  }

  @Override
  public void setGlobalDebug(boolean enabled) {
    this.globalDebug = enabled;
  }

  @Override
  public boolean isGlobalDebug() {
    return this.globalDebug;
  }

  @Override
  public void setCategory(@NotNull String category, boolean enabled) {
    this.categories.put(category, enabled);
  }

  @Override
  public boolean isCategoryEnabled(@NotNull String category) {
    return this.categories.getOrDefault(category, true);
  }

  @Override
  public Map<String, Boolean> getCategories() {
    return Collections.unmodifiableMap(this.categories);
  }

  @Override
  public void setRetentionDays(int days) {
    this.retentionDays = days;
  }

  @Override
  public int getRetentionDays() {
    return this.retentionDays;
  }

  @Override
  public void cleanupOldLogs() {
    final var days = this.retentionDays;
    if (days <= 0) return;

    final var limit = LocalDate.now().minusDays(days);

    if (!this.debugFolder.exists()) return;
    final var files = this.debugFolder.listFiles();
    if (files == null) return;

    for (final var file : files) {
      if (!file.isFile() || !file.getName().endsWith(".log")) continue;

      try {
        final var name = file.getName().replace(".log", "");
        final var date = LocalDate.parse(name);
        if (date.isBefore(limit))
          file.delete();
      } catch (Exception ignored) {}
    }
  }

  @Override
  public void addWriter(@NotNull DebugWriter writer) {
    this.writers.addIfAbsent(writer);
    this.writerStates.put(writer.getClass(), true);
  }

  @Override
  public void addWriter(@NotNull DebugWriter writer, boolean enabled) {
    this.writers.addIfAbsent(writer);
    this.writerStates.put(writer.getClass(), enabled);
    writer.setEnabled(enabled);
  }

  @Override
  public void removeWriter(@NotNull DebugWriter writer) {
    this.writers.remove(writer);
  }

  @Override
  public void setWriterEnabled(@NotNull Class<? extends DebugWriter> clazz, boolean enabled) {
    this.writerStates.put(clazz, enabled);

    for (final var writer : this.writers) {
      if (writer.getClass().equals(clazz))
        writer.setEnabled(enabled);
    }
  }

  @Override
  public boolean isWriterEnabled(Class<? extends DebugWriter> clazz) {
    return this.writerStates.getOrDefault(clazz, true);
  }

  @Override
  public void removeWriter(@NotNull Class<? extends DebugWriter> writerClass) {
    this.writers.removeIf(writer -> writer.getClass().equals(writerClass));
  }

  @Override
  public void log(@NotNull LogEntry entry) {
    if (!(this.globalDebug || isCategoryEnabled(entry.category()))) return;

    this.queue.offer(entry);
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Shuts down the debug service, stopping the worker thread and closing writers.
   * This method should be called during plugin shutdown to ensure proper resource cleanup.
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public void shutdown() {
    this.workerThread.interrupt();

    for (final var writer : this.writers) {
      if (writer instanceof DailyFileDebugWriter dfw)
        dfw.close();
    }
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Processes the log entry queue, writing entries to all registered writers.
   * This method runs in a dedicated worker thread.
   *
   * @author Dreamin
   * @since 1.0.0
   */
  private void processQueue() {
    while (true) {
      try {
        final var entry = this.queue.take();

        for (final var writer : this.writers) {

          if (!writer.isEnabled()) continue;

          try {
            writer.write(entry);
          } catch (Exception ex) {
            this.plugin.getLogger().warning(
              String.format("[DreamAPI][DebugService] Failed to write log: %s", ex.getMessage())
            );
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

}
