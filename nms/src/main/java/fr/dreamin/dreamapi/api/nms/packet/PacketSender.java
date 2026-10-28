package fr.dreamin.dreamapi.api.nms.packet;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@UtilityClass
public final class PacketSender {

  /**
   * Send one or multiple packets to a player
   * Null packets are automatically filtered out
   */
  public static void send(final @NotNull Player player, final @NotNull Object... packets) {
    Arrays.stream(packets)
      .forEach(packet -> {
        try {
          PacketReflection.sendPacket(player, packet);
        } catch (ReflectiveOperationException e) {
          throw new RuntimeException("Failed to send packet to " + player.getName(), e);
        }
      });
  }

  /**
   * Send packet to multiple players
   */
  public static void sendToAll(final @NotNull Object packet, final @NotNull Player... players) {
    Arrays.stream(players)
      .forEach(player -> send(player, packet));
  }
}
