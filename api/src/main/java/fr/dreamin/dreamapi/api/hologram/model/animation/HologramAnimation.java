package fr.dreamin.dreamapi.api.hologram.model.animation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.dreamin.dreamapi.api.hologram.model.animation.impl.ColorCycleAnimation;
import fr.dreamin.dreamapi.api.hologram.model.animation.impl.PulseAnimation;
import fr.dreamin.dreamapi.api.hologram.model.animation.impl.RotateAnimation;
import fr.dreamin.dreamapi.api.hologram.model.animation.impl.TextFrameAnimation;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = RotateAnimation.class, name = "ROTATE_Y"),
  @JsonSubTypes.Type(value = ColorCycleAnimation.class, name = "COLOR_CYCLE"),
  @JsonSubTypes.Type(value = PulseAnimation.class, name = "PULSE"),
  @JsonSubTypes.Type(value = TextFrameAnimation.class, name = "TEXT_FRAMES")
})
public interface HologramAnimation {

  String getType();

  int getIntervalTicks();

  void apply(final @NotNull Display entity, final long tick);

}
