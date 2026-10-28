package fr.dreamin.dreamapi.core.skin.service;

import fr.dreamin.dreamapi.api.annotations.Inject;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.api.skin.event.PlayerSkinChangeEvent;
import fr.dreamin.dreamapi.api.skin.event.PlayerSkinResetEvent;
import fr.dreamin.dreamapi.api.skin.model.Skin;
import fr.dreamin.dreamapi.api.skin.service.SkinService;
import fr.dreamin.dreamapi.core.utils.MojangAPI;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Inject
@RequiredArgsConstructor
@DreamAutoService(SkinService.class)
public final class SkinServiceImpl implements SkinService, DreamService {

  private static final String
    BASE_TEXTURE_URL = "https://textures.minecraft.net/texture/",
    BASE_PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";

  private final @NotNull Plugin plugin;

  private final @NotNull Map<UUID, Skin> playerSkinCache = new ConcurrentHashMap<>();
  private final @NotNull Map<String, Skin> namedSkins = new HashMap<>();

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public void setSkin(@NotNull Player player, @NotNull Skin skin) {
    final var uuid = player.getUniqueId();
    final var old = this.playerSkinCache.get(uuid);
    
    if (!new PlayerSkinChangeEvent(player, old, skin).callEvent())
      return;

    this.playerSkinCache.put(uuid, skin);

    try {
      applySkinToPlayer(player, skin);
    } catch (Exception e) {
      this.plugin.getLogger().warning(String.format("Failed to apply skin to player %s: %s", player.getName(), e.getMessage()));
    }
  }

  @Override
  public CompletableFuture<Optional<Skin>> fetchSkinFromName(@NotNull String name) {
    final var trimmed = name.trim();

    return CompletableFuture.supplyAsync(() -> {
      try {
        final var profileURL = BASE_PROFILE_URL + trimmed;

        final var uuid = MojangAPI.getUUID(name);
        if (uuid == null) return Optional.empty();

        final var profiles = MojangAPI.getSkinProperties(uuid);
        if (profiles.isEmpty()) return Optional.empty();

        final var profile = profiles.getFirst();

        return Optional.of(new Skin(profile.value(), profile.signature()));

      } catch (Exception e) {
        this.plugin.getLogger().warning(String.format("Failed to fetch skin from name %s: %s", trimmed, e.getMessage()));
        return Optional.empty();
      }
    });
  }

  @Override
  public void setSkinFromName(@NotNull Player player, @NotNull String name) {
    fetchSkinFromName(name).thenAccept(skin -> {
      if (skin.isPresent())
        Bukkit.getScheduler().runTask(this.plugin, () -> setSkin(player, skin.get()));
      else
        this.plugin.getLogger().warning(String.format("Failed to fetch skin from name %s", name));
    });
  }

  @Override
  public void resetSkin(@NotNull Player player) {
    final var uuid = player.getUniqueId();
    final var previous = this.playerSkinCache.get(uuid);

    if (!new PlayerSkinResetEvent(player, previous).callEvent())
      return;

    this.playerSkinCache.remove(uuid);

    try {
      resetPlayerSkin(player);
    } catch (Exception e) {
      this.plugin.getLogger().warning(String.format("Failed to reset skin of player %s: %s", player.getName(), e.getMessage()));
    }

  }

  @Override
  public @NotNull Optional<Skin> getCurrentSkin(@NotNull Player player) {
    return Optional.ofNullable(this.playerSkinCache.get(player.getUniqueId()));
  }

  @Override
  public void registerNamedSkin(@NotNull String name, @NotNull Skin skin) {
    this.namedSkins.put(name.toLowerCase(Locale.ROOT), skin);
  }

  @Override
  public @NotNull Optional<Skin> getNamedSkin(@NotNull String name) {
    return Optional.ofNullable(this.namedSkins.get(name.toLowerCase(Locale.ROOT)));
  }

  @Override
  public void setNamedSkin(@NotNull Player player, @NotNull String name) {
    getNamedSkin(name).ifPresent(skin -> setSkin(player, skin));
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void applySkinToPlayer(final @NotNull Player player, final @NotNull Skin skin) throws MalformedURLException {
    final var profile = player.getPlayerProfile();
    final var textures = profile.getTextures();
    final var url = new URL(BASE_TEXTURE_URL + skin.value());
    textures.setSkin(url);

    profile.setTextures(textures);
    player.setPlayerProfile(profile);
    refreshPlayer(player);
  }

  private void resetPlayerSkin(final @NotNull Player player) {
    final var profile = player.getPlayerProfile();
    profile.setTextures(null);
    player.setPlayerProfile(profile);
    refreshPlayer(player);
  }

  private void refreshPlayer(final @NotNull Player player) {
    for (final var other : Bukkit.getOnlinePlayers()) {
      if (other.equals(player)) continue;
      other.hidePlayer(this.plugin, player);
      other.showPlayer(this.plugin, player);
    }
  }

}
