package fr.dreamin.dreamapi.core.gui.item;

import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.function.BiConsumer;

/**
 * Specialized item for handling bundle selections.
 *
 * Bundles (Paper 1.21+) allow storing multiple items.
 * This item handles bundle interactions smoothly.
 *
 * @since 0.4.0
 */
@RequiredArgsConstructor
public class GuiBundleItem extends AbstractItem {

  // The bundle stack (may contain multiple items)
  private final ItemStack bundleStack;

  // Callback called when a specific slot within the bundle is selected
  // Arguments: (player, index_in_bundle)
  private final @Nullable BiConsumer<Player, Integer> onBundleSlotSelected;

  /**
   * Creates a GuiBundleItem with only the bundle.
   *
   * @param bundleStack the bundle item stack
   */
  public GuiBundleItem(@NotNull ItemStack bundleStack) {
    this(bundleStack, null);
  }

  @Override
  public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
    // Wrap the bundle in an ItemBuilder for display
    return new ItemBuilder(bundleStack).toGuiItem();
  }

  @Override
  public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
    // Normal click on bundle is handled here.
    // Selection of specific slots is handled via GUI's onBundleSelect() method.
    // InvUI automatically calls the bundle handler.
  }

  /**
   * Called when a specific slot within the bundle is selected.
   *
   * @param player the player who clicked
   * @param slotInBundle the index of the slot within the bundle
   */
  public void onBundleSlotSelect(@NotNull Player player, int slotInBundle) {
    if (onBundleSlotSelected != null) {
      try {
        onBundleSlotSelected.accept(player, slotInBundle);
      } catch (Exception e) {
        player.sendMessage("Error processing bundle: " + e.getMessage());
      }
    }
  }

  /**
   * Returns the original bundle stack.
   */
  public ItemStack getBundleStack() {
    return bundleStack;
  }

  /**
   * Helper: creates a GuiBundleItem with a callback.
   */
  public static GuiBundleItem withCallback(
    @NotNull ItemStack bundle,
    @NotNull BiConsumer<Player, Integer> callback
  ) {
    return new GuiBundleItem(bundle, callback);
  }
}


