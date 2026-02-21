package fr.dreamin.dreamapi.api.hologram.model;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.hologram.event.HologramCreateEvent;
import fr.dreamin.dreamapi.api.hologram.event.HologramUpdateEvent;
import fr.dreamin.dreamapi.api.hologram.service.HologramService;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class HologramBuilder {

  private final @NotNull String id;
  private Location location;
//  @With
  private HologramConfig config;
  private final List<Display> entities = new ArrayList<>();
  private int ticks = 0;
  private final @NotNull HologramService service;

  // ###############################################################
  // --------------------- CONSTRUCTOR METHODS ---------------------
  // ###############################################################

  public HologramBuilder(
    final @NotNull String id,
    final @NotNull HologramService service
  ) {
    this.id = id;
    this.service = DreamAPI.getAPI().getService(HologramService.class);
    this.config = HologramConfig.builder().build();
    this.service.register(this);
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public void spawn(final @NotNull Location loc) {
    this.location = loc;
    if (!new HologramCreateEvent(this.id, this).callEvent())
      return;
//    spawnEntities();
  }

  public void update() {
//    this.ticks++;
//
//    if (!shouldUpdate())
//      return;
//
//    final var newConfig = computeNewConfig();
//    if (!newConfig.equals(this.config)) {
//      if (!new HologramUpdateEvent(this.id, this.config, newConfig).callEvent())
//        return;
//
//      this.config = newConfig;
//      applyConfig();
//    }

  }

  public void teleport(final @NotNull Location newLoc) {
    this.location = newLoc;

    Bukkit.getScheduler().runTask(DreamAPI.getAPI().plugin(), () -> {
      double totalHeight = 0;
      for (HologramLine line : this.config.lines()) {
        totalHeight += line.getHeight() + this.config.lineSpacing();
      }

      double currentYOffset = totalHeight;
      for (int i = 0; i < this.entities.size(); i++) {
        final var line = this.config.lines().get(i);
        currentYOffset -= (line.getHeight() + this.config.lineSpacing());
        final var entityLoc = newLoc.clone().add(0, currentYOffset, 0);
        this.entities.get(i).teleport(entityLoc);
      }
    });
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void spawn() {
//    if (!new HologramCreateEvent(this.id, this.location, this.config).callEvent())
//      return;

    double yOffset = 0;
    for (final var line : this.config.lines()) {
      final var entityLoc = this.location.clone().add(0, yOffset, 0);
      final var display = spawnLine(entityLoc, line);
      this.entities.add(display);
      yOffset -= (line.getHeight() + this.config.lineSpacing());
    }
  }

  private Display spawnLine(final @NotNull Location loc, final @NotNull HologramLine line) {
    return switch (line) {
      case TextLine textLine -> spawnTextDisplay(loc, textLine);
      case ItemLine itemLine -> spawnItemDisplay(loc, itemLine);
      default -> throw new IllegalArgumentException("Unknown HologramLine type: " + line.getClass().getName());
    };
  }

  private TextDisplay spawnTextDisplay(final @NotNull Location loc, final @NotNull TextLine line) {
    final var display = loc.getWorld().spawn(loc, TextDisplay.class);

    display.setBillboard(Display.Billboard.CENTER);
    display.setAlignment(TextDisplay.TextAlignment.CENTER);
    display.text(line.text());
    display.setViewRange(this.config.viewRange());
    display.setShadowRadius(0.0f);
    display.setShadowStrength(1.0f);
//    display.setGlowing(this.config.glowing());

    applyDisplayTransform(display);

    display.setSeeThrough(this.config.seeThrough());

    return display;
  }

  private ItemDisplay spawnItemDisplay(final @NotNull Location loc, final @NotNull ItemLine line) {
    final var display = loc.getWorld().spawn(loc, ItemDisplay.class);

    display.setBillboard(Display.Billboard.CENTER);
    display.setItemStack(line.item());
    display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);

    display.setViewRange(this.config.viewRange());
    display.setShadowRadius(0.0F);
    display.setShadowStrength(1.0F);

    applyDisplayTransform(display);

    return display;
  }

  private void applyDisplayTransform(final Display display) {
    final var transform = display.getTransformation();

    transform.getScale().set(this.config.scale());

//    if (this.config.animation() != null && this.config.animation().type() == ROTATE) {
//      final var angleRad = (float) (this.ticks * this.config.animation().speed());
//
//      transform.getLeftRotation().setAngleAxis(angleRad, 0f, 1f, 0f);
//
//    }

    display.setTransformation(transform);
  }

  private void applyConfig() {
    Bukkit.getScheduler().runTask(DreamAPI.getAPI().plugin(), () -> {
      for (var i = 0; i < this.entities.size(); i++) {
        final var display = (Display) this.entities.get(i);
        final var line = this.config.lines().get(i);

        if (display instanceof TextDisplay textDisplay && line instanceof TextLine(Component text))
          textDisplay.text(text);
        else if (display instanceof ItemDisplay itemDisplay && line instanceof ItemLine(ItemStack item))
          itemDisplay.setItemStack(item);

        applyDisplayTransform(display);
      }
    });
  }

}
