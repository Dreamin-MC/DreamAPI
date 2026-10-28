package fr.dreamin.dreamapi.core.cmd.scanner;

import cloud.commandframework.annotations.AnnotationParser;
import fr.dreamin.dreamapi.api.cmd.DreamCmd;
import fr.dreamin.dreamapi.core.dependency.DependencyLoader;
import fr.dreamin.dreamapi.core.service.ServiceAnnotationProcessor;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@RequiredArgsConstructor
public final class CmdAnnotationProcessor {

  private final Map<Class<?>, Object> instanceCache = new HashMap<>();

  private final @NotNull Plugin plugin;
  private final AnnotationParser<CommandSender> annotationParser;
  private final @NotNull ServiceAnnotationProcessor serviceManager;
  private final @NotNull Set<Class<?>> preScannedClasses;

  private Logger log;

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public void process() {
    this.log = this.plugin.getLogger();
    final var start = System.currentTimeMillis();

    final var loaded = new AtomicInteger();
    final var failed = new AtomicInteger();

    scanDreamCmdClasses(this.preScannedClasses, loaded, failed);

    final var end = System.currentTimeMillis();

    this.log.info(String.format(
      "[DreamCmd] Loaded %d commands (%d failed) in %dms",
      loaded.get(),
      failed.get(),
      end - start
    ));
  }

  // ###############################################################
  // ------------------------- CMD CLASS ---------------------------
  // ###############################################################

  private void scanDreamCmdClasses(final @NotNull Set<Class<?>> classes, final @NotNull AtomicInteger loaded, final @NotNull AtomicInteger failed) {
    for (final var clazz : classes) {
      try  {
        if (!clazz.isAnnotationPresent(DreamCmd.class)) continue;
        if (!DependencyLoader.canLoad(clazz)) continue;

        registerCmd(clazz);
        loaded.incrementAndGet();
      } catch (Exception e) {
        failed.incrementAndGet();
        this.log.severe(String.format(
          "[DreamCmd] Failed to load command %s: %s",
          clazz.getName(),
          e.getMessage()
        ));
      }

    }
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void registerCmd(final @NotNull Class<?> clazz) {
    final var instance = getInstance(clazz);

    this.annotationParser.parse(instance);
  }

  private Object getInstance(Class<?> clazz) {
    return instanceCache.computeIfAbsent(
      clazz,
      serviceManager::createInjectedInstance
    );
  }

}
