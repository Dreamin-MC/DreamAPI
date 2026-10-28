package fr.dreamin.dreamapi.core.logger.writer;

import fr.dreamin.dreamapi.api.logger.DebugWriter;
import fr.dreamin.dreamapi.api.logger.LogEntry;
import fr.dreamin.dreamapi.api.annotations.Inject;
import fr.dreamin.dreamapi.core.logger.PlayerDebugService;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Inject
@RequiredArgsConstructor
public final class PlayerDebugWriter implements DebugWriter {

  private final @NotNull PlayerDebugService debugService;

  private volatile boolean enabled = true;

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void write(@NotNull LogEntry entry) throws Exception {
    final var meta = entry.meta();
    if (!meta.hasTarget()) return;

    assert meta.targetPlayer() != null;
    final var target = Bukkit.getPlayer(meta.targetPlayer());
    if (target == null) return;

    final var audience = this.debugService.getDebuggers(target);
    if (audience == Audience.empty()) return;

    Component msg = Component.empty()
      .append(Component.text("[DEBUG] ", NamedTextColor.DARK_GRAY))
      .append(Component.text(String.format("[%s][%s]", entry.level().name(), entry.threadName()), NamedTextColor.DARK_GRAY))
      .append(Component.text("[" + entry.category() + "::" + entry.context().methodName() + ":" + entry.context().lineNumber() + "] ",
        NamedTextColor.DARK_GRAY))
      .append(Component.text(entry.message(), NamedTextColor.WHITE));

    audience.sendMessage(msg);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private Player resolveTarget(final @NotNull String category) {
    if (!category.startsWith("Player=")) return null;

    final var key = category.substring("Player=".length());

    try {
      final var id = UUID.fromString(key);
      return Bukkit.getPlayer(id);
    } catch (IllegalArgumentException ignored) {}


    return Bukkit.getPlayerExact(key);
  }

}
