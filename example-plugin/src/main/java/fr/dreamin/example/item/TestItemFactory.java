package fr.dreamin.example.item;

import fr.dreamin.dreamapi.api.item.model.ItemAction;
import fr.dreamin.dreamapi.api.item.model.ItemDefinition;
import fr.dreamin.dreamapi.api.item.annotations.DreamItem;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class TestItemFactory {

  @DreamItem()
  public ItemDefinition apple() {
    return ItemDefinition.builder()
      .id("test_apple")
      .item(new ItemStack(Material.APPLE))
      .build();
  }

  @DreamItem()
  public ItemDefinition carrot() {
    return ItemDefinition.builder()
      .id("test_diamond")
      .item(new ItemStack(Material.CARROT_ON_A_STICK))
      .handler(ItemAction.RIGHT_CLICK, ctx -> {
        ctx.item().damage(1, ctx.player());
        return false;
      })
      .build();
  }

  @DreamItem()
  public ItemDefinition armor() {
    return ItemDefinition.builder()
      .id("test_armor")
      .item(new ItemBuilder(Material.EMERALD)
        .setEquipSlot(EquipmentSlot.HEAD)
        .build()
      )
      .handler(ItemAction.SET_ARMOR, (ctx) -> {
        ctx.player().sendMessage("You equipped the test armor!");

        return false;
      })
      .handler(ItemAction.REMOVE_ARMOR, (ctx) -> {
        ctx.player().sendMessage("You unequipped the test armor!");
        return false;
      })
      .build();
  }

  @DreamItem()
  public ItemDefinition armor2() {
    return ItemDefinition.builder()
      .id("test_armor_2")
      .item(new ItemBuilder(Material.CAKE)
        .setEquipSlot(EquipmentSlot.HEAD)
        .build()
      )
      .handler(ItemAction.SET_ARMOR, (ctx) -> {
        ctx.player().sendMessage("You equipped the test armor!");

        return false;
      })
      .handler(ItemAction.REMOVE_ARMOR, (ctx) -> {
        ctx.player().sendMessage("You unequipped the test armor!");
        return false;
      })
      .build();
  }


}
