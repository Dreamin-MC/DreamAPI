package fr.dreamin.dreamapi.api.animation.service;

import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import fr.dreamin.dreamapi.api.animation.model.KeyFrame;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AnimationService {

  @NotNull List<KeyFrame> deserializeKeyFrames(@NotNull String script);

  void applyKeyFrames(@NotNull IAnimationProperty property, @NotNull List<KeyFrame> keyFrames);

  void registerRunnable(@NotNull IAnimationProperty property, @NotNull BukkitRunnable runnable);

  void unregisterRunnable(@NotNull IAnimationProperty property);

  @Nullable BukkitRunnable getRunnable(@NotNull IAnimationProperty property);

  default boolean isRunning(@NotNull IAnimationProperty property) {
	return getRunnable(property) != null;
  }
}
