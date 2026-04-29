package fr.dreamin.example.cmd;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import fr.dreamin.dreamapi.api.cmd.DreamCmd;
import fr.dreamin.dreamapi.api.item.ItemAction;
import fr.dreamin.dreamapi.api.item.ItemDefinition;
import fr.dreamin.dreamapi.api.item.ItemRegistryService;
import fr.dreamin.dreamapi.api.item.ItemTag;
import fr.dreamin.dreamapi.api.lang.utils.LangUtils;
import fr.dreamin.dreamapi.api.nms.visual.service.VisualService;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import fr.dreamin.example.ExamplePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.i18n.Languages;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@DreamCmd
public final class TestCmd {

  @CommandDescription("Test")
  @CommandMethod("test1")
  @CommandPermission("test")
  private void test(CommandSender sender) {
    if (!(sender instanceof Player player)) return;


  }

  @CommandDescription("Test command")
  @CommandMethod("test3 <value>")
  @CommandPermission("test")
  private void test3(CommandSender sender, @Argument("value") String value) {
    if (!(sender instanceof Player player)) return;

    final var v = Languages.getInstance().getFormatString(player.locale(), value);
    player.sendMessage(Component.translatable(v != null ? v : "NULL"));
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
