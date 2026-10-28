package fr.dreamin.dreamapi.api.nms.visual.service;

import fr.dreamin.dreamapi.api.nms.visual.fakeEntity.model.FakeEntity;
import fr.dreamin.dreamapi.api.nms.visual.fakeEntity.model.FakeEntityMetadata;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface VisualService {

  @NotNull FakeEntity spawnFakeEntity(
    @NotNull EntityType type,
    @NotNull Location location,
    @NotNull Player... viewers
  ) throws ReflectiveOperationException;

  void removeFakeEntity(
    @NotNull FakeEntity entity,
    @NotNull Player... viewers
  ) throws ReflectiveOperationException;

  void updateFakeEntity(
    @NotNull FakeEntity entity,
    @NotNull Consumer<FakeEntityMetadata> metadataMutator,
    @NotNull Player... viewers
  ) throws ReflectiveOperationException;

  /**
   * Show a fake block to viewers (similar spirit to shulker-block glowing hack).
   */
  void showFakeBlock(
    @NotNull Location location,
    @NotNull Material type,
    @NotNull Player... viewers
  ) throws ReflectiveOperationException;

  /**
   * Restore the real block at this location for viewers.
   */
  void hideFakeBlock(
    @NotNull Location location,
    @NotNull Player... viewers
  ) throws ReflectiveOperationException;

  void setFrozenTime(
    @NotNull Player player,
    long time
  );

  void resetTime(
    @NotNull Player player
  );

  void clearForViewer(final @NotNull Player viewer) throws ReflectiveOperationException;

  void reapplyForViewer(final @NotNull Player viewer) throws ReflectiveOperationException;

  @NotNull Set<FakeEntity> getFakeEntities(final @NotNull Player viewer);

  @NotNull Map<Location, Material> getFakeBlocks(final @NotNull Player player);

}
