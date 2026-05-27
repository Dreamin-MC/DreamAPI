package fr.dreamin.dreamapi.api.animation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@Getter
public final class CameraKeyFrame extends KeyFrame {

  public enum CameraType {
    START, MIDDLE, END
  }

  public enum CameraTag {
    CANCEL_MOVE,
    SAVE_INVENTORY,
    SAVE_LOCATION,
    SAVE_GAME_MODE,
    SET_GAME_MODE_CREATIVE,
    SET_GAME_MODE_SURVIVAL,
    SET_GAME_MODE_ADVENTURE,
    SET_GAME_MODE_SPECTATOR,
    RESTORE_MOVE,
    RESTORE_INVENTORY,
    RESTORE_LOCATION,
    RESTORE_GAME_MODE
  }

  private final @NotNull CameraType cameraType;
  private final @NotNull CameraTag[] cameraTags;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  @JsonCreator
  public CameraKeyFrame(
    @JsonProperty("type") final @NotNull Type type,
    @JsonProperty("value") final @NotNull String value,
    @JsonProperty("bone") final @NotNull String boneValue,
    @JsonProperty("tags") final @NotNull CameraTag[] tags
  ) {
    super(type, value, boneValue);
    this.cameraType = CameraType.valueOf(value);
    this.cameraTags = tags;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void apply(@NotNull IAnimationProperty property, final @NotNull List<? extends Player> players) {
    final var bone = property.getModel().getBone(getBoneValue()).orElse(null);
    if (bone == null) return;

    setGameMode(players);

    for (final var player : players)
      player.teleport(bone.getLocation());

  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public boolean hasTags(final @NotNull CameraTag cameraTag) {
    for (final var tag : cameraTags)
      if (tag.equals(cameraTag)) return true;
    return false;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private boolean hasGameModeTags() {
    if (this.cameraTags == null) return false;

    for (final var tag : this.cameraTags)
      if (tag.name().startsWith("SET_GAME_MODE")) return true;

    return false;
  }

  private @Nullable GameMode getGameMode() {
    if (this.cameraTags == null) return null;

    for (final var tag : this.cameraTags)
      if (tag.name().startsWith("SET_GAME_MODE"))
        return GameMode.valueOf(tag.name().substring(0, "SET_GAME_MODE_".length()));

    return null;
  }

  private void setGameMode(final @NotNull Collection<? extends Player> players) {
    if (!hasGameModeTags()) return;
    final var gamemode = getGameMode();
    if (gamemode == null) return;

    for (final var player : players)
      player.setGameMode(gamemode);
  }

}
