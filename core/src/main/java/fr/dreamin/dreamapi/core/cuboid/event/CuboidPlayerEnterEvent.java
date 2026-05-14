package fr.dreamin.dreamapi.core.cuboid.event;

import fr.dreamin.dreamapi.api.cuboid.Cuboid;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@ToString
@Getter
public final class CuboidPlayerEnterEvent extends CuboidEntityEnterEvent {

  private final @NotNull Player player;

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public CuboidPlayerEnterEvent(
    final @NotNull Player player,
    final @NotNull Cuboid cuboid,
    final @NotNull Location from,
    final @NotNull Location to
  ) {
    super(player, cuboid, from, to);
    this.player = player;
  }

}
