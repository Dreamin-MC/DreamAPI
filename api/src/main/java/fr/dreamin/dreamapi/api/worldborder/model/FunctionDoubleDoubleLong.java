package fr.dreamin.dreamapi.api.worldborder.model;

/**
 * This functional interface represents a function that accepts two doubles and a long value
 * and performs an operation with them.
 */
@FunctionalInterface
public interface FunctionDoubleDoubleLong {
  /**
   * Linearly interpolates the old size of the world border to the new size over a specified time.
   *
   * @param oldSize the old size of the world border
   * @param newSize the new size of the world border
   * @param time the time (in seconds) over which to interpolate the size
   * @param startTime the start time of the interpolation in seconds
   */
  void lerp(final double oldSize, final double newSize, final long time, final long startTime);

}