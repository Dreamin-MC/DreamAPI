package fr.dreamin.dreamapi.core.gui;

import fr.dreamin.dreamapi.core.gui.handler.AbstractGuiHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.Window;

import java.util.function.Function;

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
    final var upperGui = gui.guiUpper(player);

    if (gui instanceof AbstractGuiHandler handler) {
      applyInventoryVisualizer(upperGui, handler);

      if (!handler.getBundleSelectHandlers().isEmpty())
        upperGui.addBundleSelectHandler(handler::onBundleSelect);
    }

    var windowBuilder = Window.builder()
      .setViewer(player)
      .setUpperGui(upperGui)
      .setTitle(gui.name(player))
      .setCloseable(gui.closable(player));

    if (gui instanceof AbstractGuiHandler handler) {
      var cursorViz = handler.getCursorVisualizer();
      if (cursorViz != null) {
        windowBuilder.setCursorVisualizer(cursorViz);
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
   * Applies inventory visualizer behavior to all InventoryLink slot elements.
   *
   * @param gui gui instance to configure
   * @param handler handler containing visualizer configuration
   */
  public static void applyInventoryVisualizer(
    @NotNull Gui gui,
    @NotNull AbstractGuiHandler handler
  ) {
    final var visualizer = handler.getInventoryVisualizer();
    if (visualizer == null) return;

    final var slotElements = gui.getSlotElements();

    for (int i = 0; i < slotElements.length; i++) {
      final var slotElement = slotElements[i];
      if (!(slotElement instanceof SlotElement.InventoryLink link))
        continue;

      Function<@Nullable ItemStack, @Nullable ItemProvider> mergedVisualizer = stack -> {
        final var linkVisualizer = link.visualizer();
        final var linkResult = linkVisualizer.apply(stack);
        if (linkResult != null)
          return linkResult;
        return visualizer.apply(stack);
      };

      gui.setSlotElement(
        i,
        new SlotElement.InventoryLink(link.inventory(), link.slot(), link.backgroundProperty(), mergedVisualizer)
      );
    }
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

    public FluentWindowBuilder withTitle(@NotNull net.kyori.adventure.text.Component title) {
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

