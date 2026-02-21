package fr.dreamin.dreamapi.api.hologram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ItemLine(@NotNull ItemStack item) implements HologramLine {

  @Override
  public @NotNull Component getDisplay() {
    return null;
  }

  @Override
  public double getHeight() {
    return 0.35;
  }

  @Override
  public boolean isClickable() {
    return false;
  }
}
