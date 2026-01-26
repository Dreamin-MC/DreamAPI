package fr.dreamin.example.cmd;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import fr.dreamin.dreamapi.api.annotations.Inject;
import fr.dreamin.dreamapi.api.cmd.DreamCmd;
import fr.dreamin.dreamapi.api.glowing.block.GlowingBlockManager;
import fr.dreamin.dreamapi.api.glowing.entity.GlowingEntityManager;
import fr.dreamin.dreamapi.api.glowing.packet.PacketReflection;
import fr.dreamin.dreamapi.api.glowing.team.TeamOptions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Inject
@DreamCmd
public final class GlowingTestCmd {

  private static GlowingEntityManager entityManager;
  private static GlowingBlockManager blockManager;

  static {
    PacketReflection.initialize();

    final var uid = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
    entityManager = new GlowingEntityManager(uid);
    blockManager = new GlowingBlockManager(entityManager);
  }

  // ###############################################################
  // ------------------------- ENTITY TESTS ------------------------
  // ###############################################################

  @CommandDescription("Test entity glow - make all players glow")
  @CommandMethod("glowtest entity set [color]")
  private void testEntitySet(final @NotNull CommandSender sender,
                             final @Argument(value = "color", suggestions = "colors") @Nullable String colorName) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can execute this command", NamedTextColor.RED));
      return;
    }

    final var color = parseColor(colorName);
    if (color == null) {
      player.sendMessage(Component.text("Invalid color! Use: green, red, blue, yellow, aqua, gold, white, purple", NamedTextColor.RED));
      return;
    }

    try {
      int count = 0;
      final var options = TeamOptions.builder()
        .collisionRule(Team.OptionStatus.NEVER)
        .nameTagVisibility(Team.OptionStatus.ALWAYS)
        .build();

      for (final var target : Bukkit.getOnlinePlayers()) {
        if (target.equals(player)) continue;
        entityManager.setGlowing(target, player, color, options);
        count++;
      }

      player.sendMessage(Component.text("Set glowing for ", NamedTextColor.GREEN)
        .append(Component.text(count, NamedTextColor.WHITE))
        .append(Component.text(" players!", NamedTextColor.GREEN)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: " + e.getMessage(), NamedTextColor.RED));
      e.printStackTrace();
    }
  }

  @CommandDescription("Test entity glow - make all players glow red without nametag")
  @CommandMethod("glowtest entity setred")
  private void testEntitySetRed(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can execute this command", NamedTextColor.RED));
      return;
    }

    try {
      int count = 0;
      final var options = TeamOptions.builder()
        .collisionRule(Team.OptionStatus.NEVER)
        .nameTagVisibility(Team.OptionStatus.NEVER)
        .build();

      for (final var target : Bukkit.getOnlinePlayers()) {
        if (target.equals(player)) continue;
        entityManager.setGlowing(target, player, ChatColor.RED, options);
        count++;
      }

      player.sendMessage(Component.text("Set RED glowing (no nametag) for ", NamedTextColor.RED)
        .append(Component.text(count, NamedTextColor.WHITE))
        .append(Component.text(" players!", NamedTextColor.RED)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: " + e.getMessage(), NamedTextColor.RED));
      e.printStackTrace();
    }
  }

  @CommandDescription("Test entity glow - update color to aqua")
  @CommandMethod("glowtest entity update")
  private void testEntityUpdate(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can execute this command", NamedTextColor.RED));
      return;
    }

    try {
      int count = 0;
      final var options = TeamOptions.builder()
        .collisionRule(Team.OptionStatus.NEVER)
        .nameTagVisibility(Team.OptionStatus.FOR_OTHER_TEAMS)
        .build();

      for (final var target : Bukkit.getOnlinePlayers()) {
        if (target.equals(player)) continue;
        entityManager.setGlowing(target, player, ChatColor.AQUA, options);
        count++;
      }

      player.sendMessage(Component.text("Updated to AQUA glowing for ", NamedTextColor.AQUA)
        .append(Component.text(count, NamedTextColor.WHITE))
        .append(Component.text(" players!", NamedTextColor.AQUA)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: " + e.getMessage(), NamedTextColor.RED));
      e.printStackTrace();
    }
  }

  @CommandDescription("Test entity glow - remove all")
  @CommandMethod("glowtest entity remove")
  private void testEntityRemove(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can execute this command", NamedTextColor.RED));
      return;
    }

    try {
      int count = 0;
      for (final var target : Bukkit.getOnlinePlayers()) {
        if (target.equals(player)) continue;
        entityManager.unsetGlowing(target, player);
        count++;
      }

      player.sendMessage(Component.text("Removed glowing for ", NamedTextColor.GRAY)
        .append(Component.text(count, NamedTextColor.WHITE))
        .append(Component.text(" players!", NamedTextColor.GRAY)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: " + e.getMessage(), NamedTextColor.RED));
      e.printStackTrace();
    }
  }

  @CommandDescription("Test entity glow - clear viewer")
  @CommandMethod("glowtest entity clear")
  private void testEntityClear(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can execute this command", NamedTextColor.RED));
      return;
    }

    try {
      entityManager.clearViewer(player);
      player.sendMessage(Component.text("Cleared all glowing entities for you!", NamedTextColor.GRAY));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: " + e.getMessage(), NamedTextColor.RED));
      e.printStackTrace();
    }
  }

  // ###############################################################
  // ------------------------- BLOCK TESTS -------------------------
  // ###############################################################

  @CommandDescription("Test block glow - make target block glow")
  @CommandMethod("glowtest block set [color]")
  private void testBlockSet(final @NotNull CommandSender sender,
                            final @Argument(value = "color", suggestions = "colors") @Nullable String colorName) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can execute this command", NamedTextColor.RED));
      return;
    }

    final var target = player.getTargetBlockExact(10);
    if (target == null || target.getType() == Material.AIR) {
      player.sendMessage(Component.text("No block in sight!", NamedTextColor.RED));
      return;
    }

    final var color = parseColor(colorName);
    if (color == null) {
      player.sendMessage(Component.text("Invalid color! Use: green, red, blue, yellow, aqua, gold, white, purple", NamedTextColor.RED));
      return;
    }

    try {
      final var options = TeamOptions.builder()
        .collisionRule(Team.OptionStatus.NEVER)
        .nameTagVisibility(Team.OptionStatus.NEVER)
        .build();

      blockManager.setGlowing(target, player, color, options);

      player.sendMessage(Component.text("Block at ", NamedTextColor.GREEN)
        .append(Component.text(formatLocation(target.getLocation()), NamedTextColor.WHITE))
        .append(Component.text(" is now glowing!", NamedTextColor.GREEN)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: " + e.getMessage(), NamedTextColor.RED));
      e.printStackTrace();
    }
  }

  @CommandDescription("Test block glow - make blocks in radius glow")
  @CommandMethod("glowtest block setradius <radius> [color]")
  private void testBlockSetRadius(final @NotNull CommandSender sender,
                                  final @Argument("radius") int radius,
                                  final @Argument(value = "color", suggestions = "colors") @Nullable String colorName) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can execute this command", NamedTextColor.RED));
      return;
    }

    if (radius < 1 || radius > 10) {
      player.sendMessage(Component.text("Radius must be between 1 and 10!", NamedTextColor.RED));
      return;
    }

    final var color = parseColor(colorName);
    if (color == null) {
      player.sendMessage(Component.text("Invalid color! Use: green, red, blue, yellow, aqua, gold, white, purple", NamedTextColor.RED));
      return;
    }

    try {
      final var center = player.getLocation();
      int count = 0;

      final var options = TeamOptions.builder()
        .collisionRule(Team.OptionStatus.NEVER)
        .nameTagVisibility(Team.OptionStatus.NEVER)
        .build();

      for (int x = -radius; x <= radius; x++) {
        for (int y = -radius; y <= radius; y++) {
          for (int z = -radius; z <= radius; z++) {
            final var block = center.clone().add(x, y, z).getBlock();
            if (block.getType() != Material.AIR) {
              blockManager.setGlowing(block, player, color, options);
              count++;
            }
          }
        }
      }

      player.sendMessage(Component.text("Set glowing for ", NamedTextColor.GREEN)
        .append(Component.text(count, NamedTextColor.WHITE))
        .append(Component.text(" blocks in radius ", NamedTextColor.GREEN))
        .append(Component.text(radius, NamedTextColor.WHITE))
        .append(Component.text("!", NamedTextColor.GREEN)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: " + e.getMessage(), NamedTextColor.RED));
      e.printStackTrace();
    }
  }

  @CommandDescription("Test block glow - remove target block glow")
  @CommandMethod("glowtest block remove")
  private void testBlockRemove(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can execute this command", NamedTextColor.RED));
      return;
    }

    final var target = player.getTargetBlockExact(10);
    if (target == null || target.getType() == Material.AIR) {
      player.sendMessage(Component.text("No block in sight!", NamedTextColor.RED));
      return;
    }

    try {
      blockManager.unsetGlowing(target, player);
      player.sendMessage(Component.text("Removed glowing from block at ", NamedTextColor.GRAY)
        .append(Component.text(formatLocation(target.getLocation()), NamedTextColor.WHITE)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: " + e.getMessage(), NamedTextColor.RED));
      e.printStackTrace();
    }
  }

  @CommandDescription("Test block glow - clear all glowing blocks")
  @CommandMethod("glowtest block clear")
  private void testBlockClear(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can execute this command", NamedTextColor.RED));
      return;
    }

    try {
      blockManager.clearViewer(player);
      player.sendMessage(Component.text("Cleared all glowing blocks for you!", NamedTextColor.GRAY));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: " + e.getMessage(), NamedTextColor.RED));
      e.printStackTrace();
    }
  }

  // ###############################################################
  // --------------------------- COMBO TEST ------------------------
  // ###############################################################

  @CommandDescription("Test both - make players and blocks glow")
  @CommandMethod("glowtest combo")
  private void testCombo(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can execute this command", NamedTextColor.RED));
      return;
    }

    try {
      // Glow players in green
      final var playerOptions = TeamOptions.builder()
        .collisionRule(Team.OptionStatus.NEVER)
        .nameTagVisibility(Team.OptionStatus.ALWAYS)
        .build();

      int playerCount = 0;
      for (final var target : Bukkit.getOnlinePlayers()) {
        if (target.equals(player)) continue;
        entityManager.setGlowing(target, player, ChatColor.GREEN, playerOptions);
        playerCount++;
      }

      // Glow blocks around player in red
      final var blockOptions = TeamOptions.builder()
        .collisionRule(Team.OptionStatus.NEVER)
        .nameTagVisibility(Team.OptionStatus.NEVER)
        .build();

      final var center = player.getLocation();
      int blockCount = 0;
      final var radius = 3;

      for (int x = -radius; x <= radius; x++) {
        for (int z = -radius; z <= radius; z++) {
          final var block = center.clone().add(x, -1, z).getBlock();
          if (block.getType() != Material.AIR) {
            blockManager.setGlowing(block, player, ChatColor.RED, blockOptions);
            blockCount++;
          }
        }
      }

      player.sendMessage(Component.text("Combo test:", NamedTextColor.GREEN));
      player.sendMessage(Component.text("- Players glowing: ", NamedTextColor.GREEN)
        .append(Component.text(playerCount, NamedTextColor.WHITE)));
      player.sendMessage(Component.text("- Blocks glowing: ", NamedTextColor.GREEN)
        .append(Component.text(blockCount, NamedTextColor.WHITE)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: " + e.getMessage(), NamedTextColor.RED));
      e.printStackTrace();
    }
  }

  @CommandDescription("Test clear all - remove all glowing")
  @CommandMethod("glowtest clearall")
  private void testClearAll(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can execute this command", NamedTextColor.RED));
      return;
    }

    try {
      entityManager.clearViewer(player);
      blockManager.clearViewer(player);
      player.sendMessage(Component.text("Cleared everything!", NamedTextColor.GRAY));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: " + e.getMessage(), NamedTextColor.RED));
      e.printStackTrace();
    }
  }

  // ###############################################################
  // -------------------------- INFO CMD ---------------------------
  // ###############################################################

  @CommandDescription("Show glowing info")
  @CommandMethod("glowtest info")
  private void testInfo(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can execute this command", NamedTextColor.RED));
      return;
    }

    final var divider = Component.text("────────────────────────────────────", NamedTextColor.YELLOW)
      .decoration(TextDecoration.STRIKETHROUGH, true);

    player.sendMessage(divider);
    player.sendMessage(Component.text("Glowing Test Info", NamedTextColor.GOLD, TextDecoration.BOLD));
    player.sendMessage(Component.text("Glowing entities: ", NamedTextColor.GRAY)
      .append(Component.text(entityManager.getGlowingEntityIds(player).size(), NamedTextColor.WHITE)));
    player.sendMessage(Component.text("Glowing blocks: ", NamedTextColor.GRAY)
      .append(Component.text(blockManager.getGlowingBlocks(player).size(), NamedTextColor.WHITE)));
    player.sendMessage(divider);
  }

  // ###############################################################
  // --------------------------- HELPERS ---------------------------
  // ###############################################################

  @Suggestions("colors")
  public List<String> colors(CommandContext<CommandSender> sender, String input) {
    return List.of("green", "red", "blue", "yellow", "aqua", "gold", "white", "purple", "pink");
  }

  private @Nullable ChatColor parseColor(final @Nullable String name) {
    if (name == null) return ChatColor.GREEN;

    return switch (name.toLowerCase()) {
      case "green" -> ChatColor.GREEN;
      case "red" -> ChatColor.RED;
      case "blue" -> ChatColor.BLUE;
      case "yellow" -> ChatColor.YELLOW;
      case "aqua", "cyan" -> ChatColor.AQUA;
      case "gold", "orange" -> ChatColor.GOLD;
      case "white" -> ChatColor.WHITE;
      case "purple", "pink" -> ChatColor.LIGHT_PURPLE;
      default -> null;
    };
  }

  private @NotNull String formatLocation(final @NotNull Location loc) {
    return String.format("(%d, %d, %d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
  }
}
