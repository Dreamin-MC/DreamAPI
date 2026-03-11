package fr.dreamin.dreamapi.api.hologram.model.line.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.dreamin.dreamapi.api.hologram.model.animation.HologramAnimation;
import fr.dreamin.dreamapi.api.hologram.model.line.HologramLine;
import fr.dreamin.dreamapi.api.hologram.model.line.LineConfig;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@JsonTypeName("ITEM")
public final class ItemHologramLine implements HologramLine {

  private final @NotNull String id;
  private final @NotNull LineConfig config;
  private final @Nullable HologramAnimation animation;
  private @NotNull ItemStack item;

  private final List<Display> entities = new ArrayList<>();

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  @JsonCreator
  public ItemHologramLine(
    @JsonProperty("id") final @NotNull String id,
    @JsonProperty("config") final @Nullable LineConfig config,
    @JsonProperty("animation") final @Nullable HologramAnimation animation,
    @JsonProperty("item") final @NotNull ItemStack item
  ) {
    this.id = id;
    this.config = config != null ? config : LineConfig.builder().build();
    this.animation = animation;
    this.item = item;
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void spawn(@NotNull Location location) {
    if (isSpawned()) despawn();
    final var loc = location.clone().add(
      this.config.getOffsetX(),
      this.config.getOffsetY(),
      this.config.getOffsetZ()
    );
    final var display = loc.getWorld().spawn(loc, ItemDisplay.class, d -> {
      d.setItemStack(this.item);
      d.setBillboard(Display.Billboard.CENTER);
      d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
      d.setShadowRadius(0.0f);
      d.setShadowStrength(1.0f);
    });
    this.entities.add(display);
  }

  @Override
  public void despawn() {
    this.entities.forEach(Display::remove);
    this.entities.clear();
  }

  @Override
  public void update() {
    if (!isSpawned()) return;
    for (final Display display : this.entities) {
      if (display instanceof ItemDisplay itemDisplay)
        itemDisplay.setItemStack(this.item);
    }
  }

  @Override
  public void applyAnimation(@NotNull HologramAnimation animation, long tick) {
    if (!isSpawned()) return;
    this.entities.forEach(e -> animation.apply(e, tick));
  }

  @Override
  public boolean isSpawned() {
    return !this.entities.isEmpty();
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public void setItem(final @NotNull ItemStack item) {
    this.item = item;
  }

}
