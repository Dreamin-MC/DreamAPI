package fr.dreamin.dreamapi.core.recipe.ui.fake;

import fr.dreamin.dreamapi.api.recipe.CustomRecipe;
import fr.dreamin.dreamapi.api.recipe.IngredientDefinition;
import fr.dreamin.dreamapi.api.gui.model.GuiInterface;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

@RequiredArgsConstructor
public final class FurnaceFakeGUI extends GuiInterface {

  private final CustomRecipe recipe;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public Component name(@NotNull Player player) {
    return Component.text("Furnace Recipe: " + this.recipe.getKey());
  }

  @Override
  public Gui guiUpper(@NotNull Player player) {

    final var input = this.recipe.getFurnaceInput();
    if (input == null)
      throw new IllegalStateException("FurnaceFakeGUI called on non-furnace recipe: " + recipe.getKey());

    final var result = this.recipe.getResult();

    final var fire = new ItemBuilder(Material.BLAZE_POWDER)
      .setName(Component.text("Smelthing"))
      .toGuiItem();

    return Gui.builder()
      .setStructure(
        ". . . . . . . . .",
        ". . . . I . . . .",
        ". . . . F . > . .",
        ". . . . R . . . .",
        ". . . . . . . . ."
      )
      .addIngredient('I', item(input))
      .addIngredient('F', fire)
      .addIngredient('R', item(result))
      .addIngredient('>', item(result))
      .build();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private xyz.xenondevs.invui.item.ItemBuilder item(IngredientDefinition def) {
    if (def == null) return item((ItemStack) null);
    return item(def.display());
  }

  private xyz.xenondevs.invui.item.ItemBuilder item(ItemStack st) {
    if (st == null)
      return new ItemBuilder(Material.AIR).toGuiItem();

    return new ItemBuilder(st).toGuiItem();
  }

}
