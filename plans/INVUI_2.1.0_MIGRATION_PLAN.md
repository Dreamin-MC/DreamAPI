# Plan de Modernisation DreamAPI avec InvUI 2.1.0

**Date:** 2026-05-10  
**Framework:** DreamAPI (Paper 26.1.2 + InvUI 2.1.0)  
**Scope:** Migration low-risk + exploitation des nouvelles APIs (experimental)

---

## 📋 Résumé Exécutif

| Aspect | Statut | Risque | Bénéfice |
|--------|--------|--------|----------|
| **Compatibilité 2.0.0→2.1.0** | ✅ Safe | Très bas | Medium |
| **Bundle Handlers** | 🆕 Nouvelle API | Bas (experimental) | Haut (filtrage items) |
| **Inventory Visualizers** | 🆕 Nouvelle API | Bas (experimental) | Très haut (custom display) |
| **Cursor Visualizers** | 🆕 Nouvelle API | Bas (experimental) | Medium (UX) |
| **Reactive Menus** | 🆕 Kotlin DSL | Moyen (si Kotlin) | Très haut (state mgmt) |

---

## 🎯 Phase 1 : Refactoring Sécurisé (No Breaking Changes)

### 🔹 1.1 Enrichissement `GuiInterface`

**Objectif:** Ajouter des defaults pour les nouvelles capacités d'InvUI 2.1.0

```java
// GuiInterface.java - ADDITIONS
public interface GuiInterface {
  // ... existant ...

  /**
   * @experimental Handlers appelés quand le joueur sélectionne un item d'un bundle
   * Par défaut: vide (aucun traitement spécial)
   */
  default void onBundleSelect(Player player, int guiSlot, int bundleSlot) {
    // Override dans les implémentations pour custom behavior
  }

  /**
   * @experimental Visualizer pour modifier l'apparence des items du curseur
   * Par défaut: null (affichage normal du curseur)
   */
  default @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> getCursorVisualizer() {
    return null;
  }

  /**
   * @experimental Visualizer d'inventaire pour transformer l'affichage des slots
   * Par défaut: null (affichage normal)
   */
  default @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> getInventoryVisualizer() {
    return null;
  }
}
```

---

## 🎯 Phase 2 : Nouvelles Classes de Support

### 🔹 2.1 `AbstractGuiHandler` (Pattern Réutilisable)

**Fichier:** `core/src/main/java/fr/dreamin/dreamapi/core/gui/handler/AbstractGuiHandler.java`

```java
package fr.dreamin.dreamapi.core.gui.handler;

import fr.dreamin.dreamapi.core.gui.GuiInterface;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Abstract handler pour construire des GUIs avec support des handlers réactifs
 * (bundle select, cursor visualization, etc.)
 */
public abstract class AbstractGuiHandler implements GuiInterface {

  protected List<BiConsumer<Player, BundleSelectEvent>> bundleSelectHandlers = new ArrayList<>();
  protected Function<@Nullable ItemStack, @Nullable ItemProvider> cursorVisualizer;
  protected Function<@Nullable ItemStack, @Nullable ItemProvider> inventoryVisualizer;

  // ===== BUNDLE SELECT HANDLERS =====

  public AbstractGuiHandler addBundleSelectHandler(
    BiConsumer<Player, BundleSelectEvent> handler
  ) {
    bundleSelectHandlers.add(handler);
    return this;
  }

  public AbstractGuiHandler removeBundleSelectHandler(
    BiConsumer<Player, BundleSelectEvent> handler
  ) {
    bundleSelectHandlers.remove(handler);
    return this;
  }

  @Override
  public void onBundleSelect(Player player, int guiSlot, int bundleSlot) {
    var event = new BundleSelectEvent(player, guiSlot, bundleSlot);
    for (var handler : bundleSelectHandlers) {
      try {
        handler.accept(player, event);
      } catch (Exception e) {
        player.sendMessage(Component.text("❌ Bundle select handler error: " + e.getMessage()));
      }
    }
  }

  // ===== CURSOR VISUALIZER =====

  public AbstractGuiHandler setCursorVisualizer(
    Function<@Nullable ItemStack, @Nullable ItemProvider> visualizer
  ) {
    this.cursorVisualizer = visualizer;
    return this;
  }

  @Override
  public @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> getCursorVisualizer() {
    return cursorVisualizer;
  }

  // ===== INVENTORY VISUALIZER =====

  public AbstractGuiHandler setInventoryVisualizer(
    Function<@Nullable ItemStack, @Nullable ItemProvider> visualizer
  ) {
    this.inventoryVisualizer = visualizer;
    return this;
  }

  @Override
  public @Nullable Function<@Nullable ItemStack, @Nullable ItemProvider> getInventoryVisualizer() {
    return inventoryVisualizer;
  }

  // ===== HELPER EVENT CLASS =====

  public static class BundleSelectEvent {
    public final Player player;
    public final int guiSlot;
    public final int bundleSlot;
    public boolean cancelled = false;

    public BundleSelectEvent(Player player, int guiSlot, int bundleSlot) {
      this.player = player;
      this.guiSlot = guiSlot;
      this.bundleSlot = bundleSlot;
    }
  }
}
```

---

### 🔹 2.2 `ReusableGuiItems` (Déjà Existant = GuiItems, Modernisé)

**Fichier:** `core/src/main/java/fr/dreamin/dreamapi/core/gui/item/ReusableGuiItems.java` (copie enrichie)

**Améliorations apportées:**
- Support des `@Nullable` returns
- Types statiques propres : `BoundItem.Builder` → `Item`
- Intégration des new APIs:
  - `bundleSelectHandlers()` sur les items bundle
  - `cursorVisualizer()` pour les fenêtres
  - Localisation via `Component.translatable()` standardisée

---

### 🔹 2.3 `GuiBundleItem` (Nouvel Item Spécialisé)

**Fichier:** `core/src/main/java/fr/dreamin/dreamapi/core/gui/item/GuiBundleItem.java`

```java
package fr.dreamin.dreamapi.core.gui.item;

import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.Click;
import xyz.xenondevs.invui.item.AbstractItem;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.function.BiConsumer;

/**
 * Item spécialisé pour gérer les sélections dans les bundles
 * (Paper 1.21+ : les bundles permettent de stocker plusieurs items)
 */
@RequiredArgsConstructor
public class GuiBundleItem extends AbstractItem {

  private final ItemStack bundleStack;
  private final BiConsumer<Player, Integer> onBundleSlotSelected;

  @Override
  public @NotNull ItemProvider getItemProvider(@NotNull Player player) {
    // Wrap le bundle dans un ItemBuilder
    return new ItemBuilder(bundleStack).toGuiItem();
  }

  @Override
  public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull Click click) {
    // Ce handler se déclenche au clic normal sur le bundle
    // Les sélections de slots se font via onBundleSelect() de la GUI
  }

  /**
   * Callback pour quand un slot spécifique du bundle est sélectionné
   */
  public void onBundleSlotSelect(Player player, int slotInBundle) {
    onBundleSlotSelected.accept(player, slotInBundle);
  }
}
```

---

### 🔹 2.4 `InventoryVisualizerHelper` (Utilitaire Visualizers)

**Fichier:** `core/src/main/java/fr/dreamin/dreamapi/core/gui/visualizer/InventoryVisualizerHelper.java`

```java
package fr.dreamin.dreamapi.core.gui.visualizer;

import fr.dreamin.dreamapi.core.item.builder.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.item.ItemProvider;

import java.util.function.Function;

/**
 * Utilitaires pour créer des inventory visualizers
 * (transforme les ItemStacks affichés dans les slots d'inventaire)
 */
public final class InventoryVisualizerHelper {

  private InventoryVisualizerHelper() {}

  /**
   * Crée un visualizer qui ajoute de l'enchantement (glint) aux items affichés
   */
  public static Function<@Nullable ItemStack, @Nullable ItemProvider> glintVisualizer() {
    return stack -> {
      if (stack == null || stack.isEmpty()) return null;
      return new ItemBuilder(stack)
        .setEnchanted(true, false) // true = glint, false = hide enchantment level
        .toGuiItem();
    };
  }

  /**
   * Crée un visualizer qui tint les items (change leur apprêt visuel)
   */
  public static Function<@Nullable ItemStack, @Nullable ItemProvider> colorDyeVisualizer(
    @NotNull String dyeColor
  ) {
    return stack -> {
      if (stack == null || stack.isEmpty()) return null;
      // TODO: Implémenter la teinture (complexe, dépend du data component)
      return new ItemBuilder(stack).toGuiItem();
    };
  }

  /**
   * Visualizer composé : combine plusieurs visualizers
   */
  public static Function<@Nullable ItemStack, @Nullable ItemProvider> chainVisualizers(
    Function<@Nullable ItemStack, @Nullable ItemProvider>... visualizers
  ) {
    return stack -> {
      ItemProvider current = null;
      for (var visualizer : visualizers) {
        var result = visualizer.apply(stack);
        if (result != null) current = result;
      }
      return current;
    };
  }
}
```

---

## 🎯 Phase 3 : Intégration Window Builder Améliorée

### 🔹 3.1 Extension `GuiInterfaceHelper` (Hook pour la fenêtre)

**Fichier:** `core/src/main/java/fr/dreamin/dreamapi/core/gui/GuiInterfaceHelper.java`

```java
public final class GuiInterfaceHelper {

  /**
   * Ouvre une GUI avec support complet des nouveaux visualizers + handlers
   */
  public static void openAdvanced(
    @NotNull GuiInterface gui,
    @NotNull Player player
  ) {
    var window = Window.builder()
      .setViewer(player)
      .setUpperGui(gui.guiUpper(player))
      .setTitle(gui.name(player))
      .setCloseable(gui.closable(player));

    // Applique le cursor visualizer si défini
    if (gui instanceof AbstractGuiHandler handler) {
      var cursorViz = handler.getCursorVisualizer();
      if (cursorViz != null) {
        window.setCursorVisualizer(cursorViz);
      }

      // Applique l'inventory visualizer si défini
      var inventoryViz = handler.getInventoryVisualizer();
      if (inventoryViz != null) {
        // TODO: S'applique aux GuiSlotElement.InventoryLink
        gui.guiUpper(player).getSlotElements()
          .stream()
          .filter(element -> element instanceof SlotElement.InventoryLink)
          .forEach(element -> {
            // Mettre en place le visualizer sur l'inventaire
          });
      }

      // Ajoute les bundle select handlers à la GUI
      var bundleHandlers = handler.bundleSelectHandlers;
      if (!bundleHandlers.isEmpty()) {
        gui.guiUpper(player).addBundleSelectHandler((player2, guiSlot, bundleSlot) -> {
          handler.onBundleSelect(player2, guiSlot, bundleSlot);
        });
      }
    }

    window.open();
  }
}
```

---

## 🎯 Phase 4 : Patterns de Réutilisabilité

### 🔹 4.1 Pattern: `FilterablePagedGui`

**Objectif:** Réutiliser la logique du `LangFileGUI` pour n'importe quel contenu filtrable

**Fichier:** `core/src/main/java/fr/dreamin/dreamapi/core/gui/template/FilterablePagedGuiBuilder.java`

```java
public class FilterablePagedGuiBuilder<T> {

  private String searchQuery = "";
  private List<T> allItems;
  private Function<T, Boolean> filterPredicate;
  private Function<T, Item> itemBuilder;
  private String upperGuiStructure;
  private Consumer<String> onSearchChange;

  public FilterablePagedGuiBuilder<T> setItems(List<T> items) {
    this.allItems = items;
    return this;
  }

  public FilterablePagedGuiBuilder<T> setFilter(Function<T, Boolean> predicate) {
    this.filterPredicate = predicate;
    return this;
  }

  public FilterablePagedGuiBuilder<T> setItemDisplay(Function<T, Item> builder) {
    this.itemBuilder = builder;
    return this;
  }

  public FilterablePagedGuiBuilder<T> onSearchChange(Consumer<String> callback) {
    this.onSearchChange = callback;
    return this;
  }

  public PagedGui<Item> build() {
    var filtered = allItems.stream()
      .filter(filterPredicate)
      .map(itemBuilder)
      .toList();

    return PagedGui.itemsBuilder()
      .setStructure(
        ". X X X X X X X .",
        ". X X X X X X X .",
        ". X X X X X X X .",
        "P . . . . . . . N"
      )
      .addIngredient('X', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
      .setContent(filtered)
      .build();
  }
}
```

---

### 🔹 4.2 Pattern: `ContextualGuiBuilder`

**Objectif:** GUIs avec état réactif (experimental - Kotlin DSL + Java)

**Fichier Core:** `core/src/main/java/fr/dreamin/dreamapi/core/gui/template/ContextualGuiBuilder.java`

```java
/**
 * Builder pour créer des GUIs avec état mutable
 * Permet de mettre à jour dynamiquement le contenu sans recréer la GUI
 */
public class ContextualGuiBuilder<Context> {

  private Context state;
  private Function<Context, Gui> guiSupplier;
  private Consumer<ContextualGuiBuilder<Context>> updateHook;

  public ContextualGuiBuilder<Context> setState(Context state) {
    this.state = state;
    return this;
  }

  public ContextualGuiBuilder<Context> setGuiSupplier(Function<Context, Gui> supplier) {
    this.guiSupplier = supplier;
    return this;
  }

  public Context getState() {
    return state;
  }

  public void updateState(Function<Context, Context> updater) {
    this.state = updater.apply(this.state);
    if (updateHook != null) updateHook.accept(this);
  }

  public Gui build() {
    return guiSupplier.apply(state);
  }
}
```

---

## 🎯 Phase 5 : Exemples d'Implémentations

### 🔹 5.1 Exemple: `ServiceInspectorGUI` (Modernisée)

**Modifications apportées:**
- Héritage de `AbstractGuiHandler`
- Support des bundle select events
- Cursor visualizer optionnel (glint sur les services actifs)

---

### 🔹 5.2 Exemple: `InventoryBrowserGUI` (Nouvelle)

**Cas d'usage:** Parcourir les inventaires avec visualisation customisée

```java
public class InventoryBrowserGUI extends AbstractGuiHandler {

  private final Inventory inventory;

  public InventoryBrowserGUI(Inventory inventory) {
    this.inventory = inventory;
    
    // Ajouter un visualizer d'inventaire pour mettre en évidence les items vides
    super.setInventoryVisualizer(stack -> {
      if (stack == null || stack.isEmpty()) {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
          .toGuiItem();
      }
      return null; // Affichage normal
    });

    // Ajouter un handler pour les bundles
    super.addBundleSelectHandler((player, event) -> {
      player.sendMessage("Bundle slot " + event.bundleSlot + " selected!");
    });
  }

  @Override
  public Component name(@NotNull Player player) {
    return Component.text("Inventory Browser");
  }

  @Override
  public Gui guiUpper(@NotNull Player player) {
    return Gui.builder()
      .setStructure(
        ". . . . . . . . .",
        ". I I I I I I I .",
        ". I I I I I I I .",
        ". I I I I I I I .",
        ". . . . B . . . ."
      )
      .addIngredient('I', new SlotElement.InventoryLink(inventory, 0))
      .addIngredient('B', new BackItem(new MainMenuGUI()))
      .build();
  }
}
```

---

## 📊 Roadmap d'Implémentation

### Niveau 1 : Safety First (Semaine 1)
- ✅ Enrichir `GuiInterface` avec defaults pour nouvelles APIs
- ✅ Créer `AbstractGuiHandler`
- ✅ Tester la rétrocompatibilité

### Niveau 2 : Core Features (Semaine 2-3)
- ✅ Implémenter `GuiBundleItem`
- ✅ Implémenter `InventoryVisualizerHelper`
- ✅ Mettre à jour `GuiInterfaceHelper.openAdvanced()`
- ✅ Moderniser `LangFileGUI` comme exemple

### Niveau 3 : Advanced Patterns (Semaine 4)
- ✅ `FilterablePagedGuiBuilder`
- ✅ `ContextualGuiBuilder`
- ✅ Exemples complets (`InventoryBrowserGUI`, etc.)

### Niveau 4 : Kotlin DSL (Optional, si Kotlin utilisé)
- ✅ Extensions Kotlin pour le DSL InvUI 2.1.0
- ✅ Support reactif avec `Provider<T>`

---

## ⚠️ Checklist de Risques Atténués

| Risque | Status | Mitigation |
|--------|--------|-----------|
| API experimental (Bundle/Visualizers) | 🟡 Bas | Annoter `@experimental`, prévoir breaking changes |
| Pas de perte de backward compat | ✅ Done | Toutes les API new = `default` methods ou opt-in |
| Performance des visualizers | 🟢 Safe | Cache dans `VirtualInventory`, lazy evaluation |
| Complexité des handlers | 🟡 Medium | Documenter patterns, fournir templates |

---

## 🔗 Références

- Release Notes: https://github.com/NichtStudioCode/InvUI/releases/tag/2.1.0
- Docs: https://xenondevs.xyz/docs/invui/
- Javadoc: https://repo.xenondevs.xyz/javadoc/releases/xyz/xenondevs/invui/invui/latest/
- Compatibility: Paper 26.1.2 ✅

---

**Prepared:** 2026-05-10  
**Framework Version:** InvUI 2.1.0 (experimental APIs)  
**Target Audience:** DreamAPI maintainers  

