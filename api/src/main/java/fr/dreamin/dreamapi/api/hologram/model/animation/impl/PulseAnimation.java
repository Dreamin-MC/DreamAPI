package fr.dreamin.dreamapi.api.hologram.model.animation.impl;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.dreamin.dreamapi.api.hologram.model.animation.HologramAnimation;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;

@Getter
@Builder
@Jacksonized
@JsonTypeName("PULSE")
public final class PulseAnimation implements HologramAnimation {

  @Builder.Default
  private final String type = "PULSE";

  @Builder.Default
  private final float baseScale = 1.0f;

  @Builder.Default
  private final float amplitude = 0.15f;

  @Builder.Default
  private final float speed = 0.1f;

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
  public void apply(final @NotNull Display entity, final long tick) {
    final var scale = this.baseScale + this.amplitude * (float) Math.sin(tick * this.speed);
    final var transform = entity.getTransformation();
    transform.getScale().set(scale, scale, scale);
    entity.setTransformation(transform);
    entity.setInterpolationDelay(0);
    entity.setInterpolationDuration(this.intervalTicks);
  }

}
