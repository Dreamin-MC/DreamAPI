package fr.dreamin.example.cmd;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import fr.dreamin.dreamapi.api.cmd.DreamCmd;
import fr.dreamin.dreamapi.api.item.ItemAction;
import fr.dreamin.dreamapi.api.item.ItemDefinition;
import fr.dreamin.dreamapi.api.item.ItemRegistryService;
import fr.dreamin.dreamapi.api.item.ItemTag;
import fr.dreamin.dreamapi.api.cuboid.Cuboid;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import fr.dreamin.dreamapi.core.item.handler.ItemHandlers;
import fr.dreamin.example.ExamplePlugin;
import fr.dreamin.example.TestGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@DreamCmd
public final class TestCmd {

  @CommandDescription("Test")
  @CommandMethod("test1")
  @CommandPermission("test")
  private void test(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    for (final var p : Bukkit.getOnlinePlayers()) {
      p.setGlowing(false);
    }
  }

  @CommandDescription("Test")
  @CommandMethod("test2")
  @CommandPermission("test")
  private void test2(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    final var itemRegistry = ExamplePlugin.getService(ItemRegistryService.class);

    itemRegistry
      .register(
        ItemDefinition.builder()
          .id("test")
          .item(new ItemBuilder(Material.WARPED_FUNGUS_ON_A_STICK).setMaxDamage(100).setName(Component.text("Custom")).build())
          .tag(ItemTag.of("example-tag"))
          .handler(ItemAction.RIGHT_CLICK, ctx -> {
            ctx.player().sendMessage(Component.text("RIGHT_CLICK!"));
            ctx.item().damage(1, ctx.player());
            return false;
          })
          .handler(ItemAction.CHAT_SEND, ctx -> {
            ctx.player().sendMessage(Component.text("CHAT_SEND!"));
            return false;
          })
          .handler(ItemAction.UNHELD, ctx -> {
            ctx.player().sendMessage(Component.text("UNHELD!"));
            return false;
          })
          .handler(ItemAction.HELD, ctx -> {
            ctx.player().sendMessage(Component.text("HELD!"));
            return false;
          })
          .build()
      );

    player.getInventory().addItem(itemRegistry.get("test").item());

  }

}
