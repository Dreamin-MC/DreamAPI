package fr.dreamin.dreamapi.core.recipe.ui.vanilla;

import fr.dreamin.dreamapi.api.recipe.CustomRecipe;
import fr.dreamin.dreamapi.api.recipe.IngredientDefinition;
import fr.dreamin.dreamapi.api.recipe.RecipeType;
import fr.dreamin.dreamapi.core.gui.VanillaGuiInterface;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class CraftingVanillaGUI implements VanillaGuiInterface {

  private final CustomRecipe recipe;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public @NotNull Component name(@NotNull Player player) {
    return Component.text("Crafting Preview: " + this.recipe.getKey());
  }

  @Override
  public @NotNull Inventory inventory(@NotNull Player player) {

    if (this.recipe.getType() != RecipeType.SHAPED && this.recipe.getType() != RecipeType.SHAPELESS)
      throw new IllegalStateException("CraftingVanillaGUI used on non-crafting recipe: " + recipe.getKey());

    player.openWorkbench(player.getLocation(), true);

    final var inv = (CraftingInventory) player.getOpenInventory().getTopInventory();

    inv.setResult(this.recipe.getResult().clone());

    if (this.recipe.getType() == RecipeType.SHAPED)
      fillShaped(inv);
    else
      fillShapeless(inv);

    return inv;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void fillShaped(final @NotNull CraftingInventory inv) {
    final var shape = this.recipe.getShape();

    var slot = 1;

    for (final var row : shape) {
      for (final var def : row)
        inv.setItem(slot++, safe(def));

      while ((slot - 1) % 3 != 1)
        inv.setItem(slot++, null);

    }

    while (slot <= 9) {
      inv.setItem(slot++, null);
    }

  }

  private void fillShapeless(final @NotNull CraftingInventory inv) {
    final var ingredients = this.recipe.getIngredients();

    var slot = 1;

    for (final var def : ingredients) {
      if (slot > 9) break;
      inv.setItem(slot++, safe(def));
    }

    while (slot <= 9) {
      inv.setItem(slot++, null);
    }

  }

  private ItemStack safe(final @NotNull IngredientDefinition def) {
    final var st = def.display();
    if (st == null || st.getType() == Material.AIR)
      return new ItemStack(Material.AIR);
    return st.clone();
  }

}
