package fr.dreamin.dreamapi.core.event.scanner;

import fr.dreamin.dreamapi.api.event.annotation.DreamEvent;
import fr.dreamin.dreamapi.api.event.annotation.RegisterListener;
import fr.dreamin.dreamapi.core.dependency.DependencyLoader;
import fr.dreamin.dreamapi.core.service.ServiceAnnotationProcessor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@RequiredArgsConstructor
public final class ListenerAnnotationProcessor {

  private final Map<Class<?>, Object> instanceCache = new HashMap<>();

  private final @NotNull Plugin plugin;
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

    scanListeners(this.preScannedClasses, loaded, failed);

    final var end = System.currentTimeMillis();

    this.log.info(String.format(
      "[DreamListener] Loaded %d listeners (%d failed) in %dms",
      loaded.get(),
      failed.get(),
      end - start
    ));
  }

  // ###############################################################
  // ------------------------- LISTENERS ---------------------------
  // ###############################################################

  private void scanListeners(final @NotNull Set<Class<?>> classes, final @NotNull AtomicInteger loaded, final @NotNull AtomicInteger failed) {
    for (final var clazz : classes) {
      try {
        final var isAnnotated = clazz.isAnnotationPresent(DreamEvent.class)
          || clazz.isAnnotationPresent(RegisterListener.class);
        if (!isAnnotated)
          continue;

        if (!Listener.class.isAssignableFrom(clazz)) {
          this.log.warning(String.format(
            "[DreamListener] Class %s is annotated with listener annotation but does not implement org.bukkit.event.Listener.",
            clazz.getName()
          ));
          continue;
        }

        // Version & dependency gating check
        if (!DependencyLoader.canLoad(clazz)) {
          this.log.info(String.format(
            "[DreamListener] Skipping listener %s: version or dependency requirements not met.",
            clazz.getName()
          ));
          continue;
        }

        registerListener(clazz);
        loaded.incrementAndGet();

      } catch (Exception e) {
        failed.incrementAndGet();
        this.log.severe(
          String.format(
            "[DreamListener] Failed to load listener %s: %s",
            clazz.getName(),
            e.getMessage()
          )
        );
      }
    }
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void registerListener(final Class<?> clazz) {
    final var instance = getInstance(clazz);

    if (!(instance instanceof Listener listener))
      throw new IllegalStateException(
        String.format(
          "[DreamListener] Instance of %s is not a Bukkit Listener.",
          clazz.getName()
        )
      );

    // Actual Bukkit event registration
    Bukkit.getPluginManager().registerEvents(listener, this.plugin);
    this.log.info(String.format("[DreamListener] Successfully registered listener: %s", clazz.getSimpleName()));
  }

  private Object getInstance(Class<?> clazz) {
    return instanceCache.computeIfAbsent(
      clazz,
      serviceManager::createInjectedInstance
    );
  }

}
