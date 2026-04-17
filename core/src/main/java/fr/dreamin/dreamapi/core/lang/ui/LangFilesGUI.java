package fr.dreamin.dreamapi.core.lang.ui;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.item.RegisteredItem;
import fr.dreamin.dreamapi.api.lang.model.LangFile;
import fr.dreamin.dreamapi.api.lang.service.LangService;
import fr.dreamin.dreamapi.core.gui.GuiInterface;
import fr.dreamin.dreamapi.core.gui.item.NextItem;
import fr.dreamin.dreamapi.core.gui.item.PreviousItem;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import fr.dreamin.dreamapi.core.item.ui.ItemRegistryGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.checkerframework.checker.units.qual.C;
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

public final class LangFilesGUI implements GuiInterface {

  private final @NotNull LangService langService = DreamAPI.getAPI().getService(LangService.class);

  private @NotNull String search;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public LangFilesGUI() {
    this.search = "";
  }

  public LangFilesGUI(final @NotNull String search) {
    this.search = search;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public Component name(@NotNull Player player) {
    return Component.text("Lang Files");
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

  private boolean matchesSearch(final @NotNull String langFile) {
    if (this.search.isBlank()) return true;
    return langFile.toLowerCase().contains(this.search.toLowerCase());
  }

  private List<Item> getItems() {
    final var rs = new ArrayList<Item>();

    final var list = this.langService.getLangFiles().keySet().stream()
      .filter(this::matchesSearch)
      .toList();

    for (final var lang : list) {

      final var langFile = this.langService.getLangFile(lang).orElseThrow();

      final int keyCount = langFile.keys == null ? 0 : langFile.keys.size();
      final long translationCount = langFile.keys == null
        ? 0
        : langFile.keys.stream()
          .filter(java.util.Objects::nonNull)
          .mapToLong(k -> k.lang == null ? 0 : k.lang.size())
          .sum();

      rs.add(
        Item.builder()
          .setItemProvider(new ItemBuilder(Material.BOOK)
            .setName(Component.text(lang))
            .setLore(
              List.of(
                Component.empty(),
                Component.text("Lang File Info: " + lang, NamedTextColor.GOLD),
                Component.empty(),
                Component.text("Namespace: ", NamedTextColor.DARK_GRAY)
                  .append(Component.text(langFile.namespace, NamedTextColor.WHITE)),
                Component.empty(),
                Component.text("Value: ", NamedTextColor.DARK_GRAY)
                  .append(Component.text(langFile.value, NamedTextColor.WHITE)),
                Component.empty(),
                Component.text("Default locale: ", NamedTextColor.DARK_GRAY)
                  .append(Component.text(String.valueOf(langFile.defaultLocale), NamedTextColor.WHITE)),
                Component.empty(),
                Component.text("Keys: ", NamedTextColor.DARK_GRAY)
                  .append(Component.text(String.valueOf(keyCount), NamedTextColor.WHITE)),
                Component.empty(),
                Component.text("Translations: ", NamedTextColor.DARK_GRAY)
                  .append(Component.text(String.valueOf(translationCount), NamedTextColor.WHITE))
              )
            )
            .toGuiItem()
          )
          .addClickHandler((item, click) -> {
            new LangFileGUI(langFile).open(click.player());
          })
          .build()
      );
    }

    return rs;
  }

}
