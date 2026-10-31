package fr.dreamin.dreamapi.core.logger;

import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@DreamAutoService(PlayerDebugService.class)
public final class PlayerDebugServiceImpl implements PlayerDebugService, DreamService {

  private final @NotNull Map<UUID, Set<UUID>> debugTargets = new ConcurrentHashMap<>();

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public void startDebug(@NotNull Player target, @NotNull Player executor) {
    this.debugTargets.computeIfAbsent(target.getUniqueId(), _ -> ConcurrentHashMap.newKeySet())
      .add(executor.getUniqueId());
  }

  @Override
  public void stopDebug(@NotNull Player target, @NotNull Player executor) {
    this.debugTargets.computeIfPresent(target.getUniqueId(), (_, debuggers) -> {
      debuggers.remove(executor.getUniqueId());
      return debuggers.isEmpty() ? null : debuggers;
    });
  }

  @Override
  public boolean isDebugging(@NotNull Player target, @NotNull Player executor) {
    final var debuggers = this.debugTargets.get(target.getUniqueId());
    return debuggers != null && debuggers.contains(executor.getUniqueId());
  }

  @Override
  public Audience getDebuggers(@NotNull Player target) {
    return getDebuggers(target.getUniqueId());
  }

  @Override
  public Audience getDebuggers(@NotNull UUID targetId) {
    final var ids = this.debugTargets.get(targetId);
    if (ids == null || ids.isEmpty())
      return Audience.empty();

    final var debuggers = ids.stream()
      .map(Bukkit::getPlayer)
      .filter(Objects::nonNull)
      .toList();

    return debuggers.isEmpty() ? Audience.empty() : Audience.audience(debuggers);
  }

}
