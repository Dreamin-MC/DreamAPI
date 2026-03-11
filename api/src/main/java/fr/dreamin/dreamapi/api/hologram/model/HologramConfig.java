package fr.dreamin.dreamapi.api.hologram.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.dreamin.dreamapi.api.hologram.model.animation.HologramAnimation;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class HologramConfig {

  @Builder.Default
  private final double lineSpacing = 0.05;

  @Builder.Default
  private final double scale = 1.0;

  @Builder.Default
  private final int updateIntervalTicks = 20;

  @Nullable
  private final HologramAnimation animation;

  @Builder.Default
  private final float viewRange = 48.0f;

  @Builder.Default
  private final boolean seeThrough = false;

  @Builder.Default
  private final Display.Billboard billboard = Display.Billboard.CENTER;

  @Builder.Default
  private final boolean persistent = false;

}
