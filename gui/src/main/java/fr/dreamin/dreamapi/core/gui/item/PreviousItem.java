package fr.dreamin.dreamapi.core.gui.item;

import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractPagedGuiBoundItem;
import xyz.xenondevs.invui.item.ItemProvider;

@RequiredArgsConstructor
public class PreviousItem extends AbstractPagedGuiBoundItem {

  private final Material material;

  public PreviousItem() {
    this.material = Material.AIR;
  }

  @Override
  public @NotNull ItemProvider getItemProvider(@NotNull Player player1) {
    if (hasPrevious()) return new ItemBuilder(Material.ARROW).setName(Component.text("Précédent")).toGuiItem();
    return new ItemBuilder(material).toGuiItem();
  }

  @Override
  public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
    if (!hasPrevious()) return;
    getGui().setPage(getGui().getPage() - 1);
  }

  private boolean hasPrevious() {
    return (getGui().getPage() - 1) >= 0;
  }
}
