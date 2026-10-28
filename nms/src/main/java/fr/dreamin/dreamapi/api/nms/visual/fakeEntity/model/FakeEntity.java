package fr.dreamin.dreamapi.api.nms.visual.fakeEntity.model;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public interface FakeEntity {

  int getEntityId();

  @NotNull EntityType getType();

  @NotNull Location getInitialLocation();
}