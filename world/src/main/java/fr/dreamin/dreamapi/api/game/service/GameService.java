package fr.dreamin.dreamapi.api.game.service;

import fr.dreamin.dreamapi.api.game.model.GameState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GameService {

  void switchState(final @NotNull GameState newState);

  @Nullable GameState getCurrentState();

  void tick(int currentTick);



}
