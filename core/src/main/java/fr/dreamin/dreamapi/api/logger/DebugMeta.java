package fr.dreamin.dreamapi.api.logger;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record DebugMeta(@Nullable UUID targetPlayer) {

  public static DebugMeta none() {
    return new DebugMeta(null);
  }

  public static DebugMeta of(final @NotNull Player player) {
    return new DebugMeta(player.getUniqueId());
  }

  public static DebugMeta of(final @NotNull UUID playerId) {
    return new DebugMeta(playerId);
  }

  public boolean hasTarget() {
    return targetPlayer != null;
  }

}
