package fr.dreamin.dreamapi.core;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.core.bukkit.module.*;
import fr.dreamin.dreamapi.core.time.module.TickTaskModule;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;

/**
 * <p>
 * Default implementation of the {@link DreamAPI.IApiProvider} interface.
 * </p>
 *
 * <p>
 * This class acts as the central runtime container of DreamAPI.
 * It provides access to all core managers and initializes the API provider
 * at startup (via {@link DreamAPI#setProvider(DreamAPI.IApiProvider)}).
 * </p>
 *
 * @author Dreamin
 * @since 1.0.0
 */
public record ApiProviderImpl(Plugin plugin) implements DreamAPI.IApiProvider {

  public static final List<String> OPTIONAL_JACKSON_MODULES = List.of(
    "fr.dreamin.dreamapi.core.cuboid.module.CuboidModule",
    "fr.dreamin.dreamapi.core.cuboid.module.MemoryCuboidModule",
    "fr.dreamin.dreamapi.core.cuboid.module.MovableCuboidModule",
    "fr.dreamin.dreamapi.core.particle.module.ParticleAnimationModule",
    "fr.dreamin.dreamapi.core.particle.module.ParticleChainMorphModule",
    "fr.dreamin.dreamapi.core.particle.module.ParticleOptionsModule",
    "fr.dreamin.dreamapi.core.particle.module.ParticlePathModule",
    "fr.dreamin.dreamapi.core.particle.module.ParticleSequenceItemModule",
    "fr.dreamin.dreamapi.core.particle.module.ParticleSequenceModule",
    "fr.dreamin.dreamapi.core.particle.module.ParticleShapeModule",
    "fr.dreamin.dreamapi.core.particle.module.ParticleShapeMorphModule",
    "fr.dreamin.dreamapi.core.recipe.module.CustomRecipeModule",
    "fr.dreamin.dreamapi.core.hologram.jackson.HologramJacksonModule"
  );

  public static final List<String> OPTIONAL_LISTENERS = List.of(
    "fr.dreamin.dreamapi.core.item.listener.PlayerItemListener"
  );

  /**
   * Constructs and registers a new API provider instance.
   * <p>
   * This constructor is responsible for creating all internal manager
   * implementations and registering this instance into the global
   * {@link DreamAPI} factory.
   * </p>
   */
  public ApiProviderImpl(final @NotNull Plugin plugin) {
    this.plugin = plugin;

    DreamAPI.setProvider(this);

    loadJacksonModules();
    loadListeners();
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public <T> T getService(@NotNull Class<T> serviceClass) {
    final var sm = Bukkit.getServicesManager();
    final var service = sm.load(serviceClass);
    if (service == null)
      throw new IllegalStateException("Service " + serviceClass.getName() + " is not loaded");
    return service;
  }

  @Override
  public <T extends Event> T callEvent(@NotNull T event) {
    Bukkit.getPluginManager().callEvent(event);
    return event;
  }

  @Override
  public @NotNull Plugin plugin() {
    return this.plugin;
  }

  @Override
  public @NotNull Logger getLogger() {
    return this.plugin.getLogger();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void loadJacksonModules() {
    Configurations.addModule(new BukkitLocationModule());
    Configurations.addModule(new BukkitInteractionModule());
    Configurations.addModule(new BukkitTransformationModule());
    Configurations.addModule(new BukkitWorldModule());
    Configurations.addModule(new BukkitItemStackModule());
    Configurations.addModule(new BukkitVectorModule());
    Configurations.addModule(new BukkitPotionEffectModule());
    Configurations.addModule(new BukkitComponentModule());
    Configurations.addModule(new BukkitBlockDataModule());
    Configurations.addModule(new TickTaskModule());

    for (final var moduleClass : OPTIONAL_JACKSON_MODULES) {
      try {
        final var clazz = Class.forName(moduleClass, false, getClass().getClassLoader());
        final var ctor = clazz.getDeclaredConstructor();
        ctor.setAccessible(true);
        final var module = (com.fasterxml.jackson.databind.Module) ctor.newInstance();
        Configurations.addModule(module);
      } catch (Throwable ignored) {
      }
    }
  }

  private void loadListeners() {
    for (final var listenerClass : OPTIONAL_LISTENERS) {
      try {
        final var clazz = Class.forName(listenerClass, false, getClass().getClassLoader());
        final var ctor = clazz.getDeclaredConstructor();
        ctor.setAccessible(true);
        final var listener = (Listener) ctor.newInstance();
        Bukkit.getPluginManager().registerEvents(listener, this.plugin);
      } catch (Throwable ignored) {
      }
    }
  }

}
