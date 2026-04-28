package fr.dreamin.dreamapi.plugin.cmd.admin.nms.tablist;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.nms.tablist.service.TabListMode;
import fr.dreamin.dreamapi.api.nms.tablist.service.TabListService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class TabListCmd {

  private final @NotNull TabListService tabListService = DreamAPI.getAPI().getService(TabListService.class);

  @CommandDescription("Show tab-list service global status")
  @CommandMethod("tablist status")
  @CommandPermission("dreamapi.cmd.tablist.status")
  private void onStatus(final @NotNull CommandSender sender) {
    sender.sendMessage(Component.text("TabList auto: ", NamedTextColor.GRAY)
      .append(Component.text(this.tabListService.isAutoEnabled() ? "ON" : "OFF",
        this.tabListService.isAutoEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED))
      .append(Component.text(" | default: ", NamedTextColor.DARK_GRAY))
      .append(Component.text(this.tabListService.getDefaultMode().name(), NamedTextColor.YELLOW)));
  }

  @CommandDescription("Enable/disable automatic default mode application on join")
  @CommandMethod("tablist auto <state>")
  @CommandPermission("dreamapi.cmd.tablist.auto")
  private void onAuto(
    final @NotNull CommandSender sender,
    final @Argument(value = "state", suggestions = "onoff") @NotNull String state
  ) {
    final var enabled = parseOnOff(state);
    if (enabled == null) {
      sender.sendMessage(Component.text("Invalid state. Use on/off.", NamedTextColor.RED));
      return;
    }

    this.tabListService.setAutoEnabled(enabled);
    sender.sendMessage(Component.text("TabList auto mode ", NamedTextColor.GRAY)
      .append(Component.text(enabled ? "enabled" : "disabled", enabled ? NamedTextColor.GREEN : NamedTextColor.YELLOW))
      .append(Component.text(".", NamedTextColor.GRAY)));
  }

  @CommandDescription("Show default tab-list mode")
  @CommandMethod("tablist default")
  @CommandPermission("dreamapi.cmd.tablist.default")
  private void onDefaultStatus(final @NotNull CommandSender sender) {
    sender.sendMessage(Component.text("Default tab-list mode: ", NamedTextColor.GRAY)
      .append(Component.text(this.tabListService.getDefaultMode().name(), NamedTextColor.YELLOW)));
  }

  @CommandDescription("Set default tab-list mode")
  @CommandMethod("tablist default <mode>")
  @CommandPermission("dreamapi.cmd.tablist.default")
  private void onDefaultSet(
    final @NotNull CommandSender sender,
    final @Argument(value = "mode", suggestions = "tabListModes") @NotNull String modeName
  ) {
    final var mode = parseMode(modeName);
    if (mode == null) {
      sender.sendMessage(Component.text("Invalid mode. Use visible/empty/hidden.", NamedTextColor.RED));
      return;
    }

    this.tabListService.setDefaultMode(mode);
    sender.sendMessage(Component.text("Default tab-list mode set to ", NamedTextColor.GRAY)
      .append(Component.text(mode.name(), NamedTextColor.YELLOW))
      .append(Component.text(".", NamedTextColor.GRAY)));
  }

  @CommandDescription("Show effective/custom tab-list mode for a player")
  @CommandMethod("tablist info <player>")
  @CommandPermission("dreamapi.cmd.tablist.info")
  private void onInfo(
    final @NotNull CommandSender sender,
    final @Argument("player") @NotNull Player target
  ) {
    final var effectiveMode = this.tabListService.getMode(target);
    final var custom = this.tabListService.hasCustomMode(target);

    sender.sendMessage(Component.text("TabList for ", NamedTextColor.GRAY)
      .append(Component.text(target.getName(), NamedTextColor.WHITE))
      .append(Component.text(": mode=", NamedTextColor.DARK_GRAY))
      .append(Component.text(effectiveMode.name(), NamedTextColor.YELLOW))
      .append(Component.text(" | custom=", NamedTextColor.DARK_GRAY))
      .append(Component.text(custom ? "yes" : "no", custom ? NamedTextColor.GREEN : NamedTextColor.RED)));
  }

  @CommandDescription("Set custom tab-list mode for a player")
  @CommandMethod("tablist mode <player> <mode>")
  @CommandPermission("dreamapi.cmd.tablist.mode")
  private void onMode(
    final @NotNull CommandSender sender,
    final @Argument("player") @NotNull Player target,
    final @Argument(value = "mode", suggestions = "tabListModes") @NotNull String modeName
  ) {
    final var mode = parseMode(modeName);
    if (mode == null) {
      sender.sendMessage(Component.text("Invalid mode. Use visible/empty/hidden.", NamedTextColor.RED));
      return;
    }

    this.tabListService.setMode(target, mode);
    sender.sendMessage(Component.text("Custom tab-list mode for ", NamedTextColor.GRAY)
      .append(Component.text(target.getName(), NamedTextColor.WHITE))
      .append(Component.text(" set to ", NamedTextColor.GRAY))
      .append(Component.text(mode.name(), NamedTextColor.YELLOW))
      .append(Component.text(".", NamedTextColor.GRAY)));
  }

  @CommandDescription("Clear custom tab-list mode override for a player")
  @CommandMethod("tablist reset <player>")
  @CommandPermission("dreamapi.cmd.tablist.reset")
  private void onReset(
    final @NotNull CommandSender sender,
    final @Argument("player") @NotNull Player target
  ) {
    this.tabListService.clearMode(target);
    sender.sendMessage(Component.text("Custom tab-list mode cleared for ", NamedTextColor.GRAY)
      .append(Component.text(target.getName(), NamedTextColor.WHITE))
      .append(Component.text(".", NamedTextColor.GRAY)));
  }

  @CommandDescription("Force tab-list refresh for a player")
  @CommandMethod("tablist refresh <player>")
  @CommandPermission("dreamapi.cmd.tablist.refresh")
  private void onRefresh(
    final @NotNull CommandSender sender,
    final @Argument("player") @NotNull Player target
  ) {
    this.tabListService.refresh(target);
    sender.sendMessage(Component.text("Tab-list refreshed for ", NamedTextColor.GRAY)
      .append(Component.text(target.getName(), NamedTextColor.WHITE))
      .append(Component.text(".", NamedTextColor.GRAY)));
  }

  @Suggestions("tabListModes")
  public @NotNull List<String> suggestModes(final CommandContext<CommandSender> context, final String input) {
    final var lowerInput = input.toLowerCase(Locale.ROOT);
    return Arrays.stream(TabListMode.values())
      .map(mode -> mode.name().toLowerCase(Locale.ROOT))
      .filter(name -> lowerInput.isEmpty() || name.startsWith(lowerInput))
      .collect(Collectors.toList());
  }

  private @Nullable Boolean parseOnOff(final @NotNull String value) {
    return switch (value.toLowerCase(Locale.ROOT)) {
      case "on", "true", "enable", "enabled" -> true;
      case "off", "false", "disable", "disabled" -> false;
      default -> null;
    };
  }

  private @Nullable TabListMode parseMode(final @NotNull String modeName) {
    try {
      return TabListMode.valueOf(modeName.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ignored) {
      return null;
    }
  }
}

