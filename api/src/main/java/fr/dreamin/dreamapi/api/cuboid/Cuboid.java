package fr.dreamin.dreamapi.api.cuboid;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.cuboid.service.CuboidService;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a cuboid area defined by two corner locations in a Minecraft world.
 *
 * @author Dreamin
 * @since 1.0.0
 */
@Getter @Setter
public class Cuboid {

  private Location locA, locB;

  public Cuboid(Location locA, Location locB) {
    this.locA = locA;
    this.locB = locB;

    DreamAPI.getAPI().getService(CuboidService.class).register(this);
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Calculates and returns the center location of the cuboid.
   *
   * @return Center Location of the cuboid, or null if locA and locB are not in the same world
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public Location getCenter() {
    return (locA == null || locB == null || !locA.getWorld().equals(locB.getWorld())) ? null :
      locA.clone().add(locB).multiply(0.5);
  }

  /**
   * Calculates and returns the center location of the cuboid on the XZ plane, with Y set just above the lowest Y boundary.
   *
   * @return Center Location on the XZ plane of the cuboid, or null if locA and locB are not in the same world
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public Location getCenterXZ() {
    return (locA == null || locB == null || !locA.getWorld().equals(locB.getWorld())) ? null :
      new Location(locA.getWorld(), (locA.getX() + locB.getX()) / 2, Math.min(locA.getY(), locB.getY()) + 1, (locA.getZ() + locB.getZ()) / 2);
  }

  /**
   * Retrieves all blocks in the cuboid area.
   * @return List of Blocks in the cuboid
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public List<Block> getBlocks() {
    List<Block> rs = new ArrayList<>();

    if (locA == null || locB == null || !locA.getWorld().equals(locB.getWorld())) return rs;

    World world = locA.getWorld();

    int minX = Math.min(locA.getBlockX(), locB.getBlockX());
    int maxX = Math.max(locA.getBlockX(), locB.getBlockX());
    int minY = Math.min(locA.getBlockY(), locB.getBlockY());
    int maxY = Math.max(locA.getBlockY(), locB.getBlockY());
    int minZ = Math.min(locA.getBlockZ(), locB.getBlockZ());
    int maxZ = Math.max(locA.getBlockZ(), locB.getBlockZ());

    for (int x = minX; x <= maxX; x++) {
      for (int y = minY; y <= maxY; y++) {
        for (int z = minZ; z <= maxZ; z++) {
          rs.add(world.getBlockAt(x, y, z));
        }
      }
    }

    return rs;
  }

  /**
   * Retrieves all entities within the cuboid area, with optional boundary extension.
   * The boundary extension applies only to the maximum boundaries (positive X, Y, Z directions).
   * @param boundaryExtension Vector defining how much to extend the cuboid boundaries on each axis.
   * @return List of entities within the cuboid with extended boundaries
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public List<Entity> getEntities(Vector boundaryExtension) {
    List<Entity> entities = new ArrayList<>();

    // Ensure locA and locB are in the same world and not null
    if (locA == null || locB == null || !locA.getWorld().equals(locB.getWorld())) return entities;

    World world = locA.getWorld();

    // Calculate bounds of the cuboid with extension on max boundaries only
    double minX = Math.min(locA.getX(), locB.getX());
    double maxX = Math.max(locA.getX(), locB.getX()) + boundaryExtension.getX();
    double minY = Math.min(locA.getY(), locB.getY());
    double maxY = Math.max(locA.getY(), locB.getY()) + boundaryExtension.getY();
    double minZ = Math.min(locA.getZ(), locB.getZ());
    double maxZ = Math.max(locA.getZ(), locB.getZ()) + boundaryExtension.getZ();

    // Filter entities within the extended bounds
    for (Entity entity : world.getEntities()) {
      Location loc = entity.getLocation();
      if (loc.getX() >= minX && loc.getX() <= maxX &&
        loc.getY() >= minY && loc.getY() <= maxY &&
        loc.getZ() >= minZ && loc.getZ() <= maxZ
      ) entities.add(entity);

    }

    return entities;
  }

  /**
   * Retrieves all entities within the cuboid area with a uniform boundary extension.
   * @param extension The amount to extend the boundary on all max axes.
   * @return List of entities within the cuboid with uniformly extended boundaries
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public List<Entity> getEntities(double extension) {
    return getEntities(new Vector(extension, extension, extension));
  }

  /**
   * Retrieves all entities within the cuboid area with a default boundary extension of 1.
   * @return List of entities within the cuboid with default boundary extension
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public List<Entity> getEntities() {
    return getEntities(1);
  }

  /**
   * Checks if a given location is within the cuboid area, considering boundary extension.
   *
   * @param location Location to check
   * @param boundaryExtension Vector defining how much to extend the cuboid boundaries on each axis.
   * @return True if the location is within the cuboid with extended boundaries, false otherwise
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public boolean isLocationIn(@NotNull Location location, Vector boundaryExtension) {
    return location.getX() >= Math.min(locA.getX(), locB.getX()) && location.getX() <= Math.max(locA.getX(), locB.getX()) + boundaryExtension.getX()
      && location.getY() >= Math.min(locA.getY(), locB.getY()) && location.getY() <= Math.max(locA.getY(), locB.getY()) + boundaryExtension.getY()
      && location.getZ() >= Math.min(locA.getZ(), locB.getZ()) && location.getZ() <= Math.max(locA.getZ(), locB.getZ()) + boundaryExtension.getZ();
  }

  /**
   * Checks if a given location is within the cuboid area with a default boundary extension of (1,1,1).
   *
   * @param location Location to check
   * @return True if the location is within the cuboid with default boundary extension, false otherwise
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public boolean isLocationIn(@NotNull Location location) {
    return isLocationIn(location, new Vector(1, 1, 1));
  }

  /**
   * Sets all blocks in the cuboid area to the specified material.
   * @param material Material to set the blocks to
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public void setMaterial(Material material) {
    if (locA == null || locB == null || !locA.getWorld().equals(locB.getWorld())) return;

    World world = locA.getWorld();

    int minX = Math.min(locA.getBlockX(), locB.getBlockX());
    int maxX = Math.max(locA.getBlockX(), locB.getBlockX());
    int minY = Math.min(locA.getBlockY(), locB.getBlockY());
    int maxY = Math.max(locA.getBlockY(), locB.getBlockY());
    int minZ = Math.min(locA.getBlockZ(), locB.getBlockZ());
    int maxZ = Math.max(locA.getBlockZ(), locB.getBlockZ());

    for (int x = minX; x <= maxX; x++) {
      for (int y = minY; y <= maxY; y++) {
        for (int z = minZ; z <= maxZ; z++) {
          Block block = world.getBlockAt(x, y, z);
          block.setType(material, false);
        }
      }
    }
  }

  /**
   * Sets all blocks in the cuboid area to the specified BlockData.
   *
   * @param blockData BlockData to set the blocks to
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public void setBlockData(BlockData blockData) {
    if (locA == null || locB == null || !locA.getWorld().equals(locB.getWorld())) return;

    final var world = locA.getWorld();

    int minX = Math.min(locA.getBlockX(), locB.getBlockX());
    int maxX = Math.max(locA.getBlockX(), locB.getBlockX());
    int minY = Math.min(locA.getBlockY(), locB.getBlockY());
    int maxY = Math.max(locA.getBlockY(), locB.getBlockY());
    int minZ = Math.min(locA.getBlockZ(), locB.getBlockZ());
    int maxZ = Math.max(locA.getBlockZ(), locB.getBlockZ());

    for (var x = minX; x <= maxX; x++) {
      for (var y = minY; y <= maxY; y++) {
        for (var z = minZ; z <= maxZ; z++) {
          final var block = world.getBlockAt(x, y, z);
          block.setBlockData(blockData, false);
        }
      }
    }
  }

  /**
   * Replaces all blocks of a specific material in the cuboid area with a new material.
   *
   * @param replaced Material to be replaced
   * @param newMaterial Material to replace with
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public void replaceMaterial(Material replaced, Material newMaterial) {
    if (locA == null || locB == null || !locA.getWorld().equals(locB.getWorld())) return;

    final var world = locA.getWorld();

    final var minX = Math.min(locA.getBlockX(), locB.getBlockX());
    final var maxX = Math.max(locA.getBlockX(), locB.getBlockX());
    final var minY = Math.min(locA.getBlockY(), locB.getBlockY());
    final var maxY = Math.max(locA.getBlockY(), locB.getBlockY());
    final var minZ = Math.min(locA.getBlockZ(), locB.getBlockZ());
    final var maxZ = Math.max(locA.getBlockZ(), locB.getBlockZ());

    for (var x = minX; x <= maxX; x++) {
      for (var y = minY; y <= maxY; y++) {
        for (var z = minZ; z <= maxZ; z++) {
          final var block = world.getBlockAt(x, y, z);
          if (block.getType() == replaced) block.setType(newMaterial, false);
        }
      }
    }
  }

  /**
   * Replaces all blocks with specific BlockData in the cuboid area with new BlockData.
   *
   * @param replaced BlockData to be replaced
   * @param newBlockData BlockData to replace with
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public void replaceBlockData(final @NotNull BlockData replaced, final @NotNull BlockData newBlockData) {
    if (locA == null || locB == null || !locA.getWorld().equals(locB.getWorld())) return;

    final var world = locA.getWorld();

    final var minX = Math.min(locA.getBlockX(), locB.getBlockX());
    final var maxX = Math.max(locA.getBlockX(), locB.getBlockX());
    final var minY = Math.min(locA.getBlockY(), locB.getBlockY());
    final var maxY = Math.max(locA.getBlockY(), locB.getBlockY());
    final var minZ = Math.min(locA.getBlockZ(), locB.getBlockZ());
    final var maxZ = Math.max(locA.getBlockZ(), locB.getBlockZ());

    for (var x = minX; x <= maxX; x++) {
      for (var y = minY; y <= maxY; y++) {
        for (var z = minZ; z <= maxZ; z++) {
          final var block = world.getBlockAt(x, y, z);
          if (block.getBlockData() == replaced) block.setBlockData(newBlockData, false);
        }
      }
    }
  }

  /**
   * Sets all blocks in the cuboid area to the specified material every tick, either from top to bottom or bottom to top.
   *
   * @param material Material to set the blocks to
   * @param tick Tick interval between each layer update
   * @param topToBottom If true, updates from top to bottom; if false, from bottom to top
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public void setMaterialEveryTick(final @NotNull Material material, final long tick, final boolean topToBottom) {
    final var minY = Math.min(locA.getBlockY(), locB.getBlockY());
    final var maxY = Math.max(locA.getBlockY(), locB.getBlockY());

    new BukkitRunnable() {
      private int currentY = topToBottom ? maxY : minY;

      @Override
      public void run() {
        if (topToBottom ? currentY < minY : currentY > maxY) {
          this.cancel();
          return;
        }

        for (var x = Math.min(locA.getBlockX(), locB.getBlockX()); x <= Math.max(locA.getBlockX(), locB.getBlockX()); x++) {
          for (var z = Math.min(locA.getBlockZ(), locB.getBlockZ()); z <= Math.max(locA.getBlockZ(), locB.getBlockZ()); z++) {
            final var block = locA.getWorld().getBlockAt(x, currentY, z);
            block.setType(material, false);
          }
        }

        if (topToBottom) currentY--;
        else currentY++;
      }
    }.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, tick);
  }

  /**
   * Sets all blocks in the cuboid area to the specified BlockData every tick, either from top to bottom or bottom to top.
   *
   * @param blockData BlockData to set the blocks to
   * @param tick Tick interval between each layer update
   * @param topToBottom If true, updates from top to bottom; if false, from bottom to top
   */
  public void setBlockDataEveryTick(final @NotNull BlockData blockData, final long tick, final boolean topToBottom) {
    final var minY = Math.min(locA.getBlockY(), locB.getBlockY());
    final var maxY = Math.max(locA.getBlockY(), locB.getBlockY());

    new BukkitRunnable() {
      private int currentY = topToBottom ? maxY : minY;

      @Override
      public void run() {
        if (topToBottom ? currentY < minY : currentY > maxY) {
          this.cancel();
          return;
        }

        for (int x = Math.min(locA.getBlockX(), locB.getBlockX()); x <= Math.max(locA.getBlockX(), locB.getBlockX()); x++) {
          for (int z = Math.min(locA.getBlockZ(), locB.getBlockZ()); z <= Math.max(locA.getBlockZ(), locB.getBlockZ()); z++) {
            Block block = locA.getWorld().getBlockAt(x, currentY, z);
            block.setBlockData(blockData, false);
          }
        }

        if (topToBottom) currentY--;
        else currentY++;
      }
    }.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, tick);
  }

  /**
   * Replaces blocks of a specific material in the cuboid area with a new material every tick, either from top to bottom or bottom to top.
   *
   * @param replaced
   * @param newMaterial
   * @param tick
   * @param topToBottom
   *
   * @author Dreamin
   * @since 1.0.0
   */
  public void replaceMaterialEveryTick(final @NotNull Material replaced, final @NotNull Material newMaterial, final long tick, final boolean topToBottom) {
    final int minY = Math.min(locA.getBlockY(), locB.getBlockY());
    final int maxY = Math.max(locA.getBlockY(), locB.getBlockY());

    new BukkitRunnable() {
      private int currentY = topToBottom ? maxY : minY;

      @Override
      public void run() {
        if (topToBottom ? currentY < minY : currentY > maxY) {
          this.cancel();
          return;
        }

        for (var x = Math.min(locA.getBlockX(), locB.getBlockX()); x <= Math.max(locA.getBlockX(), locB.getBlockX()); x++) {
          for (var z = Math.min(locA.getBlockZ(), locB.getBlockZ()); z <= Math.max(locA.getBlockZ(), locB.getBlockZ()); z++) {
            final var block = locA.getWorld().getBlockAt(x, currentY, z);
            if (block.getType() == replaced) block.setType(newMaterial, false); // Remplace uniquement les blocs avec le matériau spécifique
          }
        }

        if (topToBottom) currentY--;
        else currentY++;
      }
    }.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, tick);
  }

  public void replaceBlockDataEveryTick(final @NotNull BlockData replaced, final @NotNull BlockData newBlockData, final long tick, final boolean topToBottom) {
    final var minY = Math.min(locA.getBlockY(), locB.getBlockY());
    final var maxY = Math.max(locA.getBlockY(), locB.getBlockY());

    new BukkitRunnable() {
      private int currentY = topToBottom ? maxY : minY;

      @Override
      public void run() {
        if (topToBottom ? currentY < minY : currentY > maxY) {
          this.cancel();
          return;
        }

        for (var x = Math.min(locA.getBlockX(), locB.getBlockX()); x <= Math.max(locA.getBlockX(), locB.getBlockX()); x++) {
          for (var z = Math.min(locA.getBlockZ(), locB.getBlockZ()); z <= Math.max(locA.getBlockZ(), locB.getBlockZ()); z++) {
            final var block = locA.getWorld().getBlockAt(x, currentY, z);
            if (block.getBlockData().matches(replaced)) block.setBlockData(newBlockData, false); // Remplace uniquement les blocs avec le BlockData spécifique
          }
        }

        if (topToBottom) currentY--;
        else currentY++;
      }
    }.runTaskTimer(DreamAPI.getAPI().plugin(), 0L, tick);
  }

  public int countBlocksOfMaterial(Material material) {
    if (locA == null || locB == null || !locA.getWorld().equals(locB.getWorld())) return 0;

    final var world = locA.getWorld();
    var count = 0;

    final var minX = Math.min(locA.getBlockX(), locB.getBlockX());
    final var maxX = Math.max(locA.getBlockX(), locB.getBlockX());
    final var minY = Math.min(locA.getBlockY(), locB.getBlockY());
    final var maxY = Math.max(locA.getBlockY(), locB.getBlockY());
    final var minZ = Math.min(locA.getBlockZ(), locB.getBlockZ());
    final var maxZ = Math.max(locA.getBlockZ(), locB.getBlockZ());

    for (var x = minX; x <= maxX; x++) {
      for (var y = minY; y <= maxY; y++) {
        for (var z = minZ; z <= maxZ; z++) {
          final var block = world.getBlockAt(x, y, z);
          if (block.getType() == material) count++;
        }
      }
    }

    return count;
  }

  public int countBlocksOfBlockData(BlockData blockData) {
    if (locA == null || locB == null || !locA.getWorld().equals(locB.getWorld())) return 0;

    final var world = locA.getWorld();
    var count = 0;

    final var minX = Math.min(locA.getBlockX(), locB.getBlockX());
    final var maxX = Math.max(locA.getBlockX(), locB.getBlockX());
    final var minY = Math.min(locA.getBlockY(), locB.getBlockY());
    final var maxY = Math.max(locA.getBlockY(), locB.getBlockY());
    final var minZ = Math.min(locA.getBlockZ(), locB.getBlockZ());
    final var maxZ = Math.max(locA.getBlockZ(), locB.getBlockZ());

    for (var x = minX; x <= maxX; x++) {
      for (var y = minY; y <= maxY; y++) {
        for (var z = minZ; z <= maxZ; z++) {
          final var block = world.getBlockAt(x, y, z);
          if (block.getBlockData().matches(blockData)) count++;
        }
      }
    }

    return count;
  }

  public void show(final @NotNull Player player, final double radius, final boolean showBordersOnly, final @NotNull Vector boundaryExtension) {
    if (locA == null || locB == null || !locA.getWorld().equals(locB.getWorld()) || !player.getWorld().equals(locA.getWorld())) return;

    final var plrLoc = player.getLocation();
    final var world = locA.getWorld();

    // Define bounds with boundary extension on max sides only
    final var minX = Math.min(locA.getX(), locB.getX());
    final var maxX = Math.max(locA.getX(), locB.getX()) + boundaryExtension.getX();
    final var minY = Math.min(locA.getY(), locB.getY());
    final var maxY = Math.max(locA.getY(), locB.getY()) + boundaryExtension.getY();
    final var minZ = Math.min(locA.getZ(), locB.getZ());
    final var maxZ = Math.max(locA.getZ(), locB.getZ()) + boundaryExtension.getZ();

    // Loop through each block within the cuboid bounds with extensions
    for (var x = minX; x <= maxX; x++) {
      for (var y = minY; y <= maxY; y++) {
        for (var z = minZ; z <= maxZ; z++) {
          // Check if this block is on the border if showBordersOnly is true
          var isBorderBlock = (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ);
          if (showBordersOnly && !isBorderBlock) continue;

          // Create particle location without rounding to integer, keeping full precision
          final var particleLocation = new Location(world, x, y, z);

          // Check if the particle is within the specified radius from the player
          if (particleLocation.distance(plrLoc) <= radius) player.spawnParticle(Particle.DUST, particleLocation, 1, new Particle.DustOptions(Color.RED, 1));
        }
      }
    }
  }

}
