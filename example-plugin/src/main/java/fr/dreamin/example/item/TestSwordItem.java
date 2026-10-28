package fr.dreamin.example.item;

import fr.dreamin.dreamapi.api.item.model.ItemAction;
import fr.dreamin.dreamapi.api.item.model.ItemDefinition;
import fr.dreamin.dreamapi.api.item.model.ItemDefinitionSupplier;
import fr.dreamin.dreamapi.api.item.model.ItemTag;
import fr.dreamin.dreamapi.api.item.annotations.DreamItem;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

@DreamItem()
@RequiredArgsConstructor
public final class TestSwordItem implements ItemDefinitionSupplier {

  @Override
  public ItemDefinition get() {
    return ItemDefinition.builder()
      .id("test_sword")
      .tag(ItemTag.of("TEST"))
      .item(new ItemBuilder(Material.DIAMOND_SWORD).setName(Component.text("SWORD")).build())
      .handler(ItemAction.DROP, (ctx) -> {
        ctx.player().sendMessage("You tried to drop the test sword!");
        return false;
      })
      .build();
  }

}
