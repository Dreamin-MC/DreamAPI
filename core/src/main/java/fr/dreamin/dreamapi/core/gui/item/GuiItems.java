package fr.dreamin.dreamapi.core.gui.item;

import fr.dreamin.dreamapi.core.gui.GuiInterface;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.item.BoundItem;

public final class GuiItems {

  // ###############################################################
  // -------------------------- NEXT ITEM --------------------------
  // ###############################################################

  public static BoundItem.Builder NEXT() {
    return NEXT(Material.AIR, null, true);
  }

  public static BoundItem.Builder NEXT(final @NotNull String translationKey) {
    return NEXT(Material.AIR, translationKey, true);
  }

  public static BoundItem.Builder NEXT(final @NotNull String translationKey, final boolean hideToolTip) {
    return NEXT(Material.AIR, translationKey, hideToolTip);
  }

  public static BoundItem.Builder NEXT(final boolean hideToolTip) {
    return NEXT(Material.AIR, null, hideToolTip);
  }

  public static BoundItem.Builder NEXT(final @NotNull Material material) {
    return NEXT(material, null, true);
  }

  public static BoundItem.Builder NEXT(final @NotNull Material material, final boolean hideToolTip) {
    return NEXT(material, null, hideToolTip);
  }

  public static BoundItem.Builder NEXT(final @NotNull Material material, final @NotNull String translationKey) {
    return NEXT(material, translationKey, true);
  }

  public static BoundItem.Builder NEXT(final @NotNull Material material, final @Nullable String translationKey, final boolean hideToolTip) {
    return BoundItem.pagedBuilder()
      .setItemProvider((player, gui) -> {
        if (gui.getPage() > 0)
          return new ItemBuilder(Material.ARROW).setName(
            translationKey != null
            ? Component.translatable(translationKey)
            : Component.text("Suivant")
          ).toGuiItem();
        return new ItemBuilder(material).setHideToolType(material != Material.AIR && hideToolTip).toGuiItem();
      })
      .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() + 1));
  }

  // ###############################################################
  // ------------------------ PREVIOUS ITEM ------------------------
  // ###############################################################

  public static BoundItem.Builder PREVIOUS() {
    return PREVIOUS(Material.AIR, null, true);
  }

  public static BoundItem.Builder PREVIOUS(final @NotNull String translationKey) {
    return PREVIOUS(Material.AIR, translationKey, true);
  }

  public static BoundItem.Builder PREVIOUS(final @NotNull String translationKey, final boolean hideToolTip) {
    return PREVIOUS(Material.AIR, translationKey, hideToolTip);
  }

  public static BoundItem.Builder PREVIOUS(final boolean hideToolTip) {
    return PREVIOUS(Material.AIR, null, hideToolTip);
  }

  public static BoundItem.Builder PREVIOUS(final @NotNull Material material) {
    return PREVIOUS(material, null, true);
  }

  public static BoundItem.Builder PREVIOUS(final @NotNull Material material, final boolean hideToolTip) {
    return PREVIOUS(material, null, hideToolTip);
  }

  public static BoundItem.Builder PREVIOUS(final @NotNull Material material, final @NotNull String translationKey) {
    return PREVIOUS(material, translationKey, true);
  }

  public static BoundItem.Builder PREVIOUS(final @NotNull Material material, final @Nullable String translationKey, final boolean hideToolTip) {
    return BoundItem.pagedBuilder()
      .setItemProvider((player, gui) -> {
        if (gui.getPage() < gui.getPageCount() - 1)
          return new ItemBuilder(Material.ARROW).setName(
            translationKey != null
              ? Component.translatable(translationKey)
              : Component.text("Suivant")
          ).toGuiItem();
        return new ItemBuilder(material).setHideToolType(material != Material.AIR && hideToolTip).toGuiItem();
      })
      .addClickHandler((item, gui, click) -> gui.setPage(gui.getPage() - 1));
  }

  // ###############################################################
  // ------------------------- BACK ITEM ---------------------------
  // ###############################################################

  public static BoundItem.Builder BACK(final @NotNull GuiInterface backGUI) {
    return BACK(backGUI, Material.OAK_DOOR, null, true);
  }

  public static BoundItem.Builder BACK(final @NotNull GuiInterface backGUI, final @NotNull String translationKey) {
    return BACK(backGUI, Material.OAK_DOOR, translationKey, true);
  }

  public static BoundItem.Builder BACK(final @NotNull GuiInterface backGUI, final @NotNull Material material) {
    return BACK(backGUI, material, null, true);
  }

  public static BoundItem.Builder BACK(final @NotNull GuiInterface backGUI, final @NotNull Material material, final @NotNull String translationKey) {
    return BACK(backGUI, material, translationKey, true);
  }

  public static BoundItem.Builder BACK(final @NotNull GuiInterface backGUI, final boolean hideToolTip) {
    return BACK(backGUI, Material.OAK_DOOR, null, hideToolTip);
  }

  public static BoundItem.Builder BACK(final @NotNull GuiInterface backGUI, final @NotNull String translationKey, final boolean hideToolTip) {
    return BACK(backGUI, Material.OAK_DOOR, translationKey, hideToolTip);
  }

  public static BoundItem.Builder BACK(final @NotNull GuiInterface backGUI, final @NotNull Material material, final @Nullable String translationKey, final boolean hideToolTip) {
    return BoundItem.builder()
      .setItemProvider((player, gui) -> new ItemBuilder(material)
        .setName(
          translationKey != null
            ? Component.translatable(translationKey)
            : Component.text("Retour en arrière")
        )
        .setHideToolType(material != Material.AIR && hideToolTip)
        .toGuiItem())
      .addClickHandler((item, gui, click) -> backGUI.open(click.player()));

  }

}
