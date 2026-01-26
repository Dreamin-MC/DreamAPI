package fr.dreamin.dreamapi.api.glowing.animation.model;

import fr.dreamin.dreamapi.api.glowing.animation.GlowAnimation;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public final class FixedAnimation implements GlowAnimation {

  private final @NotNull ChatColor color;

  public FixedAnimation(final @NotNull ChatColor color) {
    this.color = color;
  }

  @Override
  public @NotNull ChatColor getColorAtTick(long tick) {
    return color;
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