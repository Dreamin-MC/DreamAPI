package fr.dreamin.dreamapi.core.item.builder;


import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.annotations.Internal;
import fr.dreamin.dreamapi.core.item.Items;
import io.papermc.paper.datacomponent.DataComponentType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Internal implementation of the Paper ItemBuilder system.
 * <p>
 * Use {@link Items} for all public API interactions.
 * </p>
 */
@Getter
@Internal
public class ItemBuilder {

  private static final Cache<String, PlayerProfile> PROFILE_CACHE = CacheBuilder.newBuilder()
    .expireAfterAccess(1, TimeUnit.HOURS)
    .build();

  private ItemStack is;
  private ItemMeta itemMeta;

  // ###############################################################
  // ------------------------ CONSTRUCTORS -------------------------
  // ###############################################################

  /**
   * Constructs an ItemBuilder with a specified Material and a default amount of 1.
   *
   * @param m The material for the item.
   */
  public ItemBuilder(final @NotNull Material m) {this(m, 1);}

  /**
   * Constructs an ItemBuilder with a specified Material and amount.
   *
   * @param m      The material for the item.
   * @param amount The amount of the item in the stack.
   */
  public ItemBuilder(final @NotNull Material m, final int amount) {
    this.is = new ItemStack(m, amount);
    this.itemMeta = is.getItemMeta();
  }

  /**
   * Constructs an ItemBuilder from an existing ItemStack. The item meta is cloned to ensure it doesn't modify the
   * original ItemStack.
   *
   * @param is The existing ItemStack to copy.
   */
  public ItemBuilder(final @NotNull ItemStack is) {
    this.is = is.clone();
    this.itemMeta = this.is.getItemMeta();

    if (this.itemMeta == null) {
      this.itemMeta = Bukkit.getItemFactory().getItemMeta(this.is.getType());
      this.is.setItemMeta(this.itemMeta);
    }
  }

  // ###############################################################
  // -------------------------- UTILITIES --------------------------
  // ###############################################################

  private void withMeta(final @NotNull Consumer<ItemMeta> consumer) {
    final var meta = this.itemMeta;
    consumer.accept(meta);
    this.itemMeta = meta;
  }

  private <T extends ItemMeta> void withMeta(final @NotNull Class<T> metaClass, final @NotNull Consumer<T> consumer) {
    final var meta = this.itemMeta;

    if (!metaClass.isInstance(meta)) return;
    consumer.accept(metaClass.cast(this.itemMeta));

    this.itemMeta = meta;
  }

  // ###############################################################
  // ------------------------- BASIC PROPS -------------------------
  // ###############################################################

  public ItemBuilder addCustomModelData(final @NotNull String... value) {
    withMeta(meta -> {
      final var customComponent = meta.getCustomModelDataComponent();
      final var existing = new ArrayList<>(customComponent.getStrings());
      existing.addAll(Arrays.asList(value));
      customComponent.setStrings(existing);
      meta.setCustomModelDataComponent(customComponent);
    });
    return this;
  }

  public ItemBuilder addCustomModelData(final @NotNull Float... value) {
    withMeta(meta -> {
      final var customComponent = meta.getCustomModelDataComponent();
      final var existing = new ArrayList<>(customComponent.getFloats());
      existing.addAll(Arrays.asList(value));
      customComponent.setFloats(existing);
      meta.setCustomModelDataComponent(customComponent);
    });
    return this;
  }

  public ItemBuilder addCustomModelData(final @NotNull Boolean... value) {
    withMeta(meta -> {
      final var customComponent = meta.getCustomModelDataComponent();
      final var existing = new ArrayList<>(customComponent.getFlags());
      existing.addAll(Arrays.asList(value));
      customComponent.setFlags(existing);
      meta.setCustomModelDataComponent(customComponent);
    });
    return this;
  }

  public ItemBuilder addCustomModelData(final @NotNull Color... value) {
    withMeta(meta -> {
      final var customComponent = meta.getCustomModelDataComponent();
      final var existing = new ArrayList<>(customComponent.getColors());
      existing.addAll(Arrays.asList(value));
      meta.setCustomModelDataComponent(customComponent);
    });
    return this;
  }

  public ItemBuilder setType(final @NotNull Material type) {
    this.is = is.withType(type);
    return this;
  }

  public ItemBuilder setItemModel(final @NotNull NamespacedKey key) {
    withMeta(meta -> meta.setItemModel(key));
    return this;
  }

  public ItemBuilder setRarity(final @NotNull ItemRarity rarity) {
    withMeta(meta -> meta.setRarity(rarity));
    return this;
  }

  public ItemBuilder setHideToolType(final boolean b) {
    withMeta(meta -> meta.setHideTooltip(b));
    return this;
  }


  public ItemBuilder addItemFlag(final @NotNull ItemFlag... flags) {
    withMeta(meta -> meta.addItemFlags(flags));
    return this;
  }

  public ItemBuilder setUnbreakable(final boolean unbreakable) {
    withMeta(meta -> meta.setUnbreakable(unbreakable));
    return this;
  }

  // ###############################################################
  // ----------------------- AMOUNT / STACK ------------------------
  // ###############################################################

  public ItemBuilder setAmount(final int amount) {
    this.is.setAmount(amount);
    return this;
  }

  public ItemBuilder setMaxStackSize(final int amount) {
    withMeta(meta -> meta.setMaxStackSize(amount));
    return this;
  }

  // ###############################################################
  // ---------------------------- LORE -----------------------------
  // ###############################################################

  public ItemBuilder setLore(final @NotNull List<Component> lines) {
    withMeta(meta -> meta.lore(lines));
    return this;
  }

  public ItemBuilder setLore(final @NotNull Component... lines) {
    return setLore(Arrays.asList(lines));
  }

  public ItemBuilder setLegacyLore(final @NotNull String... lines) {
    withMeta(meta -> meta.setLore(Arrays.asList(lines)));
    return this;
  }

  // ###############################################################
  // ---------------------------- NAMES ----------------------------
  // ###############################################################

  public ItemBuilder setName(final @NotNull Component name) {
    withMeta(meta -> meta.itemName(name));
    return this;
  }

  public ItemBuilder setDisplayName(final @NotNull Component name) {
    withMeta(meta -> meta.displayName(name));
    return this;
  }

  public ItemBuilder setCustomName(final @NotNull Component name) {
    withMeta(meta -> meta.customName(name));
    return this;
  }

  public ItemBuilder setLegacyName(final @NotNull String name) {
    withMeta(meta -> meta.setDisplayName(name));
    return this;
  }

  // ###############################################################
  // ------------------------ LEATHER ARMOR ------------------------
  // ###############################################################

  public ItemBuilder setLeatherArmorColor(final @NotNull Color color) {
    withMeta(LeatherArmorMeta.class, meta -> meta.setColor(color));
    return this;
  }

  // ###############################################################
  // ------------------------- DURABILITY --------------------------
  // ###############################################################

  public ItemBuilder setDamage(final int damage) {
    withMeta(Damageable.class, meta -> meta.setDamage(damage));
    return this;
  }

  public ItemBuilder setMaxDamage(final int maxDamage) {
    withMeta(Damageable.class, meta -> meta.setMaxDamage(maxDamage));
    return this;
  }

  // ###############################################################
  // ------------------------ PLAYER HEADS -------------------------
  // ###############################################################

  public ItemBuilder setHeadFromName(final @NotNull String name) {
    if (name.isBlank()) return this;

    final var key = String.format("name:%s", name.toLowerCase());
    var profile = PROFILE_CACHE.getIfPresent(key);

    if (profile == null) {
      profile = Bukkit.createProfile(name);
      PROFILE_CACHE.put(key, profile);
    }

    PlayerProfile finalProfile = profile;
    withMeta(SkullMeta.class, meta -> meta.setPlayerProfile(finalProfile));
    return this;
  }

  public ItemBuilder setHeadFromUuid(final @NotNull UUID uuid) {
    final var key = String.format("uuid:%s", uuid);

    var profile = PROFILE_CACHE.getIfPresent(key);
    if (profile == null) {
      profile = Bukkit.createProfile(uuid);
      PROFILE_CACHE.put(key, profile);
    }

    PlayerProfile finalProfile = profile;
    withMeta(SkullMeta.class, meta -> meta.setPlayerProfile(finalProfile));
    return this;
  }

  public ItemBuilder setHeadFromProfile(final @NotNull PlayerProfile profile) {
    withMeta(SkullMeta.class, meta -> meta.setPlayerProfile(profile));
    return this;
  }

  public ItemBuilder setHeadFromBase64(final @NotNull String base64) {
    final var key = String.format("bas64:%s", base64.hashCode());
    var profile = PROFILE_CACHE.getIfPresent(key);

    if (profile == null) {
      profile = Bukkit.createProfile(UUID.randomUUID());
      PROFILE_CACHE.put(key, profile);
    }

    PlayerProfile finalProfile = profile;
    withMeta(SkullMeta.class, meta -> {
      finalProfile.setProperty(new ProfileProperty("textures", base64));
      meta.setPlayerProfile(finalProfile);
    });
    return this;
  }

  // ###############################################################
  // ------------------------ ENCHANTMENTS -------------------------
  // ###############################################################

  public ItemBuilder addEnchant(final @NotNull Enchantment enchant, int level) {
    withMeta(meta -> meta.addEnchant(enchant, level, true));
    return this;
  }

  public ItemBuilder addEnchants(final Map<Enchantment, Integer> enchants) {
    withMeta(meta -> enchants.forEach((e, l) -> meta.addEnchant(e, l, true)));
    return this;
  }

  public ItemBuilder removeEnchant(final @NotNull Enchantment enchant) {
    withMeta(meta -> meta.removeEnchant(enchant));
    return this;
  }

  public ItemBuilder clearEnchants() {
    withMeta(meta -> meta.getEnchants().keySet().forEach(meta::removeEnchant));
    return this;
  }

  public ItemBuilder setEnchantGlint(final boolean enabled) {
    withMeta(meta -> meta.setEnchantmentGlintOverride(enabled));
    return this;
  }

  // ###############################################################
  // -------------------------- COOLDOWN ---------------------------
  // ###############################################################

  public ItemBuilder setCooldown(final float seconds, final @NotNull NamespacedKey namespacedKey) {
    withMeta(meta -> {
      final var cd = meta.getUseCooldown();
      cd.setCooldownSeconds(seconds);
      cd.setCooldownGroup(namespacedKey);
      meta.setUseCooldown(cd);
    });
    return this;
  }

  public ItemBuilder setCooldown(final float seconds, final @NotNull String value) {
    return this.setCooldown(seconds, new NamespacedKey(DreamAPI.getAPI().plugin(), value));
  }

  // ###############################################################
  // ---------------------------- TOOL -----------------------------
  // ###############################################################

  public ItemBuilder setDefaultMiningSpeed(final float speed) {
    withMeta(meta -> {
      final var composant = meta.getTool();
      composant.setDefaultMiningSpeed(speed);
      meta.setTool(composant);
    });
    return this;
  }

  public ItemBuilder setDamagePerBlock(final int damage) {
    withMeta(meta -> {
      final var composant = meta.getTool();
      composant.setDamagePerBlock(damage);
      meta.setTool(composant);
    });
    return this;
  }

  public ItemBuilder addRule(final @NotNull Material material, final float speed, boolean correctForDrops) {
    withMeta(meta -> {
      final var composant = meta.getTool();
      composant.addRule(material, speed, correctForDrops);
      meta.setTool(composant);
    });
    return this;
  }

  public ItemBuilder addRule(final @NotNull Collection<Material> materials, final float speed, boolean correctForDrops) {
    withMeta(meta -> {
      final var composant = meta.getTool();
      composant.addRule(materials, speed, correctForDrops);
      meta.setTool(composant);
    });
    return this;
  }

  public ItemBuilder addRule(final @NotNull Tag<Material> materials, final float speed, boolean correctForDrops) {
    withMeta(meta -> {
      final var composant = meta.getTool();
      composant.addRule(materials, speed, correctForDrops);
      meta.setTool(composant);
    });
    return this;
  }

  // ################################################################
  // ---------------------- EQUIPMENT METHODS -----------------------
  // ################################################################

  public ItemBuilder setEquipSlot(final @NotNull EquipmentSlot slot) {
    withMeta(meta -> {
      final var equippableComponent = meta.getEquippable();
      equippableComponent.setSlot(slot);
      meta.setEquippable(equippableComponent);
    });

    return this;
  }

  public ItemBuilder setEquipModel(final @NotNull NamespacedKey key) {
    withMeta(meta -> {
      final var equippableComponent = meta.getEquippable();
      equippableComponent.setModel(key);
      meta.setEquippable(equippableComponent);
    });

    return this;
  }

  public ItemBuilder setEquipModel(final @NotNull String key) {
    withMeta(meta -> {
      final var equippableComponent = meta.getEquippable();
      equippableComponent.setModel(new NamespacedKey(DreamAPI.getAPI().plugin(), key));
      meta.setEquippable(equippableComponent);
    });

    return this;
  }

  public ItemBuilder setEquipCameraOverlay(final @NotNull NamespacedKey key) {
    withMeta(meta -> {
      final var equippableComponent = meta.getEquippable();
      equippableComponent.setCameraOverlay(key);
      meta.setEquippable(equippableComponent);
    });

    return this;
  }

  public ItemBuilder setEquipCameraOverlay(final @NotNull String key) {
    withMeta(meta -> {
      final var equippableComponent = meta.getEquippable();
      equippableComponent.setCameraOverlay(new NamespacedKey(DreamAPI.getAPI().plugin(), key));
      meta.setEquippable(equippableComponent);
    });

    return this;
  }

  public ItemBuilder setEquipSound(final @NotNull Sound sound) {
    withMeta(meta -> {
      final var equippableComponent = meta.getEquippable();
      equippableComponent.setEquipSound(sound);
      meta.setEquippable(equippableComponent);
    });

    return this;
  }

  public ItemBuilder setEquipSwappable(final Boolean b) {
    withMeta(meta -> {
      final var equippableComponent = meta.getEquippable();
      equippableComponent.setSwappable(b);
      meta.setEquippable(equippableComponent);
    });

    return this;
  }

  public ItemBuilder setEquipementComponent(final @NotNull EquippableComponent c) {
    withMeta(meta -> meta.setEquippable(c));

    return this;
  }

  // ###############################################################
  // ---------------------------- FOOD -----------------------------
  // ###############################################################

  public ItemBuilder setSaturation(final float value) {
    withMeta(meta -> {
      final var composant = meta.getFood();
      composant.setSaturation(value);
      meta.setFood(composant);
    });
    return this;
  }

  public ItemBuilder setNutrition(final int value) {
    withMeta(meta -> {
      final var composant = meta.getFood();
      composant.setNutrition(value);
      meta.setFood(composant);
    });
    return this;
  }

  public ItemBuilder setAlwaysEat(final boolean value) {
    withMeta(meta -> {
      final var composant = meta.getFood();
      composant.setCanAlwaysEat(value);
      meta.setFood(composant);
    });
    return this;
  }

  public ItemBuilder setFoodLevel(final int nutrition, final float saturation) {
    withMeta(meta -> {
      final var composant = meta.getFood();
      composant.setNutrition(nutrition);
      composant.setSaturation(saturation);
      meta.setFood(composant);
    });
    return this;
  }

  public ItemBuilder setFoodLevel(final int nutrition, final float saturation, final boolean alwaysEat) {
    withMeta(meta -> {
      final var composant = meta.getFood();
      composant.setNutrition(nutrition);
      composant.setSaturation(saturation);
      composant.setCanAlwaysEat(alwaysEat);
      meta.setFood(composant);
    });
    return this;
  }

  // ###############################################################
  // ------------------------- ATTRIBUTES --------------------------
  // ###############################################################

  public ItemBuilder addAttribute(final @NotNull Attribute attribute, final double value, final @NotNull AttributeModifier.Operation operation, final @NotNull EquipmentSlotGroup slot) {
    withMeta(meta -> {
      AttributeModifier modifier = new AttributeModifier(
        NamespacedKey.fromString(attribute.getKey().getKey() + "_" + value, DreamAPI.getAPI().plugin()),
        value, operation, slot);
      meta.addAttributeModifier(attribute, modifier);
    });
    return this;
  }

  public ItemBuilder addAttribute(final @NotNull Attribute attribute, final double value, final @NotNull AttributeModifier.Operation operation) {
    return addAttribute(attribute, value, operation, EquipmentSlotGroup.HAND);
  }

  public ItemBuilder addAttribute(final @NotNull Attribute attribute, final double value) {
    return addAttribute(attribute, value, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND);
  }

  public ItemBuilder addAttackSpeed(final double value) {
    return addAttribute(Attribute.ATTACK_SPEED, value);
  }

  public ItemBuilder addAttackDmg(final double value) {
    return addAttribute(Attribute.ATTACK_DAMAGE, value);
  }

  // ###############################################################
  // ----------------------- PERSISTENT DATA -----------------------
  // ###############################################################

  public <T, Z> ItemBuilder addPersistentData(final @Nullable NamespacedKey key, final @NotNull PersistentDataType<T, Z> type, final @NotNull Z value) {
    withMeta(meta -> meta.getPersistentDataContainer().set(key, type, value));
    return this;
  }

  public ItemBuilder addStringTag(final @NotNull String key, final @NotNull String value) {
    return addPersistentData(new NamespacedKey(DreamAPI.getAPI().plugin(), key), PersistentDataType.STRING, value);
  }

  public ItemBuilder addBooleanTag(final @NotNull String key, final boolean value) {
    return addPersistentData(new NamespacedKey(DreamAPI.getAPI().plugin(), key), PersistentDataType.BOOLEAN, value);
  }

  public ItemBuilder addIntTag(final @NotNull String key, final int value) {
    return addPersistentData(new NamespacedKey(DreamAPI.getAPI().plugin(), key), PersistentDataType.INTEGER, value);
  }

  public ItemBuilder addDoubleTag(final @NotNull String key, final double value) {
    return addPersistentData(new NamespacedKey(DreamAPI.getAPI().plugin(), key), PersistentDataType.DOUBLE, value);
  }

  // ###############################################################
  // ------------------------- COMPONENTS --------------------------
  // ###############################################################

  public <T> ItemBuilder setDataComponent(final @NotNull DataComponentType.Valued<T> key, T value) {
    is.setData(key, value);
    return this;
  }

  // ###############################################################
  // --------------------------- EXPORT ----------------------------
  // ###############################################################

  public ItemStack build() {
    this.is.setItemMeta(this.itemMeta);
    return this.is.clone();
  }

  public ItemBuilder copy(final @NotNull Consumer<ItemBuilder> modifier) {
    ItemBuilder clone = clone();
    modifier.accept(clone);
    return clone;
  }

  public xyz.xenondevs.invui.item.ItemBuilder toGuiItem() {
    return new xyz.xenondevs.invui.item.ItemBuilder(build());
  }

  @Override
  public ItemBuilder clone() {
    return new ItemBuilder(is);
  }

  // ###############################################################
  // ------------------------ META EDITING -------------------------
  // ###############################################################

  public <T extends ItemMeta> ItemBuilder editMeta(final @NotNull Class<T> metaClass, final @NotNull Consumer<T> consumer) {
    withMeta(metaClass, consumer);
    return this;
  }

}