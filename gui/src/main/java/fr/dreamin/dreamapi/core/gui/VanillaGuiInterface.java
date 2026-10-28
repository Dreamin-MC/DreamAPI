package fr.dreamin.dreamapi.core.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public interface VanillaGuiInterface {

  @NotNull Component name(final @NotNull Player player);

  @NotNull Inventory inventory(final @NotNull Player player);

  default void open(final @NotNull Player player) {
    player.openInventory(inventory(player));
  }

}
