package fr.dreamin.dreamapi.core.gui;

import fr.dreamin.dreamapi.core.gui.handler.AbstractGuiHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.window.Window;

/**
 * Helper class for opening GUIs with support for InvUI 2.1.0 features.
 *
 * Supported features:
 * - Cursor visualizers
 * - Inventory visualizers
 * - Bundle select handlers
 *
 * @since 0.4.0
 */
public final class GuiInterfaceHelper {

  private GuiInterfaceHelper() {}

  /**
   * Opens a GUI with support for advanced visualizers and handlers.
   *
   * @param gui GUI interface to open
   * @param player target player
   */
  public static void openAdvanced(
    @NotNull GuiInterface gui,
    @NotNull Player player
  ) {
    var windowBuilder = Window.builder()
      .setViewer(player)
      .setUpperGui(gui.guiUpper(player))
      .setTitle(gui.name(player))
      .setCloseable(gui.closable(player));

    // Apply advanced customizations when using AbstractGuiHandler
    if (gui instanceof AbstractGuiHandler handler) {
      // ========== CURSOR VISUALIZER ==========
      var cursorViz = handler.getCursorVisualizer();
      if (cursorViz != null) {
        windowBuilder.setCursorVisualizer(cursorViz);
      }

      // ========== INVENTORY VISUALIZER ==========
      // Inventory visualizer is applied on InventoryLink slot elements,
      // and is usually configured where those links are created.

      // ========== BUNDLE SELECT HANDLERS ==========
      // Attach bundle select handlers to the opened GUI
      var bundleHandlers = handler.getBundleSelectHandlers();
      if (!bundleHandlers.isEmpty()) {
        var upperGui = gui.guiUpper(player);
        upperGui.addBundleSelectHandler(handler::onBundleSelect);
      }
    }

    var window = windowBuilder.build();
    window.open();
  }

  /**
   * Opens a GUI using the simple default behavior.
   *
   * @param gui GUI interface to open
   * @param player target player
   */
  public static void openSimple(
    @NotNull GuiInterface gui,
    @NotNull Player player
  ) {
    gui.open(player);
  }

  /**
   * Creates a partially configured window builder for a GUI.
   *
   * @param gui GUI interface
   * @param player target player
   * @return preconfigured window builder
   */
  public static Window.Builder<Window, ?> buildWindow(
    @NotNull GuiInterface gui,
    @NotNull Player player
  ) {
    return Window.builder()
      .setViewer(player)
      .setUpperGui(gui.guiUpper(player))
      .setTitle(gui.name(player))
      .setCloseable(gui.closable(player));
  }

  /**
   * Applies inventory visualizer behavior if provided by the handler.
   *
   * @param gui gui instance to configure
   * @param handler handler containing visualizer configuration
   */
  public static void applyInventoryVisualizer(
    @NotNull Gui gui,
    @NotNull AbstractGuiHandler handler
  ) {
    var visualizer = handler.getInventoryVisualizer();
    if (visualizer == null) return;

    // This depends on how InventoryLink elements are built in each GUI.
    // Keep this method as integration point for future centralized wiring.
  }

  /**
   * Fluent helper for configuring and opening windows.
   */
  public static class FluentWindowBuilder {

    private final @NotNull Player player;
    private final Window.Builder<Window, ?> builder;

    public FluentWindowBuilder(
      @NotNull GuiInterface gui,
      @NotNull Player player
    ) {
      this.player = player;
      this.builder = buildWindow(gui, player);
    }

    public FluentWindowBuilder withTitle(@NotNull Component title) {
      this.builder.setTitle(title);
      return this;
    }

    public FluentWindowBuilder closeable(boolean closeable) {
      this.builder.setCloseable(closeable);
      return this;
    }

    public FluentWindowBuilder withFallback(@NotNull GuiInterface fallback) {
      this.builder.setFallbackWindow(() -> {
        fallback.open(player);
        return null;
      });
      return this;
    }

    public void open() {
      var window = this.builder.build();
      window.open();
    }

    public void build() {
      this.builder.build().open();
    }
  }

  /**
   * Creates a fluent builder for elegant window configuration.
   */
  public static FluentWindowBuilder fluentBuilder(
    @NotNull GuiInterface gui,
    @NotNull Player player
  ) {
    return new FluentWindowBuilder(gui, player);
  }
}

