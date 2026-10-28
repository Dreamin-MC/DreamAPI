package fr.dreamin.dreamapi.core.item.scanner;

import fr.dreamin.dreamapi.api.item.*;
import fr.dreamin.dreamapi.api.item.annotations.DreamItem;
import fr.dreamin.dreamapi.api.item.annotations.OnDreamItemUse;
import fr.dreamin.dreamapi.core.service.ServiceAnnotationProcessor;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@RequiredArgsConstructor
public final class ItemAnnotationProcessor {

  private final Map<Class<?>, Object> instanceCache = new HashMap<>();

  private final @NotNull Plugin plugin;
  private final @NotNull ItemRegistryService registry;
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

    scanDreamItems(this.preScannedClasses, loaded, failed);
    scanItemHandlers(this.preScannedClasses, loaded, failed);

    final var end = System.currentTimeMillis();

    this.log.info(String.format(
      "[DreamItem] Loaded %d items/handlers (%d failed) in %dms",
      loaded.get(),
      failed.get(),
      end - start
    ));

  }

  // ###############################################################
  // ------------------------- DREAM ITEMS -------------------------
  // ###############################################################

  private void scanDreamItems(final @NotNull Set<Class<?>> classes, final @NotNull AtomicInteger loaded, final @NotNull AtomicInteger failed) {
    for (final var clazz : classes) {
      try {
        if (clazz.isAnnotationPresent(DreamItem.class)) {
          registerItemFromClass(clazz);
          loaded.incrementAndGet();
        }

        for (final var method : clazz.getDeclaredMethods()) {
          if (!method.isAnnotationPresent(DreamItem.class)) continue;

          registerItemFromMethod(clazz, method);
          loaded.incrementAndGet();
        }

      } catch (Exception e) {
        failed.incrementAndGet();
        this.log.severe(String.format(
          "[DreamItem] Failed to load item from %s: %s",
          clazz.getName(),
          e.getMessage()
        ));
      }
    }
  }

  private void registerItemFromClass(final @NotNull Class<?> clazz) {
    if (!ItemDefinitionSupplier.class.isAssignableFrom(clazz))
      throw new IllegalStateException(
        String.format("[ItemScanner] Class %s is annotated with @DreamItem but does not implement ItemDefinitionSupplier.", clazz.getName())
      );

    final var instance = getInstance(clazz);
    final var def = ((ItemDefinitionSupplier) instance).get();
    this.registry.register(def);
  }

  private void registerItemFromMethod(final @NotNull Class<?> clazz, final @NotNull Method method) {
    if (!ItemDefinition.class.isAssignableFrom(method.getReturnType()))
      throw new IllegalStateException(
        String.format("[DreamItem] Method %s.%s is annotated with @DreamItem but does not return ItemDefinition.", clazz.getName(), method.getName())
      );

    if (method.getParameterCount() != 0)
      throw new IllegalStateException(
        String.format("[DreamItem] Method %s.%s is annotated with @DreamItem but has parameters.", clazz.getName(), method.getName())
      );

    try {
      final var instance = getInstance(clazz);
      method.setAccessible(true);

      final var def = (ItemDefinition) method.invoke(instance);
      this.registry.register(def);

    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  // ###############################################################
  // ----------------------- ITEM HANDLERS -------------------------
  // ###############################################################

  private void scanItemHandlers(final @NotNull Set<Class<?>> classes, final @NotNull AtomicInteger loaded, final @NotNull AtomicInteger failed) {
    for (final var clazz : classes) {
      for (final var method : clazz.getDeclaredMethods()) {

        final var ann = method.getAnnotation(OnDreamItemUse.class);
        if (ann == null) continue;

        try {
          validateHandlerMethod(method, ann);

          final var instance = getInstance(clazz);
          method.setAccessible(true);

          final ItemHandler handler = ctx -> {
            try {
              return (boolean) method.invoke(instance, ctx);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          };

          registerHandler(ann, handler);
          loaded.incrementAndGet();

        } catch (Exception e) {
          failed.incrementAndGet();
          log.severe(String.format(
            "[DreamItem] Failed to register item handler %s.%s: %s",
            clazz.getName(),
            method.getName(),
            e.getMessage()
          ));
        }
      }
    }
  }


  private void validateHandlerMethod(final @NotNull Method method, final @NotNull OnDreamItemUse ann) {
    if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != ItemContext.class)
      throw new IllegalStateException(
        String.format("[ItemScanner] Method %s.%s is annotated with @OnDreamItemUse but has invalid parameters.", method.getDeclaringClass().getName(), method.getName())
      );

    if (method.getReturnType() != boolean.class)
      throw new IllegalStateException(
        String.format("[ItemScanner] Method %s.%s is annotated with @OnDreamItemUse but does not return boolean.", method.getDeclaringClass().getName(), method.getName())
      );

    if (ann.itemId().isEmpty() && ann.tags().length == 0)
      throw new IllegalStateException(
        String.format("[ItemScanner] Method %s.%s is annotated with @OnDreamItemUse but has no itemId or tags specified.", method.getDeclaringClass().getName(), method.getName())
      );
  }

  private void registerHandler(final @NotNull OnDreamItemUse ann, final @NotNull ItemHandler handler) {
    if (!ann.itemId().isEmpty())
      this.registry.addHandler(ann.itemId(), ann.action(), handler);

    for (final var tag : ann.tags()) {
      this.registry.addHandler(ItemTag.of(tag), ann.action(), handler);
    }

  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################


  private Object getInstance(Class<?> clazz) {
    return instanceCache.computeIfAbsent(
      clazz,
      serviceManager::createInjectedInstance
    );
  }

}
