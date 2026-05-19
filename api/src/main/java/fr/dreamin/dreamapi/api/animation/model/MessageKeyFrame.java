package fr.dreamin.dreamapi.api.animation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public final class MessageKeyFrame extends KeyFrame {

  public enum SendType {
    CHAT, ACTION_BAR
  }

  private final @NotNull SendType sendType;
  private final boolean translatable;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  @JsonCreator
  public MessageKeyFrame(
    @JsonProperty("type") final @NotNull Type type,
    @JsonProperty("value") final @NotNull String value,
    @JsonProperty("sendType") final SendType sendType,
    @JsonProperty("translatable") final Boolean translatable
  ) {
    super(type, value, null);
    this.sendType = sendType == null ? SendType.CHAT : sendType;
    this.translatable = translatable != null && translatable;
  }

  public MessageKeyFrame(
    @JsonProperty("type") final @NotNull Type type,
    @JsonProperty("value") final @NotNull String value
  ) {
    super(type, value, null);
    this.sendType = SendType.CHAT;
    this.translatable = false;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void apply(@NotNull IAnimationProperty property, final @NotNull List<? extends Player> players) {
    final var component = this.isTranslatable() ? Component.translatable(getValue()) : Component.text(getValue());

    switch (this.sendType) {
      case CHAT -> {
        for (final var player : players)
          player.sendMessage(component);
      }
      case ACTION_BAR -> {
        for (final var player : players)
          player.sendActionBar(component);
      }
    }

  }

}
