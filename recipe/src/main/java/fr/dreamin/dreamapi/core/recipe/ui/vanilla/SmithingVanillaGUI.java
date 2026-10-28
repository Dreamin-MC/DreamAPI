package fr.dreamin.dreamapi.core.recipe.ui.vanilla;

import fr.dreamin.dreamapi.api.recipe.CustomRecipe;
import fr.dreamin.dreamapi.api.recipe.IngredientDefinition;
import fr.dreamin.dreamapi.api.recipe.RecipeType;
import fr.dreamin.dreamapi.core.gui.VanillaGuiInterface;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class SmithingVanillaGUI implements VanillaGuiInterface {

  private final CustomRecipe recipe;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public @NotNull Component name(@NotNull Player player) {
    return Component.text("Smithing Preview: " + this.recipe.getKey());
  }

  @Override
  public @NotNull Inventory inventory(@NotNull Player player) {
    if (this.recipe.getType() != RecipeType.SMITHING)
      throw new IllegalStateException("SmithingVanillaGUI used on non-smithing recipe: " + recipe.getKey());

    final var inv = Bukkit.createInventory(null, InventoryType.SMITHING, name(player));

    final var template = this.recipe.getSmithingTemplate();
    final var base = this.recipe.getSmithingBase();
    final var addition = this.recipe.getSmithingAddition();

    inv.setItem(0, safe(template));
    inv.setItem(1, safe(base));
    inv.setItem(2, safe(addition));
    inv.setItem(3, this.recipe.getResult().clone());

    return inv;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private ItemStack safe(IngredientDefinition def) {
    if (def == null) return null;

    final var st = def.display();
    if (st == null || st.getType() == Material.AIR)
      return new ItemStack(Material.AIR);

    return st.clone();
  }

}
