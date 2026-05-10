package fr.dreamin.dreamapi.plugin.cmd.admin.gui;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.gui.service.GuiService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class GUICmd {

  private final GuiService guiService = DreamAPI.getAPI().getService(GuiService.class);

  @CommandDescription("Show current and history GUI state for a player")
  @CommandMethod("gui info <player>")
  @CommandPermission("dreamapi.cmd.gui.info")
  private void info(
	final CommandSender sender,
	final @Argument("player") Player target
  ) {
	final var current = this.guiService.getCurrentSession(target);
	final var history = this.guiService.getHistory(target);
	final var previous = this.guiService.getPreviousGui(target);

	sender.sendMessage(Component.text("------ GUI Service Info ------", NamedTextColor.GOLD));
	sender.sendMessage(Component.text("Player: " + target.getName(), NamedTextColor.GRAY));
	sender.sendMessage(Component.text("Current: " + (current == null ? "none" : current.getGuiName()), NamedTextColor.GRAY));
	sender.sendMessage(Component.text("Previous: " + (previous == null ? "none" : previous.getClass().getSimpleName()), NamedTextColor.GRAY));
	sender.sendMessage(Component.text("History size: " + history.size(), NamedTextColor.GRAY));
	sender.sendMessage(Component.text("------------------------------", NamedTextColor.GOLD));
  }

  @CommandDescription("Reopen the last GUI opened by a player")
  @CommandMethod("gui reopen <player>")
  @CommandPermission("dreamapi.cmd.gui.reopen")
  private void reopen(
	final CommandSender sender,
	final @Argument("player") Player target
  ) {
	final var current = this.guiService.getCurrentSession(target);
	if (current != null) {
	  current.getGui().open(target);
	  sender.sendMessage(Component.text("Reopened current GUI for " + target.getName() + ".", NamedTextColor.GREEN));
	  return;
	}

	final var history = this.guiService.getHistory(target);
	if (history.isEmpty()) {
	  sender.sendMessage(Component.text("No GUI history found for " + target.getName() + ".", NamedTextColor.RED));
	  return;
	}

	history.getFirst().getGui().open(target);
	sender.sendMessage(Component.text("Reopened last GUI from history for " + target.getName() + ".", NamedTextColor.GREEN));
  }

  @CommandDescription("Open the previous GUI from player history")
  @CommandMethod("gui back <player>")
  @CommandPermission("dreamapi.cmd.gui.back")
  private void back(
	final CommandSender sender,
	final @Argument("player") Player target
  ) {
	final var previous = this.guiService.getPreviousGui(target);
	if (previous == null) {
	  sender.sendMessage(Component.text("No previous GUI available for " + target.getName() + ".", NamedTextColor.RED));
	  return;
	}

	previous.open(target);
	sender.sendMessage(Component.text("Opened previous GUI for " + target.getName() + ".", NamedTextColor.GREEN));
  }

  @CommandDescription("Clear GUI current session and history for a player")
  @CommandMethod("gui clear <player>")
  @CommandPermission("dreamapi.cmd.gui.clear")
  private void clear(
	final CommandSender sender,
	final @Argument("player") Player target
  ) {
	this.guiService.clear(target);
	sender.sendMessage(Component.text("Cleared GUI history for " + target.getName() + ".", NamedTextColor.YELLOW));
  }

  @CommandDescription("List latest GUI history entries for a player")
  @CommandMethod("gui history <player>")
  @CommandPermission("dreamapi.cmd.gui.history")
  private void history(
	final CommandSender sender,
	final @Argument("player") Player target
  ) {
	final var history = this.guiService.getHistory(target);
	if (history.isEmpty()) {
	  sender.sendMessage(Component.text("No GUI history found for " + target.getName() + ".", NamedTextColor.RED));
	  return;
	}

	sender.sendMessage(Component.text("------ GUI History: " + target.getName() + " ------", NamedTextColor.GOLD));
	final int limit = Math.min(history.size(), 5);
	for (int i = 0; i < limit; i++) {
	  final var session = history.get(i);
	  sender.sendMessage(Component.text(
		String.format(Locale.ROOT, "#%d %s (%s)", i + 1, session.getGuiName(), session.getGuiClass()),
		NamedTextColor.GRAY
	  ));
	}
	sender.sendMessage(Component.text("--------------------------------", NamedTextColor.GOLD));
  }
}
