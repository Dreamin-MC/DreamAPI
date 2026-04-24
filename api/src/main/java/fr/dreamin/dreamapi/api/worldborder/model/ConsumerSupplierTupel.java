package fr.dreamin.dreamapi.api.worldborder.model;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A tuple containing a Consumer and Supplier for a specific data type.
 *
 * @param <T> the type of data
 * @param consumer the consumer
 * @param supplier the supplier
 */
public record ConsumerSupplierTupel<T>(
  @NotNull Consumer<T> consumer,
  @NotNull Supplier<T> supplier
) {

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Retrieves the value from the supplier.
   *
   * @return the value retrieved from the supplier
   */
  public T get() {
    return supplier.get();
  }
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
    void lerp(double oldSize, double newSize, long time, long startTime);
  }
  /**
   * Sets the value of the ConsumerSupplierTuple.
   *
   * @param value the value to set
   */
  public void set(final @NotNull T value) {
    consumer.accept(value);
  }

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  /**
   * A tuple containing a Consumer and Supplier for a specific data type.
   *
   * @param <T> the type of data
   *
   * @param consumer the consumer function that accepts a value of type T
   * @param supplier the supplier function that supplies a value of type T
   * @return an instance of consumer supplier tupel
   */
  public static <T> ConsumerSupplierTupel<T> of(final @NotNull Consumer<T> consumer, final @NotNull Supplier<T> supplier) {
    return new ConsumerSupplierTupel<>(consumer, supplier);
  }

}
