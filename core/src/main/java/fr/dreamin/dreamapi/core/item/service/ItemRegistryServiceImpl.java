package fr.dreamin.dreamapi.core.item.service;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import fr.dreamin.dreamapi.api.item.*;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.api.annotations.Inject;
import fr.dreamin.dreamapi.core.dependency.DependencyLoader;
import fr.dreamin.dreamapi.core.item.RegisteredItemImpl;
import fr.dreamin.dreamapi.core.item.event.PlayerItemUseEvent;
import fr.dreamin.dreamapi.core.modelengine.listener.ModelEngineItemListener;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Inject
@DreamAutoService(value= ItemRegistryService.class)
@RequiredArgsConstructor
public final class ItemRegistryServiceImpl implements ItemRegistryService, DreamService, Listener {

  private static final Map<PlayerItemUseEvent.ActionType, ItemAction> ACTION_MAP = Map.of(
    PlayerItemUseEvent.ActionType.SWAP, ItemAction.SWAP,
    PlayerItemUseEvent.ActionType.SHIFT_SWAP, ItemAction.SHIFT_SWAP
  );

  private final @NotNull Plugin plugin;

  private final @NotNull HashMap<String, RegisteredItem> byId = new HashMap<>();

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onLoad(@NotNull Plugin plugin) {

    if (DependencyLoader.canLoad(ModelEngineItemListener.class)) {
      plugin.getServer().getPluginManager().registerEvents(
        new ModelEngineItemListener(this),
        plugin
      );
    }

  }


  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public void registers(@NotNull Collection<ItemDefinition> definitions) {
    for (final var def : definitions) {
      register(def);
    }
  }

  @Override
  public void register(@NotNull ItemDefinition def) {
    if (this.byId.containsKey(def.getId()))
      throw new IllegalStateException(String.format("Item with id '%s' is already registered.", def.getId()));

    final var registered = new RegisteredItemImpl(
      def.getId(),
      def.getItem(),
      def.getTags(),
      def.getHandlers()
    );

    this.byId.put(def.getId(), registered);

  }

  @Override
  public void addHandler(@NotNull String id, @NotNull ItemAction action, @NotNull ItemHandler handler) {
    final var registered = get(id);
    if (registered == null)
      throw new IllegalStateException(String.format("Item with id '%s' is not registered.", id));

    registered.addHandler(action, handler);
  }

  @Override
  public void addHandler(@NotNull ItemTag tag, @NotNull ItemAction action, @NotNull ItemHandler handler) {
    final var tagName = tag.name();
    this.byId.values().stream()
      .filter(registered -> registered.tags().stream()
        .anyMatch(t -> t.name().equalsIgnoreCase(tagName)))
      .forEach(registered -> registered.addHandler(action, handler));
  }

  @Override
  public Collection<RegisteredItem> getAllRegisteredItems() {
    return Collections.unmodifiableCollection(this.byId.values());
  }

  @Override
  public Collection<ItemStack> getAllRegisteredItemStacks() {
    return this.byId.values().stream()
      .map(RegisteredItem::item)
      .toList();
  }

  @Override
  public RegisteredItem get(@NotNull String id) {
    return this.byId.get(id);
  }

  @Override
  public RegisteredItem get(@NotNull ItemStack item) {
    if (!item.hasItemMeta()) return null;

    final var id = item.getItemMeta()
      .getPersistentDataContainer()
      .get(ItemKeys.ITEM_ID, PersistentDataType.STRING);

    return id != null ? this.byId.get(id) : null;
  }


  @Override
  public Collection<RegisteredItem> getAllRegisteredItems(@NotNull ItemTag tag) {
    final var tagName = tag.name();
    return this.byId.values().stream()
      .filter(registered -> registered.tags().stream()
        .anyMatch(t -> t.name().equalsIgnoreCase(tagName)))
      .toList();
  }

  @Override
  public Collection<ItemStack> getAllRegisteredItemStacks(@NotNull ItemTag tag) {
    final var tagName = tag.name();
    return this.byId.values().stream()
      .filter(registered -> registered.tags().stream()
        .anyMatch(t -> t.name().equalsIgnoreCase(tagName)))
      .map(RegisteredItem::item)
      .toList();
  }

  @Override
  public @Nullable ItemStack getItem(@NotNull String id) {
    final var registered = get(id);
    return registered != null ? registered.item().clone() : null;
  }

  @Override
  public boolean isRegistered(@NotNull String id) {
    return get(id) != null;
  }

  @Override
  public boolean isRegistered(@NotNull ItemStack item) {
    return get(item) != null;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private ItemAction mapAction(PlayerItemUseEvent event) {
    var simple = ACTION_MAP.get(event.getAction());
    if (simple != null) return simple;

    switch (event.getAction()) {
      case LEFT -> {
        if (event.hasBlock()) return ItemAction.LEFT_CLICK_BLOCK;
        if (event.hasEntity()) return ItemAction.LEFT_CLICK_ENTITY;
        return ItemAction.LEFT_CLICK_AIR;
      }
      case RIGHT -> {
        if (event.hasBlock()) return ItemAction.RIGHT_CLICK_BLOCK;
        if (event.hasEntity()) return ItemAction.RIGHT_CLICK_ENTITY;
        return ItemAction.RIGHT_CLICK_AIR;
      }
      case SHIFT_LEFT -> {
        if (event.hasBlock()) return ItemAction.SHIFT_LEFT_CLICK_BLOCK;
        if (event.hasEntity()) return ItemAction.SHIFT_LEFT_CLICK_ENTITY;
        return ItemAction.SHIFT_LEFT_CLICK_AIR;
      }
      case SHIFT_RIGHT -> {
        if (event.hasBlock()) return ItemAction.SHIFT_RIGHT_CLICK_BLOCK;
        if (event.hasEntity()) return ItemAction.SHIFT_RIGHT_CLICK_ENTITY;
        return ItemAction.SHIFT_RIGHT_CLICK_AIR;
      }
      default -> {
        return null;
      }
    }
  }

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler
  private void onInteract(final @NotNull PlayerItemUseEvent event) {
    final var player = event.getPlayer();
    final var item = event.getIs();

    final var registered = get(item);
    if (registered == null) return;

    final var itemAction = mapAction(event);
    if (itemAction == null) return;

    registered.execute(
      itemAction,
      new ItemContext(player, item, event)
    );
  }

  @EventHandler
  private void onDrop(final @NotNull PlayerDropItemEvent event) {
    final var player = event.getPlayer();
    final var item = event.getItemDrop().getItemStack();
    final var registered = get(item);
    if (registered == null) return;

    registered.execute(
      ItemAction.DROP,
      new ItemContext(player, item, event)
    );
  }

  @EventHandler
  private void onPickUp(final @NotNull EntityPickupItemEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;
    final var item = event.getItem().getItemStack();
    final var registered = get(item);
    if (registered == null) return;

    registered.execute(
      ItemAction.PICKUP,
      new ItemContext(player, item, event)
    );
  }

  @EventHandler
  private void onChat(final @NotNull AsyncChatEvent event) {
    final var player = event.getPlayer();
    final var item = player.getInventory().getItemInMainHand();
    if (item.isEmpty()) return;

    final var registered = get(item);
    if (registered == null) return;

    registered.execute(
      ItemAction.CHAT_SEND,
      new ItemContext(player, item, event)
    );
  }

  @EventHandler
  private void onItemHeld(final @NotNull PlayerItemHeldEvent event) {
    final var player = event.getPlayer();
    final var inv = player.getInventory();
    final var newItem = inv.getItem(event.getNewSlot());
    final var oldItem = inv.getItem(event.getPreviousSlot());

    if (newItem != null && !newItem.isEmpty()) {
      final var registered = get(newItem);
      if (registered != null)
        registered.execute(
          ItemAction.HELD,
          new ItemContext(player, newItem, event)
        );
    }

    if (oldItem != null && !oldItem.isEmpty()) {
      final var registered = get(oldItem);
      if (registered != null)
        registered.execute(
          ItemAction.UNHELD,
          new ItemContext(player, oldItem, event)
        );
    }
  }

  @EventHandler
  private void onItemHeldChange(final @NotNull PlayerInventorySlotChangeEvent event) {
    final var player = event.getPlayer();
    final var heldSlot = player.getInventory().getHeldItemSlot();
    if (heldSlot != event.getSlot()) return;

    final var newItem = event.getNewItemStack();
    final var oldItem = event.getOldItemStack();
    final var newRegistered = get(newItem);
    final var oldRegistered = get(oldItem);

    if (oldRegistered != null && newRegistered == oldRegistered)
      return;

    if (newRegistered != null) {
      newRegistered.execute(
        ItemAction.HELD,
        new ItemContext(player, newItem, event)
      );
    }

    if (oldRegistered != null) {
      oldRegistered.execute(
        ItemAction.UNHELD,
        new ItemContext(player, oldItem, event)
      );
    }
  }

  @EventHandler
  private void onAttack(final @NotNull EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player)) return;

    final var item = player.getInventory().getItemInMainHand();
    final var registered = get(item);
    if (registered == null) return;

    registered.execute(
      ItemAction.ATTACK,
      new ItemContext(player, item, event)
    );

  }

  @EventHandler
  private void onEquip(final @NotNull PlayerArmorChangeEvent event) {
    final var player = event.getPlayer();
    final var newItem = event.getNewItem();
    final var oldItem = event.getOldItem();

    final var registeredOld = get(oldItem);
    if (registeredOld != null)
      registeredOld.execute(
        ItemAction.REMOVE_ARMOR,
        new ItemContext(player, oldItem, event)
      );

    final var registeredNew = get(newItem);
    if (registeredNew != null)
      registeredNew.execute(
        ItemAction.SET_ARMOR,
        new ItemContext(player, newItem, event)
      );

  }

  @EventHandler
  private void onConsume(final @NotNull PlayerItemConsumeEvent event) {
    final var player = event.getPlayer();
    final var item = event.getItem();

    final var registeredItem = get(item);
    if (registeredItem == null) return;

    registeredItem.execute(
      ItemAction.CONSUME,
      new ItemContext(player, item, event)
    );

  }

  @EventHandler
  private void onEat(final @NotNull FoodLevelChangeEvent event) {
    if (!(event.getEntity() instanceof Player player)) return;

    final var item = event.getItem();
    if (item == null) return;

    final var registered = get(item);
    if (registered == null) return;

    registered.execute(
      ItemAction.EAT,
      new ItemContext(player, item, event)
    );

  }

}
