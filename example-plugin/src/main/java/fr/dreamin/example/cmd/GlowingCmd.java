package fr.dreamin.example.cmd;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import fr.dreamin.dreamapi.api.annotations.Inject;
import fr.dreamin.dreamapi.api.cmd.DreamCmd;
import fr.dreamin.dreamapi.api.glowing.GlowingService;
import fr.dreamin.dreamapi.core.team.TeamService;
import fr.dreamin.example.ExamplePlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

@Inject
@DreamCmd
@RequiredArgsConstructor
public class GlowingCmd {

  private final GlowingService glowingService;
  private final TeamService teamService;

  @CommandDescription("glowing set")
  @CommandMethod("glowing set")
  private void glowingSet(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      this.teamService.createOrGetTeam(onlinePlayer).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

      if (player.equals(onlinePlayer)) continue;

      this.glowingService.glowEntity(onlinePlayer, ChatColor.GREEN, player);
    }
  }

  @CommandDescription("glowing remove")
  @CommandMethod("glowing remove")
  private void glowingRemove(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      this.teamService.removeTeam(onlinePlayer);

      if (player.equals(onlinePlayer)) continue;
      this.glowingService.stopEntity(onlinePlayer, player);
    }
  }

  @CommandDescription("Glowing SetCD")
  @CommandMethod("glowing setcooldown")
  private void glowingSetCd(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      this.teamService.createOrGetTeam(onlinePlayer).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

      if (player.equals(onlinePlayer)) continue;

      this.glowingService.glowEntity(onlinePlayer, ChatColor.GREEN, 100, player);
    }

    Bukkit.getScheduler().runTaskLater(ExamplePlugin.getInstance(), () -> {
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        this.teamService.removeTeam(onlinePlayer);
      }
    }, 120);

  }

  @CommandDescription("glowing set")
  @CommandMethod("glowing set1")
  private void glowingSet1(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      try {
        this.glowingService.getGlowingEntities().setGlowing(onlinePlayer, player, ChatColor.GREEN);
        this.teamService.createOrGetTeam(onlinePlayer).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @CommandDescription("glowing remove")
  @CommandMethod("glowing remove1")
  private void glowingRemove1(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      try {
        this.teamService.removeTeam(onlinePlayer);
        this.glowingService.getGlowingEntities().unsetGlowing(onlinePlayer, player);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
