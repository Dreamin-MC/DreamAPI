package fr.dreamin.dreamapi.core.item.builder;

import fr.dreamin.dreamapi.api.annotations.Internal;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Getter
@Internal
public final class PotionBuilder extends ItemBuilder {

  private final PotionMeta potionMeta;

  public PotionBuilder(final @NotNull PotionItemType type) {
    this(type, 1);
  }

  public PotionBuilder(final @NotNull PotionItemType type, final int amount) {
    super(type.getMaterial(), amount);

    this.potionMeta = (PotionMeta) getIs().getItemMeta();
  }

  public PotionBuilder(final @NotNull ItemStack stack) {
    super(stack);

    this.potionMeta = (PotionMeta) getIs().getItemMeta();
  }


  // ###############################################################
  // ---------------------------- TYPE -----------------------------
  // ###############################################################

  public @NotNull PotionBuilder basePotionType(final @NotNull PotionType data) {
    this.potionMeta.setBasePotionType(data);
    return this;
  }

  // ###############################################################
  // --------------------------- EFFECT ----------------------------
  // ###############################################################

  public @NotNull PotionBuilder addEffect(final @NotNull PotionEffect effect) {
    this.potionMeta.addCustomEffect(effect, true);
    return this;
  }

  public @NotNull PotionBuilder addEffects(final @NotNull Collection<PotionEffect> effects) {
    effects.forEach(effect -> this.potionMeta.addCustomEffect(effect, true));
    return this;
  }

  public @NotNull PotionBuilder addEffect(final @NotNull PotionEffectType type, final int durationTicks, final int amplifier) {
    this.potionMeta.addCustomEffect(new PotionEffect(type, durationTicks, amplifier), true);
    return this;
  }

  // ###############################################################
  // ---------------------------- COLOR ----------------------------
  // ###############################################################

  public @NotNull PotionBuilder color(final @NotNull Color color) {
    this.potionMeta.setColor(color);

    return this;
  }

  public @NotNull PotionBuilder color(final @NotNull java.awt.Color color) {
    this.potionMeta.setColor(Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue()));
    return this;
  }

  public @NotNull PotionBuilder clearEffects() {
    for (final var effect : this.potionMeta.getCustomEffects()) {
      this.potionMeta.removeCustomEffect(effect.getType());
    }
    return this;
  }

  @Override
  public ItemStack build() {
    getIs().setItemMeta(this.potionMeta);
    return getIs().clone();
  }

  @Override
  public xyz.xenondevs.invui.item.ItemBuilder toGuiItem() {
    return new xyz.xenondevs.invui.item.ItemBuilder(build());
  }

}
