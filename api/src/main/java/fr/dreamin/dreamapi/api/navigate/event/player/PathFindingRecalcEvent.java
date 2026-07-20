package fr.dreamin.dreamapi.api.navigate.event.player;

import fr.dreamin.dreamapi.api.event.ToolsEvent;
import fr.dreamin.dreamapi.api.navigate.model.PathFindingTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class PathFindingRecalcEvent extends ToolsEvent {
  private final @NotNull PathFindingTask task;
  private final @NotNull List<Location> newPath;
}
