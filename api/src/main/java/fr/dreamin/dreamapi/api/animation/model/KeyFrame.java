package fr.dreamin.dreamapi.api.animation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = TitleKeyFrame.class, name = "TITLE"),
  @JsonSubTypes.Type(value = MessageKeyFrame.class, name = "MESSAGE"),
  @JsonSubTypes.Type(value = SoundKeyFrame.class, name = "SOUND"),
  @JsonSubTypes.Type(value = ParticleKeyFrame.class, name = "PARTICLE")
})
public abstract class KeyFrame {

  public enum Type {
    TITLE, MESSAGE, SOUND, PARTICLE
  }

  private final @NotNull Type type;
  private final @NotNull String value;
  private final @Nullable String boneValue;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  @JsonCreator
  public KeyFrame(
    @JsonProperty("type") final @NotNull Type type,
    @JsonProperty("value") final @NotNull String value,
    @JsonProperty("bone") final @Nullable String boneValue
  ) {
    this.type = type;
    this.value = value;
    this.boneValue = boneValue;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  public abstract void apply(final @NotNull IAnimationProperty property);

}
