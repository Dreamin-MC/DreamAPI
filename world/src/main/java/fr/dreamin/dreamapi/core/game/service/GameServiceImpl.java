package fr.dreamin.dreamapi.core.game.service;

import fr.dreamin.dreamapi.api.game.service.GameService;
import fr.dreamin.dreamapi.api.game.model.GameState;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@DreamAutoService(value = GameService.class)
@RequiredArgsConstructor
public class GameServiceImpl implements GameService, DreamService {

  private final Plugin plugin;
  private GameState currentState;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void switchState(@NotNull GameState newState) {
    final var previous = this.currentState;
    if (previous != null) {
      previous.exit(newState);
      previous.getListeners().forEach(HandlerList::unregisterAll);
    }

    this.currentState = newState;
    newState.enter(previous);

    for (final var listener : newState.getListeners()) {
      Bukkit.getPluginManager().registerEvents(listener, this.plugin);
    }

  }

  @Override
  public @Nullable GameState getCurrentState() {
    return this.currentState;
  }

  @Override
  public void tick(int currentTick) {
    if (this.currentState != null)
      this.currentState.tick(currentTick);
  }
}
