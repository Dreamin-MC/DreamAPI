package fr.dreamin.dreamapi.api.animation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public final class SoundKeyFrame extends KeyFrame {

  private final float volume, pitch;
  private final @NotNull SoundCategory category;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  @JsonCreator
  public SoundKeyFrame(
    @JsonProperty("type") final @NotNull Type type,
    @JsonProperty("value") final @NotNull String value,
    @JsonProperty("bone")  final String boneValue,
    @JsonProperty("volume") final Float volume,
    @JsonProperty("pitch") final Float pitch,
    @JsonProperty("category") final SoundCategory category
  ) {
    super(type, value, boneValue);
    this.volume = volume == null ? 1.0f : volume;
    this.pitch = pitch == null ? 1.0f : pitch;
    this.category = category == null ? SoundCategory.MASTER : category;
  }

  public SoundKeyFrame(
    @JsonProperty("type") final @NotNull Type type,
    @JsonProperty("value") final @NotNull String value,
    @JsonProperty("volume") final Float volume,
    @JsonProperty("pitch") final Float pitch
  ) {
    super(type, value, null);
    this.volume = volume == null ? 1.0f : volume;
    this.pitch = pitch == null ? 1.0f : pitch;
    this.category = SoundCategory.MASTER;
  }

  public SoundKeyFrame(
    @JsonProperty("type") final @NotNull Type type,
    @JsonProperty("value") final @NotNull String value,
    @JsonProperty("bone") final @NotNull String boneValue,
    @JsonProperty("volume") final Float volume,
    @JsonProperty("pitch") final Float pitch
  ) {
    super(type, value, boneValue);
    this.volume = volume == null ? 1.0f : volume;
    this.pitch = pitch == null ? 1.0f : pitch;
    this.category = SoundCategory.MASTER;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void apply(final @NotNull IAnimationProperty property, final @NotNull List<? extends Player> players) {
    if (getBoneValue() == null) {
      for (final var player : players)
        player.playSound(player, getValue(), getCategory(), getVolume(), getPitch());
    }
    else {
      final var bone = property.getModel().getBone(getBoneValue()).orElse(null);
      if (bone == null) return;

      for (final var player : players)
        player.playSound(bone.getLocation(), getValue(), getCategory(), getVolume(), getPitch());

    }
  }

}
