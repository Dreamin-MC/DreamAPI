package fr.dreamin.dreamapi.core.gui.visualizer;

import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.function.Function;

/**
 * Utilities for creating inventory visualizers (transforming ItemStack rendering).
 *
 * Visualizers change how an item is displayed in a slot
 * without modifying the real item stored in the inventory.
 *
 * @since 0.3.3
 */
public final class InventoryVisualizerHelper {

  /**
   * Creates a visualizer that adds enchantment glint to displayed items.
   *
   * @return visualizer applying glint to all displayed items
   */
  public static Function<@Nullable ItemStack, @Nullable ItemProvider> glintVisualizer() {
    return stack -> {
      if (stack == null || stack.isEmpty()) return null;
      return new ItemBuilder(stack).setEnchantGlint(true).toGuiItem();
    };
  }

  /**
   * Creates a visualizer that replaces empty items with a visual placeholder.
   *
   * @param placeholder material used for empty slots
   * @return visualizer showing placeholders for empty stacks
   */
  public static Function<@Nullable ItemStack, @Nullable ItemProvider> emptySlotPlaceholder(
    @NotNull Material placeholder
  ) {
    return stack -> {
      if (stack == null || stack.isEmpty())
        return new ItemBuilder(placeholder).toGuiItem();
      return null;
    };
  }

  /**
   * Creates a visualizer that replaces empty items with colored glass.
   *
   * @param glassColor glass pane material (e.g. Material.LIGHT_GRAY_STAINED_GLASS_PANE)
   * @return visualizer using colored glass for empty slots
   */
  public static Function<@Nullable ItemStack, @Nullable ItemProvider> emptyGlassPane(
    @NotNull Material glassColor
  ) {
    if (!glassColor.toString().contains("GLASS_PANE"))
      throw new IllegalArgumentException("Material must be a glass pane type");

    return emptySlotPlaceholder(glassColor);
  }

  /**
   * Creates a conditional visualizer that returns a placeholder when condition is true.
   *
   * @param condition condition evaluated for each ItemStack
   * @param placeholder placeholder factory when condition matches
   * @return conditional visualizer
   */
  public static Function<@Nullable ItemStack, @Nullable ItemProvider> conditionalVisualizer(
    @NotNull Function<@Nullable ItemStack, Boolean> condition,
    @NotNull Function<@Nullable ItemStack, @NotNull ItemStack> placeholder
  ) {
    return stack -> {
      if (condition.apply(stack))
        return new ItemBuilder(placeholder.apply(stack)).toGuiItem();
      return null; // Keep normal rendering
    };
  }

  /**
   * Chains multiple visualizers together.
   * The first non-null visualizer result is used.
   *
   * @param visualizers visualizers to combine
   * @return composed visualizer applying visualizers in sequence
   */
  @SafeVarargs
  public static Function<@Nullable ItemStack, @Nullable ItemProvider> chainVisualizers(
    @NotNull Function<@Nullable ItemStack, @Nullable ItemProvider>... visualizers
  ) {
    return stack -> {
      for (var visualizer : visualizers) {
        var result = visualizer.apply(stack);
        if (result != null) return result;
      }
      return null; // No visualizer produced a replacement
    };
  }

  /**
   * Creates a visualizer that adds invisible enchantment to matching items.
   * Useful to visually mark specific items (e.g. boosted items).
   *
   * @param predicate condition deciding whether glint is applied
   * @return visualizer applying conditional glint
   */
  public static Function<@Nullable ItemStack, @Nullable ItemProvider> conditionalGlint(
    @NotNull Function<@Nullable ItemStack, Boolean> predicate
  ) {
    return stack -> {
      if (stack != null && !stack.isEmpty() && predicate.apply(stack))
        return new ItemBuilder(stack).setEnchantGlint(true).toGuiItem();
      return null;
    };
  }

  /**
   * Creates a visualizer that removes enchantment display from items.
   *
   * @return visualizer removing visible enchantments
   */
  public static Function<@Nullable ItemStack, @Nullable ItemProvider> removeEnchantmentGlint() {
    return stack -> {
      if (stack == null || stack.isEmpty()) return null;
      return new ItemBuilder(stack).setEnchantGlint(false).toGuiItem();
    };
  }
}
