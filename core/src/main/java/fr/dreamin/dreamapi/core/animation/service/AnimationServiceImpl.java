package fr.dreamin.dreamapi.core.animation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.animation.model.KeyFrame;
import fr.dreamin.dreamapi.api.animation.service.AnimationService;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DreamAutoService(AnimationService.class)
public final class AnimationServiceImpl implements DreamService, AnimationService {

  private final @NotNull Map<IAnimationProperty, BukkitRunnable> runningRunnables = new HashMap<>();

  @Override
  public void onClose() {
		final var runnables = new ArrayList<>(this.runningRunnables.values());
		this.runningRunnables.clear();

		for (final var runnable : runnables) {
			runnable.cancel();
		}
  }

  @Override
  public @NotNull List<KeyFrame> deserializeKeyFrames(@NotNull String script) {
		if (script.isBlank())
			return List.of();

		try {
			final var root = Configurations.MAPPER.readTree(script);
			if (root == null || root.isNull())
				return List.of();

			if (root.isArray()) {
				final var keyFrames = new ArrayList<KeyFrame>();
				for (int i = 0; i < root.size(); i++) {
					try {
						final var node = root.get(i);
						keyFrames.add(deserializeKeyFrame(node));
					} catch (final Exception e) {
						DreamAPI.getAPI().getLogger().warning("Failed to parse keyframe at index " + i + ": " + e.getMessage());
						throw e;
					}
				}
				return List.copyOf(keyFrames);
			}

			return List.of(deserializeKeyFrame(root));
		} catch (final Exception exception) {
			// Log the problematic JSON for debugging
			final var preview = script.substring(0, Math.min(200, script.length()));
			final var errorMsg = "Unable to parse animation keyframes\nJSON Preview: " + preview + (script.length() > 200 ? "..." : "");
			DreamAPI.getAPI().getLogger().warning(errorMsg);
			throw new IllegalArgumentException(exception.getMessage(), exception);
		}
  }

  @Override
  public void applyKeyFrames(@NotNull IAnimationProperty property, @NotNull List<KeyFrame> keyFrames) {
		for (final var keyFrame : keyFrames) {
			try {
				keyFrame.apply(property);
			} catch (final RuntimeException exception) {
				DreamAPI.getAPI().getLogger().warning("Failed to apply animation keyframe " + keyFrame.getClass().getSimpleName() + ": " + exception.getMessage());
			}
		}
  }

  @Override
  public synchronized void registerRunnable(@NotNull IAnimationProperty property, @NotNull BukkitRunnable runnable) {
		final var previous = this.runningRunnables.get(property);
		if (previous != null && previous != runnable)
			previous.cancel();

		this.runningRunnables.put(property, runnable);
  }

  @Override
  public synchronized void unregisterRunnable(@NotNull IAnimationProperty property) {
		this.runningRunnables.remove(property);
  }

  @Override
  public synchronized BukkitRunnable getRunnable(@NotNull IAnimationProperty property) {
		return this.runningRunnables.get(property);
  }

  private @NotNull KeyFrame deserializeKeyFrame(@NotNull JsonNode node) {
		final var typeNode = node.get("type");
		if (typeNode == null || typeNode.isNull()) {
			throw new IllegalArgumentException("Animation keyframe is missing the 'type' property. JSON: " + node.toString());
		}

		final var typeName = typeNode.asText();
		try {
			// Jackson gerera automatiquement la polymorphie via @JsonTypeInfo
			return Configurations.MAPPER.treeToValue(node, KeyFrame.class);
		} catch (final Exception e) {
			final var msg = "Failed to deserialize keyframe of type '" + typeName + "'";
			DreamAPI.getAPI().getLogger().warning(msg + ": " + e.getMessage());
			throw new IllegalArgumentException(msg + ": " + e.getMessage(), e);
		}
  }


}
