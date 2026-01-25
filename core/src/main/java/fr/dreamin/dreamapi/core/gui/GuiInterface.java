package fr.dreamin.dreamapi.core.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.window.Window;

public interface GuiInterface {

  Component name(final @NotNull Player player);// Retourne le nom/titre de la GUI

  default boolean closable(final @NotNull Player player) {
    return true;
  };

  Gui guiUpper(final @NotNull Player player); // Retourne l'instance de GUI pour le joueur

  default Gui pagedGui(final @NotNull Player player) {
    return Gui.builder()
      .setStructure(
        ". . . . . . . . . ",
        ". . . . . . . . . ",
        ". . . . . . . . . ",
        ". . . . . . . . . "
      )
      .build();
  }// Retourne

  // Ouvre la GUI pour le joueur, méthode par défaut pour les GUIs en mode "single"
  default void open(final @NotNull Player player) {
    Window.builder()
      .setViewer(player)
      .setUpperGui(guiUpper(player))
      .setTitle(name(player))
      .setCloseable(closable(player))
      .open(player);
  }
}