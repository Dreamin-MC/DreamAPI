package fr.dreamin.dreamapi.core.gui.example;

import fr.dreamin.dreamapi.core.gui.GuiInterface;
import fr.dreamin.dreamapi.core.gui.handler.AbstractGuiHandler;
import fr.dreamin.dreamapi.core.gui.item.BackItem;
import fr.dreamin.dreamapi.core.gui.visualizer.InventoryVisualizerHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.inventory.Inventory;

/**
 * Example implementation: inventory browser GUI with visualizers.
 *
 * This GUI demonstrates:
 * - Extending AbstractGuiHandler for handler support
 * - Using inventory visualizers
 * - Using bundle select handlers
 * - Integrating InventoryLink slot elements
 *
 * @since 0.4.0
 */
public class InventoryBrowserGUI extends AbstractGuiHandler {

  private final Inventory inventory;
  private final GuiInterface backGUI;

  public InventoryBrowserGUI(
    @NotNull Inventory inventory,
    @NotNull GuiInterface backGUI
  ) {
    this.inventory = inventory;
    this.backGUI = backGUI;

    // Configure inventory visualizer for empty slots
    super.setInventoryVisualizer(InventoryVisualizerHelper.emptyGlassPane(Material.LIGHT_GRAY_STAINED_GLASS_PANE));

    // Add handler for bundle selection events
    super.addBundleSelectHandler((player, event) -> {
      player.sendMessage(
        Component.text("Bundle slot ", NamedTextColor.GRAY)
          .append(Component.text(event.bundleSlot, NamedTextColor.GOLD))
          .append(Component.text(" selected!", NamedTextColor.GRAY))
      );
    });
  }

  @Override
  public Component name(@NotNull Player player) {
    return Component.text("Inventory Browser", NamedTextColor.AQUA);
  }

  @Override
  public boolean closable(@NotNull Player player) {
    return true;
  }

  @Override
  public Gui guiUpper(@NotNull Player player) {
    return Gui.builder()
      .setStructure(
        ". . . . . . . . .",
        ". I I I I I I I .",
        ". I I I I I I I .",
        ". I I I I I I I .",
        ". . . B . . . . ."
      )
      .addIngredient('I', new SlotElement.InventoryLink(inventory, 0))
      .addIngredient('B', new BackItem(backGUI))
      .build();
  }

  // ==================== STATIC FACTORY ====================

  /**
   * Creates an InventoryBrowserGUI for browsing an inventory.
   */
  public static InventoryBrowserGUI browse(
    @NotNull Inventory inventory,
    @NotNull GuiInterface backGUI
  ) {
    return new InventoryBrowserGUI(inventory, backGUI);
  }

  /**
   * Creates an InventoryBrowserGUI builder with extended options.
   */
  public static Builder builder() {
    return new Builder();
  }

  // ==================== BUILDER ====================

  public static class Builder {

    private Inventory inventory;
    private GuiInterface backGUI;
    private boolean showEmptyPlaceholders = true;
    private Material placeholderMaterial = Material.LIGHT_GRAY_STAINED_GLASS_PANE;

    public Builder inventory(@NotNull Inventory inventory) {
      this.inventory = inventory;
      return this;
    }

    public Builder backButton(@NotNull GuiInterface backGUI) {
      this.backGUI = backGUI;
      return this;
    }

    public Builder emptyPlaceholders(boolean show) {
      this.showEmptyPlaceholders = show;
      return this;
    }

    public Builder placeholderMaterial(@NotNull Material material) {
      this.placeholderMaterial = material;
      return this;
    }

    public InventoryBrowserGUI build() {
      if (inventory == null) {
        throw new IllegalStateException("inventory must be set");
      }
      if (backGUI == null) {
        throw new IllegalStateException("backGUI must be set");
      }

      var gui = new InventoryBrowserGUI(inventory, backGUI);

      if (showEmptyPlaceholders) {
        gui.setInventoryVisualizer(InventoryVisualizerHelper.emptyGlassPane(placeholderMaterial));
      }

      return gui;
    }
  }
}
