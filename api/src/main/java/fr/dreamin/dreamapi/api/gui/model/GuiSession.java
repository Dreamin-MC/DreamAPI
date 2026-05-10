package fr.dreamin.dreamapi.api.gui.model;

import lombok.Getter;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents one GUI viewing session for a player.
 */
@Getter
public final class GuiSession {

  private final @NotNull UUID playerId;
  private final @NotNull String playerName;
  private final @NotNull String guiName;
  private final @NotNull String guiClass;
  private final @NotNull GuiInterface gui;
  private final long openedAtMillis;
  private @Nullable Long closedAtMillis;
  private @Nullable Reason closeReason;

  public GuiSession(final @NotNull UUID playerId, final @NotNull String playerName, final @NotNull String guiName, final @NotNull String guiClass, final @NotNull GuiInterface gui) {
    this.playerId = playerId;
    this.playerName = playerName;
    this.guiName = guiName;
    this.guiClass = guiClass;
    this.gui = gui;
    this.openedAtMillis = System.currentTimeMillis();
  }

  public void close(@Nullable Reason reason) {
    this.closedAtMillis = System.currentTimeMillis();
    this.closeReason = reason;
  }
}

