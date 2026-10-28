package fr.dreamin.dreamapi.api.skin.service;

import fr.dreamin.dreamapi.api.skin.model.Skin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface SkinService {

  void setSkin(final @NotNull Player player, final @NotNull Skin skin);

  CompletableFuture<Optional<Skin>> fetchSkinFromName(final @NotNull String name);

  void setSkinFromName(final @NotNull Player player, final @NotNull String name);

  void resetSkin(final @NotNull Player player);

  @NotNull Optional<Skin> getCurrentSkin(final @NotNull Player player);

  void registerNamedSkin(final @NotNull String name, final @NotNull Skin skin);

  @NotNull Optional<Skin> getNamedSkin(final @NotNull String name);

  void setNamedSkin(final @NotNull Player player, final @NotNull String name);

}
