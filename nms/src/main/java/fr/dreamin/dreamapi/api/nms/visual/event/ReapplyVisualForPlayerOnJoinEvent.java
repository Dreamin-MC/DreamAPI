package fr.dreamin.dreamapi.api.nms.visual.event;

import fr.dreamin.dreamapi.api.event.ToolsCancelEvent;
import fr.dreamin.dreamapi.api.nms.visual.fakeEntity.model.FakeEntity;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public final class ReapplyVisualForPlayerOnJoinEvent extends ToolsCancelEvent {

  private final @NotNull Player player;
  private final @NotNull Set<FakeEntity> entities;
  private final @NotNull Map<Location, Material> blocks;

}
