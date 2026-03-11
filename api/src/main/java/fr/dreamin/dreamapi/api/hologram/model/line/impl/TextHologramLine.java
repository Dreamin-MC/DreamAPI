package fr.dreamin.dreamapi.api.hologram.model.line.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.dreamin.dreamapi.api.hologram.model.animation.HologramAnimation;
import fr.dreamin.dreamapi.api.hologram.model.line.HologramLine;
import fr.dreamin.dreamapi.api.hologram.model.line.LineConfig;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@JsonTypeName("TEXT")
public final class TextHologramLine implements HologramLine {

  private final @NotNull String id;
  private final @NotNull LineConfig config;
  private final @Nullable HologramAnimation animation;

  private @NotNull String text;

  private final List<Display> entities = new ArrayList<>();

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  @JsonCreator
  public TextHologramLine(
    @JsonProperty("id") final @NotNull String id,
    @JsonProperty("config") final @Nullable LineConfig config,
    @JsonProperty("animation") final @Nullable HologramAnimation animation,
    @JsonProperty("text") final @NotNull String text
  ) {
    this.id = id;
    this.config = config != null ? config : LineConfig.builder().build();
    this.animation = animation;
    this.text = text;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void spawn(final @NotNull Location location) {
    if (isSpawned()) despawn();
    final var loc = location.clone().add(
      this.config.getOffsetX(),
      this.config.getOffsetY(),
      this.config.getOffsetZ()
    );

    final var display = loc.getWorld().spawn(loc, TextDisplay.class, d -> {
      d.text(getComponent());
      d.setBillboard(Display.Billboard.CENTER);
      d.setAlignment(TextDisplay.TextAlignment.CENTER);
      d.setShadowRadius(0.0f);
      d.setShadowStrength(1.0f);
      d.setSeeThrough(false);
    });
    this.entities.add(display);
  }

  @Override
  public void despawn() {
    this.entities.forEach(Display::remove);
    this.entities.clear();
  }

  @Override
  public void update() {
    if (!isSpawned()) return;

    for (final var display : this.entities) {
      if (display instanceof TextDisplay textDisplay)
        textDisplay.text(getComponent());
    }

  }

  @Override
  public void applyAnimation(@NotNull HologramAnimation animation, long tick) {
    if (!isSpawned()) return;
    this.entities.forEach(e -> animation.apply(e, tick));
  }

  @Override
  public boolean isSpawned() {
    return !this.entities.isEmpty();
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public void setText(final @NotNull String text) {
    this.text = text;
  }

  public @NotNull Component getComponent() {
    return MiniMessage.miniMessage().deserialize(this.text);
  }

}
