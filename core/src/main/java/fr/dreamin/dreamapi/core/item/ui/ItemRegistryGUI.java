package fr.dreamin.dreamapi.core.item.ui;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.item.ItemRegistryService;
import fr.dreamin.dreamapi.api.item.RegisteredItem;
import fr.dreamin.dreamapi.core.gui.GuiInterface;
import fr.dreamin.dreamapi.core.gui.item.NextItem;
import fr.dreamin.dreamapi.core.gui.item.PreviousItem;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import fr.dreamin.dreamapi.core.sound.SoundHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.AnvilWindow;

import java.util.ArrayList;
import java.util.List;

public final class ItemRegistryGUI implements GuiInterface {

  private final @NotNull ItemRegistryService itemRegistryService = DreamAPI.getAPI().getService(ItemRegistryService.class);

  private @Nullable String search;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public ItemRegistryGUI() {
    this.search = "";
  }

  public ItemRegistryGUI(final @Nullable String search) {
    this.search = search;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public Component name(@NotNull Player player) {
    return Component.text("Item Registry");
  }

  @Override
  public Gui guiUpper(@NotNull Player player) {
    return Gui.builder()
      .setStructure(3, 1, "X D #")
      .addIngredient('X', new ItemBuilder(Material.PAPER)
        .setName(Component.text(search != null ? search : ""))
        .setHideToolType(true)
        .toGuiItem())
      .addIngredient('#', new AbstractItem() {
        @Override
        public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
          return new ItemBuilder(Material.RED_TERRACOTTA)
            .setName(Component.text("Reset search", NamedTextColor.RED))
            .toGuiItem();
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
          player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1, 1);
          new ItemRegistryGUI().open(player);
        }
      })
      .build();
  }


  public PagedGui<Item> pagedGui(@NotNull Player player) {
    return PagedGui.itemsBuilder()
      .setStructure(
        ". X X X X X X X .",
        ". X X X X X X X .",
        ". X X X X X X X .",
        "P . . . . . . . N"
      )
      .addIngredient('X', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
      .addIngredient('P', new PreviousItem())
      .addIngredient('N', new NextItem())
      .setContent(getItems())
      .build();
  }

  @Override
  public void open(@NotNull Player player) {
    final var buildLower = pagedGui(player);

    final var window = AnvilWindow.builder()
      .setViewer(player)
      .setUpperGui(guiUpper(player))
      .setLowerGui(buildLower)
      .setTitle(name(player))
      .addRenameHandler(text -> {
        this.search = text;
        buildLower.setContent(getItems());
        player.playSound(player, "dreamin:click_keyboard", SoundCategory.UI, 1, 1);
      })
      .build();
    window.open();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private boolean matchesSearch(final @NotNull RegisteredItem registered) {
    if (this.search == null || this.search.isBlank()) return true;

    final var query = this.search.toLowerCase();

    if (registered.id().toLowerCase().contains(query)) return true;

    final var meta = registered.item().getItemMeta();
    if (meta == null) return false;

    if (meta.hasDisplayName()) {
      final var name = PlainTextComponentSerializer.plainText()
        .serialize(meta.displayName())
        .toLowerCase();

      if (name.contains(query)) return true;
    }

    return registered.item().getType().name().toLowerCase().contains(query);
  }

  private List<Item> getItems() {
    final var rs = new ArrayList<Item>();

    final var list = this.itemRegistryService.getAllRegisteredItems().stream()
      .filter(this::matchesSearch)
      .toList();

    for (final var registered : list) {
      rs.add(new AbstractItem() {
        @Override
        public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
          return new ItemBuilder(registered.item()).toGuiItem();
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
          player.getInventory().addItem(registered.item());
        }
      });
    }

    return rs;
  }

}
