package fr.dreamin.example.cmd;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import fr.dreamin.dreamapi.api.cmd.DreamCmd;
import fr.dreamin.dreamapi.core.gui.GuiInterface;
import fr.dreamin.dreamapi.core.gui.example.InventoryBrowserGUI;
import fr.dreamin.dreamapi.core.gui.handler.AbstractGuiHandler;
import fr.dreamin.dreamapi.core.gui.visualizer.InventoryVisualizerHelper;
import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.inventory.VirtualInventory;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;

/**
 * CloudCommand-based GUI test command showcasing multiple InvUI patterns.
 */
@DreamCmd
public final class GuiTestCommand {

  @CommandDescription("Show GUI test command help")
  @CommandMethod("guitest|gt")
  @CommandPermission("example.guitest")
  private void guiTestHelp(CommandSender sender) {
    if (!(sender instanceof Player player)) return;
    sendHelp(player);
  }

  @CommandDescription("Open a basic GUI")
  @CommandMethod("guitest|gt simple")
  @CommandPermission("example.guitest")
  private void simple(CommandSender sender) {
    if (!(sender instanceof Player player)) return;
    openSimpleGUI(player);
  }

  @CommandDescription("Open a visualizer demo GUI")
  @CommandMethod("guitest|gt visualizer")
  @CommandPermission("example.guitest")
  private void visualizer(CommandSender sender) {
    if (!(sender instanceof Player player)) return;
    openVisualizerGUI(player);
  }

  @CommandDescription("Open a bundle-handler demo GUI")
  @CommandMethod("guitest|gt handler")
  @CommandPermission("example.guitest")
  private void handler(CommandSender sender) {
    if (!(sender instanceof Player player)) return;
    openHandlerGUI(player);
  }

  @CommandDescription("Open an inventory browser GUI")
  @CommandMethod("guitest|gt inventory")
  @CommandPermission("example.guitest")
  private void inventory(CommandSender sender) {
    if (!(sender instanceof Player player)) return;
    openInventoryBrowserGUI(player);
  }

  @CommandDescription("Open a paged GUI with many entries")
  @CommandMethod("guitest|gt paged")
  @CommandPermission("example.guitest")
  private void paged(CommandSender sender) {
    if (!(sender instanceof Player player)) return;
    openPagedGUI(player);
  }

  @CommandDescription("Open an advanced GUI using multiple features")
  @CommandMethod("guitest|gt advanced")
  @CommandPermission("example.guitest")
  private void advanced(CommandSender sender) {
    if (!(sender instanceof Player player)) return;
    openAdvancedGUI(player);
  }

  // ==================== SIMPLE GUI ====================
  // Basic GUI without any special features
  private void openSimpleGUI(Player player) {
    var gui = Gui.builder()
      .setStructure(
        "X X X",
        "X X X",
        "X X X"
      )
      .addIngredient('X', new ItemBuilder(Material.DIAMOND).toGuiItem())
      .build();

    Window.builder()
      .setViewer(player)
      .setUpperGui(gui)
      .setTitle(Component.text("Simple GUI", NamedTextColor.BLUE))
      .setCloseable(true)
      .build()
      .open();

    player.sendMessage("§a✓ Simple GUI opened!");
  }

  // ==================== VISUALIZER GUI ====================
  // Shows inventory visualizer-ready content
  private void openVisualizerGUI(Player player) {
    var inventory = new VirtualInventory(9);
    inventory.setItem(null, 0, new ItemStack(Material.DIAMOND, 1));
    inventory.setItem(null, 4, new ItemStack(Material.GOLD_INGOT, 64));

    var gui = Gui.builder()
      .setStructure(
        "X X X",
        "X X X",
        "X X X"
      )
      .addIngredient('X', inventory, 0)
      .build();

    Window.builder()
      .setViewer(player)
      .setUpperGui(gui)
      .setTitle(Component.text("Visualizer GUI", NamedTextColor.GREEN))
      .setCloseable(true)
      .build()
      .open();

    player.sendMessage("§a✓ Visualizer GUI opened!");
  }

  // ==================== HANDLER GUI ====================
  // Shows bundle select handlers
  private void openHandlerGUI(Player player) {
    var gui = new BundleTestGUI();
    gui.open(player);
    player.sendMessage("§a✓ Handler GUI opened! Try clicking items.");
  }

  // ==================== INVENTORY BROWSER GUI ====================
  // Uses the example InventoryBrowserGUI
  private void openInventoryBrowserGUI(Player player) {
    var inventory = new VirtualInventory(27);
    for (int i = 0; i < 27; i++) {
      if (i % 3 == 0) {
        inventory.setItem(null, i, new ItemStack(Material.DIAMOND, i + 1));
      }
    }

    var backGUI = new SimpleMenuGUI();
    var browser = InventoryBrowserGUI.builder()
      .inventory(inventory)
      .backButton(backGUI)
      .emptyPlaceholders(true)
      .placeholderMaterial(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
      .build();

    browser.open(player);
    player.sendMessage("§a✓ Inventory Browser opened with visualizers!");
  }

  // ==================== PAGED GUI ====================
  // Shows pagination with items
  private void openPagedGUI(Player player) {
    var items = new ArrayList<Item>();
    for (int i = 1; i <= 50; i++) {
      final int num = i;
      items.add(new AbstractItem() {
        @Override
        public @NotNull ItemProvider getItemProvider(@NotNull Player p) {
          return new ItemBuilder(Material.PAPER)
            .setName(Component.text("Item #" + num))
            .setLore(Component.text("Click me!", NamedTextColor.GRAY))
            .toGuiItem();
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player p, @NotNull Click click) {
          p.sendMessage(Component.text("You clicked item #" + num, NamedTextColor.YELLOW));
        }
      });
    }

    var gui = PagedGui.itemsBuilder()
      .setStructure(
        ". X X X X X X X .",
        ". X X X X X X X .",
        ". X X X X X X X .",
        "P . . . . . . . N"
      )
      .addIngredient('X', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
      .addIngredient('P', new ItemBuilder(Material.ARROW).setName(Component.text("Previous")).toGuiItem())
      .addIngredient('N', new ItemBuilder(Material.ARROW).setName(Component.text("Next")).toGuiItem())
      .setContent(items)
      .build();

    Window.builder()
      .setViewer(player)
      .setUpperGui(gui)
      .setTitle(Component.text("Paged GUI", NamedTextColor.AQUA))
      .setCloseable(true)
      .build()
      .open();

    player.sendMessage("§a✓ Paged GUI opened (50 items total)!");
  }

  // ==================== ADVANCED GUI ====================
  // Shows multiple advanced features combined
  private void openAdvancedGUI(Player player) {
    var gui = new AdvancedTestGUI();
    gui.open(player);
    player.sendMessage("§a✓ Advanced GUI opened with visualizers + handlers!");
  }

  // ==================== HELP ====================
  private void sendHelp(Player player) {
    player.sendMessage(Component.empty()
      .append(Component.text("═════════════════════════════════", NamedTextColor.BLUE))
      .appendNewline()
      .append(Component.text("GUI Test Command Help", NamedTextColor.AQUA))
      .appendNewline()
      .append(Component.text("═════════════════════════════════", NamedTextColor.BLUE))
      .appendNewline()
      .append(Component.text("/guitest simple", NamedTextColor.YELLOW))
      .append(Component.text(" - Basic GUI with items", NamedTextColor.GRAY))
      .appendNewline()
      .append(Component.text("/guitest visualizer", NamedTextColor.YELLOW))
      .append(Component.text(" - GUI with visualizer-ready content", NamedTextColor.GRAY))
      .appendNewline()
      .append(Component.text("/guitest handler", NamedTextColor.YELLOW))
      .append(Component.text(" - GUI with bundle handlers", NamedTextColor.GRAY))
      .appendNewline()
      .append(Component.text("/guitest inventory", NamedTextColor.YELLOW))
      .append(Component.text(" - Inventory browser", NamedTextColor.GRAY))
      .appendNewline()
      .append(Component.text("/guitest paged", NamedTextColor.YELLOW))
      .append(Component.text(" - Paged GUI (50 items)", NamedTextColor.GRAY))
      .appendNewline()
      .append(Component.text("/guitest advanced", NamedTextColor.YELLOW))
      .append(Component.text(" - Advanced features combined", NamedTextColor.GRAY))
      .appendNewline()
      .append(Component.text("═════════════════════════════════", NamedTextColor.BLUE))
    );
  }

  // ==================== TEST IMPLEMENTATIONS ====================

  private static class SimpleMenuGUI implements GuiInterface {
    @Override
    public Component name(@NotNull Player player) {
      return Component.text("Menu");
    }

    @Override
    public Gui guiUpper(@NotNull Player player) {
      return Gui.builder()
        .setStructure("X X X")
        .addIngredient('X', new ItemBuilder(Material.PAPER).toGuiItem())
        .build();
    }
  }

  private static class BundleTestGUI extends AbstractGuiHandler {
    public BundleTestGUI() {
      this.addBundleSelectHandler((player, event) -> {
        player.sendMessage(
          Component.text("Bundle slot ", NamedTextColor.GRAY)
            .append(Component.text(event.bundleSlot, NamedTextColor.GOLD))
            .append(Component.text(" selected at GUI slot ", NamedTextColor.GRAY))
            .append(Component.text(event.guiSlot, NamedTextColor.GOLD))
        );
      });

    }

    @Override
    public Component name(@NotNull Player player) {
      return Component.text("Bundle Handler Test", NamedTextColor.GOLD);
    }

    @Override
    public Gui guiUpper(@NotNull Player player) {
      return Gui.builder()
        .setStructure(
          "X X X",
          "X X X",
          "X X X"
        )
        .addIngredient('X', new ItemBuilder(Material.EMERALD).toGuiItem())
        .build();
    }
  }

  private static class AdvancedTestGUI extends AbstractGuiHandler {
    public AdvancedTestGUI() {
      this.setInventoryVisualizer(
        InventoryVisualizerHelper.emptyGlassPane(Material.GRAY_STAINED_GLASS_PANE)
      );

      this.setCursorVisualizer(
        InventoryVisualizerHelper.glintVisualizer()
      );

      this.addBundleSelectHandler((player, event) -> {
        player.sendMessage(
          Component.text("Advanced: Bundle slot ", NamedTextColor.LIGHT_PURPLE)
            .append(Component.text(event.bundleSlot, NamedTextColor.GOLD))
        );
      });
    }

    @Override
    public Component name(@NotNull Player player) {
      return Component.text("Advanced Features", NamedTextColor.LIGHT_PURPLE);
    }

    @Override
    public Gui guiUpper(@NotNull Player player) {
      var inventory = new VirtualInventory(9);
      inventory.setItem(null, 0, new ItemStack(Material.DIAMOND_BLOCK, 1));
      inventory.setItem(null, 8, new ItemStack(Material.GOLD_BLOCK, 1));

      return Gui.builder()
        .setStructure(
          "X X X",
          "X X X",
          "X X X"
        )
        .addIngredient('X', inventory, 0)
        .build();
    }
  }
}
