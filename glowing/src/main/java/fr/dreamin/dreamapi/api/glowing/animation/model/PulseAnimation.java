package fr.dreamin.dreamapi.api.glowing.animation.model;

import fr.dreamin.dreamapi.api.glowing.animation.GlowAnimation;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public final class PulseAnimation implements GlowAnimation {

  private final @NotNull ChatColor primaryColor;
  private static final long PULSE_INTERVAL = 5L; // 1 second

  public PulseAnimation(final @NotNull ChatColor primaryColor) {
    this.primaryColor = primaryColor;
  }

  @Override
  public @NotNull ChatColor getColorAtTick(long tick) {
    return (tick / PULSE_INTERVAL) % 2 == 0 ? primaryColor : ChatColor.WHITE;
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