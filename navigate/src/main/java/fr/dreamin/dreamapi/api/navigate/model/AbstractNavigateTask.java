package fr.dreamin.dreamapi.api.navigate.model;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base abstract class for any navigation-related BukkitRunnable task.
 * Contains common pathfinding states such as the current path, the path index,
 * and the recalculating flag.
 * <p>
 * Extending this class makes it easier to create custom navigation tasks via the API.
 */
@Getter
public abstract class AbstractNavigateTask extends BukkitRunnable {

  @NotNull
  protected final Location targetLocation;
  @NotNull
  protected final AStartPathFinder pathFinder;

  @Nullable
  protected List<Location> currentPath;
  protected int currentPathIndex = 0;

  protected boolean recalculating = false;
  protected boolean finished = false;

  public AbstractNavigateTask(final @NotNull Location targetLocation, final @NotNull AStartPathFinder pathFinder) {
    this.targetLocation = targetLocation;
    this.pathFinder = pathFinder;
  }

}
