package fr.dreamin.dreamapi.core.animation.model;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.animation.event.AnimationStartEvent;
import fr.dreamin.dreamapi.api.animation.event.AnimationStopEvent;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import fr.dreamin.dreamapi.api.animation.service.AnimationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;


@Getter
public final class AnimationScriptRunnable extends BukkitRunnable {

  private final @NotNull IAnimationProperty property;
  private boolean started;
  private boolean stopped;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public AnimationScriptRunnable(final @NotNull IAnimationProperty property) {
    this.property = property;
    runTaskTimer(DreamAPI.getAPI().plugin(), 0L, 1L);
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void run() {
    final var animationService = DreamAPI.getAPI().getService(AnimationService.class);

    if (this.stopped)
      return;


    if (this.property.isFinished()) {
      stop(animationService, AnimationStopEvent.Reason.FINISHED);
      super.cancel();
      return;
    }

    if (!this.started) {
      final var startEvent = DreamAPI.getAPI().callEvent(new AnimationStartEvent(this.property));
      if (startEvent.isCancelled()) {
        this.stopped = true;
        super.cancel();
        return;
      }

      this.started = true;
      animationService.registerRunnable(this.property, this);
    }

    final var scriptFrames = this.property.getScriptFrame();
    if (scriptFrames == null || scriptFrames.isEmpty())
      return;

    for (final var script : scriptFrames) {
      try {
        final var keyFrames = animationService.deserializeKeyFrames(script.script());
        animationService.applyKeyFrames(this.property, keyFrames);
      } catch (final IllegalArgumentException exception) {
        final var errorMsg = "Failed to parse animation script: " + exception.getMessage();
        DreamAPI.getAPI().getLogger().warning(errorMsg);
        if (exception.getCause() != null) {
          DreamAPI.getAPI().getLogger().warning("Cause: " + exception.getCause().getMessage());
        }
      }
    }

  }

  @Override
  public synchronized void cancel() throws IllegalStateException {
    if (this.stopped)
      return;


    if (!this.started) {
      this.stopped = true;
      super.cancel();
      return;
    }

    stop(DreamAPI.getAPI().getService(AnimationService.class), AnimationStopEvent.Reason.CANCELLED);
    super.cancel();
  }

  private void stop(@NotNull AnimationService animationService, @NotNull AnimationStopEvent.Reason reason) {
    if (this.stopped)
      return;

    this.stopped = true;
    if (this.started) {
      animationService.unregisterRunnable(this.property);
      DreamAPI.getAPI().callEvent(new AnimationStopEvent(this.property, reason));
    }
  }



}
