package fr.dreamin.dreamapi.api.glowing.animation.model;

import fr.dreamin.dreamapi.api.glowing.animation.GlowAnimation;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public final class RainbowAnimation implements GlowAnimation {

  private static final ChatColor[] COLORS = {
    ChatColor.RED,
    ChatColor.GOLD,
    ChatColor.YELLOW,
    ChatColor.GREEN,
    ChatColor.AQUA,
    ChatColor.BLUE,
    ChatColor.LIGHT_PURPLE
  };

  private static final long CYCLE_DURATION = 10L; // 10 ticks per color

  @Override
  public @NotNull ChatColor getColorAtTick(long tick) {
    final var index = (int) ((tick / CYCLE_DURATION) % COLORS.length);
    return COLORS[index];
  }

  @Override
  public boolean isComplete(long tick) {
    return false; // Infinite
  }

  @Override
  public long getDuration() {
    return 0; // Infinite
  }
}