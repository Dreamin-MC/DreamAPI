package fr.dreamin.dreamapi.api.world.service;

import fr.dreamin.dreamapi.api.world.WorldDefinition;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.function.Consumer;

public interface WorldService {

  void resetWorld(@NotNull String worldLabel, @NotNull String templateLabel, @Nullable Consumer<World> callback);
  void cloneWorld(@NotNull String sourceWorldLabel, @NotNull String cloneWorldLabel, @Nullable Consumer<World> callback);
  void deleteWorld(@NotNull String worldLabel, @Nullable Runnable callback);

  void markTemporary(@NotNull String worldLabel, @NotNull Duration duration);
  void cancelAutoDelete(@NotNull String worldLabel);
  void extendAutoDelete(@NotNull String worldLabel, @NotNull Duration newDuration);
  boolean isTemporary(@NotNull String worldLabel);

  void createWorld(final @NotNull WorldDefinition definition, final @Nullable Consumer<World> callback);

}
