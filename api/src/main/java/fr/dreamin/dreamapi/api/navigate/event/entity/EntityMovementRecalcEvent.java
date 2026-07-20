package fr.dreamin.dreamapi.api.navigate.event.entity;

import fr.dreamin.dreamapi.api.event.ToolsEvent;
import fr.dreamin.dreamapi.api.navigate.model.EntityMovementTask;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class EntityMovementRecalcEvent extends ToolsEvent {
  private final @NotNull EntityMovementTask task;
  private final @NotNull List<Location> newPath;

}
