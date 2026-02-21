package fr.dreamin.dreamapi.api.hologram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TextLine(@NotNull Component text) implements HologramLine {

  @Override
  public @NotNull Component getDisplay() {
    return this.text;
  }

  @Override
  public double getHeight() {
    return 0.25;
  }

  @Override
  public boolean isClickable() {
    return false;
  }
}
