package fr.dreamin.dreamapi.core.recipe.ui.vanilla;

import fr.dreamin.dreamapi.api.recipe.CustomRecipe;
import fr.dreamin.dreamapi.core.gui.VanillaGuiInterface;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class FurnaceVanillaGUI implements VanillaGuiInterface {

  private final CustomRecipe recipe;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public @NotNull Component name(@NotNull Player player) {
    return Component.text("Furnace Preview: " + this.recipe.getKey());
  }

  @Override
  public @NotNull Inventory inventory(@NotNull Player player) {

    final var input = this.recipe.getFurnaceInput();
    if (input == null)
      throw new IllegalStateException("FurnaceVanillaGUI called on non-furnace recipe: " + recipe.getKey());

    final var inv = Bukkit.createInventory(null, InventoryType.FURNACE, name(player));
    final var furnace = (FurnaceInventory) inv;

    furnace.setSmelting(input.display());
    furnace.setFuel(new ItemStack(Material.BLAZE_POWDER));
    furnace.setResult(this.recipe.getResult().clone());

    return inv;
  }
}
