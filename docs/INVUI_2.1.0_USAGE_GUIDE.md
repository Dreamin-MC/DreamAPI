# Guide d'Utilisation: InvUI 2.1.0 pour DreamAPI

**Date:** 2026-05-10  
**Version:** 1.0  
**Pour:** Développeurs DreamAPI

---

## Table des Matières

1. [Quick Start](#quick-start)
2. [Concepts Clés](#concepts-clés)
3. [Classes Principales](#classes-principales)
4. [Patterns Courants](#patterns-courants)
5. [Exemples Complets](#exemples-complets)
6. [FAQ / Troubleshooting](#faq)

---

## Quick Start

### Installation

Les classes sont déjà distribuées:
- `AbstractGuiHandler` → héritable pour vos GUIs
- `InventoryVisualizerHelper` → utilitaires pour transformer l'affichage
- `GuiBundleItem` → items spécialisés pour les bundles
- `GuiInterfaceHelper` → helpers pour ouvrir les GUIs

### Cas d'Usage Simple

```java
// Avant (InvUI 2.0.0)
public class MyGUI implements GuiInterface {
  @Override
  public Gui guiUpper(Player player) {
    return Gui.builder()
      .setStructure("X X X")
      .build();
  }
}

// Après (InvUI 2.1.0)
public class MyGUI extends AbstractGuiHandler {
  
  public MyGUI() {
    // Activer le visualizer pour les slots vides
    this.setInventoryVisualizer(InventoryVisualizerHelper.emptyGlassPane(Material.LIGHT_GRAY_STAINED_GLASS_PANE));
    
    // Ajouter un handler pour les bundles
    this.addBundleSelectHandler((player, event) -> {
      player.sendMessage("Bundle slot " + event.bundleSlot + " sélectionné!");
    });
  }
  
  @Override
  public Gui guiUpper(Player player) {
    return Gui.builder()
      .setStructure("X X X")
      .build();
  }
}
```

---

## Concepts Clés

### 1️⃣ Inventory Visualizers

**Qu'est-ce que c'est?**  
Les visualizers transforment l'apparence des ItemStacks affichés dans les slots, **sans modifier les items réels** stockés.

**Cas d'usage:**
- ✅ Afficher des placeholders pour les slots vides
- ✅ Ajouter un glint/enchantement visuel aux items spéciaux
- ✅ Changer la couleur/apparence selon le contexte

**Exemple:**

```java
// Montrer du verre gris pour les slots vides
var visualizer = InventoryVisualizerHelper.emptyGlassPane(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
gui.setInventoryVisualizer(visualizer);
```

### 2️⃣ Cursor Visualizers

**Qu'est-ce que c'est?**  
Transforme l'apparence de l'ItemStack sur le curseur du joueur.

**Cas d'usage:**
- ✅ Ajouter un glint spécial aux items du curseur
- ✅ Bloquer visuellement certains items
- ✅ Feedback visuel durant des interactions

**Exemple:**

```java
// Ajouter un glint au curseur si une condition est remplie
var cursorVisualizer = (ItemStack stack) -> {
  if (stack != null && isSpecialItem(stack)) {
    return new ItemBuilder(stack).setEnchanted(true, false).toGuiItem();
  }
  return null; // Affichage normal
};
gui.setCursorVisualizer(cursorVisualizer);
```

### 3️⃣ Bundle Select Handlers

**Qu'est-ce que c'est?**  
Callbacks appelés quand un joueur sélectionne un item d'un bundle via le menu contextuel.

**Cas d'usage:**
- ✅ Crafting: sélectionner un ingrédient spécifique dans un bundle
- ✅ Inventaires: accéder rapidement aux stacks dans un bundle
- ✅ Logique métier: réagir à la sélection d'un slot

**Exemple:**

```java
gui.addBundleSelectHandler((player, event) -> {
  int guiSlot = event.guiSlot;           // Slot GUI du bundle
  int bundleSlot = event.bundleSlot;     // Index dans le bundle
  
  player.sendMessage("Vous avez sélectionné l'item " + bundleSlot + " du bundle!");
});
```

---

## Classes Principales

### `AbstractGuiHandler` 

**Héritage recommandé pour vos GUIs.**

```java
public abstract class AbstractGuiHandler implements GuiInterface {
  
  // Bundle select handlers
  public AbstractGuiHandler addBundleSelectHandler(BiConsumer<...> handler);
  public AbstractGuiHandler removeBundleSelectHandler(BiConsumer<...> handler);
  public void onBundleSelect(Player player, int guiSlot, int bundleSlot);
  
  // Visualizers
  public AbstractGuiHandler setCursorVisualizer(Function<ItemStack, ItemProvider> visualizer);
  public AbstractGuiHandler setInventoryVisualizer(Function<ItemStack, ItemProvider> visualizer);
  
  // Utilities
  public List<BiConsumer<...>> getBundleSelectHandlers();
}
```

**Quand l'utiliser?**
- ✅ Si votre GUI a besoin de handlers spécialisés
- ✅ Si vous voulez supporter les bundles
- ✅ Si vous voulez du support pour les visualizers

---

### `InventoryVisualizerHelper`

**Factory pour créer des visualizers courants.**

```java
// Ajouter un glint aux items affichés
var glint = InventoryVisualizerHelper.glintVisualizer();

// Afficher un placeholder pour les slots vides
var empty = InventoryVisualizerHelper.emptyGlassPane(Material.LIGHT_GRAY_STAINED_GLASS_PANE);

// Visualizer conditionnel
var conditional = InventoryVisualizerHelper.conditionalVisualizer(
  stack -> stack != null && isSpecial(stack),
  stack -> new ItemBuilder(stack).setEnchanted(true, false).build()
);

// Composer plusieurs visualizers
var chain = InventoryVisualizerHelper.chainVisualizers(glint, empty, conditional);
```

---

### `GuiBundleItem`

**Item spécialisé pour les bundles.**

```java
var bundle = new ItemStack(Material.BUNDLE);
// ... ajouter des items au bundle ...

var item = GuiBundleItem.withCallback(bundle, (player, slotInBundle) -> {
  player.sendMessage("Vous avez sélectionné l'item " + slotInBundle);
});
```

---

### `GuiInterfaceHelper`

**Utilitaires pour ouvrir les GUIs.**

```java
// Ouvrir une GUI simple
GuiInterfaceHelper.openSimple(gui, player);

// Ouvrir avec support complet des APIs 2.1.0
GuiInterfaceHelper.openAdvanced(gui, player);

// Builder fluent
GuiInterfaceHelper.fluentBuilder(gui, player)
  .withTitle(Component.text("Mon Titre"))
  .closeable(true)
  .withFallback(backGUI)
  .open();
```

---

## Patterns Courants

### Pattern 1: GUI Filtrable et Paginée

**Utiliser:** `FilterablePagedGuiBuilder<T>`

```java
var builder = new FilterablePagedGuiBuilder<String>()
  .setItems(myItems)
  .withStringSearch(String::valueOf)  // Filtre par texte
  .setItemDisplay(item -> new ItemBuilder(Material.PAPER)
    .setName(Component.text(item))
    .toGuiItem() as Item
  )
  .showPagination(true);

var gui = builder.build();
```

### Pattern 2: GUI avec État Réactif

**Cas:** GUI qui change selon l'inventaire, le joueur, etc.

```java
public class ReactiveLangGUI extends AbstractGuiHandler {
  
  private String searchQuery = "";
  
  public ReactiveLangGUI() {
    super.addBundleSelectHandler((player, event) -> {
      updateContent();
    });
  }
  
  private void updateContent() {
    // Mettre à jour la PagedGui
  }
}
```

### Pattern 3: GUI avec Visualizers Composés

```java
public class AdvancedInventoryGUI extends AbstractGuiHandler {
  
  public AdvancedInventoryGUI(Inventory inventory) {
    // Ajouter plusieurs visualizers en chaîne
    this.setInventoryVisualizer(InventoryVisualizerHelper.chainVisualizers(
      // 1. Glint sur items boosted
      InventoryVisualizerHelper.conditionalGlint(this::isBoosted),
      // 2. Placeholder pour slots vides
      InventoryVisualizerHelper.emptyGlassPane(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
    ));
  }
  
  private boolean isBoosted(ItemStack stack) {
    // Logique custom
    return false;
  }
}
```

---

## Exemples Complets

### Exemple 1: InventoryBrowserGUI

Voir: `fr.dreamin.dreamapi.core.gui.example.InventoryBrowserGUI`

```java
var browser = InventoryBrowserGUI.builder()
  .inventory(myInventory)
  .backButton(mainGUI)
  .emptyPlaceholders(true)
  .placeholderMaterial(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
  .build();

browser.open(player);
```

### Exemple 2: Service Inspector (Modernisé)

**Avant:**
```java
class ServiceDetailGUI implements GuiInterface {
  // ... implémentation manuelle ...
}
```

**Après:**
```java
class ServiceDetailGUI extends AbstractGuiHandler {
  
  public ServiceDetailGUI(Class<?> serviceClass, DreamService service) {
    // Support des bundles + visualizers automatiques
    this.addBundleSelectHandler((player, event) -> {
      // Nouvelle logique pour les bundles
    });
    
    this.setInventoryVisualizer(InventoryVisualizerHelper.emptyGlassPane(
      Material.LIGHT_GRAY_STAINED_GLASS_PANE
    ));
  }
  
  @Override
  public Gui guiUpper(Player player) {
    // ... implémentation inchangée ...
  }
}
```

---

## FAQ

### Q1: Comment migrer une GUI existante?

**Réponse:**

1. Changer `implements GuiInterface` → `extends AbstractGuiHandler`
2. Appeler `super.setInventoryVisualizer(...)` dans le constructeur si nécessaire
3. Appeler `super.addBundleSelectHandler(...)` pour ajouter des handlers
4. Utiliser `GuiInterfaceHelper.openAdvanced()` pour ouvrir avec support complet

**Risque:** Très bas - toutes les APIs sont additive et backward-compatible.

---

### Q2: Visualizers = performance issue?

**Réponse:**

Non, c'est fast:
- `VirtualInventory` cache les visualizations par slot
- Les visualizers ne s'exécutent que quand nécessaire
- Cache invalidé uniquement sur update

Performance: **✅ Safe**

---

### Q3: Peut-on mixer les API 2.0.0 et 2.1.0?

**Réponse:**

Oui! Toutes les APIs 2.1.0 sont `default` methods ou opt-in:
- Les anciennes implémentations de `GuiInterface` continuent de fonctionner
- Les nouvelles classes `AbstractGuiHandler` sont optionnelles
- Zero breaking changes

---

### Q4: Comment tester les bundles?

**Réponse:**

Les bundles (Paper 1.21+) offrent un menu contextuel quand cliqué avec Shift+Right-Click.
```java
// Créer un bundle
var bundle = new ItemStack(Material.BUNDLE);
var inventory = Bukkit.createInventory(null, 1);
inventory.setItem(0, bundle);
// Le reste est automatique
```

---

### Q5: Cas avancé: Visualizer réactif / provenant de Provider?

**En cours de discussion** pour la prochaine release (2.2.0).  
Pour maintenant: utiliser `ContextualGuiBuilder` (non encore implémenté, voir le plan).

---

## Ressources

- 📖 [Plan complet](./INVUI_2.1.0_MIGRATION_PLAN.md)
- 🔗 [Documentation InvUI](https://xenondevs.xyz/docs/invui/)
- 🔗 [Javadoc](https://repo.xenondevs.xyz/javadoc/releases/xyz/xenondevs/invui/invui/latest/)
- 🐛 [Issues/Discussions](https://github.com/NichtStudioCode/InvUI/discussions)

---

**Prepared by:** GitHub Copilot  
**For:** DreamAPI Framework  
**Last Updated:** 2026-05-10  

