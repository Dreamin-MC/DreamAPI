package fr.dreamin.dreamapi.api.cuboid.service;

import fr.dreamin.dreamapi.api.cuboid.Cuboid;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

/**
 * Service interface for managing Cuboid objects.
 *
 * @author Dreamin
 * @since 1.0.0
 */
public interface CuboidService {

  /**
   * Registers a new cuboid.
   *
   * @param cuboid the cuboid to register.
   *
   * @author Dreamin
   * @since 1.0.0
   */
  void register(final @NotNull Cuboid cuboid);

  /**
   * Unregisters an existing cuboid.
   *
   * @param cuboid the cuboid to unregister.
   *
   * @author Dreamin
   * @since 1.0.0
   */
  void unregister(final @NotNull Cuboid cuboid);

  /**
   * Clears all registered cuboids.
   *
   * @author Dreamin
   * @since 1.0.0
   */
  void clear();

  /**
   * Gets all registered cuboids.
   *
   * @return a set of all registered cuboids.
   *
   * @author Dreamin
   * @since 1.0.0
   */
  @NotNull Set<Cuboid> getCuboids();

  /**
   * Gets all cuboids associated with a specific UUID.
   *
   * @param uuid the UUID to filter cuboids by.
   * @return a set of cuboids associated with the given UUID.
   *
   * @author Dreamin
   * @since 1.0.0
   */
  @NotNull Set<Cuboid> getCuboidsOf(final @NotNull UUID uuid);

}
