package fr.dreamin.dreamapi.api.hologram.model;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder
public record HologramAnim(@NotNull Type type, double speed) {

  public enum Type {
    COLOR_CYCLE,
    ROTATE_Y,
    ROTATE_X,
    PULSE_SCALE,
    RAINBOW
  }

}
