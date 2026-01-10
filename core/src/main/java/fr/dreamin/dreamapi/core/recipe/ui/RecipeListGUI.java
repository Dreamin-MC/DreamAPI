package fr.dreamin.dreamapi.core.recipe.ui;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.recipe.service.RecipeRegistryService;
import fr.dreamin.dreamapi.api.recipe.RecipeTag;
import fr.dreamin.dreamapi.api.recipe.RecipeCategory;
import fr.dreamin.dreamapi.core.recipe.ui.fake.FurnaceFakeGUI;
import fr.dreamin.dreamapi.core.recipe.ui.fake.ShapedFakeGUI;
import fr.dreamin.dreamapi.core.recipe.ui.fake.ShapelessFakeGUI;
import fr.dreamin.dreamapi.core.recipe.ui.fake.SmithingFakeGUI;
import fr.dreamin.dreamapi.core.recipe.ui.vanilla.CraftingVanillaGUI;
import fr.dreamin.dreamapi.core.recipe.ui.vanilla.FurnaceVanillaGUI;
import fr.dreamin.dreamapi.core.recipe.ui.vanilla.SmithingVanillaGUI;
import fr.dreamin.dreamapi.core.gui.GuiInterface;
import fr.dreamin.dreamapi.core.gui.item.NextItem;
import fr.dreamin.dreamapi.core.gui.item.PreviousItem;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public final class RecipeListGUI implements GuiInterface {

  private final RecipeCategory category;
  private final RecipeRegistryService craftService = DreamAPI.getAPI().getService(RecipeRegistryService.class);

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public Component name(@NotNull Player player) {
    return Component.text("Recipes: ").append(this.category.getDisplayName());
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
      .setContent(getRecipeItems())
      .build();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private List<Item> getRecipeItems() {
    List<Item> items = new ArrayList<>();

    // Récupération des recettes possédant au moins un des tags de la catégorie
    Set<RecipeTag> tags = category.getAssociatedTags();

    craftService.getAllRecipes().stream()
      .filter(r -> r.getTags().stream().anyMatch(tags::contains))
      .forEach(recipe -> items.add(new AbstractItem() {

        @Override
        public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
          return new ItemBuilder(recipe.getResult())
            .setName(Component.text(recipe.getKey(), NamedTextColor.GOLD))
            .setLore(
              Component.empty(),
              Component.text("Type: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getType().name(), NamedTextColor.YELLOW)),
              Component.text("Tags: ", NamedTextColor.GRAY)
                .append(Component.text(recipe.getTags().size(), NamedTextColor.AQUA)),
              Component.empty(),
              Component.text("Left-click: Fake preview", NamedTextColor.DARK_GRAY),
              Component.text("Right-click: Vanilla preview", NamedTextColor.DARK_GRAY),
              Component.empty()
            )
            .toGuiItem();
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
          // ---------------- FAKE GUI (InvUI) ----------------
          if (clickType.isLeftClick()) {
            switch (recipe.getType()) {
              case SHAPED -> new ShapedFakeGUI(recipe).open(player);
              case SHAPELESS -> new ShapelessFakeGUI(recipe).open(player);
              case FURNACE -> new FurnaceFakeGUI(recipe).open(player);
              case SMITHING -> new SmithingFakeGUI(recipe).open(player);
            }
            return;
          }

          // ---------------- VANILLA PREVIEW ----------------
          if (clickType.isRightClick()) {
            switch (recipe.getType()) {
              case SHAPED, SHAPELESS -> new CraftingVanillaGUI(recipe).open(player);
              case FURNACE -> new FurnaceVanillaGUI(recipe).open(player);
              case SMITHING -> new SmithingVanillaGUI(recipe).open(player);
            }
          }
        }
      }));

    return items;
  }
}
