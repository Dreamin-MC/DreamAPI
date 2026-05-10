package fr.dreamin.dreamapi.core.gui.service;

import fr.dreamin.dreamapi.api.gui.service.GuiService;
import fr.dreamin.dreamapi.api.gui.model.GuiSession;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.gui.model.GuiInterface;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default runtime implementation of the GUI service.
 */
@RequiredArgsConstructor
@DreamAutoService(GuiService.class)
public final class GuiServiceImpl implements GuiService, Listener {

  private final Map<UUID, GuiSession> currentSessions = new ConcurrentHashMap<>();
  private final Map<UUID, Deque<GuiSession>> history = new ConcurrentHashMap<>();

  @Override
  public void recordOpen(@NotNull Player player, @NotNull GuiInterface gui) {
    final var session = new GuiSession(
      player.getUniqueId(),
      player.getName(),
      String.valueOf(gui.name(player)),
      gui.getClass().getName(),
      gui
    );
    this.currentSessions.put(player.getUniqueId(), session);
    this.history.computeIfAbsent(player.getUniqueId(), _ -> new ArrayDeque<>()).addFirst(session);
  }

  @Override
  public void recordClose(@NotNull Player player, @NotNull GuiInterface gui, Reason reason) {
    final var session = currentSessions.remove(player.getUniqueId());
    if (session != null)
      session.close(reason);
  }

  @Override
  public GuiSession getCurrentSession(@NotNull UUID playerId) {
    return this.currentSessions.get(playerId);
  }

  @Override
  public @NotNull List<GuiSession> getHistory(@NotNull UUID playerId) {
    final var deque = this.history.get(playerId);
    if (deque == null) return List.of();
    return List.copyOf(deque);
  }

  @Override
  public GuiInterface getPreviousGui(@NotNull UUID playerId) {
    final var deque = this.history.get(playerId);
    if (deque == null || deque.size() < 2) return null;

    final var iterator = deque.iterator();
    iterator.next(); // current gui session
    return iterator.next().getGui();
  }

  @Override
  public void clear(@NotNull UUID playerId) {
    this.currentSessions.remove(playerId);
    this.history.remove(playerId);
  }

}

