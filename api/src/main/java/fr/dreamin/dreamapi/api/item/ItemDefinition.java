package fr.dreamin.dreamapi.api.item;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

@Builder
@Getter
public class ItemDefinition {

  private final String id;

  @Setter
  private ItemStack item;

  @Singular
  private final Set<ItemTag> tags;

  private final Map<ItemAction, List<ItemHandler>> handlers;

  public static ItemDefinitionBuilder builder() {
    return new ItemDefinitionBuilder();
  }

  public static class ItemDefinitionBuilder {
    private final Map<ItemAction, List<ItemHandler>> handlers = new HashMap<>();

    public ItemDefinitionBuilder handler(ItemAction action, ItemHandler handler) {
      this.handlers.computeIfAbsent(action, k -> new ArrayList<>()).add(handler);
      return this;
    }

    public ItemDefinitionBuilder handlers(ItemAction action, List<ItemHandler> handlersList) {
      this.handlers.computeIfAbsent(action, k -> new ArrayList<>()).addAll(handlersList);
      return this;
    }

    public ItemDefinitionBuilder handlers(ItemAction action, ItemHandler... handlers) {
      this.handlers.computeIfAbsent(action, k -> new ArrayList<>()).addAll(List.of(handlers));
      return this;
    }

    public ItemDefinition build() {
      Objects.requireNonNull(id, "ItemDefinition id cannot be null");
      Objects.requireNonNull(item, "ItemDefinition item cannot be null");

      final var built = item.clone();
      final var meta = built.getItemMeta();

      meta.getPersistentDataContainer().set(
        ItemKeys.ITEM_ID,
        PersistentDataType.STRING,
        id
      );

      built.setItemMeta(meta);

      return new ItemDefinition(
        id,
        built,
        tags == null ? Set.of() : Set.copyOf(tags),
        deepCopy(handlers)
      );
    }

    private Map<ItemAction, List<ItemHandler>> deepCopy(Map<ItemAction, List<ItemHandler>> source) {
      Map<ItemAction, List<ItemHandler>> copy = new EnumMap<>(ItemAction.class);
      source.forEach((k, v) -> copy.put(k, List.copyOf(v)));
      return copy;
    }


  }

}
