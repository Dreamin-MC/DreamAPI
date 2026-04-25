package fr.dreamin.dreamapi.core.cuboid.event;

import fr.dreamin.dreamapi.api.cuboid.Cuboid;
import fr.dreamin.dreamapi.api.event.ToolsCancelEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Event triggered when a player leaves a cuboid region.
 *
 * @author Dreamin
 * @since 1.0.0
 */
@ToString
@Getter
@RequiredArgsConstructor
public class CuboidEntityLeaveEvent extends ToolsCancelEvent {

  private final @NotNull Entity player;
  private final @NotNull Cuboid cuboid;

}
