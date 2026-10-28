package fr.dreamin.dreamapi.api.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VersionUtils {

  private static final String rawVersion;
  private static final int major;
  private static final int minor;
  private static final int patch;

  static {
    var detected = "26.1.2";
    try {
      if (Bukkit.getServer() != null) {
        try {
          detected = Bukkit.getMinecraftVersion();
        } catch (Throwable ignored) {
          final var v = Bukkit.getVersion();
          final var start = v.indexOf(':');
          final var end = v.lastIndexOf(')');
          if (start != -1 && end != -1 && start + 2 < end)
            detected = v.substring(start + 2, end);
        }
      }
    } catch (Throwable ignored) {
    }

    rawVersion = detected;
    final var parts = toMajorMinorPatch(detected);
    major = parts[0];
    minor = parts[1];
    patch = parts[2];
  }

  public static @NotNull String getVersionString() {
    return rawVersion;
  }

  public static int getMajor() {
    return major;
  }

  public static int getMinor() {
    return minor;
  }

  public static int getPatch() {
    return patch;
  }

  /**
   * Converts a version string like "26.1.2", "26.2", "26.4+", or "26.4-rc1" to an array of three ints.
   *
   * @param version The version to check against
   * @return The version as an array of three ints [major, minor, patch].
   */
  public static int[] toMajorMinorPatch(final @Nullable String version) {
    if (version == null) {
      return new int[] {0, 0, 0};
    }
    var clean = version.trim();
    if (clean.isEmpty()) {
      return new int[] {0, 0, 0};
    }

    final var spaceIdx = clean.indexOf(' ');
    if (spaceIdx != -1)
      clean = clean.substring(0, spaceIdx);
    final var dashIdx = clean.indexOf('-');
    if (dashIdx != -1)
      clean = clean.substring(0, dashIdx);
    final var plusIdx = clean.indexOf('+');
    if (plusIdx != -1)
      clean = clean.substring(0, plusIdx);

    final var parts = clean.split("\\.");

    final var major = parts.length > 0 ? parseSegment(parts[0]) : 0;
    final var minor = parts.length > 1 ? parseSegment(parts[1]) : 0;
    final var patch = parts.length > 2 ? parseSegment(parts[2]) : 0;

    return new int[] {major, minor, patch};
  }

  private static int parseSegment(final @Nullable String segment) {
    if (segment == null || segment.isEmpty()) {
      return 0;
    }
    final var digits = segment.replaceAll("\\D+", "");
    if (digits.isEmpty()) {
      return 0;
    }
    try {
      return Integer.parseInt(digits);
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  /**
   * Compares two version strings lexicographically by [major, minor, patch].
   *
   * @param v1 the first version string
   * @param v2 the second version string
   * @return a negative integer, zero, or a positive integer as v1 is less than, equal to, or greater than v2
   */
  public static int compare(final @Nullable String v1, final @Nullable String v2) {
    final var p1 = toMajorMinorPatch(v1);
    final var p2 = toMajorMinorPatch(v2);
    for (int i = 0; i < 3; i++) {
      final var diff = Integer.compare(p1[i], p2[i]);
      if (diff != 0)
        return diff;
    }
    return 0;
  }

  /**
   * Returns whether version1 is higher than or equal to version2.
   *
   * @param version1 the version to check
   * @param version2 the reference version
   * @return true if version1 >= version2
   */
  public static boolean isHigherOrEqual(final @Nullable String version1, final @Nullable String version2) {
    return compare(version1, version2) >= 0;
  }

  /**
   * Returns if the given version is higher or equal to the one currently running on the server.
   *
   * @param version The version to check against
   * @return If the given version is running on this server or a newer one.
   */
  public static boolean isHigherOrEqualThanServer(String version) {
    return isHigherOrEqualThanServer(toMajorMinorPatch(version));
  }

  /**
   * Returns if the given version is higher or equal to the one currently running on the server.
   *
   * @param version The version to check against
   * @return If the given version is running on this server or a newer one.
   * @throws IllegalArgumentException If the version array does not have a size of 3
   */
  public static boolean isHigherOrEqualThanServer(int... version) {
    if (version.length != 3)
      throw new IllegalArgumentException("Version array must have a size of 3");

    return version[0] > major
      || (version[0] == major && version[1] > minor)
      || (version[0] == major && version[1] == minor && version[2] >= patch);
  }

  /**
   * Returns if the server is on this or a newer version of Minecraft.
   *
   * @param version The version to check against
   * @return If the server is running this exact or a newer version of Minecraft.
   */
  public static boolean isServerHigherOrEqual(String version) {
    return isServerHigherOrEqual(toMajorMinorPatch(version));
  }

  /**
   * Returns if the server is on this or a newer version of Minecraft.
   *
   * @param version The version to check against
   * @return If the server is running this exact or a newer version of Minecraft.
   * @throws IllegalArgumentException If the version array does not have a size of 3
   */
  public static boolean isServerHigherOrEqual(int... version) {
    if (version.length != 3)
      throw new IllegalArgumentException("Version array must have a size of 3");

    return major > version[0]
      || (major == version[0] && minor > version[1])
      || (major == version[0] && minor == version[1] && patch >= version[2]);
  }

  /**
   * Checks if the given version is within the [min, max] range (inclusive).
   *
   * @param version the version string to test
   * @param min the minimum allowed version (inclusive, null or blank for unbounded)
   * @param max the maximum allowed version (inclusive, null or blank for unbounded)
   * @return true if version is within [min, max]
   */
  public static boolean isWithinRange(
    final @Nullable String version,
    final @Nullable String min,
    final @Nullable String max
  ) {
    if (min != null && !min.isBlank() && max != null && !max.isBlank()) {
      if (compare(min, max) > 0) {
        return false;
      }
    }
    if (min != null && !min.isBlank() && compare(version, min) < 0) {
      return false;
    }
    if (max != null && !max.isBlank() && compare(version, max) > 0) {
      return false;
    }
    return true;
  }

  /**
   * Checks if the current server version is within the [min, max] range (inclusive).
   */
  public static boolean isWithinRange(final @Nullable String min, final @Nullable String max) {
    return isWithinRange(rawVersion, min, max);
  }

  public static boolean isAtLeast26_1_2() {
    return isServerHigherOrEqual(26, 1, 2);
  }

  public static boolean isAtLeast26_2() {
    return isServerHigherOrEqual(26, 2, 0);
  }

  public static boolean isAtLeast26_3() {
    return isServerHigherOrEqual(26, 3, 0);
  }

  public static boolean isAtLeast26_4() {
    return isServerHigherOrEqual(26, 4, 0);
  }

}
