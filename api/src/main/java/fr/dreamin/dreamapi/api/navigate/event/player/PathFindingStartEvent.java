package fr.dreamin.dreamapi.api.navigate.event.player;

import fr.dreamin.dreamapi.api.event.ToolsCancelEvent;
import fr.dreamin.dreamapi.api.navigate.model.AbstractNavigateTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public class PathFindingStartEvent extends ToolsCancelEvent {
  private final @NotNull AbstractNavigateTask task;
}
