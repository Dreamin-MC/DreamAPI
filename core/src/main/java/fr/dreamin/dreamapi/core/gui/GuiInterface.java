package fr.dreamin.dreamapi.core.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.Window;

import java.util.function.Function;

/**
 * Base interface for all DreamAPI GUI implementations.
 *
 * Supports InvUI 2.1.0 experimental APIs:
 * - Bundle select handlers
 * - Inventory visualizers
 * - Cursor visualizers
 */
public interface GuiInterface {

  // Returns the GUI name/title for this player
  Component name(final @NotNull Player player);

  // Returns whether the GUI can be closed (default: true)
  default boolean closable(final @NotNull Player player) {
    return true;
  }

  // Returns the upper GUI instance for this player
  Gui guiUpper(final @NotNull Player player);

  // Returns a default paged-like GUI layout (override when needed)
  default Gui pagedGui(final @NotNull Player player) {
    return Gui.builder()
      .setStructure(
        ". . . . . . . . . ",
        ". . . . . . . . . ",
        ". . . . . . . . . ",
        ". . . . . . . . . "
      )
      .build();
  }

  // ==================== NEW APIs (InvUI 2.1.0) ====================

  /**
   * @experimental Called when a player selects an item from a bundle.
   * Override to implement custom behavior.
   *
   * @param player player who performed the selection
   * @param guiSlot GUI slot containing the bundle
   * @param bundleSlot selected item index inside the bundle
   * @since 0.4.0
   */
  default void onBundleSelect(
    @NotNull Player player,
    int guiSlot,
    int bundleSlot
  ) {
    // Override if needed
  }

  /**
   * @experimental Returns the cursor visualizer for this GUI.
   * The visualizer transforms the cursor ItemStack rendering.
   *
   * @return function mapping ItemStack -> ItemProvider, or null for default rendering
   * @since 0.4.0
   */
  default @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> getCursorVisualizer() {
    return null;
  }

  /**
   * @experimental Returns the inventory visualizer for this GUI.
   * The visualizer transforms rendering of items shown in inventory slots.
   *
   * @return function mapping ItemStack -> ItemProvider, or null for default rendering
   * @since 0.4.0
   */
  default @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> getInventoryVisualizer() {
    return null;
  }

  // ==================== DEFAULT IMPLEMENTATION ====================

  /**
   * Opens the GUI for a player using the default single-window behavior.
   */
  default void open(final @NotNull Player player) {
    Window.builder()
      .setViewer(player)
      .setUpperGui(guiUpper(player))
      .setTitle(name(player))
      .setCloseable(closable(player))
      .open(player);
  }
}