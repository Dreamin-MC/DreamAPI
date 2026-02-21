package fr.dreamin.dreamapi.core.hologram.service;

import fr.dreamin.dreamapi.api.hologram.event.HologramDeleteEvent;
import fr.dreamin.dreamapi.api.hologram.model.HologramBuilder;
import fr.dreamin.dreamapi.api.hologram.service.HologramService;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@DreamAutoService(HologramService.class)
public final class HologramServiceImpl implements HologramService, DreamService {

  private final @NotNull Plugin plugin;

  private final Map<String, HologramBuilder> holograms = new ConcurrentHashMap<>();

  private final BukkitTask updateTask;

  public HologramServiceImpl(final @NotNull Plugin plugin) {
    this.plugin = plugin;
    this.updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
      this::updateAllHolograms, 1L, 1L
    );
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

//  @Override
//  public HologramBuilder.@NonNull HologramBuilderBuilder builder(@NotNull String id) {
//    return HologramBuilder.builder();
//  }

  @Override
  public @Nullable HologramBuilder getHologram(@NotNull String id) {
    return this.holograms.get(id);
  }

  @Override
  public void deleteHologram(@NotNull String id) {
    final var hologram = this.holograms.remove(id);
    if (hologram != null) {
      new HologramDeleteEvent(id, hologram).callEvent();
//      hologram.destroy();
    }
  }

  @Override
  public void deleteHologram(@NonNull HologramBuilder hologramBuilder) {
    if (!this.holograms.containsValue(hologramBuilder)) return;

    new HologramDeleteEvent(hologramBuilder.getId(), hologramBuilder).callEvent();
//    hologramBuilder.destroy();
  }

  @Override
  public void deleteAllHolograms() {
//    this.holograms.values().forEach(HologramBuilder::destroy);
    this.holograms.clear();
  }

  @Override
  public @NotNull Collection<HologramBuilder> getAllHolograms() {
    return new ArrayList<>(this.holograms.values());
  }

  @Override
  public void register(final @NotNull HologramBuilder hologram) {
    this.holograms.put(hologram.getId(), hologram);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void updateAllHolograms() {
    for (final var hologram : this.holograms.values()) {
      hologram.update();
    }
  }

}
