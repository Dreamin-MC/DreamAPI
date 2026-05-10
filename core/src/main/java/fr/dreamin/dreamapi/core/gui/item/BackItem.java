package fr.dreamin.dreamapi.core.gui.item;

import fr.dreamin.dreamapi.api.gui.model.GuiInterface;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

@RequiredArgsConstructor
public class BackItem extends AbstractItem {

  private final GuiInterface gui;

  @Override
  public @NotNull ItemProvider getItemProvider(@NotNull Player player1) {
    return new ItemBuilder(Material.OAK_DOOR)
      .setName(Component.translatable("Retour en arrière"))
      .toGuiItem();
  }

  @Override
  public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
    if (this.gui == null) return;
    this.gui.open(player);
  }
}
