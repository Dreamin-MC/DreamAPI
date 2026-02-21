package fr.dreamin.dreamapi.api.hologram.model;

import lombok.Builder;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Builder
public record HologramConfig(
  @NotNull List<HologramLine> lines,
  double lineSpacing,
  double scale,
  int updateIntervalTicks,
  @Nullable HologramAnim animation,
  float viewRange,
  boolean seeThrough,
  @Nullable Color color,
  @NotNull Display.Billboard billboard
) {
  public HologramConfig {
    lineSpacing = Math.max(0.0, lineSpacing);
    scale = Math.max(0.1, Math.min(5.0, scale));
    viewRange = Math.max(16.0f, Math.min(256.0f, viewRange));
  }
}
