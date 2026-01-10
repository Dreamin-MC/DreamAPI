package fr.dreamin.dreamapi.core.recipe.ui;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.recipe.service.RecipeRegistryService;
import fr.dreamin.dreamapi.api.recipe.service.RecipeCategoryRegistryService;
import fr.dreamin.dreamapi.api.recipe.RecipeCategory;
import fr.dreamin.dreamapi.core.gui.GuiInterface;
import fr.dreamin.dreamapi.core.gui.item.NextItem;
import fr.dreamin.dreamapi.core.gui.item.PreviousItem;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RecipeCategoryGUI implements GuiInterface {

  private final RecipeCategoryRegistryService categoryService = DreamAPI.getAPI().getService(RecipeCategoryRegistryService.class);
  private final RecipeRegistryService craftService = DreamAPI.getAPI().getService(RecipeRegistryService.class);

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public Component name(@NotNull Player player) {
    return Component.text("Recipe Categories");
  }

  @Override
  public Gui guiUpper(@NotNull Player player) {
    return PagedGui.itemsBuilder()
      .setStructure(
        ". . . . . . . . .",
        ". X X X X X X X .",
        ". X X X X X X X .",
        ". X X X X X X X .",
        ". X X X X X X X .",
        ". P . . . . . N ."
      )
      .addIngredient('X', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
      .addIngredient('P', new PreviousItem(Material.ARROW))
      .addIngredient('N', new NextItem(Material.ARROW))
      .setContent(getCategoryItems())
      .build();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private List<Item> getCategoryItems() {
    final var items = new ArrayList<Item>();

    this.categoryService.getAllCategories().stream()
      .sorted(Comparator.comparingInt(RecipeCategory::getPriority))
      .forEach(category -> items.add(new AbstractItem() {
        @Override
        public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
          return new ItemBuilder(category.getIcon())
            .setName(category.getDisplayName())
            .setLore(
              Component.empty(),
              Component.text("ID: ", NamedTextColor.GRAY)
                .append(Component.text(category.getId(), NamedTextColor.YELLOW)),
              Component.text("Tags: ", NamedTextColor.GRAY)
                .append(Component.text(category.getAssociatedTags().size(), NamedTextColor.YELLOW)),
              Component.empty(),
              Component.text("Click to show all recipes", NamedTextColor.DARK_GRAY)
            )
            .toGuiItem();
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
          new RecipeListGUI(category).open(player);
        }
      }));


    return items;
  }

}
