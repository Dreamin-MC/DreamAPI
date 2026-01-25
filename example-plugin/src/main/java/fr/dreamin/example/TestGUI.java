package fr.dreamin.example;

import fr.dreamin.dreamapi.api.gui.PictureGui;
import fr.dreamin.dreamapi.core.gui.GuiInterface;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;

public class TestGUI implements GuiInterface {

  @Override
  public Component name(@NotNull Player player) {
    return PictureGui.GENERIC_9.getLabel();
  }

  @Override
  public Gui guiUpper(@NotNull Player player) {
    return Gui.builder()
      .setStructure(". . . . . . . . .")
      .build();
  }
}
