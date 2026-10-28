package fr.dreamin.dreamapi.api.hologram.model.animation.impl;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.dreamin.dreamapi.api.hologram.model.animation.HologramAnimation;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@Getter
@Builder
@Jacksonized
@JsonTypeName("COLOR_CYCLE")
public final class ColorCycleAnimation implements HologramAnimation {

  @Builder.Default
  private final String type = "COLOR_CYCLE";

  @Builder.Default
  private final float speed = 0.005f;

  @Builder.Default
  private final float saturation = 1.0f;

  @Builder.Default
  private final float brightness = 1.0f;

  @Builder.Default
  private final int intervalTicks = 1;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public int getIntervalTicks() {
    return this.intervalTicks;
  }

  @Override
  public void apply(@NotNull Display entity, long tick) {
    if (!(entity instanceof TextDisplay textDisplay)) return;
    final var hue = (tick * this.speed) % 1.0f;
    final var rgb = Color.HSBtoRGB(hue, this.saturation, this.brightness);
    final var awtColor = new Color(rgb);
    textDisplay.setBackgroundColor(
      org.bukkit.Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue())
    );
  }
}
