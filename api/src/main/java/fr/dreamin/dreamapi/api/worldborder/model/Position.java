package fr.dreamin.dreamapi.api.worldborder.model;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a position in a 2-dimensional space, defined by its x and z coordinates.
 * @param x x coordinate
 * @param z z coordinate
 */
public record Position(double x, double z) {

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  /**
   * Returns a new position object based on the given vector.
   *
   * @param vector the vector containing x and z coordinates
   * @return a new Position object
   */
  public static Position of(final @NotNull Vector vector) {
    return new Position(vector.getX(), vector.getZ());
  }

  /**
   * Returns a new {@link Position} object based on the given {@link Location}.
   *
   * @param location the location to create a position from
   * @return a new Position object
   */
  public static Position of(final @NotNull Location location) {
    return new Position(location.getX(), location.getZ());
  }

}
