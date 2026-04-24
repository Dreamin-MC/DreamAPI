package fr.dreamin.dreamapi.api.worldborder.model;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * This interface defines a set of methods for interacting with world borders.
 */
public interface WorldBorderService {

  /**
   * Retrieves the world border for a specific player.
   *
   * @param p the player for which to retrieve the world border
   * @return the world border for the specified player
   */
  IWorldBorder getWorldBorder(final @NotNull Player p);

  /**
   * Retrieves the world border for a specific world.
   *
   * @param world the world for which to retrieve the world border
   * @return the world border for the specified world
   */
  IWorldBorder getWorldBorder(final @NotNull World world);

  /**
   * Resets the world border for a specific player to the global world border.
   *
   * @param player the player for which to reset the world border to global
   */
  void resetWorldBorderToGlobal(final @NotNull Player player);

  /**
   * Sets the border size for a specific player.
   *
   * @param player the player for which to set the border size
   * @param size   the size of the border to set
   */
  void setBorder(final @NotNull Player player, final double size);

  /**
   * Sets the border size and center position for a specific player.
   *
   * @param player the player for which to set the border size and center position
   * @param size   the size of the border to set
   * @param vector the vector representing the center position of the border
   */
  void setBorder(final @NotNull Player player, final double size, final @NotNull Vector vector);

  /**
   * Sets the border size and center position for a specific player.
   *
   * @param player    the player for which to set the border size and center position
   * @param size      the size of the border to set
   * @param location  the location representing the center position of the border
   */
  void setBorder(final @NotNull Player player, final double size, final @NotNull Location location);

  /**
   * Sets the border size and center position for a specific player.
   *
   * @param player    the player for which to set the border size and center position
   * @param size      the size of the border to set
   * @param position  the position representing the center position of the border
   */
  void setBorder(final @NotNull Player player, double size, final @NotNull Position position);

  /**
   * Sends a red screen to the specified player for a given number of seconds.
   *
   * @param player      the player to send the red screen to
   * @param time the number of seconds to display the red screen
   */
  void sendRedScreenForSeconds(final @NotNull Player player, final @NotNull Duration time);

  /**
   * Retrieves the WorldBorderData for a player.
   *
   * @param p the player
   * @return the WorldBorderData object containing the size, center coordinates, damage buffer, warning time, and warning distance of the world border
   */
  WorldBorderData getWorldBorderData(final @NotNull Player p);

  /**
   * Sets the border size and duration for a specific player.
   *
   * @param player  the player for which to set the border size and duration
   * @param size    the size of the border to set
   * @param milliSeconds the duration of the border animation in milliseconds
   */
  void setBorder(final @NotNull Player player, final double size, final long milliSeconds);

  /**
   * Sets the border size and duration for a specific player.
   *
   * @param player    the player for which to set the border size and duration
   * @param size      the size of the border to set
   * @param time      the duration of the border animation
   * @param timeUnit  the time unit of the duration parameter
   */
  void setBorder(final @NotNull Player player, final double size, final long time, final @NotNull TimeUnit timeUnit);
}