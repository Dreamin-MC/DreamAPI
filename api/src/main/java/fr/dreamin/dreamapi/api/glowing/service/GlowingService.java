package fr.dreamin.dreamapi.api.glowing.service;

import fr.dreamin.dreamapi.api.glowing.GlowingStats;
import fr.dreamin.dreamapi.api.glowing.animation.GlowAnimation;
import fr.dreamin.dreamapi.api.glowing.block.GlowingBlockManager;
import fr.dreamin.dreamapi.api.glowing.entity.GlowingEntityManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Service for managing glowing effects on entities and blocks.
 * Uses Paper 1.21.10-compatible mechanics via GlowingEntities/GlowingBlocks.
 */
public interface GlowingService {

  @NotNull GlowingEntityManager getEntityManager();
  @NotNull GlowingBlockManager getGlowingBlocks();

  // ###############################################################
  // --------------------------- ENTITY ----------------------------
  // ###############################################################

  /** Make an entity glow for one or more viewers (no auto-unset). */
  void glowEntity(final @NotNull Entity entity, final @NotNull ChatColor color, final @NotNull Player... viewers);

  /** Make an entity glow for viewers, auto-unset after duration ticks. */
  void glowEntity(final @NotNull Entity entity, final @NotNull ChatColor color, final long durationTicks, final @NotNull Player... viewers);

  /** Stop glowing an entity for the given viewers (if none passed, stop for all viewers). */
  void stopEntity(final @NotNull Entity entity, final @NotNull Player... viewers);

  // ###############################################################
  // --------------------------- BLOCK -----------------------------
  // ###############################################################

  /** Make a block glow for one or more viewers (no auto-unset). */
  void glowBlock(final @NotNull Block block, final @NotNull ChatColor color, final @NotNull Player... viewers);

  /** Make a block glow for viewers, auto-unset after duration ticks. */
  void glowBlock(final @NotNull Block block, final @NotNull ChatColor color, final long durationTicks, final @NotNull Player... viewers);

  /** Stop glowing a block for the given viewers (if none passed, stop for all viewers). */
  void stopBlock(final @NotNull Block block, final @NotNull Player... viewers);

  // ###############################################################
  // ------------------------- ANIMATIONS --------------------------
  // ###############################################################

  /** Make an entity glow with an animated color */
  void glowEntityAnimated(final @NotNull Entity entity, final @NotNull GlowAnimation animation, final @NotNull Player... viewers);

  /** Make an entity glow with an animated color for a duration */
  void glowEntityAnimated(final @NotNull Entity entity, final @NotNull GlowAnimation animation, final long durationTicks, final @NotNull Player... viewers);

  /** Make a block glow with an animated color */
  void glowBlockAnimated(final @NotNull Block block, final @NotNull GlowAnimation animation, final @NotNull Player... viewers);

  /** Make a block glow with an animated color for a duration */
  void glowBlockAnimated(final @NotNull Block block, final @NotNull GlowAnimation animation, final long durationTicks, final @NotNull Player... viewers);

  // ###############################################################
  // ----------------------- CONDITIONAL ---------------------------
  // ###############################################################

  /** Glow entities matching a condition, auto-updated */
  void glowEntitiesMatching(final @NotNull Predicate<Entity> condition, final @NotNull ChatColor color, final long checkInterval, final @NotNull Player viewer);

  /** Stop conditional glowing for a viewer */
  void stopConditionalGlowing(final @NotNull Player viewer);

  // ###############################################################
  // ------------------------- BY ZONE -----------------------------
  // ###############################################################

  /** Glow all entities in a radius */
  void glowEntitiesInRadius(final @NotNull Location center, final double radius, final @Nullable EntityType type, final @NotNull ChatColor color, final @NotNull Player... viewers);

  /** Glow all blocks in a radius */
  void glowBlocksInRadius(final @NotNull Location center, final double radius, final @NotNull ChatColor color, final @NotNull Player... viewers);

  // ###############################################################
  // --------------------- LINE OF SIGHT ---------------------------
  // ###############################################################

  /** Glow the entity the player is looking at */
  @Nullable Entity glowEntityInCrosshair(final @NotNull Player viewer, final int maxDistance, final @NotNull ChatColor color, final long duration);

  /** Glow the block the player is looking at */
  @Nullable Block glowBlockInCrosshair(final @NotNull Player viewer, final int maxDistance, final @NotNull ChatColor color, final long duration);

  /** Glow all entities visible to the player */
  void glowVisibleEntities(final @NotNull Player viewer, final double maxDistance, final @Nullable Predicate<Entity> filter, final @NotNull ChatColor color);

  /** Glow all blocks visible to the player */
  void glowVisibleBlocks(final @NotNull Player viewer, final double maxDistance, final @NotNull ChatColor color);

  // ###############################################################
  // ----------------------- BULK / VIEWER -------------------------
  // ###############################################################

  /** Clear all glows (entities & blocks) visible by this viewer. */
  void clearForViewer(final @NotNull Player viewer);

  /** Re-apply all glows for a viewer (e.g., on join). */
  void reapplyForViewer(final @NotNull Player viewer);

  /** Re-apply glowing for a specific target player for all viewers. */
  void reapplyTargetPlayerForAllViewers(final @NotNull Player target);

  // ###############################################################
  // --------------------------- QUERIES ---------------------------
  // ###############################################################

  /** Returns the set of glowing entities seen by the viewer. */
  Set<Entity> getGlowingEntities(final @NotNull Player viewer);

  /** Returns the set of glowing blocks seen by the viewer. */
  Set<Block> getGlowingBlocks(final @NotNull Player viewer);

  @NotNull GlowingStats getStats();

}
