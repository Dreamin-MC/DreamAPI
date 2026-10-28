package fr.dreamin.dreamapi.api.nms.visual.fakeEntity.model;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface FakeEntityMetadata {

  void setCustomName(final @Nullable Component name);
  void setCustomNameVisible(final boolean visible);

  void setGlowing(final boolean glowing);
  void setInvisible(final boolean invisible);
  void setNoGravity(final boolean noGravity);
  void setSilent(final boolean silent);

  void setHelmet(final @Nullable ItemStack item);
  void setChestplate(final @Nullable ItemStack item);
  void setLeggings(final @Nullable ItemStack item);
  void setBoots(final @Nullable ItemStack item);
  void setMainHand(final @Nullable ItemStack item);
  void setOffHand(final @Nullable ItemStack item);

}
