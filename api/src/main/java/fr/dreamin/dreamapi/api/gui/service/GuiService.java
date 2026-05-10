package fr.dreamin.dreamapi.api.gui.service;

import fr.dreamin.dreamapi.api.gui.model.GuiInterface;
import fr.dreamin.dreamapi.api.gui.model.GuiSession;
import fr.dreamin.dreamapi.api.services.DreamService;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Service that tracks GUI viewers and their navigation history.
 */
public interface GuiService extends DreamService {

  void recordOpen(@NotNull Player player, @NotNull GuiInterface gui);

  void recordClose(@NotNull Player player, @NotNull GuiInterface gui, @Nullable org.bukkit.event.inventory.InventoryCloseEvent.Reason reason);

  @Nullable GuiSession getCurrentSession(@NotNull UUID playerId);

  default @Nullable GuiSession getCurrentSession(@NotNull Player player) {
    return getCurrentSession(player.getUniqueId());
  }

  @NotNull List<GuiSession> getHistory(@NotNull UUID playerId);

  default @NotNull List<GuiSession> getHistory(@NotNull Player player) {
    return getHistory(player.getUniqueId());
  }

  @Nullable GuiInterface getPreviousGui(@NotNull UUID playerId);

  default @Nullable GuiInterface getPreviousGui(@NotNull Player player) {
    return getPreviousGui(player.getUniqueId());
  }

  void clear(@NotNull UUID playerId);

  default void clear(@NotNull Player player) {
    clear(player.getUniqueId());
  }
}

