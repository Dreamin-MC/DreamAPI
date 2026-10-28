package fr.dreamin.dreamapi.api.hologram.model.line.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.dreamin.dreamapi.api.hologram.model.animation.HologramAnimation;
import fr.dreamin.dreamapi.api.hologram.model.line.HologramLine;
import fr.dreamin.dreamapi.api.hologram.model.line.LineConfig;
import fr.dreamin.dreamapi.api.hologram.model.line.LineElement;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@JsonTypeName("COMPOSITE")
public final class CompositeHologramLine implements HologramLine {

  private final @NotNull String id;
  private final @NotNull LineConfig config;
  private final @Nullable HologramAnimation animation;
  private final @NotNull List<LineElement> elements;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  @JsonCreator
  public CompositeHologramLine(
    @JsonProperty("id") final @NotNull String id,
    @JsonProperty("config") final @Nullable LineConfig config,
    @JsonProperty("animation") final @Nullable HologramAnimation animation,
    @JsonProperty("elements") final @NotNull List<LineElement> elements
  ) {
    this.id = id;
    this.config = config != null ? config : LineConfig.builder().build();
    this.animation = animation;
    this.elements = elements;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################


  @Override
  public @NotNull List<Display> getEntities() {
    final var all = new ArrayList<Display>();
    this.elements.forEach(e -> all.addAll(e.getLine().getEntities()));
    return all;
  }

  @Override
  public void spawn(@NotNull Location location) {
    final var base = location.clone().add(
      this.config.getOffsetX(),
      this.config.getOffsetY(),
      this.config.getOffsetZ()
    );
    this.elements.forEach(e -> e.getLine().spawn(base));
  }

  @Override
  public void despawn() {
    this.elements.forEach(e -> e.getLine().despawn());
  }

  @Override
  public void update() {
    this.elements.forEach(e -> e.getLine().update());
  }

  @Override
  public void applyAnimation(@NotNull HologramAnimation animation, long tick) {
    for (final var element : this.elements) {
      final var effective = element.getAnimation() != null ? element.getAnimation() : animation;
      element.getLine().applyAnimation(effective, tick);
    }
  }

  @Override
  public boolean isSpawned() {
    return this.elements.stream().anyMatch(e -> e.getLine().isSpawned());
  }
}
