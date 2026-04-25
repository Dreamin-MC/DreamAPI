package fr.dreamin.dreamapi.core.cuboid.event;

import fr.dreamin.dreamapi.api.cuboid.Cuboid;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.entity.Mannequin;
import org.jetbrains.annotations.NotNull;

@ToString
@Getter
public final class CuboidMannequinEnterEvent extends CuboidEntityEnterEvent {

  private final @NotNull Mannequin mannequin;

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public CuboidMannequinEnterEvent(final @NotNull Mannequin mannequin, final @NotNull Cuboid cuboid) {
    super(mannequin, cuboid);
    this.mannequin = mannequin;
  }

}
