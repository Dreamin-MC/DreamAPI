package fr.dreamin.dreamapi.api.navigate.event.entity;

import fr.dreamin.dreamapi.api.event.ToolsEvent;
import fr.dreamin.dreamapi.api.navigate.model.EntityMovementTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
public class EntityMovementFinishEvent extends ToolsEvent {
  private final @NotNull EntityMovementTask task;
}
