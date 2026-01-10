package fr.dreamin.example.cmd;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import fr.dreamin.dreamapi.api.cmd.DreamCmd;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@DreamCmd
public final class GUICmd {

  @CommandDescription("Test")
  @CommandMethod("gui test")
  @CommandPermission("test")
  private void test(CommandSender sender) {
    if (!(sender instanceof Player player)) return;


  }

}
