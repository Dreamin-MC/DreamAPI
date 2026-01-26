package fr.dreamin.dreamapi.plugin.cmd.admin.nms.visual;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.nms.visual.service.VisualService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class VisualCmd {

  private final @NotNull VisualService visualService = DreamAPI.getAPI().getService(VisualService.class);

  // ###############################################################
  // ----------------------- FAKE ENTITIES -------------------------
  // ###############################################################

  @CommandDescription("Spawn a fake entity at your location")
  @CommandMethod("visual entity spawn <type>")
  @CommandPermission("dreamapi.visual.entity.spawn")
  private void entitySpawn(
    final @NotNull CommandSender sender,
    final @Argument(value = "type", suggestions = "entityTypes") @NotNull String typeName
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var type = parseEntityType(typeName);
    if (type == null) {
      player.sendMessage(Component.text("Invalid entity type: ", NamedTextColor.RED)
        .append(Component.text(typeName, NamedTextColor.WHITE)));
      return;
    }

    try {
      final var entity = this.visualService.spawnFakeEntity(type, player.getLocation(), player);

      player.sendMessage(Component.text("Spawned fake ", NamedTextColor.GREEN)
        .append(Component.text(type.name(), NamedTextColor.YELLOW))
        .append(Component.text(" (ID: ", NamedTextColor.GRAY))
        .append(Component.text(entity.getEntityId(), NamedTextColor.WHITE))
        .append(Component.text(")", NamedTextColor.GRAY)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: ", NamedTextColor.RED)
        .append(Component.text(e.getMessage(), NamedTextColor.GRAY)));
      e.printStackTrace();
    }
  }

  @CommandDescription("Remove all fake entities for yourself")
  @CommandMethod("visual entity clear")
  @CommandPermission("dreamapi.visual.entity.clear")
  private void entityClear(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    try {
      final var fakeEntities = this.visualService.getFakeEntities(player);
      final var count = fakeEntities.size();

      if (count == 0) {
        player.sendMessage(Component.text("No fake entities to clear!", NamedTextColor.GRAY));
        return;
      }

      for (final var entity : fakeEntities) {
        this.visualService.removeFakeEntity(entity, player);
      }

      player.sendMessage(Component.text("Cleared ", NamedTextColor.GRAY)
        .append(Component.text(count, NamedTextColor.WHITE))
        .append(Component.text(" fake entities", NamedTextColor.GRAY)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: ", NamedTextColor.RED)
        .append(Component.text(e.getMessage(), NamedTextColor.GRAY)));
      e.printStackTrace();
    }
  }

  @CommandDescription("List all fake entities you can see")
  @CommandMethod("visual entity list")
  @CommandPermission("dreamapi.visual.entity.list")
  private void entityList(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var fakeEntities = this.visualService.getFakeEntities(player);

    if (fakeEntities.isEmpty()) {
      player.sendMessage(Component.text("No fake entities visible", NamedTextColor.GRAY));
      return;
    }

    player.sendMessage(Component.text("─────────────────────────────", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));
    player.sendMessage(Component.text("Fake Entities ", NamedTextColor.YELLOW, TextDecoration.BOLD)
      .append(Component.text("(" + fakeEntities.size() + ")", NamedTextColor.GRAY)));
    player.sendMessage(Component.text("─────────────────────────────", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));

    int index = 1;
    for (final var entity : fakeEntities) {
      player.sendMessage(Component.text(index + ". ", NamedTextColor.DARK_GRAY)
        .append(Component.text(entity.getType().name(), NamedTextColor.YELLOW))
        .append(Component.text(" (ID: ", NamedTextColor.GRAY))
        .append(Component.text(entity.getEntityId(), NamedTextColor.WHITE))
        .append(Component.text(") at ", NamedTextColor.GRAY))
        .append(Component.text(formatLocation(entity.getInitialLocation()), NamedTextColor.WHITE)));
      index++;
    }
  }

  // ###############################################################
  // ------------------------ FAKE BLOCKS --------------------------
  // ###############################################################

  @CommandDescription("Show a fake block at target location")
  @CommandMethod("visual block show <material>")
  @CommandPermission("dreamapi.visual.block.show")
  private void blockShow(
    final @NotNull CommandSender sender,
    final @Argument(value = "material", suggestions = "materials") @NotNull String materialName
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

    final var material = parseMaterial(materialName);
    if (material == null) {
      player.sendMessage(Component.text("Invalid material: ", NamedTextColor.RED)
        .append(Component.text(materialName, NamedTextColor.WHITE)));
      return;
    }

    try {
      this.visualService.showFakeBlock(block.getLocation(), material, player);

      player.sendMessage(Component.text("Showing fake ", NamedTextColor.GREEN)
        .append(Component.text(material.name(), NamedTextColor.YELLOW))
        .append(Component.text(" at ", NamedTextColor.GRAY))
        .append(Component.text(formatLocation(block.getLocation()), NamedTextColor.WHITE)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: ", NamedTextColor.RED)
        .append(Component.text(e.getMessage(), NamedTextColor.GRAY)));
      e.printStackTrace();
    }
  }

  @CommandDescription("Hide fake block (restore real block)")
  @CommandMethod("visual block hide")
  @CommandPermission("dreamapi.visual.block.hide")
  private void blockHide(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var block = player.getTargetBlockExact(10);
    if (block == null || block.getType() == Material.AIR) {
      player.sendMessage(Component.text("No block in sight!", NamedTextColor.RED));
      return;
    }

    try {
      this.visualService.hideFakeBlock(block.getLocation(), player);

      player.sendMessage(Component.text("Restored real block at ", NamedTextColor.GRAY)
        .append(Component.text(formatLocation(block.getLocation()), NamedTextColor.WHITE)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: ", NamedTextColor.RED)
        .append(Component.text(e.getMessage(), NamedTextColor.GRAY)));
      e.printStackTrace();
    }
  }

  @CommandDescription("Fill area with fake blocks")
  @CommandMethod("visual block fill <material> <radius>")
  @CommandPermission("dreamapi.visual.block.fill")
  private void blockFill(
    final @NotNull CommandSender sender,
    final @Argument(value = "material", suggestions = "materials") @NotNull String materialName,
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

    final var material = parseMaterial(materialName);
    if (material == null) {
      player.sendMessage(Component.text("Invalid material: ", NamedTextColor.RED)
        .append(Component.text(materialName, NamedTextColor.WHITE)));
      return;
    }

    final var center = player.getLocation();
    int count = 0;

    try {
      for (int x = -radius; x <= radius; x++) {
        for (int y = -radius; y <= radius; y++) {
          for (int z = -radius; z <= radius; z++) {
            if (x * x + y * y + z * z > radius * radius) continue;

            final var block = center.clone().add(x, y, z).getBlock();
            if (block.getType() != Material.AIR) {
              this.visualService.showFakeBlock(block.getLocation(), material, player);
              count++;
            }
          }
        }
      }

      player.sendMessage(Component.text("Filled ", NamedTextColor.GREEN)
        .append(Component.text(count, NamedTextColor.YELLOW))
        .append(Component.text(" blocks with ", NamedTextColor.GREEN))
        .append(Component.text(material.name(), NamedTextColor.YELLOW)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: ", NamedTextColor.RED)
        .append(Component.text(e.getMessage(), NamedTextColor.GRAY)));
      e.printStackTrace();
    }
  }

  @CommandDescription("Replace blocks in area with fake blocks")
  @CommandMethod("visual block replace <from> <to> <radius>")
  @CommandPermission("dreamapi.visual.block.replace")
  private void blockReplace(
    final @NotNull CommandSender sender,
    final @Argument(value = "from", suggestions = "materials") @NotNull String fromName,
    final @Argument(value = "to", suggestions = "materials") @NotNull String toName,
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

    final var fromMaterial = parseMaterial(fromName);
    final var toMaterial = parseMaterial(toName);

    if (fromMaterial == null || toMaterial == null) {
      player.sendMessage(Component.text("Invalid material!", NamedTextColor.RED));
      return;
    }

    final var center = player.getLocation();
    int count = 0;

    try {
      for (int x = -radius; x <= radius; x++) {
        for (int y = -radius; y <= radius; y++) {
          for (int z = -radius; z <= radius; z++) {
            if (x * x + y * y + z * z > radius * radius) continue;

            final var block = center.clone().add(x, y, z).getBlock();
            if (block.getType() == fromMaterial) {
              this.visualService.showFakeBlock(block.getLocation(), toMaterial, player);
              count++;
            }
          }
        }
      }

      player.sendMessage(Component.text("", NamedTextColor.GREEN, TextDecoration.BOLD)
        .append(Component.text("Replaced ", NamedTextColor.GREEN))
        .append(Component.text(count, NamedTextColor.YELLOW))
        .append(Component.text(" blocks (", NamedTextColor.GRAY))
        .append(Component.text(fromMaterial.name(), NamedTextColor.WHITE))
        .append(Component.text(" → ", NamedTextColor.DARK_GRAY))
        .append(Component.text(toMaterial.name(), NamedTextColor.YELLOW))
        .append(Component.text(")", NamedTextColor.GRAY)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: ", NamedTextColor.RED)
        .append(Component.text(e.getMessage(), NamedTextColor.GRAY)));
      e.printStackTrace();
    }
  }

  @CommandDescription("Clear all fake blocks for yourself")
  @CommandMethod("visual block clear")
  @CommandPermission("dreamapi.visual.block.clear")
  private void blockClear(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    try {
      final var fakeBlocks = this.visualService.getFakeBlocks(player);
      final var count = fakeBlocks.size();

      if (count == 0) {
        player.sendMessage(Component.text("No fake blocks to clear!", NamedTextColor.GRAY));
        return;
      }

      this.visualService.clearForViewer(player);

      player.sendMessage(Component.text("Cleared ", NamedTextColor.GRAY)
        .append(Component.text(count, NamedTextColor.WHITE))
        .append(Component.text(" fake blocks", NamedTextColor.GRAY)));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: ", NamedTextColor.RED)
        .append(Component.text(e.getMessage(), NamedTextColor.GRAY)));
      e.printStackTrace();
    }
  }

  @CommandDescription("List all fake blocks you can see")
  @CommandMethod("visual block list")
  @CommandPermission("dreamapi.visual.block.list")
  private void blockList(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var fakeBlocks = this.visualService.getFakeBlocks(player);

    if (fakeBlocks.isEmpty()) {
      player.sendMessage(Component.text("No fake blocks visible", NamedTextColor.GRAY));
      return;
    }

    player.sendMessage(Component.text("─────────────────────────────", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));
    player.sendMessage(Component.text("Fake Blocks ", NamedTextColor.YELLOW, TextDecoration.BOLD)
      .append(Component.text("(" + fakeBlocks.size() + ")", NamedTextColor.GRAY)));
    player.sendMessage(Component.text("─────────────────────────────", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));

    int index = 1;
    for (final var entry : fakeBlocks.entrySet()) {
      player.sendMessage(Component.text(index + ". ", NamedTextColor.DARK_GRAY)
        .append(Component.text(entry.getValue().name(), NamedTextColor.YELLOW))
        .append(Component.text(" at ", NamedTextColor.GRAY))
        .append(Component.text(formatLocation(entry.getKey()), NamedTextColor.WHITE)));
      index++;
    }
  }

  // ###############################################################
  // --------------------------- TIME ------------------------------
  // ###############################################################

  @CommandDescription("Freeze time for yourself")
  @CommandMethod("visual time freeze <time>")
  @CommandPermission("dreamapi.visual.time.freeze")
  private void timeFreeze(
    final @NotNull CommandSender sender,
    final @Argument("time") long time
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    this.visualService.setFrozenTime(player, time);

    player.sendMessage(Component.text("Time frozen at ", NamedTextColor.AQUA)
      .append(Component.text(time, NamedTextColor.WHITE))
      .append(Component.text(" ticks (", NamedTextColor.GRAY))
      .append(Component.text(getTimeOfDay(time), NamedTextColor.YELLOW))
      .append(Component.text(")", NamedTextColor.GRAY)));
  }

  @CommandDescription("Reset time to server time")
  @CommandMethod("visual time reset")
  @CommandPermission("dreamapi.visual.time.reset")
  private void timeReset(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    this.visualService.resetTime(player);

    player.sendMessage(Component.text("Time reset to server time", NamedTextColor.GRAY));
  }

  @CommandDescription("Set time to preset (day/noon/night/midnight)")
  @CommandMethod("visual time set <preset>")
  @CommandPermission("dreamapi.visual.time.set")
  private void timeSet(
    final @NotNull CommandSender sender,
    final @Argument(value = "preset", suggestions = "timePresets") @NotNull String preset
  ) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    final var time = switch (preset.toLowerCase()) {
      case "day", "morning" -> 1000L;
      case "noon", "midday" -> 6000L;
      case "sunset", "evening" -> 12000L;
      case "night" -> 13000L;
      case "midnight" -> 18000L;
      default -> null;
    };

    if (time == null) {
      player.sendMessage(Component.text("Invalid preset! Use: day, noon, sunset, night, or midnight", NamedTextColor.RED));
      return;
    }

    this.visualService.setFrozenTime(player, time);

    player.sendMessage(Component.text("Time set to ", NamedTextColor.AQUA)
      .append(Component.text(preset, NamedTextColor.YELLOW)));
  }

  // ###############################################################
  // ------------------------- CLEARALL ----------------------------
  // ###############################################################

  @CommandDescription("Clear all visual effects for you")
  @CommandMethod("visual clearall")
  @CommandPermission("dreamapi.visual.clearall")
  private void clearAll(final @NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
      return;
    }

    try {
      this.visualService.clearForViewer(player);
      player.sendMessage(Component.text("Cleared all visual effects", NamedTextColor.GRAY));
    } catch (Exception e) {
      player.sendMessage(Component.text("Error: ", NamedTextColor.RED)
        .append(Component.text(e.getMessage(), NamedTextColor.GRAY)));
      e.printStackTrace();
    }
  }

  // ###############################################################
  // ------------------------ SUGGESTIONS --------------------------
  // ###############################################################

  @Suggestions("materials")
  public List<String> suggestMaterials(final CommandContext<CommandSender> context, final String input) {
    return Arrays.stream(Material.values())
      .filter(Material::isBlock)
      .filter(m -> !m.isAir())
      .map(m -> m.name().toLowerCase())
      .filter(name -> input.isEmpty() || name.startsWith(input.toLowerCase()))
      .sorted()
      .limit(20)
      .collect(Collectors.toList());
  }

  @Suggestions("entityTypes")
  public List<String> suggestEntityTypes(final CommandContext<CommandSender> context, final String input) {
    return Arrays.stream(EntityType.values())
      .filter(EntityType::isSpawnable)
      .map(type -> type.name().toLowerCase())
      .filter(name -> input.isEmpty() || name.startsWith(input.toLowerCase()))
      .sorted()
      .limit(20)
      .collect(Collectors.toList());
  }

  @Suggestions("timePresets")
  public List<String> suggestTimePresets(final CommandContext<CommandSender> context, final String input) {
    return List.of("day", "noon", "sunset", "night", "midnight");
  }

  // ###############################################################
  // --------------------------- HELPERS ---------------------------
  // ###############################################################

  private @Nullable Material parseMaterial(final @NotNull String name) {
    try {
      final var material = Material.valueOf(name.toUpperCase());
      return material.isBlock() && !material.isAir() ? material : null;
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private @Nullable EntityType parseEntityType(final @NotNull String name) {
    try {
      final var type = EntityType.valueOf(name.toUpperCase());
      return type.isSpawnable() ? type : null;
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private @NotNull String formatLocation(final org.bukkit.Location loc) {
    return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
  }

  private @NotNull String getTimeOfDay(final long time) {
    final var normalizedTime = time % 24000;
    if (normalizedTime < 6000) return "Morning";
    if (normalizedTime < 12000) return "Day";
    if (normalizedTime < 13000) return "Sunset";
    if (normalizedTime < 18000) return "Night";
    return "Midnight";
  }
}
