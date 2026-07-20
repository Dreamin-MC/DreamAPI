package fr.dreamin.dreamapi.api.navigate.event.player;

import fr.dreamin.dreamapi.api.event.ToolsEvent;
import fr.dreamin.dreamapi.api.navigate.model.PathFindingTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public class PathFindingFinishEvent extends ToolsEvent {
  private final @NotNull PathFindingTask task;
}
