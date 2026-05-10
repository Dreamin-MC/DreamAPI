package fr.dreamin.dreamapi.core.gui.handler;

import fr.dreamin.dreamapi.core.gui.GuiInterface;
import fr.dreamin.dreamapi.core.gui.GuiInterfaceHelper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Abstract handler for building GUIs with full support of reactive handlers
 * (bundle select, cursor visualization, inventory visualization, etc.)
 *
 * @since 0.4.0
 */
public abstract class AbstractGuiHandler implements GuiInterface {

  protected final List<BiConsumer<Player, BundleSelectEvent>> bundleSelectHandlers = new ArrayList<>();
  protected @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> cursorVisualizer;
  protected @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> inventoryVisualizer;

  // ==================== BUNDLE SELECT HANDLERS ====================

  /**
   * Adds a handler called when a player selects an item from a bundle.
   *
   * @param handler the handler to add
   * @return this for fluent chaining
   */
  public AbstractGuiHandler addBundleSelectHandler(
    @NotNull BiConsumer<Player, BundleSelectEvent> handler
  ) {
    this.bundleSelectHandlers.add(handler);
    return this;
  }

  /**
   * Removes a bundle select handler.
   *
   * @param handler the handler to remove
   * @return this for fluent chaining
   */
  public AbstractGuiHandler removeBundleSelectHandler(
    @NotNull BiConsumer<Player, BundleSelectEvent> handler
  ) {
    this.bundleSelectHandlers.remove(handler);
    return this;
  }

  /**
   * Returns all registered handlers.
   */
  public List<BiConsumer<Player, BundleSelectEvent>> getBundleSelectHandlers() {
    return new ArrayList<>(this.bundleSelectHandlers);
  }

  @Override
  public void onBundleSelect(Player player, int guiSlot, int bundleSlot) {
    var event = new BundleSelectEvent(player, guiSlot, bundleSlot);
    for (var handler : this.bundleSelectHandlers) {
      try {
        handler.accept(player, event);
        if (event.cancelled) break;
      } catch (Exception e) {
        // Log or ignore the error
      }
    }
  }

  // ==================== CURSOR VISUALIZER ====================

  /**
   * Sets the cursor visualizer for this GUI.
   * The visualizer transforms the appearance of the ItemStack on the player's cursor.
   *
   * @param visualizer function transforming ItemStack → ItemProvider, or null
   * @return this for fluent chaining
   */
  public AbstractGuiHandler setCursorVisualizer(
    @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> visualizer
  ) {
    this.cursorVisualizer = visualizer;
    return this;
  }

  @Override
  public @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> getCursorVisualizer() {
    return this.cursorVisualizer;
  }

  // ==================== INVENTORY VISUALIZER ====================

  /**
   * Sets the inventory visualizer for this GUI.
   * The visualizer transforms the appearance of ItemStacks displayed in inventory slots.
   *
   * @param visualizer function transforming ItemStack → ItemProvider, or null
   * @return this for fluent chaining
   */
  public AbstractGuiHandler setInventoryVisualizer(
    @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> visualizer
  ) {
    this.inventoryVisualizer = visualizer;
    return this;
  }

  @Override
  public @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> getInventoryVisualizer() {
    return this.inventoryVisualizer;
  }

  @Override
  public void open(@NotNull Player player) {
    GuiInterfaceHelper.openAdvanced(this, player);
  }

  // ==================== EVENT CLASS ====================

  /**
   * Event triggered when a player selects an item from a bundle.
   */
  @Getter @Setter
  public static class BundleSelectEvent {
    // The player who performed the selection
    public final Player player;
    // The index of the GUI slot containing the bundle
    public final int guiSlot;
    // The index of the selected item within the bundle
    public final int bundleSlot;
    // If true, cancels propagation to subsequent handlers
    public boolean cancelled = false;

    public BundleSelectEvent(Player player, int guiSlot, int bundleSlot) {
      this.player = player;
      this.guiSlot = guiSlot;
      this.bundleSlot = bundleSlot;
    }

    public void cancel() {
      this.cancelled = true;
    }

  }
}
