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
public final class SmithingFakeGUI extends GuiInterface {

  private final CustomRecipe recipe;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public Component name(@NotNull Player player) {
    return Component.text("Smithing Recipe: " + this.recipe.getKey());
  }

  @Override
  public Gui guiUpper(@NotNull Player player) {

    final var template = recipe.getSmithingTemplate();
    final var base = recipe.getSmithingBase();
    final var addition = recipe.getSmithingAddition();

    if (template == null || base == null || addition == null)
      throw new IllegalStateException("SmithingFakeGUI called on non-smithing recipe: " + recipe.getKey());

    return Gui.builder()
      .setStructure(
        ". . . . . . . . .",
        ". . T B A . > . .",
        ". . . . . . . . ."
      )
      .addIngredient('T', item(template))
      .addIngredient('B', item(base))
      .addIngredient('A', item(addition))
      .addIngredient('>', item(recipe.getResult()))
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
    if (st == null) {
      return new ItemBuilder(Material.AIR).toGuiItem();
    }
    return new ItemBuilder(st).toGuiItem();
  }

}
