package fr.dreamin.dreamapi.plugin.cmd.admin.worldborder;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.cmd.DreamCmd;
import fr.dreamin.dreamapi.api.worldborder.model.IWorldBorder;
import fr.dreamin.dreamapi.api.worldborder.model.Position;
import fr.dreamin.dreamapi.api.worldborder.model.WorldBorderData;
import fr.dreamin.dreamapi.api.worldborder.model.WorldBorderService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

@DreamCmd
public final class WorldBorderCmd {

  private final @NotNull WorldBorderService worldBorderService = DreamAPI.getAPI().getService(WorldBorderService.class);

  // ###############################################################
  // ----------------------- COMMANDS METHODS ----------------------
  // ###############################################################

  @CommandDescription("Show world border info for a player")
  @CommandMethod("worldborder info <player>")
  @CommandPermission("dreamapi.cmd.worldborder.info")
  private void onInfo(
	final @NotNull CommandSender sender,
	final @Argument(value = "player") @NotNull Player target
  ) {

		final IWorldBorder border = this.worldBorderService.getWorldBorder(target);
		final WorldBorderData data = this.worldBorderService.getWorldBorderData(target);

		Component msg = Component.text("--------------------------------", NamedTextColor.GOLD)
			.appendNewline()
			.append(Component.text("WorldBorder Info: " + target.getName(), NamedTextColor.GOLD))
			.appendNewline()
			.append(Component.text("Center: ", NamedTextColor.DARK_GRAY))
			.append(Component.text(formatPosition(border.getCenter()), NamedTextColor.WHITE))
			.appendNewline()
			.append(Component.text("Size: ", NamedTextColor.DARK_GRAY))
			.append(Component.text(String.valueOf(border.getSize()), NamedTextColor.WHITE))
			.appendNewline()
			.append(Component.text("Warning distance: ", NamedTextColor.DARK_GRAY))
			.append(Component.text(String.valueOf(border.getWarningDistanceInBlocks()), NamedTextColor.WHITE))
			.appendNewline()
			.append(Component.text("Warning time: ", NamedTextColor.DARK_GRAY))
			.append(Component.text(String.valueOf(border.getWarningTimerInSeconds()), NamedTextColor.WHITE))
			.appendNewline()
			.append(Component.text("Persistent data: ", NamedTextColor.DARK_GRAY))
			.append(Component.text(data == null ? "none" : "present", data == null ? NamedTextColor.GRAY : NamedTextColor.GREEN))
			.appendNewline()
			.append(Component.text("--------------------------------", NamedTextColor.GOLD));

		sender.sendMessage(msg);
  }

  @CommandDescription("Set a player world border with player location as center")
  @CommandMethod("worldborder set <player> <size>")
  @CommandPermission("dreamapi.cmd.worldborder.set")
  private void onSet(
	final @NotNull CommandSender sender,
	final @Argument(value = "player") @NotNull Player target,
	final @Argument("size") double size
  ) {

		if (size <= 0) {
			sender.sendMessage(Component.text("Size must be > 0.", NamedTextColor.RED));
			return;
		}

		this.worldBorderService.setBorder(target, size);
		sender.sendMessage(Component.text("WorldBorder updated for ", NamedTextColor.GREEN)
			.append(Component.text(target.getName(), NamedTextColor.WHITE))
			.append(Component.text(" (size: " + size + ")", NamedTextColor.GREEN)));
  }

  @CommandDescription("Set a player world border with custom center")
  @CommandMethod("worldborder set <player> <size> <x> <z>")
  @CommandPermission("dreamapi.cmd.worldborder.set")
  private void onSetCenter(
	final @NotNull CommandSender sender,
	final @Argument(value = "player") @NotNull Player target,
	final @Argument("size") double size,
	final @Argument("x") double x,
	final @Argument("z") double z
  ) {

		if (size <= 0) {
			sender.sendMessage(Component.text("Size must be > 0.", NamedTextColor.RED));
			return;
		}

		this.worldBorderService.setBorder(target, size, new Position(x, z));
		sender.sendMessage(Component.text("WorldBorder updated for ", NamedTextColor.GREEN)
			.append(Component.text(target.getName(), NamedTextColor.WHITE))
			.append(Component.text(" (size: " + size + ", center: " + formatPosition(new Position(x, z)) + ")", NamedTextColor.GREEN)));
  }

  @CommandDescription("Animate border size for a player")
  @CommandMethod("worldborder lerp <player> <size> <seconds>")
  @CommandPermission("dreamapi.cmd.worldborder.lerp")
  private void onLerp(
	final @NotNull CommandSender sender,
	final @Argument(value = "player") @NotNull Player target,
	final @Argument("size") double size,
	final @Argument("seconds") long seconds
  ) {

		if (size <= 0 || seconds <= 0) {
			sender.sendMessage(Component.text("Size and seconds must be > 0.", NamedTextColor.RED));
			return;
		}

		this.worldBorderService.setBorder(target, size, Duration.ofSeconds(seconds).toMillis());
		sender.sendMessage(Component.text("WorldBorder lerp sent to ", NamedTextColor.GREEN)
			.append(Component.text(target.getName(), NamedTextColor.WHITE))
			.append(Component.text(" (target size: " + size + ", duration: " + seconds + "s)", NamedTextColor.GREEN)));
  }

  @CommandDescription("Send red border warning screen to a player")
  @CommandMethod("worldborder redscreen <player> <seconds>")
  @CommandPermission("dreamapi.cmd.worldborder.redscreen")
  private void onRedScreen(
	final @NotNull CommandSender sender,
	final @Argument(value = "player") @NotNull Player target,
	final @Argument("seconds") long seconds
  ) {

		if (seconds <= 0) {
			sender.sendMessage(Component.text("Seconds must be > 0.", NamedTextColor.RED));
			return;
		}

		this.worldBorderService.sendRedScreenForSeconds(target, Duration.ofSeconds(seconds));
		sender.sendMessage(Component.text("Red screen sent to ", NamedTextColor.GREEN)
			.append(Component.text(target.getName(), NamedTextColor.WHITE))
			.append(Component.text(" for " + seconds + "s", NamedTextColor.GREEN)));
  }

  @CommandDescription("Reset a player border to global world border")
  @CommandMethod("worldborder reset <player>")
  @CommandPermission("dreamapi.cmd.worldborder.reset")
  private void onReset(
	final @NotNull CommandSender sender,
	final @Argument(value = "player") @NotNull Player target
  ) {
		this.worldBorderService.resetWorldBorderToGlobal(target);
		sender.sendMessage(Component.text("WorldBorder reset for ", NamedTextColor.GREEN)
			.append(Component.text(target.getName(), NamedTextColor.WHITE)));
  }

  @CommandDescription("Play a pulse effect on a player's world border")
  @CommandMethod("worldborder pulse <player> <minSize> <maxSize> <pulses> <seconds>")
  @CommandPermission("dreamapi.cmd.worldborder.pulse")
  private void onPulse(
	final @NotNull CommandSender sender,
	final @Argument(value = "player") @NotNull Player target,
	final @Argument("minSize") double minSize,
	final @Argument("maxSize") double maxSize,
	final @Argument("pulses") int pulses,
	final @Argument("seconds") long seconds
  ) {
		if (minSize <= 0 || maxSize <= 0 || pulses <= 0 || seconds <= 0) {
			sender.sendMessage(Component.text("minSize, maxSize, pulses and seconds must be > 0.", NamedTextColor.RED));
			return;
		}

		final double low = Math.min(minSize, maxSize);
		final double high = Math.max(minSize, maxSize);

		try {
			this.worldBorderService.pulseBorder(target, minSize, maxSize, pulses, Duration.ofSeconds(seconds));
			sender.sendMessage(Component.text("WorldBorder pulse sent to ", NamedTextColor.GREEN)
				.append(Component.text(target.getName(), NamedTextColor.WHITE))
				.append(Component.text(" (" + low + " <-> " + high + ", pulses=" + pulses + ", duration=" + seconds + "s)", NamedTextColor.GREEN)));
		} catch (IllegalArgumentException e) {
			sender.sendMessage(Component.text("Invalid pulse params: " + e.getMessage(), NamedTextColor.RED));
		}
  }

  @CommandDescription("Enable health-based world border red overlay")
  @CommandMethod("worldborder healthoverlay on")
  @CommandPermission("dreamapi.cmd.worldborder.healthoverlay.on")
  private void onHealthOverlayOn(final @NotNull CommandSender sender) {
		this.worldBorderService.setHealthOverlayEnabled(true);
		sender.sendMessage(Component.text("WorldBorder health overlay enabled.", NamedTextColor.GREEN));
  }

  @CommandDescription("Disable health-based world border red overlay")
  @CommandMethod("worldborder healthoverlay off")
  @CommandPermission("dreamapi.cmd.worldborder.healthoverlay.off")
  private void onHealthOverlayOff(final @NotNull CommandSender sender) {
		this.worldBorderService.setHealthOverlayEnabled(false);
		sender.sendMessage(Component.text("WorldBorder health overlay disabled.", NamedTextColor.YELLOW));
  }

  @CommandDescription("Show health-based world border overlay status")
  @CommandMethod("worldborder healthoverlay status")
  @CommandPermission("dreamapi.cmd.worldborder.healthoverlay.status")
  private void onHealthOverlayStatus(final @NotNull CommandSender sender) {
	final var enabled = this.worldBorderService.isHealthOverlayEnabled();
		sender.sendMessage(Component.text("WorldBorder health overlay: ", NamedTextColor.GRAY)
	  	.append(Component.text(enabled ? "ON" : "OFF", enabled ? NamedTextColor.GREEN : NamedTextColor.RED)));
  }

  // ###############################################################
  // ------------------------- SUGGESTIONS -------------------------
  // ###############################################################

  @Suggestions("onlinePlayers")
  public List<String> suggestPlayers(
	final CommandContext<CommandSender> context,
	final String input
  ) {
		return Bukkit.getOnlinePlayers().stream()
			.map(Player::getName)
			.sorted()
			.filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT)))
			.toList();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private @NotNull String formatPosition(final @NotNull Position position) {
		return String.format(Locale.ROOT, "x=%.2f, z=%.2f", position.x(), position.z());
  }
}
