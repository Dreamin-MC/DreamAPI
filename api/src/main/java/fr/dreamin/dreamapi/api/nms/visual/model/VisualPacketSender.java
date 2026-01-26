package fr.dreamin.dreamapi.api.nms.visual.model;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface VisualPacketSender {

  void sendPacket(final @NotNull Player player, final @NotNull Object packet);

  default void sendPacket(Iterable<? extends Player> players, final @NotNull Object packet) {
    for (final var player : players) {
      sendPacket(player, packet);
    }
  }
}
