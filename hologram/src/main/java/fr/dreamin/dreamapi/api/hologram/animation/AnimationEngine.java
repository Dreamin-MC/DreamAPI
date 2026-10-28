package fr.dreamin.dreamapi.api.hologram.animation;

import fr.dreamin.dreamapi.api.hologram.event.HologramUpdateEvent;
import fr.dreamin.dreamapi.api.hologram.model.Hologram;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class AnimationEngine {

  private final @NotNull Plugin plugin;
  private final @NotNull Supplier<Collection<Hologram>> hologramSupplier;
  private BukkitTask task;
  private int currentTick = 0;

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public void start() {
    if (this.task != null) return;
    this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 1L, 1L);
  }

  public void stop() {
    if (this.task == null) return;
    this.task.cancel();
    this.task = null;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void tick() {
    final var tick = this.currentTick++;
    for (final var hologram : this.hologramSupplier.get()) {
      if (!hologram.isSpawned()) continue;

      hologram.tick(tick);

      if (tick % hologram.getConfig().getUpdateIntervalTicks() == 0)
        new HologramUpdateEvent(hologram.getId(), hologram, tick).callEvent();

    }
  }

}
