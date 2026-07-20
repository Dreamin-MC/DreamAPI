package fr.dreamin.dreamapi.api.navigate.model;

import com.destroystokyo.paper.MaterialSetTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class AStartPathFinder {

  private static final Set<Material> PASSABLE_MATERIALS = EnumSet.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR, Material.WATER, Material.LAVA, Material.SHORT_GRASS, Material.TALL_GRASS, Material.FERN, Material.LARGE_FERN, Material.COBWEB, Material.TRIPWIRE,
    Material.TORCH, Material.WALL_TORCH, Material.REDSTONE_TORCH, Material.REDSTONE_WALL_TORCH, Material.LEVER, Material.SUGAR_CANE, Material.KELP, Material.SEAGRASS, Material.TALL_SEAGRASS, Material.REDSTONE_WIRE,
    Material.REPEATER, Material.COMPARATOR, Material.SNOW, Material.BAMBOO_SAPLING, Material.SWEET_BERRY_BUSH, Material.GLOW_LICHEN, Material.SCULK_VEIN, Material.SMALL_DRIPLEAF);

  private static final int MAX_PATHFINDING_ITERATIONS = 100000;

  private static final int MAX_PRE_PATHFINDING_RADIUS = 32;

  private final boolean safeMode;

  private final Set<Material> allowedMaterials;
  private final Set<Material> ignoredMaterials;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public AStartPathFinder(final boolean safeMode, final @NotNull Set<Material> allowedMaterials, final @NotNull Set<Material> ignoredMaterials) {
    this.safeMode = safeMode;
    this.allowedMaterials = allowedMaterials;
    this.ignoredMaterials = ignoredMaterials;
    PASSABLE_MATERIALS.addAll(MaterialSetTag.BUTTONS.getValues());
    PASSABLE_MATERIALS.addAll(MaterialSetTag.FLOWERS.getValues());
    PASSABLE_MATERIALS.addAll(MaterialSetTag.FLOWER_POTS.getValues());
    PASSABLE_MATERIALS.addAll(MaterialSetTag.PRESSURE_PLATES.getValues());
    PASSABLE_MATERIALS.addAll(MaterialSetTag.WOOL_CARPETS.getValues());
    PASSABLE_MATERIALS.addAll(MaterialSetTag.RAILS.getValues());
    PASSABLE_MATERIALS.addAll(MaterialSetTag.CLIMBABLE.getValues());
    PASSABLE_MATERIALS.addAll(MaterialSetTag.DOORS.getValues());
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public List<Location> findPath(final @NotNull Location start, final @NotNull Location end) {
    if (!start.getWorld().equals(end.getWorld()))
      return Collections.emptyList();

    var prePath = new ArrayList<Location>();
    var actualStart = start;

    if (this.allowedMaterials != null && !this.allowedMaterials.isEmpty()) {
      final var blockBelowStart = start.clone().subtract(0, 1, 0);
      if (!this.allowedMaterials.contains(blockBelowStart.getBlock().getType())) {
        prePath.addAll(findPathToClosesAllowedBlock(start));
        if (prePath.isEmpty())
          return Collections.emptyList();
        actualStart = prePath.get(prePath.size() - 1);
      }
    }

    final var openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost));
    final var allNodes = new HashMap<Location, Node>();
    final var closedSet = new HashSet<Location>();

    final var startNode = new Node(actualStart, null, 0, getDistance(actualStart, end));
    openSet.add(startNode);
    allNodes.put(actualStart, startNode);
    int iterations = 0;

    while (!openSet.isEmpty()) {
      iterations++;

      if (iterations > MAX_PATHFINDING_ITERATIONS)
        return Collections.emptyList();
      final var currentNode = openSet.poll();
      closedSet.add(currentNode.getLocation());

      if (currentNode.getLocation().getBlockX() == end.getBlockX() && currentNode.getLocation().getBlockY()  == end.getBlockY() && currentNode.getLocation().getBlockZ() == end.getBlockZ()) {
        final var mainPath = reconstructPath(currentNode);
        final var fullPath = new ArrayList<>(prePath);
        fullPath.addAll(mainPath);

        return fullPath;
      }

      for (final var neighborLoc : getNeighbors(currentNode.getLocation(), end)) {
        if (closedSet.contains(neighborLoc))
          continue;

        final var newGCost = currentNode.getGCost() + getDistance(currentNode.getLocation(), neighborLoc);
        var neighborNode = allNodes.get(neighborLoc);
        if (neighborNode == null || newGCost < neighborNode.getGCost()) {
          neighborNode = new Node(neighborLoc, currentNode, newGCost, getDistance(neighborLoc, end));
          allNodes.put(neighborLoc, neighborNode);
          if (!openSet.contains(neighborNode))
            openSet.add(neighborNode);
        }

      }
    }

    return Collections.emptyList();
  }

  public List<Location> reconstructPath(final @NotNull Node endNode) {
    final var path = new ArrayList<Location>();
    var currentNode = endNode;
    while (currentNode != null) {
      path.add(currentNode.getLocation());
      currentNode = currentNode.getParent();
    }
    Collections.reverse(path);
    return path;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private boolean isPassable(final Material mat) {
    return PASSABLE_MATERIALS.contains(mat) || (this.ignoredMaterials != null && this.ignoredMaterials.contains(mat));
  }

  private double getDistance(final @NotNull Location loc1, final @NotNull Location loc2) {
    return loc1.distance(loc2);
  }

  private List<Location> findPathToClosesAllowedBlock(final @NotNull Location start) {
    if (this.allowedMaterials == null || this.allowedMaterials.isEmpty())
      return Collections.emptyList();

    final var queue = new LinkedList<Location>();
    final var cameFrom = new HashMap<Location, Location>();
    final var visited = new HashSet<Location>();
    final var startBlockLoc = start.getBlock().getLocation();

    queue.add(startBlockLoc);
    visited.add(startBlockLoc);
    cameFrom.put(startBlockLoc, null);
    int iterations = 0;

    while (!queue.isEmpty() && iterations < MAX_PATHFINDING_ITERATIONS) {
      final var current = queue.poll();
      iterations++;
      final var blockBelow = current.clone().subtract(0, 1, 0);
      if (this.allowedMaterials.contains(blockBelow.getBlock().getType())) {
        final var path = new ArrayList<Location>();
        var temp = current;
        while (temp != null) {
          path.add(temp);
          temp = cameFrom.get(temp);
        }
        Collections.reverse(path);
        return path;
      }

      for (var dx = -1; dx <= 1; dx++) {
        for (var dy = -1; dy <= 1; dy++) {
          for (var dz = -1; dz <= 1; dz++) {
            if (dx != 0 || dy != 0 || dz != 0) {
              final var neighbor = new Location(current.getWorld(), current.getX() + dx,  current.getY() + dy, current.getZ() + dz);
              if (!visited.contains(neighbor) && start.distance(neighbor) < 32 && isPathableForPrePath(neighbor, current)) {
                visited.add(neighbor);
                cameFrom.put(neighbor, current);
                queue.add(neighbor);
              }
            }
          }
        }
      }

    }
    return Collections.emptyList();
  }

  private boolean isPathableForPrePath(final @NotNull Location loc, final @NotNull Location previousLoc) {
    final var block = loc.getBlock();
    final var blockAbove = loc.clone().add(0, 1, 0).getBlock();
    final var blockBelow = loc.clone().add(0, -1, 0).getBlock();

    if (!isPassable(block.getType()) || !isPassable(blockAbove.getType()))
      return false;

    if (isDiagonalMove(loc, previousLoc) && !isDiagonalPassable(previousLoc, loc))
      return false;

    if (loc.getBlockY() == previousLoc.getBlockY()) {
      return (blockBelow.getType().isSolid() || blockBelow
        .getType() == Material.WATER || blockBelow
        .getType() == Material.LAVA);
    }

    if (loc.getBlockY() > previousLoc.getBlockY()) {
      int yDiff = loc.getBlockY() - previousLoc.getBlockY();
      if (yDiff != 1)
        return false;
      return blockBelow.getType().isSolid();
    }
    if (loc.getBlockY() < previousLoc.getBlockY()) {
      int yDiff = previousLoc.getBlockY() - loc.getBlockY();
      if (this.safeMode) {
        if (yDiff > 3)
          return false;
        final var landingCheck = loc.clone().add(0.0D, -1.0D, 0.0D);
        return (landingCheck.getBlock().getType().isSolid() || landingCheck
          .clone().add(0.0D, -1.0D, 0.0D).getBlock().getType().isSolid());
      }
      Location checkLoc = loc.clone();
      while (checkLoc.getBlockY() > 0) {
        checkLoc.add(0.0D, -1.0D, 0.0D);
        final var checkBlock = checkLoc.getBlock();
        if (checkBlock.getType().isSolid())
          return true;
        if (!isPassable(checkBlock.getType()))
          return false;
      }
      return false;
    }
    return false;

  }

  private List<Location> getNeighbors(Location loc, Location end) {
    List<Location> neighbors = new ArrayList<>();
    int x = loc.getBlockX();
    int y = loc.getBlockY();
    int z = loc.getBlockZ();
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        for (int dz = -1; dz <= 1; dz++) {
          if (dx != 0 || dy != 0 || dz != 0) {
            Location neighborLoc = new Location(loc.getWorld(), (x + dx), (y + dy), (z + dz));
            if (isWalkable(neighborLoc, loc, end))
              neighbors.add(neighborLoc);
          }
        }
      }
    }
    return neighbors;
  }

  private boolean isWalkable(Location loc, Location previousLoc, Location end) {
    final var block = loc.getBlock();
    final var blockAbove = loc.clone().add(0.0D, 1.0D, 0.0D).getBlock();
    
    if (MaterialSetTag.CLIMBABLE.getValues().contains(block.getType()))
      return true;
      
    if (isDiagonalMove(loc, previousLoc) && !isDiagonalPassable(previousLoc, loc))
      return false;
      
    if (!isPassable(block.getType()) || !isPassable(blockAbove.getType()))
      return false;

    boolean isDest = (loc.getBlockX() == end.getBlockX() && loc.getBlockY() == end.getBlockY() && loc.getBlockZ() == end.getBlockZ());

    if (loc.getBlockY() == previousLoc.getBlockY()) {
      final var blockBelow = loc.clone().add(0.0D, -1.0D, 0.0D).getBlock();
      if (!blockBelow.getType().isSolid() && blockBelow.getType() != Material.WATER && blockBelow.getType() != Material.LAVA)
        return false;
      if (!isDest && this.allowedMaterials != null && !this.allowedMaterials.isEmpty() && !this.allowedMaterials.contains(blockBelow.getType()))
        return false;
      return true;
    }
    
    if (loc.getBlockY() > previousLoc.getBlockY()) {
      final var yDiff = loc.getBlockY() - previousLoc.getBlockY();
      if (yDiff != 1)
        return false;
      final var blockBelow = loc.clone().add(0.0D, -1.0D, 0.0D).getBlock();
      if (!blockBelow.getType().isSolid())
        return false;
      if (!isDest && this.allowedMaterials != null && !this.allowedMaterials.isEmpty() && !this.allowedMaterials.contains(blockBelow.getType()))
        return false;
      return true;
    }
    
    if (loc.getBlockY() < previousLoc.getBlockY()) {
      final var yDiff = previousLoc.getBlockY() - loc.getBlockY();
      if (this.safeMode && yDiff > 3)
        return false;
        
      final var checkLoc = loc.clone();
      while (checkLoc.getBlockY() > 0) {
        checkLoc.add(0.0D, -1.0D, 0.0D);
        final var checkBlock = checkLoc.getBlock();
        if (checkBlock.getType().isSolid()) {
          if (!isDest && this.allowedMaterials != null && !this.allowedMaterials.isEmpty() && !this.allowedMaterials.contains(checkBlock.getType()))
            return false;
          return true;
        }
        if (!isPassable(checkBlock.getType()))
          return false;
      }
      return false;
    }
    return false;
  }

  private boolean isDiagonalMove(Location from, Location to) {
    return (from.getBlockX() != to.getBlockX() && from
      .getBlockZ() != to.getBlockZ());
  }

  private boolean isDiagonalPassable(Location from, Location to) {
    final var xDir = Integer.compare(to.getBlockX(), from.getBlockX());
    final var zDir = Integer.compare(to.getBlockZ(), from.getBlockZ());
    final var corner1 = new Location(from.getWorld(), (from.getBlockX() + xDir), from.getBlockY(), from.getBlockZ());
    final var corner2 = new Location(from.getWorld(), from.getBlockX(), from.getBlockY(), (from.getBlockZ() + zDir));
    final var block1 = corner1.getBlock();
    final var block1Above = corner1.clone().add(0.0D, 1.0D, 0.0D).getBlock();
    final var block2 = corner2.getBlock();
    final var block2Above = corner2.clone().add(0.0D, 1.0D, 0.0D).getBlock();
    return (isPassable(block1.getType()) && isPassable(block1Above.getType()) && 
            isPassable(block2.getType()) && isPassable(block2Above.getType()));
  }

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  public static String getDirection(final @NotNull Location from, final @NotNull Location to) {
    if (!from.getWorld().equals(to.getWorld()))
      return "N/A";

    var angle = getYaw(from, to);

    if (angle < 90.0)
      angle += 360.0;

    if (angle >= 337.5 || angle < 22.5)
      return "South";
    if (angle >= 22.5 && angle < 67.5)
      return "South-West";
    if (angle >= 67.5 && angle < 112.5)
      return "West";
    if (angle >= 112.5 && angle < 157.5)
      return "North-West";
    if (angle >= 157.5 && angle < 202.5)
      return "North";
    if (angle >= 202.5 && angle < 247.5)
      return "North-East";
    if (angle >= 247.5 && angle < 292.5)
      return "East";
    if (angle >= 292.5 && angle < 337.5)
      return "South-East";

    return "N/A";
  }

  public static float getYaw(final @NotNull Location from, final @NotNull Location to) {
    if (!from.getWorld().equals(to.getWorld()))
      return 0F;

    final var dx = to.getX() - from.getX();
    final var dz = to.getZ() - from.getZ();
    final var yaw = Math.toDegrees(Math.atan2(dx, dz)) - 90.0;

    return (float) yaw;
  }

  public static float getPitch(final @NotNull Location from, final @NotNull Location to) {
    if (!from.getWorld().equals(to.getWorld()))
      return 0F;

    final var dx = to.getX() - from.getX();
    final var dy = to.getY() - from.getY();
    final var dz = to.getZ() - from.getZ();

    final var distanceXZ = Math.sqrt(dx * dx + dz * dz);
    final var pitch = -Math.toDegrees(Math.atan2(dy, distanceXZ));

    return (float) pitch;
  }

  public static int getDistance2D(final @NotNull Location from, final @NotNull Location to) {
    if (!from.getWorld().equals(to.getWorld()))
      return -1;
    return (int) Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getZ() - from.getZ(), 2));
  }



}
