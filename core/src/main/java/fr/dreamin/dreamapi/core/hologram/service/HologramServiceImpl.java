package fr.dreamin.dreamapi.core.hologram.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.api.hologram.animation.AnimationEngine;
import fr.dreamin.dreamapi.api.hologram.event.HologramSaveEvent;
import fr.dreamin.dreamapi.api.hologram.model.DreamHologram;
import fr.dreamin.dreamapi.api.hologram.model.Hologram;
import fr.dreamin.dreamapi.api.hologram.service.HologramService;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@DreamAutoService(HologramService.class)
public final class HologramServiceImpl implements HologramService, DreamService {

  private final @NotNull Plugin plugin;
  private final @NotNull File dataFolder;
  private final Map<String, Hologram> holograms = new ConcurrentHashMap<>();
  private final AnimationEngine engine;

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public HologramServiceImpl(final @NotNull Plugin plugin) {
    this.plugin = plugin;
    this.dataFolder = new File(this.plugin.getDataFolder(), "holograms");
    this.dataFolder.mkdirs();
    this.engine = new AnimationEngine(this.plugin, this.holograms::values);
    this.engine.start();
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onClose() {
    shutdown();
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public @NotNull Hologram create(@NotNull String id) {
    final var hologram = new DreamHologram(id, null, null, null);
    this.holograms.put(id, hologram);
    return hologram;
  }

  @Override
  public void spawn(@NotNull String id, @NotNull Location location) {
    getHologram(id).ifPresent(h -> h.spawn(location));
  }

  @Override
  public void spawn(@NotNull Hologram hologram, @NotNull Location location) {
    hologram.spawn(location);
  }

  @Override
  public @NotNull Optional<Hologram> getHologram(@NotNull String id) {
    return Optional.ofNullable(this.holograms.get(id));
  }

  @Override
  public boolean delete(@NotNull String id) {
    final var removed = this.holograms.remove(id);

    if (removed != null) {
      removed.despawn();
      return true;
    }

    return false;
  }

  @Override
  public boolean delete(@NotNull Hologram hologram) {
    final var removed = this.holograms.remove(hologram.getId());

    if (removed != null) {
      removed.despawn();
      return true;
    }

    return false;
  }

  @Override
  public void deleteAll() {
    this.holograms.values().forEach(Hologram::despawn);
    this.holograms.clear();
  }

  @Override
  public @NotNull Collection<Hologram> getAll() {
    return this.holograms.values();
  }

  @Override
  public void register(@NotNull Hologram hologram) {
    this.holograms.put(hologram.getId(), hologram);
  }

  @Override
  public void save(@NotNull String id, @NotNull File file) throws IOException {
    final var hologram = this.holograms.get(id);
    if (hologram == null) throw new IllegalArgumentException("Hologram with id " + id + " does not exist");
    save(hologram, file);
  }

  @Override
  public void save(@NotNull Hologram hologram, @NotNull File file) throws IOException {
    if (!new HologramSaveEvent(hologram.getId(), hologram, file).callEvent())
      return;
    Configurations.MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, hologram);
  }

  @Override
  public void save(@NotNull String id) throws IOException {
    save(id, new File(this.dataFolder, id + ".json"));
  }

  @Override
  public void save(@NotNull Hologram hologram) throws IOException {
    save(hologram, new File(this.dataFolder, hologram.getId() + ".json"));
  }

  @Override
  public @NotNull Hologram load(@NotNull File file) throws IOException {
    final var hologram = Configurations.MAPPER.readValue(file, Hologram.class);
    this.holograms.put(hologram.getId(), hologram);
    return hologram;
  }

  @Override
  public void loadAll() {
    final var files = this.dataFolder.listFiles(
      (dir, name) -> name.endsWith(".json")
    );

    if (files == null) return;
    for (final var f : files) {
      try {
        load(f);
      } catch (IOException e) {
        this.plugin.getLogger().log(Level.SEVERE, "Failed to load hologram: " + f.getName(), e);
      }
    }

  }

  @Override
  public void shutdown() {
    this.engine.stop();
    this.holograms.values().forEach(Hologram::despawn);
  }

}
