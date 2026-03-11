package fr.dreamin.dreamapi.api.hologram.model;

import fr.dreamin.dreamapi.api.hologram.model.animation.HologramAnimation;
import fr.dreamin.dreamapi.api.hologram.model.line.HologramLine;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface Hologram {

  @NotNull String getId();
  @NotNull HologramConfig getConfig();
  @Nullable Location getLocation();
  @NotNull List<HologramLine> getLines();

  // ###############################################################
  // ------------------------- LIFE CYCLE --------------------------
  // ###############################################################

  void spawn(final @NotNull Location location);

  void despawn();

  void teleport(final @NotNull Location location);

  boolean isSpawned();

  // ###############################################################
  // --------------------------- LIGNES ----------------------------
  // ###############################################################

  void addLine(final @NotNull HologramLine line);
  void removeLine(final @NotNull String lineId);
  void removeLine(final @NotNull HologramLine line);
  void insertLine(final int index, final @NotNull HologramLine line);
  @NotNull Optional<HologramLine> getLine(final @NotNull String lineId);

  // ###############################################################
  // --------------------------- UPDATE ----------------------------
  // ###############################################################

  void tick(final long tick);

}
