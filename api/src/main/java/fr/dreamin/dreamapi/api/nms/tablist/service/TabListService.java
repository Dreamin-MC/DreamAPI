package fr.dreamin.dreamapi.api.nms.tablist.service;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Controls tab-list visibility per viewer.
 */
public interface TabListService {

  /**
   * Sets the tab-list mode for a player.
   */
  void setMode(final @NotNull Player player, final @NotNull TabListMode mode);

  /**
   * Returns the currently active mode for a player.
   */
  @NotNull TabListMode getMode(final @NotNull Player player);

  /**
   * Clears a player's custom mode override and falls back to the default mode.
   */
  void clearMode(final @NotNull Player player);

  /**
   * Returns true if this player has an explicit custom mode.
   */
  boolean hasCustomMode(final @NotNull Player player);

  /**
   * Forces a reapply/resync of the current tab-list state for this player.
   */
  void refresh(final @NotNull Player player);

  /**
   * Returns the default mode used when no custom mode exists for the player.
   */
  @NotNull TabListMode getDefaultMode();

  /**
   * Sets the default mode used when no custom mode exists for the player.
   */
  void setDefaultMode(final @NotNull TabListMode mode);

  /**
   * Returns whether automatic application of default mode is enabled on join.
   */
  boolean isAutoEnabled();

  /**
   * Enables or disables automatic application of default mode on join.
   */
  void setAutoEnabled(final boolean enabled);

  /**
   * Convenience method: empties the tab-list entries.
   */
  default void setEmpty(final @NotNull Player player) {
    setMode(player, TabListMode.EMPTY);
  }

  /**
   * Convenience method: empties entries and hides header/footer.
   */
  default void setHidden(final @NotNull Player player) {
    setMode(player, TabListMode.HIDDEN);
  }

  /**
   * Convenience method: restores vanilla behavior.
   */
  default void reset(final @NotNull Player player) {
    clearMode(player);
  }
}


