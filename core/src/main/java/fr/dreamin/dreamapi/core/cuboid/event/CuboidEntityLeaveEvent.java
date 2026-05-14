package fr.dreamin.dreamapi.core.cuboid.event;

import fr.dreamin.dreamapi.api.cuboid.Cuboid;
import fr.dreamin.dreamapi.api.event.ToolsCancelEvent;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when a entity leaves a cuboid region.
 *
 * @author Dreamin
 * @since 1.0.0
 */
@ToString
@Getter
public class CuboidEntityLeaveEvent extends ToolsCancelEvent {

  private final @NotNull Entity entity;
  private final @NotNull Cuboid cuboid;
  private final @NotNull Location from;
  private final @NotNull Location to;

  public CuboidEntityLeaveEvent(
    final @NotNull Entity entity,
    final @NotNull Cuboid cuboid,
    final @NotNull Location from,
    final @NotNull Location to
  ) {
    this.entity = entity;
    this.cuboid = cuboid;
    this.from = from.clone();
    this.to = to.clone();
  }

}
