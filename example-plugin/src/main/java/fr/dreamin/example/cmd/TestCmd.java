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
import net.kyori.adventure.text.Component;
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
//
//    final var animationService = DreamAPI.getAPI().getService(AnimationService.class);
//
//    var anim = animationService
//      .cinematic("intro")
//      .camera(
//        new Location(player.getWorld(), 100, 80, 100),
//        new Location(player.getWorld(), 110, 85, 105),
//        Duration.ofSeconds(2),
//        InterpolationType.EASE_IN_OUT
//      )
//      .camera(
//        new Location(player.getWorld(), 110, 85, 105),
//        new Location(player.getWorld(), 120, 88, 150),
//        Duration.ofSeconds(3)
//      )
//      .returnToStart(true)
//      .copyInventory(true)
//      .build();
//
//    anim.play(player);

    final var locA = new Location(player.getWorld(), -100, -100, -100);
    final var locB = new Location(player.getWorld(), 100, 100, 100);

    Cuboid cuboid = new Cuboid(locA, locB);

    cuboid.isLocationIn(player.getLocation());

    cuboid.setMaterialEveryTick(Material.IRON_BLOCK, 2, true);


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
          .item(new ItemBuilder(Material.PAPER).build())
          .tag(ItemTag.of("example-tag"))
          .handler(ItemAction.DROP, ctx -> {
            ctx.player().sendMessage(Component.text("You dropped the test item!"));
            return false;
          })
          .handler(ItemAction.DROP, ctx -> {
            ctx.player().sendMessage(Component.text("ttttttt!"));
            return false;
          })
          .handler(ItemAction.PICKUP, ctx -> {
            ctx.player().sendMessage(Component.text("You picked up the test item!"));
            return false;
          })
          .handlers(ItemAction.RIGHT_CLICK_AIR,
            List.of(
              ItemHandlers.permission("example.use"),
              ItemHandlers.cooldown(5),
              ctx -> {
                ctx.item().add(-1);
                ctx.player().sendMessage(Component.text("You used the test item!"));
                return false;
              })
          )
          .build()
      );

    player.getInventory().addItem(itemRegistry.get("test").item());

  }

}
