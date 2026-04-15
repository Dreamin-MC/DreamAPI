package fr.dreamin.dreamapi.api.hologram.model.animation.impl;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.dreamin.dreamapi.api.hologram.model.animation.HologramAnimation;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;

@Getter
@Builder
@Jacksonized
@JsonTypeName("ROTATE_Y")
public final class RotateAnimation implements HologramAnimation {

  public enum Axis { X, Y, Z }

  @Builder.Default
  private final String type = "ROTATE_Y";

  @Builder.Default
  private final float speed = 0.05f;

  @Builder.Default
  private final Axis axis = Axis.Y;

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
    final var angle = (tick * this.speed) % (float) (2* Math.PI);
    final var transform = entity.getTransformation();
    final var ax = this.axis == Axis.X ? 1f : 0f;
    final var ay = this.axis == Axis.Y ? 1f : 0f;
    final var az = this.axis == Axis.Z ? 1f : 0f;

    transform.getLeftRotation().set(new AxisAngle4f(angle, ax, ay, az));
    entity.setTransformation(transform);
    entity.setInterpolationDelay(0);
    entity.setInterpolationDuration(this.intervalTicks);
  }
}
