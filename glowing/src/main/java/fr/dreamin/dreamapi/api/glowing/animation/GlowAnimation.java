package fr.dreamin.dreamapi.api.glowing.animation;

import fr.dreamin.dreamapi.api.glowing.animation.model.*;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public interface GlowAnimation {

  @NotNull ChatColor getColorAtTick(final long tick);

  boolean isComplete(final long tick);

  long getDuration();

  // ###############################################################
  // ----------------------- PREDEFINED ----------------------------
  // ###############################################################

  static @NotNull GlowAnimation rainbow() {
    return new RainbowAnimation();
  }

  static @NotNull GlowAnimation pulse(@NotNull ChatColor color) {
    return new PulseAnimation(color);
  }

  static @NotNull GlowAnimation fade(@NotNull ChatColor from, @NotNull ChatColor to, long duration) {
    return new FadeAnimation(from, to, duration);
  }

  static @NotNull GlowAnimation blink(@NotNull ChatColor color, long interval) {
    return new BlinkAnimation(color, interval);
  }

  static @NotNull GlowAnimation fixed(@NotNull ChatColor color) {
    return new FixedAnimation(color);
  }
}