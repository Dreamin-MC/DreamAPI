package fr.dreamin.dreamapi.api.glowing.animation.model;

import fr.dreamin.dreamapi.api.glowing.animation.GlowAnimation;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public final class FadeAnimation implements GlowAnimation {

  private final @NotNull ChatColor from;
  private final @NotNull ChatColor to;
  private final long duration;

  public FadeAnimation(final @NotNull ChatColor from, final @NotNull ChatColor to, final long duration) {
    this.from = from;
    this.to = to;
    this.duration = duration;
  }

  @Override
  public @NotNull ChatColor getColorAtTick(long tick) {
    if (tick >= duration) return to;
    return tick < duration / 2 ? from : to;
  }

  @Override
  public boolean isComplete(long tick) {
    return tick >= duration;
  }

  @Override
  public long getDuration() {
    return duration;
  }
}