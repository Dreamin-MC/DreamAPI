package fr.dreamin.dreamapi.api.util.math;

/**
 * Defines interpolation curve and easing behavior.
 *
 * @author Dreamin
 * @since 1.0.0
 */
public enum InterpolationType {
  /** Constant speed from start to end. */
  LINEAR,
  /** Starts slow, speeds up progressively. */
  EASE_IN,
  /** Starts fast, slows down progressively. */
  EASE_OUT,
  /** Slow at start and end, fast in middle. */
  EASE_IN_OUT
}
