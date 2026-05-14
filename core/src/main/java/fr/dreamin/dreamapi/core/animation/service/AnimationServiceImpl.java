package fr.dreamin.dreamapi.core.animation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.animation.model.KeyFrame;
import fr.dreamin.dreamapi.api.animation.model.MessageKeyFrame;
import fr.dreamin.dreamapi.api.animation.model.ParticleKeyFrame;
import fr.dreamin.dreamapi.api.animation.model.SoundKeyFrame;
import fr.dreamin.dreamapi.api.animation.model.TitleKeyFrame;
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
				for (final var node : root) {
					keyFrames.add(deserializeKeyFrame(node));
				}
				return List.copyOf(keyFrames);
			}

			return List.of(deserializeKeyFrame(root));
		} catch (final Exception exception) {
			throw new IllegalArgumentException("Unable to parse animation keyframes", exception);
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

  private @NotNull KeyFrame deserializeKeyFrame(@NotNull JsonNode node) throws JsonProcessingException {
		final var typeNode = node.get("type");
		if (typeNode == null || typeNode.isNull())
			throw new IllegalArgumentException("Animation keyframe is missing the 'type' property");

		return switch (KeyFrame.Type.valueOf(typeNode.asText())) {
			case TITLE -> Configurations.MAPPER.treeToValue(node, TitleKeyFrame.class);
			case MESSAGE -> Configurations.MAPPER.treeToValue(node, MessageKeyFrame.class);
			case SOUND -> Configurations.MAPPER.treeToValue(node, SoundKeyFrame.class);
			case PARTICLE -> Configurations.MAPPER.treeToValue(node, ParticleKeyFrame.class);
		};
  }


}
