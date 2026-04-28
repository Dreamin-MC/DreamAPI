package fr.dreamin.dreamapi.api.nms.tablist.service;

/**
 * Player tab-list visibility modes.
 */
public enum TabListMode {

  /**
   * Default behavior: vanilla tab-list packets are displayed.
   */
  VISIBLE,

  /**
   * Keeps the tab-list entries empty while preserving normal UI.
   */
  EMPTY,

  /**
   * Attempts to hide the tab-list UI by combining EMPTY mode with blank header/footer.
   */
  HIDDEN
}

