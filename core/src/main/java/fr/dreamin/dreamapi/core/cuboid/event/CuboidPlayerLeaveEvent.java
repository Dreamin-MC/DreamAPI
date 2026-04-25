package fr.dreamin.dreamapi.core.cuboid.event;

import fr.dreamin.dreamapi.api.cuboid.Cuboid;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@ToString
@Getter
public final class CuboidPlayerLeaveEvent extends CuboidEntityLeaveEvent {

  private final @NotNull Player player;

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public CuboidPlayerLeaveEvent(final @NotNull Player player, final @NotNull Cuboid cuboid) {
    super(player, cuboid);
    this.player = player;
  }

}
