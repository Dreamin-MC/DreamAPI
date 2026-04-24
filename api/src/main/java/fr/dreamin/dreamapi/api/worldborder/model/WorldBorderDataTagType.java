package fr.dreamin.dreamapi.api.worldborder.model;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class WorldBorderDataTagType implements PersistentDataType<PersistentDataContainer, WorldBorderData> {

  private final @NotNull NamespacedKey sizeKey;
  private final @NotNull NamespacedKey xKey;
  private final @NotNull NamespacedKey zKey;
  private final @NotNull NamespacedKey damageBufferInBlocksKey;
  private final @NotNull NamespacedKey warningTimeSecondsKey;
  private final @NotNull NamespacedKey warningDistanceKey;

  /**
   * Ctor
   *
   * @param javaPlugin the java plugin
   */
  public WorldBorderDataTagType(Plugin javaPlugin) {
    this.sizeKey = new NamespacedKey(javaPlugin, "size");
    this.xKey = new NamespacedKey(javaPlugin, "center_x");
    this.zKey = new NamespacedKey(javaPlugin, "center_z");
    this.damageBufferInBlocksKey = new NamespacedKey(javaPlugin, "damage_buffer_in_blocks");
    this.warningTimeSecondsKey = new NamespacedKey(javaPlugin, "warning_time_seconds");
    this.warningDistanceKey = new NamespacedKey(javaPlugin, "warning_distance");
  }


  @Override
  public @NonNull Class<PersistentDataContainer> getPrimitiveType() {
    return PersistentDataContainer.class;
  }

  @Override
  public @NonNull Class<WorldBorderData> getComplexType() {
    return WorldBorderData.class;
  }

  @Override
  public @NonNull PersistentDataContainer toPrimitive(final @NotNull WorldBorderData complex, final @NotNull PersistentDataAdapterContext context) {
    final var persistentDataContainer = context.newPersistentDataContainer();

    persistentDataContainer.set(sizeKey, PersistentDataType.DOUBLE, complex.getSize());
    complex.applyCenter((x, z) -> {
      persistentDataContainer.set(xKey, PersistentDataType.DOUBLE, x);
      persistentDataContainer.set(zKey, PersistentDataType.DOUBLE, z);
    });
    persistentDataContainer.set(damageBufferInBlocksKey, PersistentDataType.DOUBLE, complex.getDamageBuffer());
    persistentDataContainer.set(warningTimeSecondsKey, PersistentDataType.INTEGER, complex.getWarningTimeSeconds());
    persistentDataContainer.set(warningDistanceKey, PersistentDataType.INTEGER, complex.getWarningDistance());

    return persistentDataContainer;
  }

  @Override
  public @NonNull WorldBorderData fromPrimitive(@NonNull PersistentDataContainer primitive, @NonNull PersistentDataAdapterContext context) {
    final var worldBorderData = new WorldBorderData();

    get(primitive, sizeKey, PersistentDataType.DOUBLE).ifPresent(worldBorderData::setSize);
    Optional<Double> centerX = get(primitive, xKey, PersistentDataType.DOUBLE);
    Optional<Double> centerZ = get(primitive, zKey, PersistentDataType.DOUBLE);
    if (centerX.isPresent() && centerZ.isPresent()) {
      worldBorderData.setCenter(centerX.get(), centerZ.get());
    }
    get(primitive, damageBufferInBlocksKey, PersistentDataType.DOUBLE).ifPresent(worldBorderData::setDamageBuffer);
    get(primitive, warningTimeSecondsKey, PersistentDataType.INTEGER).ifPresent(worldBorderData::setWarningTimeSeconds);
    get(primitive, warningDistanceKey, PersistentDataType.INTEGER).ifPresent(worldBorderData::setWarningDistance);

    return worldBorderData;
  }

  private <T, Z> Optional<Z> get(final @NotNull PersistentDataContainer persistentDataContainer, final @NotNull NamespacedKey namespacedKey, final @NotNull PersistentDataType<T, Z> type) {
    if (persistentDataContainer.has(namespacedKey, type)) {
      return Optional.ofNullable(persistentDataContainer.get(namespacedKey, type));
    }
    return Optional.empty();
  }

}