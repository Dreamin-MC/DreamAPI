package fr.dreamin.dreamapi.core.gui.template;

import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Template builder for creating paged and filterable GUIs.
 *
 * Common use cases:
 * - GUIs displaying item lists (services, players, resources)
 * - GUIs with real-time search/filtering
 * - Paged GUIs with Previous/Next navigation
 *
 * @param <T> type of entries to display
 * @since 0.4.0
 */
public class FilterablePagedGuiBuilder<T> {

  private String queryString = "";
  private List<T> allItems = new ArrayList<>();
  private Predicate<T> filterPredicate = item -> true;
  private Function<T, Item> itemDisplay;
  private Consumer<String> onQueryChange;
  private boolean showPagination = true;

  // ==================== SETTERS ====================

  /**
   * Sets the full list of items to display.
   */
  public FilterablePagedGuiBuilder<T> setItems(@NotNull List<T> items) {
    this.allItems = new ArrayList<>(items);
    return this;
  }

  /**
   * Adds one item to the source list.
   */
  public FilterablePagedGuiBuilder<T> addItem(@NotNull T item) {
    this.allItems.add(item);
    return this;
  }

  /**
   * Sets the filter predicate (default: accept all).
   *
   * @param predicate filter applied to each item
   * @return this for fluent chaining
   */
  public FilterablePagedGuiBuilder<T> setFilter(@NotNull Predicate<T> predicate) {
    this.filterPredicate = predicate;
    return this;
  }

  /**
   * Sets how each item is rendered in the GUI.
   *
   * @param builder function mapping T -> Item
   * @return this for fluent chaining
   */
  public FilterablePagedGuiBuilder<T> setItemDisplay(@NotNull Function<T, Item> builder) {
    this.itemDisplay = builder;
    return this;
  }

  /**
   * Sets a callback triggered when the query changes.
   */
  public FilterablePagedGuiBuilder<T> onQueryChange(@NotNull Consumer<String> callback) {
    this.onQueryChange = callback;
    return this;
  }

  /**
   * Sets the current search query.
   */
  public FilterablePagedGuiBuilder<T> setQuery(@NotNull String query) {
    this.queryString = query;
    if (onQueryChange != null) {
      onQueryChange.accept(query);
    }
    return this;
  }

  /**
   * Shows or hides pagination controls (Previous/Next).
   */
  public FilterablePagedGuiBuilder<T> showPagination(boolean show) {
    this.showPagination = show;
    return this;
  }

  // ==================== BUILD ====================

  /**
   * Builds a paged GUI with filtered content.
   *
   * @return ready-to-use paged GUI
   */
  public PagedGui<Item> build() {
    if (itemDisplay == null) {
      throw new IllegalStateException("itemDisplay must be set before building");
    }

    // Apply filtering and map to InvUI items
    var filtered = allItems.stream()
      .filter(filterPredicate)
      .map(itemDisplay)
      .toList();

    var builder = PagedGui.itemsBuilder()
      .setStructure(
        ". X X X X X X X .",
        ". X X X X X X X .",
        ". X X X X X X X .",
        (showPagination ? "P . . . . . . . N" : ". . . . . . . . .")
      )
      .addIngredient('X', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
      .setContent(filtered);

    return builder.build();
  }

  /**
   * Builds a paged GUI intended for dynamic content refresh.
   *
   * @return paged GUI that can be refreshed with setContent()
   */
  public PagedGui<Item> buildDynamic() {
    if (itemDisplay == null) {
      throw new IllegalStateException("itemDisplay must be set before building");
    }

    return build();
  }

  // ==================== HELPERS ====================

  /**
   * Adds case-insensitive substring filtering based on a string extractor.
   *
   * @param getter function extracting the searchable text
   * @return this for fluent chaining
   */
  public FilterablePagedGuiBuilder<T> withStringSearch(
    @NotNull Function<T, String> getter
  ) {
    this.filterPredicate = item -> getter.apply(item)
      .toLowerCase()
      .contains(queryString.toLowerCase());
    return this;
  }

  /**
   * Returns currently filtered source items (useful for debug/logging).
   */
  public List<T> getFilteredItems() {
    return allItems.stream()
      .filter(filterPredicate)
      .toList();
  }

  // ==================== CALLBACK ====================

  @FunctionalInterface
  public interface Consumer<T> {
    void accept(T value);
  }
}
