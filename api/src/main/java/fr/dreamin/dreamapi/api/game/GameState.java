package fr.dreamin.dreamapi.api.game;

import fr.dreamin.dreamapi.api.DreamAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class representing a game state.
 * This class provides the structure for entering, exiting, and ticking game states.
 *
 * @author Dreamin
 * @since 1.0.0
 *
 */
@Getter
public abstract class GameState {

  private final List<Listener> listeners = new ArrayList<>();

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * This method is called when the game state is entered.
   * It should contain the logic for entering the game state.
   *
   * @param previousState the previous game state
   */
  public final void enter(final GameState previousState) {
    this.onEnter(previousState);
  }

  /**
   * This method is called when the game state is exited.
   * It should contain the logic for exiting the game state.
   *
   * @param nextState the next game state to transition to
   */
  public final void exit(final GameState nextState) {
    this.onExit(nextState);
    this.listeners.forEach(HandlerList::unregisterAll);
  }

  /**
   * This method is called every tick.
   * It should contain the logic for the game state.
   */
  public abstract void tick(int currentTick);

  /**
   * This method is called when the game state is entered.
   * It should contain the logic for initializing the game state.
   *
   * @param previousState the previous game state
   */
  protected abstract void onEnter(final GameState previousState);

  /**
   * This method is called when the game state is exited.
   * It should contain the logic for cleaning up the game state.
   *
   * @param nextState the next game state
   */
  protected abstract void onExit(final GameState nextState);

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Adds listeners to the game state.
   * This method registers the provided listeners with the Bukkit plugin manager.
   *
   * @param listeners the listeners to be added
   */
  protected void addListeners(@NotNull Listener... listeners) {
    for (Listener listener : listeners) {
      Bukkit.getPluginManager().registerEvents(listener, DreamAPI.getAPI().plugin());
    }
    this.listeners.addAll(List.of(listeners));
  }

}
