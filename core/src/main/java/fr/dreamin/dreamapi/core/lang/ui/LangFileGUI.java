package fr.dreamin.dreamapi.core.lang.ui;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.lang.model.LangEntry;
import fr.dreamin.dreamapi.api.lang.model.LangFile;
import fr.dreamin.dreamapi.api.lang.service.LangService;
import fr.dreamin.dreamapi.core.gui.GuiInterface;
import fr.dreamin.dreamapi.core.gui.item.NextItem;
import fr.dreamin.dreamapi.core.gui.item.PreviousItem;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import fr.dreamin.dreamapi.core.item.ui.ItemRegistryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
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
import xyz.xenondevs.invui.window.AnvilWindow;

import java.util.ArrayList;
import java.util.List;

public final class LangFileGUI implements GuiInterface {

  private final @NotNull LangService langService = DreamAPI.getAPI().getService(LangService.class);

  private final @NotNull LangFile langFile;
  private @NotNull String search;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public LangFileGUI(final @NotNull LangFile langFile) {
    this.langFile = langFile;
    this.search = "";
  }

  public LangFileGUI(final @NotNull LangFile langFile, final @NotNull String search) {
    this.langFile = langFile;
    this.search = search;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public Component name(@NotNull Player player) {
    return Component.text("Lang File");
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

  private boolean matchesSearch(final @NotNull LangEntry langEntry) {
    if (this.search.isBlank()) return true;
    return langEntry.key.toLowerCase().contains(this.search.toLowerCase());
  }

  private List<Item> getItems() {
    final var rs = new ArrayList<Item>();

    final var list = this.langFile.keys.stream()
      .filter(this::matchesSearch)
      .toList();

    for (final var langEntry : list) {

      final long translationCount = langEntry.lang.stream()
        .mapToLong(l -> l == null ? 0 : 1)
        .sum();

      final var lore = new ArrayList<Component>(List.of(
        Component.empty(),
        Component.text("Lang Entry Info: " + langEntry, NamedTextColor.GOLD),
        Component.empty(),
        Component.text("Translations: ", NamedTextColor.DARK_GRAY)
          .append(Component.text(String.valueOf(translationCount), NamedTextColor.WHITE))
      ));

      for (final var lang : langEntry.lang) {
        lore.addAll(List.of(
          Component.empty(),
          Component.text("Locale: ", NamedTextColor.DARK_GRAY)
            .append(Component.text(lang.value, NamedTextColor.WHITE))
        ));
      }

      rs.add(
        Item.builder()
          .setItemProvider(new ItemBuilder(Material.PAPER)
            .setName(Component.text(langEntry.key))
            .setLore(lore)
            .toGuiItem()
          )
          .addClickHandler((item, click) -> {
            click.player().sendMessage(Component.translatable(langEntry.key));
          })
          .build()
      );
    }

    return rs;
  }

}
