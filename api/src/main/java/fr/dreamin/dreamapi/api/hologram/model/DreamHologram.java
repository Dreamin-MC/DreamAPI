package fr.dreamin.dreamapi.api.hologram.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.dreamin.dreamapi.api.hologram.event.HologramCreateEvent;
import fr.dreamin.dreamapi.api.hologram.event.HologramDeleteEvent;
import fr.dreamin.dreamapi.api.hologram.model.line.HologramLine;
import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public final class DreamHologram implements Hologram {

  private final @NotNull String id;
  private @NotNull HologramConfig config;
  private @Nullable Location location;
  private final @NotNull List<HologramLine> lines;
  private boolean spawned = false;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  @JsonCreator
  public DreamHologram(
    @JsonProperty("id") final @NotNull String id,
    @JsonProperty("config") final @Nullable HologramConfig config,
    @JsonProperty("location") final @Nullable Location location,
    @JsonProperty("lines") final @Nullable List<HologramLine> lines
  ) {
    this.id = id;
    this.config = config != null ? config : HologramConfig.builder().build();
    this.location = location;
    this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################


  @Override
  public void spawn(@NotNull Location location) {
    if (this.spawned) return;
    this.location = location;
    if (!new HologramCreateEvent(this.id, this).callEvent())
      return;

    double yOffset = 0;

    for (var i = this.lines.size() - 1; i >= 0; i--) {
      final var line = this.lines.get(i);
      final var anchor = location.clone().add(0, yOffset, 0);
      line.spawn(anchor);
      yOffset += line.getConfig().getHeight() + this.config.getLineSpacing();
    }
    this.spawned = true;
  }

  @Override
  public void despawn() {
    if (!this.spawned) return;
    if (!new HologramDeleteEvent(this.id, this).callEvent())
      return;
    this.lines.forEach(HologramLine::despawn);
    this.spawned = false;
  }

  @Override
  public void teleport(@NotNull Location location) {
    this.location = location;
    if (!this.spawned) return;

    double yOffset = 0;
    for (int i = this.lines.size() - 1; i >= 0; i--) {
      final var line = this.lines.get(i);
      line.despawn();
      line.spawn(location.clone().add(0, yOffset, 0));
      yOffset += line.getConfig().getHeight() + this.config.getLineSpacing();
    }

  }

  @Override
  public boolean isSpawned() {
    return this.spawned;
  }

  @Override
  public void addLine(final @NotNull HologramLine line) {
    this.lines.add(line);
  }

  @Override
  public void removeLine(final @NotNull String lineId) {
    this.lines.stream()
      .filter(l -> l.getId().equals(lineId))
      .findFirst()
      .ifPresent(l -> {
        l.despawn();
        this.lines.remove(l);
      });
  }

  @Override
  public void removeLine(@NotNull HologramLine line) {
    line.despawn();
    this.lines.remove(line);
  }

  @Override
  public void insertLine(final int index, final @NotNull HologramLine line) {
    this.lines.add(Math.min(index, this.lines.size()), line);
  }

  @Override
  public @NotNull Optional<HologramLine> getLine(final @NotNull String lineId) {
    return this.lines.stream().filter(l -> l.getId().equals(lineId)).findFirst();
  }

  @Override
  public void tick(final long tick) {
    if (!this.spawned) return;
    for (final var line : this.lines) {
      final int interval = line.getConfig().resolveInterval(this.config.getUpdateIntervalTicks());

      if (tick % interval == 0)
        line.update();

      final var anim = line.getAnimation() != null
        ? line.getAnimation()
        : this.config.getAnimation();

      if (anim != null && tick % anim.getIntervalTicks() == 0)
        line.applyAnimation(anim, tick);
    }
  }

}
