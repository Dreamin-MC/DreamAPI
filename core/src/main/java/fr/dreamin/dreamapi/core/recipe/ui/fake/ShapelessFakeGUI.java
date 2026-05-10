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

import java.util.List;

@RequiredArgsConstructor
public final class ShapelessFakeGUI extends GuiInterface {

  private final CustomRecipe recipe;

  @Override
  public Component name(@NotNull Player player) {
    return Component.text("Recipe: " + this.recipe.getKey());
  }

  @Override
  public Gui guiUpper(@NotNull Player player) {

    final var ing = this.recipe.getIngredients();
    if (ing == null)
      throw new IllegalStateException("ShapelessFakeGUI called on non-shapeless recipe: " + this.recipe.getKey());

    final var A = get(ing, 0);
    final var B = get(ing, 1);
    final var C = get(ing, 2);

    final var D = get(ing, 3);
    final var E = get(ing, 4);
    final var F = get(ing, 5);

    final var G = get(ing, 6);
    final var H = get(ing, 7);
    final var I = get(ing, 8);

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

  private IngredientDefinition get(List<IngredientDefinition> list, int index) {
    return index < list.size() ? list.get(index) : null;
  }

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
