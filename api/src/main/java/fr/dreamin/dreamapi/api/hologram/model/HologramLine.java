package fr.dreamin.dreamapi.api.hologram.model;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public sealed interface HologramLine permits TextLine, ItemLine {
  @NotNull Component getDisplay();
  double getHeight();
  boolean isClickable();
}
