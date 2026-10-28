package fr.dreamin.dreamapi.core.item.cmd;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ItemRegistryCmd {

  @CommandDescription("Open GUi")
  @CommandMethod("itemregistry gui")
  @CommandPermission("dreamapi.cmd.itemregistry")
  public void openGUI(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    try {
      final var guiClass = Class.forName("fr.dreamin.dreamapi.core.item.ui.ItemRegistryGUI");
      final var gui = guiClass.getDeclaredConstructor().newInstance();
      guiClass.getMethod("open", Player.class).invoke(gui, player);
    } catch (Throwable t) {
      sender.sendMessage(Component.text("The dreamapi-gui module is required to open the item registry GUI.", NamedTextColor.RED));
    }
  }

}
