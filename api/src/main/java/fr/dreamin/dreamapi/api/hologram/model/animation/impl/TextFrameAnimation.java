package fr.dreamin.dreamapi.api.hologram.model.animation.impl;

import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.hologram.model.animation.HologramAnimation;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Builder
@Jacksonized
@JsonTypeName("TEXT_FRAMES")
public final class TextFrameAnimation implements HologramAnimation {

  @Builder.Default
  private final String type = "TEXT_FRAMES";

  @Singular
  private final List<String> frames;

  @Builder.Default
  private final int intervalTicks = 20;

  @Override
  public int getIntervalTicks() {
    return this.intervalTicks;
  }

  @Override
  public void apply(final @NotNull Display entity, final long tick) {
    if (!(entity instanceof TextDisplay textDisplay) || this.frames.isEmpty()) return;
    final int index = (int) ((tick / this.intervalTicks) % this.frames.size());
    final String raw = this.frames.get(index);
    final Component component = DreamAPI.LEGACY_COMPONENT_SERIALIZER.deserialize(raw);
    textDisplay.text(component);
  }
}