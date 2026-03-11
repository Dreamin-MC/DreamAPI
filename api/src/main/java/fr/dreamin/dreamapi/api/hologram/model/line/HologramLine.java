package fr.dreamin.dreamapi.api.hologram.model.line;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.dreamin.dreamapi.api.hologram.model.animation.HologramAnimation;
import fr.dreamin.dreamapi.api.hologram.model.line.impl.BlockHologramLine;
import fr.dreamin.dreamapi.api.hologram.model.line.impl.CompositeHologramLine;
import fr.dreamin.dreamapi.api.hologram.model.line.impl.ItemHologramLine;
import fr.dreamin.dreamapi.api.hologram.model.line.impl.TextHologramLine;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "lineType")
@JsonSubTypes({
  @JsonSubTypes.Type(value = TextHologramLine.class, name = "TEXT"),
  @JsonSubTypes.Type(value = ItemHologramLine.class, name = "ITEM"),
  @JsonSubTypes.Type(value = BlockHologramLine.class, name = "BLOCK"),
  @JsonSubTypes.Type(value = CompositeHologramLine.class, name = "COMPOSITE")
})
public interface HologramLine {

  @NotNull String getId();

  @NotNull LineConfig getConfig();

  @Nullable HologramAnimation getAnimation();

  @NotNull List<Display> getEntities();

  void spawn(final @NotNull Location location);
  void despawn();
  void update();
  void applyAnimation(final @NotNull HologramAnimation animation, final long tick);

  boolean isSpawned();

}
