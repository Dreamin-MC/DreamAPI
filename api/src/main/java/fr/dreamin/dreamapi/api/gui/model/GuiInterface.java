package fr.dreamin.dreamapi.api.gui.model;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.gui.event.GuiWindowCloseEvent;
import fr.dreamin.dreamapi.api.gui.event.GuiWindowOpenEvent;
import fr.dreamin.dreamapi.api.gui.service.GuiService;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.ClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Central abstract base class for all DreamAPI GUIs.
 *
 * It directly provides:
 * - GUI opening logic
 * - Inventory visualizers
 * - Cursor visualizers
 * - Open/close Bukkit events
 * - GUI viewer history tracking
 */
public abstract class GuiInterface {

  private final @NotNull GuiService guiService = DreamAPI.getAPI().getService(GuiService.class);

  private @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> cursorVisualizer;
  private @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> inventoryVisualizer;
  private @Nullable Supplier<? extends Component> titleSupplier;
  private final @NotNull List<Runnable> openHandlers = new ArrayList<>();
  private final @NotNull List<Consumer<? super ClickEvent>> outsideClickHandlers = new ArrayList<>();
  private final @NotNull List<Consumer<? super Integer>> windowStateChangeHandlers = new ArrayList<>();
  private @Nullable Supplier<? extends @Nullable Window> fallbackWindowSupplier;
  private final @NotNull List<Consumer<? super Window>> modifiers = new ArrayList<>();

  // Returns the GUI name/title for this player
  public abstract Component name(final @NotNull Player player);

  // Returns whether the GUI can be closed (default: true)
  public boolean closable(final @NotNull Player player) {
    return true;
  }

  // Returns the upper GUI instance for this player
  public abstract Gui guiUpper(final @NotNull Player player);

  // ==================== VISUALIZERS ====================

  public @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> getCursorVisualizer() {
    return cursorVisualizer;
  }

  public GuiInterface setCursorVisualizer(@Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> visualizer) {
    this.cursorVisualizer = visualizer;
    return this;
  }

  public @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> getInventoryVisualizer() {
    return inventoryVisualizer;
  }

  public GuiInterface setInventoryVisualizer(@Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> visualizer) {
    this.inventoryVisualizer = visualizer;
    return this;
  }

  public @Nullable Supplier<? extends Component> getTitleSupplier() {
    return titleSupplier;
  }

  public GuiInterface setTitleSupplier(final @Nullable Supplier<? extends Component> supplier) {
    this.titleSupplier = supplier;
    return this;
  }

  public GuiInterface setTitle(final @Nullable Component title) {
    this.titleSupplier = title == null ? null : () -> title;
    return this;
  }

  public GuiInterface setTitle(final @Nullable String title) {
    this.titleSupplier = title == null ? null : () -> Component.text(title);
    return this;
  }

  public @NotNull List<Runnable> getOpenHandlers() {
    return List.copyOf(openHandlers);
  }

  public GuiInterface setOpenHandlers(final @Nullable List<? extends Runnable> handlers) {
    this.openHandlers.clear();
    if (handlers != null)
      this.openHandlers.addAll(handlers);
    return this;
  }

  public GuiInterface addOpenHandler(final @NotNull Runnable handler) {
    this.openHandlers.add(handler);
    return this;
  }

  public @NotNull List<Consumer<? super ClickEvent>> getOutsideClickHandlers() {
    return List.copyOf(outsideClickHandlers);
  }

  public GuiInterface setOutsideClickHandlers(final @Nullable List<? extends Consumer<? super ClickEvent>> handlers) {
    this.outsideClickHandlers.clear();
    if (handlers != null)
      this.outsideClickHandlers.addAll(handlers);
    return this;
  }

  public GuiInterface addOutsideClickHandler(final @NotNull Consumer<? super ClickEvent> handler) {
    this.outsideClickHandlers.add(handler);
    return this;
  }

  public @NotNull List<Consumer<? super Integer>> getWindowStateChangeHandlers() {
    return List.copyOf(windowStateChangeHandlers);
  }

  public GuiInterface setWindowStateChangeHandlers(final @Nullable List<? extends Consumer<? super Integer>> handlers) {
    this.windowStateChangeHandlers.clear();
    if (handlers != null)
      this.windowStateChangeHandlers.addAll(handlers);
    return this;
  }

  public GuiInterface addWindowStateChangeHandler(final @NotNull Consumer<? super Integer> handler) {
    this.windowStateChangeHandlers.add(handler);
    return this;
  }

  public @Nullable Supplier<? extends @Nullable Window> getFallbackWindowSupplier() {
    return fallbackWindowSupplier;
  }

  public GuiInterface setFallbackWindow(final @Nullable Window fallbackWindow) {
    this.fallbackWindowSupplier = fallbackWindow == null ? null : () -> fallbackWindow;
    return this;
  }

  public GuiInterface setFallbackWindow(final @Nullable Supplier<? extends @Nullable Window> fallbackWindowSupplier) {
    this.fallbackWindowSupplier = fallbackWindowSupplier;
    return this;
  }

  public @NotNull List<Consumer<? super Window>> getModifiers() {
    return List.copyOf(modifiers);
  }

  public GuiInterface setModifiers(final @Nullable List<? extends Consumer<? super Window>> modifiers) {
    this.modifiers.clear();
    if (modifiers != null)
      this.modifiers.addAll(modifiers);
    return this;
  }

  public GuiInterface addModifier(final @NotNull Consumer<? super Window> modifier) {
    this.modifiers.add(modifier);
    return this;
  }

  // ==================== OPEN / CLOSE FLOW ====================

  /**
   * Opens the GUI for a player using the default single-window behavior.
   */
  public void open(final @NotNull Player player) {
    final var window = createWindowBuilder(player).build();
    openWindow(player, window);
  }

  /**
   * Opens a pre-built window while still firing lifecycle events and tracking history.
   */
  protected final void openWindow(final @NotNull Player player, final @NotNull Window window) {
    if (!new GuiWindowOpenEvent(this, player, window).callEvent())
      return;

    window.addCloseHandler(reason -> {
      new GuiWindowCloseEvent(this, player, window, reason).callEvent();

      tryRecordClose(player, reason);
    });

    window.open();
    tryRecordOpen(player);
  }

  /**
   * Builds a window using the default open flow.
   */
  protected final Window.Builder<Window, ?> createWindowBuilder(final @NotNull Player player) {
    final var upperGui = guiUpper(player);
    applyInventoryVisualizer(upperGui);

    final var builder = Window.builder()
      .setViewer(player)
      .setUpperGui(upperGui)
      .setCloseable(closable(player));

    if (titleSupplier != null)
      builder.setTitleSupplier(() -> {
        final var supplied = titleSupplier.get();
        return supplied != null ? supplied : name(player);
      });
    else
      builder.setTitle(name(player));

    if (!openHandlers.isEmpty())
      builder.setOpenHandlers(openHandlers);

    if (!outsideClickHandlers.isEmpty())
      builder.setOutsideClickHandlers(outsideClickHandlers);

    if (!windowStateChangeHandlers.isEmpty())
      builder.setWindowStateChangeHandlers(windowStateChangeHandlers);

    if (fallbackWindowSupplier != null)
      builder.setFallbackWindow(fallbackWindowSupplier);

    if (!modifiers.isEmpty())
      builder.setModifiers(modifiers);

    if (cursorVisualizer != null)
      builder.setCursorVisualizer(cursorVisualizer);

    return builder;
  }


  private void tryRecordOpen(Player player) {
    if (!DreamAPI.isInitialized()) return;
    try {
      this.guiService.recordOpen(player, this);
    } catch (Throwable ignored) {
      // GUI service is optional at runtime
    }
  }

  private void tryRecordClose(Player player, @Nullable Reason reason) {
    if (!DreamAPI.isInitialized()) return;
    try {
      this.guiService.recordClose(player, this, reason);
    } catch (Throwable ignored) {
      // GUI service is optional at runtime
    }
  }

  /**
   * Applies this GUI inventory visualizer to InventoryLink slot elements.
   */
  protected void applyInventoryVisualizer(@NotNull Gui gui) {
    if (inventoryVisualizer == null) return;

    final var slotElements = gui.getSlotElements();

    // Rewrite InventoryLink elements so they use the GUI-level visualizer chain.
    for (int i = 0; i < slotElements.length; i++) {
      final var slotElement = slotElements[i];
      if (!(slotElement instanceof SlotElement.InventoryLink link))
        continue;

      Function<@Nullable ItemStack, @Nullable ItemProvider> mergedVisualizer = stack -> {
        var existing = link.visualizer();
        var existingResult = existing.apply(stack);
        if (existingResult != null) {
          return existingResult;
        }
        return inventoryVisualizer.apply(stack);
      };

      gui.setSlotElement(
        i,
        new SlotElement.InventoryLink(link.inventory(), link.slot(), link.backgroundProperty(), mergedVisualizer)
      );
    }
  }

}