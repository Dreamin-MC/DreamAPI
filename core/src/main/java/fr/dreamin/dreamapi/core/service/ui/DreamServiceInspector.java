package fr.dreamin.dreamapi.core.service.ui;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.logger.DebugService;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.api.gui.model.GuiInterface;
import fr.dreamin.dreamapi.core.gui.item.BackItem;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import fr.dreamin.dreamapi.core.service.ServiceAnnotationProcessor;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class DreamServiceInspector {

  private final Plugin plugin;
  private final ServiceAnnotationProcessor manager;

  public void openMainMenu(final @NotNull Player player) {
    new MainMenuGUI().open(player);
  }

  public void openServiceDetail(final @NotNull Player player, final @NotNull Class<?> clazz, final @NotNull DreamService service) {
    new ServiceDetailGUI(clazz, service).open(player);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private Material materialForStatus(DreamService.ServiceStatus status) {
    return switch (status) {
      case LOADED -> Material.LIME_STAINED_GLASS_PANE;
      case LOADING -> Material.YELLOW_STAINED_GLASS_PANE;
      case FAILED -> Material.RED_STAINED_GLASS_PANE;
      case CLOSED -> Material.GRAY_STAINED_GLASS_PANE;
      default -> Material.WHITE_STAINED_GLASS_PANE;
    };
  }

  private String priorityOf(Class<?> clazz) {
    final var auto = clazz.getAnnotation(DreamAutoService.class);
    return auto != null ? auto.priority().name() : "N/A";
  }

  private class MainMenuGUI extends GuiInterface {

    // ##############################################################
    // ---------------------- SERVICE METHODS -----------------------
    // ##############################################################

    @Override
    public Component name(@NotNull Player player) {
      return Component.text("Service Inspector");
    }

    @Override
    public Gui guiUpper(@NotNull Player player) {
      return PagedGui.itemsBuilder()
        .setStructure(
          ". . . . . . . . .",
          ". X X X X X X X .",
          ". X X X X X X X .",
          ". X X X X X X X .",
          ". X X X X X X X .",
          ". P . . . . . N ."
        )
        .addIngredient('X', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .setContent(getItems())
        .build();
    }

    // ###############################################################
    // ----------------------- PRIVATE METHODS -----------------------
    // ###############################################################

    private List<Item> getItems() {
      final var items = new ArrayList<Item>();

      manager.getAllLoadedServices().forEach((clazz, service) -> {
        items.add(new AbstractItem() {
          @Override
          public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
            return new ItemBuilder(materialForStatus(service.getStatus()))
              .setName(Component.text(clazz.getSimpleName(), NamedTextColor.DARK_GRAY))
              .setLore(
                Component.empty(),
                Component.text("Status: ", NamedTextColor.GRAY).append(Component.text(service.getStatus().name(), NamedTextColor.GOLD)),
                Component.text("Priority: ", NamedTextColor.GRAY).append(Component.text(priorityOf(clazz), NamedTextColor.GOLD)),
                Component.empty(),
                Component.text("Click for more info.", NamedTextColor.DARK_GRAY),
                Component.empty()
              )
              .toGuiItem();
          }

          @Override
          public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            new ServiceDetailGUI(clazz, service).open(player);
          }
        });
      });

      return items;
    }

  }

  @RequiredArgsConstructor
  private class ServiceDetailGUI extends GuiInterface {

    private final @NotNull Class<?> clazz;
    private final @NotNull DreamService service;


    @Override
    public Component name(@NotNull Player player) {
      return Component.text("Service Details");
    }

    @Override
    public Gui guiUpper(@NotNull Player player) {
      final var auto = clazz.getAnnotation(DreamAutoService.class);
      final var deps = auto != null ? Arrays.asList(auto.dependencies()) : List.of();

      final var debug = DreamAPI.getAPI().getService(DebugService.class);

      final var componentDeps = new ArrayList<Component>();
      componentDeps.add(Component.empty());

      if (deps.isEmpty())
        componentDeps.add(Component.text("No dependencies.", NamedTextColor.DARK_GRAY));
      else
        for (final var dep : deps) {
          componentDeps.add(Component.text("- " + dep.getClass().getSimpleName(), NamedTextColor.GOLD));
        }

      final var dependants = manager.getAllLoadedServices().entrySet().stream()
        .filter(e -> {
          final var a = e.getKey().getAnnotation(DreamAutoService.class);
          if (a == null) return false;
          return Arrays.asList(a.dependencies()).contains(clazz);
        })
        .map(e -> e.getKey().getSimpleName())
        .toList();

      return Gui.builder()
        .setStructure(
          ". . . . . . . . .",
          ". S . . D . . E .",
          ". R . G . C . U .",
          ". . . . B . . . ."
        )
        .addIngredient('S', new ItemBuilder(Material.PAPER)
          .setName(Component.text(this.clazz.getSimpleName(), NamedTextColor.DARK_GRAY))
          .setLore(
            Component.empty(),
            Component.text("Status: ", NamedTextColor.GRAY).append(Component.text(service.getStatus().name(), NamedTextColor.GOLD)),
            Component.text("Priority: ", NamedTextColor.GRAY).append(Component.text(priorityOf(clazz), NamedTextColor.GOLD))
          )
          .toGuiItem()
        )
        .addIngredient('D', new ItemBuilder(Material.BOOK)
          .setName(Component.text("Dependencies", NamedTextColor.DARK_GRAY))
          .setLore(componentDeps)
          .toGuiItem()
        )
        .addIngredient('E', new ItemBuilder(Material.WRITABLE_BOOK)
          .setName(Component.text("Dependants", NamedTextColor.DARK_GRAY))
          .setLore(
            Component.empty(),
            dependants.isEmpty()
              ? Component.text("No dependants.", NamedTextColor.DARK_GRAY)
              : dependants.stream()
                .map(name -> Component.text("- " + name, NamedTextColor.GOLD))
                .reduce((c1, c2) -> c1.append(Component.newline()).append(c2))
                .orElse(Component.empty())
          )
          .toGuiItem()
        )
        .addIngredient('R', new AbstractItem() {
          @Override
          public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
            return new ItemBuilder(Material.LIME_DYE).setName(Component.text("Reload", NamedTextColor.GRAY)).toGuiItem();
          }

          @Override
          public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            manager.reloadService(service);
            new ServiceDetailGUI(clazz, service).open(player);
          }
        })
        .addIngredient('G', new AbstractItem() {
          @Override
          public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
            return new ItemBuilder(Material.BLUE_DYE).setName(Component.text("Reset", NamedTextColor.GRAY)).toGuiItem();
          }

          @Override
          public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            manager.resetService(service);
            new ServiceDetailGUI(clazz, service).open(player);
          }
        })
        .addIngredient('C', new AbstractItem() {
          @Override
          public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
            return new ItemBuilder(Material.RED_DYE).setName(Component.text("Close", NamedTextColor.GRAY)).toGuiItem();
          }

          @Override
          public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            manager.closeService(service);
            new ServiceDetailGUI(clazz, service).open(player);
          }
        })
        .addIngredient('U', new AbstractItem() {
          @Override
          public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
            return new ItemBuilder(Material.AMETHYST_SHARD)
              .setName(Component.text("Debug: ", NamedTextColor.GRAY).append(Component.text(debug.isCategoryEnabled(clazz.getSimpleName()) ? "ON" : "OFF", NamedTextColor.GOLD)))
              .toGuiItem();
          }

          @Override
          public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
            final var now = !debug.isCategoryEnabled(clazz.getSimpleName());
            debug.setCategory(clazz.getSimpleName(), now);
            new ServiceDetailGUI(clazz, service).open(player);
          }
        })
        .addIngredient('B', new BackItem(new MainMenuGUI()))
        .build();
    }

  }

}
