package fr.dreamin.dreamapi.plugin.cmd.admin.debug;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.logger.DebugService;
import fr.dreamin.dreamapi.api.logger.DebugWriter;
import fr.dreamin.dreamapi.core.logger.PlayerDebugService;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class DebugCmd {

  private final DebugService debug = DreamAPI.getAPI().getService(DebugService.class);
  private final PlayerDebugService playerDebug = DreamAPI.getAPI().getService(PlayerDebugService.class);


  @Suggestions("debug-writers")
  public List<String> suggestWriters(CommandContext<CommandSender> sender, String input) {
    return debug.getWriters().stream()
      .map(w -> w.getClass().getSimpleName())
      .toList();
  }

  @Suggestions("debug-categories")
  public List<String> suggestCategories(CommandContext<CommandSender> sender, String input) {
    if (this.debug.getCategories().isEmpty()) return List.of();
    return this.debug.getCategories().keySet().stream().toList();
  }

  // ###############################################################
  // ------------------------- DEBUG INFO --------------------------
  // ###############################################################

  @CommandDescription("Show debug configuration")
  @CommandMethod("debug info")
  @CommandPermission("dreamapi.cmd.debug.info")
  private void onInfo(CommandSender sender) {

    final Component[] rs = {Component.newline()
      .append(Component.text("DreamAPI Debug Settings", NamedTextColor.GOLD))
      .appendNewline()
      .append(Component.text("Global: " + (this.debug.isGlobalDebug() ? "ON" : "OFF"), NamedTextColor.GOLD))
      .appendNewline()
      .append(Component.text("Retention Days: " + this.debug.getRetentionDays(), NamedTextColor.GOLD))
      .appendNewline()};

    if (this.debug.getCategories().isEmpty())
      rs[0] = rs[0].append(Component.text("Categories: none", NamedTextColor.GOLD));
    else {
      rs[0] = rs[0].append(Component.text("Categories: ", NamedTextColor.GOLD));

      this.debug.getCategories().forEach((cat, active) ->
        rs[0] = rs[0].appendNewline().append(Component.text(String.format(" - %s = %s", cat, (active ? "ON" : "OFF")), NamedTextColor.GOLD))
      );
    }

    rs[0] = rs[0].appendNewline();

    sender.sendMessage(rs[0]);
  }

  // ###############################################################
  // ------------------------ GLOBAL DEBUG -------------------------
  // ###############################################################

  @CommandDescription("Enable or disable global debug")
  @CommandMethod("debug global <state>")
  @CommandPermission("dreamapi.cmd.debug.global")
  private void onGlobal(CommandSender sender, @Argument(value = "state", suggestions = "onoff") String state) {
    final var enabled = state.equalsIgnoreCase("on");

    this.debug.setGlobalDebug(enabled);

    sender.sendMessage(Component.text(String.format("Global debug = %s", (enabled ? "ON": "OFF")), NamedTextColor.GOLD));
  }


  // ###############################################################
  // ------------------------- CATEGORIES --------------------------
  // ###############################################################

  @CommandDescription("Enable or disable a debug category")
  @CommandMethod("debug category <name> <state>")
  @CommandPermission("dreamapi.cmd.debug.category")
  private void onCategory(
    CommandSender sender,
    @Argument(value = "name", suggestions = "debug-categories") String category,
    @Argument(value = "state", suggestions = "onoff") String state
  ) {
    final var enabled = state.equalsIgnoreCase("on");
    this.debug.setCategory(category, enabled);

    sender.sendMessage(Component.text(String.format("Category %s = %s", category, (enabled ? "ON" : "OFF")), NamedTextColor.GOLD));
  }

  // ###############################################################
  // -------------------------- RETENTION --------------------------
  // ###############################################################

  @CommandDescription("Set how many days worth of logs to retain")
  @CommandMethod("debug retention <days>")
  @CommandPermission("dreamapi.cmd.debug.retention")
  private void onRetention(CommandSender sender, @Argument("days") int days) {
    this.debug.setRetentionDays(days);
    sender.sendMessage(Component.text(String.format("Log retention set to %s days.", days), NamedTextColor.GOLD));
  }

  @CommandDescription("Manually clean up old logs")
  @CommandMethod("debug cleanup")
  @CommandPermission("dreamapi.cmd.debug.cleanup")
  private void onCleanup(CommandSender sender) {
    this.debug.cleanupOldLogs();
    sender.sendMessage(Component.text("Old logs cleaned successfully.", NamedTextColor.GOLD));
  }

  // ###############################################################
  // ------------------------ TARGET DEBUGGING ---------------------
  // ###############################################################

  @CommandDescription("Add an executor who will receive debug logs for a target player")
  @CommandMethod("debug target add <target>")
  @CommandPermission("dreamapi.cmd.debug.target")
  private void onTargetAdd(
    CommandSender sender,
    @Argument("target") Player target
  ) {
    if (!(sender instanceof Player player)) return;

    this.playerDebug.startDebug(target, player);

    player.sendMessage(Component.text(String.format("Added debugger for %s", target.getName()), NamedTextColor.GREEN));
  }

  @CommandDescription("Remove an executor from a player debug ")
  @CommandMethod("debug target remove <target>")
  @CommandPermission("dreamapi.cmd.debug.target")
  private void onTargetRemove(
    CommandSender sender,
    @Argument("target") Player target
  ) {
    if (!(sender instanceof Player player)) return;

    this.playerDebug.stopDebug(target, player);

    player.sendMessage(Component.text(String.format("Removed debugger for %s", target.getName()), NamedTextColor.GREEN));
  }

  @CommandDescription("List all executors debugging a target player")
  @CommandMethod("debug target list <target>")
  @CommandPermission("dreamapi.cmd.debug.target")
  private void onTargetList(
    CommandSender sender,
    @Argument("target") Player target
  ) {
    if (!(sender instanceof Player player)) return;
    final var audience = this.playerDebug.getDebuggers(target);

    if (audience == Audience.empty()) {
      player.sendMessage(Component.text(String.format("%s has no active debuggers.", target.getName()), NamedTextColor.GOLD));
      return;
    }

    AtomicReference<Component> msg = new AtomicReference<>(Component.text(String.format("Debuggers for %s :", target.getName()), NamedTextColor.GOLD));

    audience.forEachAudience(a -> {
      if (a instanceof Player p)
        msg.set(msg.get().appendNewline().append(
          Component.text(" -", NamedTextColor.DARK_GRAY)
            .append(Component.text(p.getName(), NamedTextColor.WHITE))
        ));
    });

    player.sendMessage(msg.get());
  }

  // ###############################################################
  // ------------------------ DEBUG WRITER -------------------------
  // ###############################################################

  @CommandDescription("List all debug writers")
  @CommandMethod("debug writer list")
  @CommandPermission("dreamapi.cmd.debug.writer")
  private void onWriterList(CommandSender sender) {

    Component msg = Component.text("Debug Writers:", NamedTextColor.GOLD);

    for (DebugWriter writer : debug.getWriters()) {
      boolean enabled = writer.isEnabled();

      msg = msg.appendNewline().append(
        Component.text(
          " - " + writer.getClass().getSimpleName() + " = " + (enabled ? "ON" : "OFF"),
          enabled ? NamedTextColor.GREEN : NamedTextColor.RED
        )
      );
    }

    sender.sendMessage(msg);
  }

  @CommandDescription("Enable or disable a debug writer")
  @CommandMethod("debug writer <writer> <state>")
  @CommandPermission("dreamapi.cmd.debug.writer")
  private void onWriterToggle(
    CommandSender sender,
    @Argument(value = "writer", suggestions = "debug-writers") String writerName,
    @Argument(value = "state", suggestions = "onoff") String state
  ) {

    final var clazz = resolveWriterClass(writerName);
    if (clazz == null) {
      sender.sendMessage(Component.text("Unknown writer: " + writerName, NamedTextColor.RED));
      return;
    }

    boolean enabled = state.equalsIgnoreCase("on");

    this.debug.setWriterEnabled(clazz, enabled);

    sender.sendMessage(Component.text(
      String.format("Writer %s = %s", writerName, enabled ? "ON" : "OFF"),
      NamedTextColor.GOLD
    ));
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private Class<? extends DebugWriter> resolveWriterClass(String name) {
    for (DebugWriter writer : debug.getWriters()) {
      if (writer.getClass().getSimpleName().equalsIgnoreCase(name))
        return writer.getClass();
    }
    return null;
  }

}
