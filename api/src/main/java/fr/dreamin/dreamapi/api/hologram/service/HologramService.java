package fr.dreamin.dreamapi.api.hologram.service;

import fr.dreamin.dreamapi.api.hologram.model.Hologram;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

public interface HologramService {

  @NotNull Hologram create(final @NotNull String id);

  void spawn(final @NotNull String id, final @NotNull Location location);
  void spawn(final @NotNull Hologram hologram, final @NotNull Location location);

  @NotNull Optional<Hologram> getHologram(final @NotNull String id);

  boolean delete(final @NotNull String id);
  boolean delete(final @NotNull Hologram hologram);

  void deleteAll();

  @NotNull Collection<Hologram> getAll();

  void register(final @NotNull Hologram hologram);

  void save(final @NotNull String id, final @NotNull File file) throws IOException;
  void save(final @NotNull Hologram hologram, final @NotNull File file) throws IOException;
  void save(final @NotNull String id) throws IOException;
  void save(final @NotNull Hologram hologram) throws IOException;

  @NotNull Hologram load(final @NotNull File file) throws IOException;

  void loadAll();

  void shutdown();

}
