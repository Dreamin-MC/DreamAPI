package fr.dreamin.dreamapi.api.glowing;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public interface GlowingStats {

  int getTotalGlowingEntities();

  int getTotalGlowingBlocks();

  @NotNull Map<UUID, Integer> getViewerEntityCounts();

  @NotNull Map<UUID, Integer> getViewerBlockCounts();

  @NotNull Map<ChatColor, Integer> getColorDistribution();

  int getActiveViewers();

  long getTotalOperations();

}
