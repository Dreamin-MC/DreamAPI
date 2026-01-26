package fr.dreamin.dreamapi.plugin.cmd.admin.glowing;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.glowing.animation.GlowAnimation;
import fr.dreamin.dreamapi.api.glowing.service.GlowingService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Advanced glowing system test command
 */
public final class GlowingCmd {

  private final @NotNull GlowingService glowingService = DreamAPI.getAPI().getService(GlowingService.class);

  // ###############################################################
  // --------------------------- ENTITY ----------------------------
  // ###############################################################

  @CommandDescription("Make nearby entities glow")
  @CommandMethod("glowing entity set <color> [duration]")
  @CommandPermission("dreamapi.glowing.entity.set")
  private void entitySet(
    final @NotNull CommandSender sender,
    final @Argument(value = "color", suggestions = "colors") @NotNull String colorName,
    final @Argument("duration") @Nullable Integer duration
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var color = parseColor(colorName);
    if (color == null) {
      player.sendMessage(Component.text("Invalid color! Use: red, green, blue, yellow, aqua, gold, white, purple", NamedTextColor.RED));
      return;
    }

    final var entities = player.getNearbyEntities(20, 20, 20);
    if (entities.isEmpty()) {
      player.sendMessage(Component.text("No entities nearby!", NamedTextColor.RED));
      return;
    }

    if (duration != null && duration > 0) {
      entities.forEach(e -> this.glowingService.glowEntity(e, color, duration * 20L, player));
      player.sendMessage(Component.text("Set glowing for ", NamedTextColor.GREEN)
        .append(Component.text(entities.size(), NamedTextColor.WHITE))
        .append(Component.text(" entities for ", NamedTextColor.GREEN))
        .append(Component.text(duration + "s", NamedTextColor.WHITE)));
    } else {
      entities.forEach(e -> this.glowingService.glowEntity(e, color, player));
      player.sendMessage(Component.text("Set glowing for ", NamedTextColor.GREEN)
        .append(Component.text(entities.size(), NamedTextColor.WHITE))
        .append(Component.text(" entities", NamedTextColor.GREEN)));
    }
  }

  @CommandDescription("Remove entity glowing")
  @CommandMethod("glowing entity clear")
  @CommandPermission("dreamapi.glowing.entity.clear")
  private void entityClear(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var count = this.glowingService.getGlowingEntities(player).size();
    this.glowingService.getGlowingEntities(player).forEach(e -> this.glowingService.stopEntity(e, player));
    
    player.sendMessage(Component.text("Cleared ", NamedTextColor.GRAY)
      .append(Component.text(count, NamedTextColor.WHITE))
      .append(Component.text(" glowing entities", NamedTextColor.GRAY)));
  }

  // ###############################################################
  // --------------------------- BLOCK -----------------------------
  // ###############################################################

  @CommandDescription("Make target block glow")
  @CommandMethod("glowing block set <color> [duration]")
  @CommandPermission("dreamapi.glowing.block.set")
  private void blockSet(
    final @NotNull CommandSender sender,
    final @Argument(value = "color", suggestions = "colors") @NotNull String colorName,
    final @Argument("duration") @Nullable Integer duration
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var block = player.getTargetBlockExact(10);
    if (block == null || block.getType() == Material.AIR) {
      player.sendMessage(Component.text("No block in sight!", NamedTextColor.RED));
      return;
    }

    final var color = parseColor(colorName);
    if (color == null) {
      player.sendMessage(Component.text("Invalid color!", NamedTextColor.RED));
      return;
    }

    if (duration != null && duration > 0) {
      this.glowingService.glowBlock(block, color, duration * 20L, player);
      player.sendMessage(Component.text("Block glowing for ", NamedTextColor.GREEN)
        .append(Component.text(duration + "s", NamedTextColor.WHITE)));
    } else {
      this.glowingService.glowBlock(block, color, player);
      player.sendMessage(Component.text("Block is now glowing!", NamedTextColor.GREEN));
    }
  }

  @CommandDescription("Remove block glowing")
  @CommandMethod("glowing block clear")
  @CommandPermission("dreamapi.glowing.block.clear")
  private void blockClear(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var count = this.glowingService.getGlowingBlocks(player).size();
    this.glowingService.getGlowingBlocks(player).forEach(b -> this.glowingService.stopBlock(b, player));
    
    player.sendMessage(Component.text("Cleared ", NamedTextColor.GRAY)
      .append(Component.text(count, NamedTextColor.WHITE))
      .append(Component.text(" glowing blocks", NamedTextColor.GRAY)));
  }

  // ###############################################################
  // ------------------------- ANIMATIONS --------------------------
  // ###############################################################

  @CommandDescription("Animate entities with rainbow effect")
  @CommandMethod("glowing anim rainbow [duration]")
  @CommandPermission("dreamapi.glowing.anim.rainbow")
  private void animRainbow(final @NotNull CommandSender sender, final @Argument("duration") @Nullable Integer duration) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var entities = player.getNearbyEntities(20, 20, 20);
    if (entities.isEmpty()) {
      player.sendMessage(Component.text("No entities nearby!", NamedTextColor.RED));
      return;
    }

    final var animation = GlowAnimation.rainbow();
    
    if (duration != null && duration > 0) {
      entities.forEach(e -> this.glowingService.glowEntityAnimated(e, animation, duration * 20L, player));
      player.sendMessage(Component.text("Rainbow animation for ", NamedTextColor.LIGHT_PURPLE)
        .append(Component.text(entities.size(), NamedTextColor.WHITE))
        .append(Component.text(" entities (" + duration + "s)", NamedTextColor.LIGHT_PURPLE)));
    } else {
      entities.forEach(e -> this.glowingService.glowEntityAnimated(e, animation, player));
      player.sendMessage(Component.text("Rainbow animation started for ", NamedTextColor.LIGHT_PURPLE)
        .append(Component.text(entities.size(), NamedTextColor.WHITE))
        .append(Component.text(" entities", NamedTextColor.LIGHT_PURPLE)));
    }
  }

  @CommandDescription("Animate entities with pulse effect")
  @CommandMethod("glowing anim pulse <color> [duration]")
  @CommandPermission("dreamapi.glowing.anim.pulse")
  private void animPulse(
    final @NotNull CommandSender sender,
    final @Argument(value = "color", suggestions = "colors") @NotNull String colorName,
    final @Argument("duration") @Nullable Integer duration
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var color = parseColor(colorName);
    if (color == null) {
      player.sendMessage(Component.text("Invalid color!", NamedTextColor.RED));
      return;
    }

    final var entities = player.getNearbyEntities(20, 20, 20);
    if (entities.isEmpty()) {
      player.sendMessage(Component.text("No entities nearby!", NamedTextColor.RED));
      return;
    }

    final var animation = GlowAnimation.pulse(color);
    
    if (duration != null && duration > 0) {
      entities.forEach(e -> this.glowingService.glowEntityAnimated(e, animation, duration * 20L, player));
      player.sendMessage(Component.text("Pulse animation for ", NamedTextColor.AQUA)
        .append(Component.text(entities.size(), NamedTextColor.WHITE))
        .append(Component.text(" entities (" + duration + "s)", NamedTextColor.AQUA)));
    } else {
      entities.forEach(e -> this.glowingService.glowEntityAnimated(e, animation, player));
      player.sendMessage(Component.text("Pulse animation started for ", NamedTextColor.AQUA)
        .append(Component.text(entities.size(), NamedTextColor.WHITE))
        .append(Component.text(" entities", NamedTextColor.AQUA)));
    }
  }

  @CommandDescription("Animate blocks with blink effect")
  @CommandMethod("glowing anim blink <color> <radius>")
  @CommandPermission("dreamapi.glowing.anim.blink")
  private void animBlink(
    final @NotNull CommandSender sender,
    final @Argument(value = "color", suggestions = "colors") @NotNull String colorName,
    final @Argument("radius") int radius
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    if (radius < 1 || radius > 10) {
      player.sendMessage(Component.text("Radius must be between 1 and 10!", NamedTextColor.RED));
      return;
    }

    final var color = parseColor(colorName);
    if (color == null) {
      player.sendMessage(Component.text("Invalid color!", NamedTextColor.RED));
      return;
    }

    final var center = player.getLocation();
    final var animation = GlowAnimation.blink(color, 10L);
    int count = 0;

    for (int x = -radius; x <= radius; x++) {
      for (int y = -radius; y <= radius; y++) {
        for (int z = -radius; z <= radius; z++) {
          final var block = center.clone().add(x, y, z).getBlock();
          if (block.getType() != Material.AIR) {
            this.glowingService.glowBlockAnimated(block, animation, player);
            count++;
          }
        }
      }
    }

    player.sendMessage(Component.text("Blink animation for ", NamedTextColor.YELLOW)
      .append(Component.text(count, NamedTextColor.WHITE))
      .append(Component.text(" blocks", NamedTextColor.YELLOW)));
  }

  // ###############################################################
  // ----------------------- CONDITIONAL ---------------------------
  // ###############################################################

  @CommandDescription("Auto-glowing low health entities")
  @CommandMethod("glowing auto lowhp <threshold>")
  @CommandPermission("dreamapi.glowing.auto.lowhp")
  private void autoLowHp(
    final @NotNull CommandSender sender,
    final @Argument("threshold") double threshold
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    this.glowingService.glowEntitiesMatching(
      entity -> entity instanceof LivingEntity living && living.getHealth() <= threshold,
      ChatColor.RED,
      20L,
      player
    );

    player.sendMessage(Component.text("Auto-glowing entities with HP ≤ ", NamedTextColor.GREEN)
      .append(Component.text(threshold, NamedTextColor.WHITE)));
  }

  @CommandDescription("Auto-glowing specific entity type")
  @CommandMethod("glowing auto type <type> <color>")
  @CommandPermission("dreamapi.glowing.auto.type")
  private void autoType(
    final @NotNull CommandSender sender,
    final @Argument(value = "type", suggestions = "entityTypes") @NotNull String typeName,
    final @Argument(value = "color", suggestions = "colors") @NotNull String colorName
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var type = parseEntityType(typeName);
    if (type == null) {
      player.sendMessage(Component.text("Invalid entity type!", NamedTextColor.RED));
      return;
    }

    final var color = parseColor(colorName);
    if (color == null) {
      player.sendMessage(Component.text("Invalid color!", NamedTextColor.RED));
      return;
    }

    this.glowingService.glowEntitiesMatching(
      entity -> entity.getType() == type,
      color,
      40L,
      player
    );

    player.sendMessage(Component.text("Auto-glowing all ", NamedTextColor.GREEN)
      .append(Component.text(type.name(), NamedTextColor.WHITE))
      .append(Component.text(" entities", NamedTextColor.GREEN)));
  }

  @CommandDescription("Stop auto-glowing")
  @CommandMethod("glowing auto stop")
  @CommandPermission("dreamapi.glowing.auto.stop")
  private void autoStop(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    this.glowingService.stopConditionalGlowing(player);
    player.sendMessage(Component.text("Stopped auto-glowing", NamedTextColor.GRAY));
  }

  // ###############################################################
  // ------------------------- BY ZONE -----------------------------
  // ###############################################################

  @CommandDescription("Glow entities in radius")
  @CommandMethod("glowing zone entities <radius> <color> [type]")
  @CommandPermission("dreamapi.glowing.zone.entities")
  private void zoneEntities(
    final @NotNull CommandSender sender,
    final @Argument("radius") double radius,
    final @Argument(value = "color", suggestions = "colors") @NotNull String colorName,
    final @Argument(value = "type", suggestions = "entityTypes") @Nullable String typeName
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    if (radius < 1 || radius > 50) {
      player.sendMessage(Component.text("Radius must be between 1 and 50!", NamedTextColor.RED));
      return;
    }

    final var color = parseColor(colorName);
    if (color == null) {
      player.sendMessage(Component.text("Invalid color!", NamedTextColor.RED));
      return;
    }

    final var type = typeName != null ? parseEntityType(typeName) : null;

    this.glowingService.glowEntitiesInRadius(player.getLocation(), radius, type, color, player);

    player.sendMessage(Component.text("Glowing entities in ", NamedTextColor.GREEN)
      .append(Component.text(radius + "m", NamedTextColor.WHITE))
      .append(Component.text(" radius", NamedTextColor.GREEN)));
  }

  @CommandDescription("Glow blocks in radius")
  @CommandMethod("glowing zone blocks <radius> <color>")
  @CommandPermission("dreamapi.glowing.zone.blocks")
  private void zoneBlocks(
    final @NotNull CommandSender sender,
    final @Argument("radius") double radius,
    final @Argument(value = "color", suggestions = "colors") @NotNull String colorName
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    if (radius < 1 || radius > 20) {
      player.sendMessage(Component.text("Radius must be between 1 and 20!", NamedTextColor.RED));
      return;
    }

    final var color = parseColor(colorName);
    if (color == null) {
      player.sendMessage(Component.text("Invalid color!", NamedTextColor.RED));
      return;
    }

    this.glowingService.glowBlocksInRadius(player.getLocation(), radius, color, player);

    player.sendMessage(Component.text("Glowing blocks in ", NamedTextColor.GREEN)
      .append(Component.text(radius + "m", NamedTextColor.WHITE))
      .append(Component.text(" radius", NamedTextColor.GREEN)));
  }

  // ###############################################################
  // --------------------- LINE OF SIGHT ---------------------------
  // ###############################################################

  @CommandDescription("Glow entity you're looking at")
  @CommandMethod("glowing sight entity <color> [duration]")
  @CommandPermission("dreamapi.glowing.sight.entity")
  private void sightEntity(
    final @NotNull CommandSender sender,
    final @Argument(value = "color", suggestions = "colors") @NotNull String colorName,
    final @Argument("duration") @Nullable Integer duration
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var color = parseColor(colorName);
    if (color == null) {
      player.sendMessage(Component.text("Invalid color!", NamedTextColor.RED));
      return;
    }

    final var ticks = duration != null ? duration * 20L : 200L;
    final var entity = this.glowingService.glowEntityInCrosshair(player, 50, color, ticks);

    if (entity != null) {
      player.sendMessage(Component.text("Glowing ", NamedTextColor.GREEN)
        .append(Component.text(entity.getType().name(), NamedTextColor.WHITE))
        .append(Component.text(" in crosshair", NamedTextColor.GREEN)));
    } else {
      player.sendMessage(Component.text("No entity in sight!", NamedTextColor.RED));
    }
  }

  @CommandDescription("Glow block you're looking at")
  @CommandMethod("glowing sight block <color> [duration]")
  @CommandPermission("dreamapi.glowing.sight.block")
  private void sightBlock(
    final @NotNull CommandSender sender,
    final @Argument(value = "color", suggestions = "colors") @NotNull String colorName,
    final @Argument("duration") @Nullable Integer duration
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var color = parseColor(colorName);
    if (color == null) {
      player.sendMessage(Component.text("Invalid color!", NamedTextColor.RED));
      return;
    }

    final var ticks = duration != null ? duration * 20L : 200L;
    final var block = this.glowingService.glowBlockInCrosshair(player, 50, color, ticks);

    if (block != null) {
      player.sendMessage(Component.text("Glowing ", NamedTextColor.GREEN)
        .append(Component.text(block.getType().name(), NamedTextColor.WHITE))
        .append(Component.text(" in crosshair", NamedTextColor.GREEN)));
    } else {
      player.sendMessage(Component.text("No block in sight!", NamedTextColor.RED));
    }
  }

  @CommandDescription("Glow all visible entities")
  @CommandMethod("glowing sight visible <color> <distance>")
  @CommandPermission("dreamapi.glowing.sight.visible")
  private void sightVisible(
    final @NotNull CommandSender sender,
    final @Argument(value = "color", suggestions = "colors") @NotNull String colorName,
    final @Argument("distance") double distance
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var color = parseColor(colorName);
    if (color == null) {
      player.sendMessage(Component.text("Invalid color!", NamedTextColor.RED));
      return;
    }

    if (distance < 1 || distance > 100) {
      player.sendMessage(Component.text("Distance must be between 1 and 100!", NamedTextColor.RED));
      return;
    }

    this.glowingService.glowVisibleEntities(player, distance, null, color);

    player.sendMessage(Component.text("Glowing all visible entities within ", NamedTextColor.GREEN)
      .append(Component.text(distance + "m", NamedTextColor.WHITE)));
  }

  // ###############################################################
  // ------------------------ STATISTICS ---------------------------
  // ###############################################################

  @CommandDescription("Show glowing statistics")
  @CommandPermission("dreamapi.glowing.stats")
  @CommandMethod("glowing stats")
  private void stats(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var stats = this.glowingService.getStats();

    player.sendMessage(Component.text("===== Glowing Statistics =====", NamedTextColor.GOLD, TextDecoration.BOLD));
    player.sendMessage(Component.text("Total glowing entities: ", NamedTextColor.YELLOW)
      .append(Component.text(stats.getTotalGlowingEntities(), NamedTextColor.WHITE)));
    player.sendMessage(Component.text("Total glowing blocks: ", NamedTextColor.YELLOW)
      .append(Component.text(stats.getTotalGlowingBlocks(), NamedTextColor.WHITE)));
    player.sendMessage(Component.text("Active viewers: ", NamedTextColor.YELLOW)
      .append(Component.text(stats.getActiveViewers(), NamedTextColor.WHITE)));
    player.sendMessage(Component.text("Your glowing entities: ", NamedTextColor.YELLOW)
      .append(Component.text(this.glowingService.getGlowingEntities(player).size(), NamedTextColor.WHITE)));
    player.sendMessage(Component.text("Your glowing blocks: ", NamedTextColor.YELLOW)
      .append(Component.text(this.glowingService.getGlowingBlocks(player).size(), NamedTextColor.WHITE)));
    player.sendMessage(Component.text("Total operations: ", NamedTextColor.YELLOW)
      .append(Component.text(stats.getTotalOperations(), NamedTextColor.WHITE)));

    final var colorDist = stats.getColorDistribution();
    if (!colorDist.isEmpty()) {
      player.sendMessage(Component.text("Color distribution:", NamedTextColor.AQUA));
      colorDist.forEach((color, count) -> {
        player.sendMessage(Component.text("  " + color.name() + ": ", parseTextColor(color))
          .append(Component.text(count, NamedTextColor.WHITE)));
      });
    }

    player.sendMessage(Component.text("==============================", NamedTextColor.GOLD, TextDecoration.BOLD));

  }

  @CommandDescription("Clear all glowing for you")
  @CommandMethod("glowing clearall")
  private void clearAll(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    this.glowingService.clearForViewer(player);
    player.sendMessage(Component.text("Cleared all glowing effects", NamedTextColor.GRAY));
  }

  // ###############################################################
  // ------------------------- SUGGESTIONS -------------------------
  // ###############################################################

  @Suggestions("colors")
  public List<String> suggestColors(final CommandContext<CommandSender> context, final String input) {
    return List.of("red", "green", "blue", "yellow", "aqua", "gold", "white", "purple", "dark_red", "dark_green", "dark_blue", "dark_purple", "dark_aqua", "dark_gray", "gray", "black");
  }

  @Suggestions("entityTypes")
  public List<String> suggestEntityTypes(final CommandContext<CommandSender> context, final String input) {
    return Arrays.stream(EntityType.values())
      .filter(EntityType::isAlive)
      .map(type -> type.name().toLowerCase())
      .sorted()
      .collect(Collectors.toList());
  }

  // ###############################################################
  // --------------------------- HELPERS ---------------------------
  // ###############################################################

  private @Nullable ChatColor parseColor(final @NotNull String name) {
    return switch (name.toLowerCase()) {
      case "red" -> ChatColor.RED;
      case "green" -> ChatColor.GREEN;
      case "blue" -> ChatColor.BLUE;
      case "yellow" -> ChatColor.YELLOW;
      case "aqua", "cyan" -> ChatColor.AQUA;
      case "gold", "orange" -> ChatColor.GOLD;
      case "white" -> ChatColor.WHITE;
      case "purple", "pink" -> ChatColor.LIGHT_PURPLE;
      case "dark_red" -> ChatColor.DARK_RED;
      case "dark_green" -> ChatColor.DARK_GREEN;
      case "dark_blue" -> ChatColor.DARK_BLUE;
      case "dark_purple" -> ChatColor.DARK_PURPLE;
      case "dark_aqua" -> ChatColor.DARK_AQUA;
      case "dark_gray" -> ChatColor.DARK_GRAY;
      case "gray" -> ChatColor.GRAY;
      case "black" -> ChatColor.BLACK;
      default -> null;
    };
  }

  private @NotNull NamedTextColor parseTextColor(final @NotNull ChatColor color) {
    return switch (color) {
      case RED -> NamedTextColor.RED;
      case GREEN -> NamedTextColor.GREEN;
      case BLUE -> NamedTextColor.BLUE;
      case YELLOW -> NamedTextColor.YELLOW;
      case AQUA -> NamedTextColor.AQUA;
      case GOLD -> NamedTextColor.GOLD;
      case WHITE -> NamedTextColor.WHITE;
      case LIGHT_PURPLE -> NamedTextColor.LIGHT_PURPLE;
      case DARK_RED -> NamedTextColor.DARK_RED;
      case DARK_GREEN -> NamedTextColor.DARK_GREEN;
      case DARK_BLUE -> NamedTextColor.DARK_BLUE;
      case DARK_PURPLE -> NamedTextColor.DARK_PURPLE;
      case DARK_AQUA -> NamedTextColor.DARK_AQUA;
      case DARK_GRAY -> NamedTextColor.DARK_GRAY;
      case GRAY -> NamedTextColor.GRAY;
      case BLACK -> NamedTextColor.BLACK;
      default -> NamedTextColor.WHITE;
    };
  }

  private @Nullable EntityType parseEntityType(final @NotNull String name) {
    try {
      return EntityType.valueOf(name.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
