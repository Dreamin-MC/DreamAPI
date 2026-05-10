package fr.dreamin.dreamapi.core.recipe.ui.fake;

import fr.dreamin.dreamapi.api.recipe.CustomRecipe;
import fr.dreamin.dreamapi.api.recipe.IngredientDefinition;
import fr.dreamin.dreamapi.api.gui.model.GuiInterface;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

import java.util.List;

@RequiredArgsConstructor
public final class ShapedFakeGUI extends GuiInterface {

  private final CustomRecipe recipe;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public Component name(@NotNull Player player) {
    return Component.text("Recipe: " + this.recipe.getKey());
  }

  @Override
  public Gui guiUpper(@NotNull Player player) {

    final var shape = this.recipe.getShape();
    if (shape == null)
      throw new IllegalStateException("ShapedFakeGUI called on non-shaped recipe: " + this.recipe.getKey());

    final var A = get(shape, 0, 0);
    final var B = get(shape, 0, 1);
    final var C = get(shape, 0, 2);

    final var D = get(shape, 1, 0);
    final var E = get(shape, 1, 1);
    final var F = get(shape, 1, 2);

    final var G = get(shape, 2, 0);
    final var H = get(shape, 2, 1);
    final var I = get(shape, 2, 2);

    return Gui.builder()
      .setStructure(
        ". . . . . . . . .",
        ". . A B C . . . .",
        ". . D E F . > . .",
        ". . G H I . . . .",
        ". . . . . . . . ."
      )
      .addIngredient('A', item(A))
      .addIngredient('B', item(B))
      .addIngredient('C', item(C))
      .addIngredient('D', item(D))
      .addIngredient('E', item(E))
      .addIngredient('F', item(F))
      .addIngredient('G', item(G))
      .addIngredient('H', item(H))
      .addIngredient('I', item(I))
      .addIngredient('>', item(this.recipe.getResult()))
      .build();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private IngredientDefinition get(List<List<IngredientDefinition>> shape, int row, int col) {
    if (row >= shape.size()) return null;
    var r = shape.get(row);
    if (col >= r.size()) return null;
    return r.get(col);
  }

  private xyz.xenondevs.invui.item.ItemBuilder item(IngredientDefinition def) {
    if (def == null) return item((ItemStack) null);
    return item(def.display());
  }

  private xyz.xenondevs.invui.item.ItemBuilder item(ItemStack st) {
    if (st == null)
      return new ItemBuilder(org.bukkit.Material.AIR).toGuiItem();

    return new ItemBuilder(st).toGuiItem();
  }

}
