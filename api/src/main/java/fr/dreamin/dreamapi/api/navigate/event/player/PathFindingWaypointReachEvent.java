package fr.dreamin.dreamapi.api.navigate.event.player;

import fr.dreamin.dreamapi.api.event.ToolsEvent;
import fr.dreamin.dreamapi.api.navigate.model.AbstractNavigateTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public class PathFindingWaypointReachEvent extends ToolsEvent {
  private final @NotNull AbstractNavigateTask task;
  private final @NotNull Location waypoint;
  private final int waypointIndex;
}
