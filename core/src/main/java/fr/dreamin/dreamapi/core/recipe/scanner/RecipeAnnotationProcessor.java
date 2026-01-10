package fr.dreamin.dreamapi.core.recipe.scanner;

import fr.dreamin.dreamapi.api.recipe.service.RecipeRegistryService;
import fr.dreamin.dreamapi.api.recipe.CustomRecipe;
import fr.dreamin.dreamapi.api.recipe.annotation.DreamRecipe;
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
public final class RecipeAnnotationProcessor {

  private final Map<Class<?>, Object> instanceCache = new HashMap<>();

  private final @NotNull Plugin plugin;
  private final @NotNull RecipeRegistryService recipeRegistryService;
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

    scannDreamRecipesClasses(this.preScannedClasses, loaded, failed);
    scannDreamRecipesMethods(this.preScannedClasses, loaded, failed);

    final var end = System.currentTimeMillis();

    this.log.info(String.format(
      "[DreamRecipe] Loaded %d recipes (%d failed) in %dms",
      loaded.get(),
      failed.get(),
      end - start
    ));

  }

  // ###############################################################
  // ------------------------ RECIPE CLASS -------------------------
  // ###############################################################

  private void scannDreamRecipesClasses(final @NotNull Set<Class<?>> classes, final @NotNull AtomicInteger loaded, final @NotNull AtomicInteger failed) {
    for (final var clazz : classes) {
      if (!clazz.isAnnotationPresent(DreamRecipe.class)) continue;

      for (final var method : clazz.getDeclaredMethods()) {

        try {
          registerRecipe(clazz, method);
          loaded.incrementAndGet();
        } catch (Exception e) {
          failed.incrementAndGet();
          this.log.severe(String.format(
            "[DreamRecipe] Failed to load recipe %s#%s: %s",
            clazz.getName(),
            method.getName(),
            e.getMessage()
          ));
        }

      }

    }

  }

  // ###############################################################
  // ----------------------- RECIPE METHOD -------------------------
  // ###############################################################

  private void scannDreamRecipesMethods(final @NotNull Set<Class<?>> classes, final @NotNull AtomicInteger loaded, final @NotNull AtomicInteger failed) {
    for (final var clazz : classes) {
      for (final var method : clazz.getDeclaredMethods()) {
        if (!method.isAnnotationPresent(DreamRecipe.class)) continue;

        try {
          registerRecipe(clazz, method);
          loaded.incrementAndGet();
        } catch (Exception e) {
          failed.incrementAndGet();
          this.log.severe(String.format(
            "[DreamRecipe] Failed to load recipe %s#%s: %s",
            clazz.getName(),
            method.getName(),
            e.getMessage()
          ));
        }

      }
    }
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void registerRecipe(final @NotNull Class<?> clazz, final @NotNull Method method) throws InvocationTargetException, IllegalAccessException {

    if (!CustomRecipe.class.isAssignableFrom(method.getReturnType())) {
      throw new IllegalStateException(
        String.format(
          "Method %s.%s is annotated with @DreamRecipe but does not return CustomRecipe",
          clazz.getName(),
          method.getName()
        )
      );
    }

    final var instance = getInstance(clazz);
    method.setAccessible(true);

    final var recipe = (CustomRecipe) method.invoke(instance);
    this.recipeRegistryService.registerRecipe(recipe);
  }

  private Object getInstance(Class<?> clazz) {
    return instanceCache.computeIfAbsent(
      clazz,
      serviceManager::createInjectedInstance
    );
  }

}
