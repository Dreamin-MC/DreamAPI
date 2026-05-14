package fr.dreamin.dreamapi.api.animation.model;

import com.destroystokyo.paper.ParticleBuilder;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import lombok.Getter;
import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

@Getter
public final class ParticleKeyFrame extends KeyFrame {

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  private final @NotNull Particle particle;
  private final int count;
  private final @NotNull Double[] offsets;
  private final double speed;
  private final boolean force;

  @JsonCreator
  public ParticleKeyFrame(
    @JsonProperty("type") final @NotNull Type type,
    @JsonProperty("value") final @NotNull String value,
    @JsonProperty("bone")  final @NotNull String boneValue,
    @JsonProperty("count") final int count,
    @JsonProperty("offsets") final Double[] offsets,
    @JsonProperty("speed") final double speed,
    @JsonProperty("force") final boolean force
  ) {
    super(type, value, boneValue);
    this.particle = Particle.valueOf(value);
    this.count = count;
    this.offsets = (offsets != null && offsets.length >= 3) ? offsets : new Double[]{0.0, 0.0, 0.0};
    this.speed = speed;
    this.force = force;
  }

  public ParticleKeyFrame(
    @JsonProperty("type") final @NotNull Type type,
    @JsonProperty("value") final @NotNull String value,
    @JsonProperty("bone")  final @NotNull String boneValue,
    @JsonProperty("count") final int count,
    @JsonProperty("speed") final double speed,
    @JsonProperty("force") final boolean force
  ) {
    super(type, value, boneValue);
    this.particle = Particle.valueOf(value);
    this.count = count;
    this.offsets = new Double[]{0.0, 0.0, 0.0};
    this.speed = speed;
    this.force = force;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void apply(@NotNull IAnimationProperty property) {
    final var bone = property.getModel().getBone(getBoneValue()).orElse(null);
    if (bone == null) return;

    final var particle = new ParticleBuilder(this.particle)
      .count(this.count)
      .offset(this.offsets[0], this.offsets[1], this.offsets[2])
      .extra(this.speed)
      .location(bone.getLocation())
      .force(this.force);

    particle.spawn();

  }


}
