package fr.dreamin.dreamapi.api.animation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

@Getter
public final class TitleKeyFrame extends KeyFrame {

  private final @NotNull String subtitle;
  private final @NotNull Title.Times time;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  @JsonCreator
  public TitleKeyFrame(
    @JsonProperty("type") final @NotNull Type type,
    @JsonProperty("value") final @NotNull String value,
    @JsonProperty("subtitle") final @NotNull String subtitle,
    @JsonProperty("fades") final long[] fades
  ) {
    super(type, value, null);
    this.subtitle = subtitle;

    if (fades != null && fades.length >= 3) {
      this.time = Title.Times.times(Duration.ofMillis(fades[0]), Duration.ofMillis(fades[1]), Duration.ofMillis(fades[2]));
      return;
    }

    this.time = Title.DEFAULT_TIMES;
  }

  public TitleKeyFrame(
    @JsonProperty("type") final @NotNull Type type,
    @JsonProperty("value") final @NotNull String value,
    @JsonProperty("subtitle") final @NotNull String subtitle
  ) {
    super(type, value, null);
    this.subtitle = subtitle;
    this.time = Title.DEFAULT_TIMES;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void apply(@NotNull IAnimationProperty property, final @NotNull List<? extends Player> players) {
    for (final var player : players)
      player.showTitle(Title.title(
        Component.translatable(getValue()),
        Component.translatable(getSubtitle()),
        this.time
      ));

  }

}
