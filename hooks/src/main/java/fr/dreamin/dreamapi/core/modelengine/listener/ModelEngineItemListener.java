package fr.dreamin.dreamapi.core.modelengine.listener;

import com.ticxo.modelengine.api.events.BaseEntityInteractEvent;
import fr.dreamin.dreamapi.api.dependency.RequiresDependency;
import fr.dreamin.dreamapi.api.item.ItemAction;
import fr.dreamin.dreamapi.api.item.ItemContext;
import fr.dreamin.dreamapi.api.item.ItemRegistryService;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@RequiresDependency(value = "ModelEngine", hard = true, strict = true, loadAfter = true)
public final class ModelEngineItemListener implements Listener {

  private final @NotNull ItemRegistryService itemRegistryService;

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler
  private void onInteract(final @NotNull BaseEntityInteractEvent event) {
    if (event.getSlot() != EquipmentSlot.HAND || event.getAction() != BaseEntityInteractEvent.Action.INTERACT) return;

    final var player = event.getPlayer();
    final var item = event.getItem();
    if (item == null) return;

    final var registered = itemRegistryService.get(item);
    if (registered == null) return;

    registered.execute(ItemAction.INTERACT_MODEL_ENGINE, new ItemContext(player, item, event));
  }

}
