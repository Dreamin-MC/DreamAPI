package fr.dreamin.dreamapi.api.glowing.animation.model;

import fr.dreamin.dreamapi.api.glowing.animation.GlowAnimation;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public final class BlinkAnimation implements GlowAnimation {

  private final @NotNull ChatColor color;
  private final long interval;

  public BlinkAnimation(final @NotNull ChatColor color, final long interval) {
    this.color = color;
    this.interval = interval;
  }

  @Override
  public @NotNull ChatColor getColorAtTick(long tick) {
    return (tick / interval) % 2 == 0 ? color : ChatColor.WHITE;
  }

  @Override
  public boolean isComplete(long tick) {
    return false;
  }

  @Override
  public long getDuration() {
    return 0;
  }
}