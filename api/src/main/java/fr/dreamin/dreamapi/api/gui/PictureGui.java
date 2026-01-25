package fr.dreamin.dreamapi.api.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Dreamin
 * @since 0.0.1
 */
@Getter
@RequiredArgsConstructor
public enum PictureGui {

  DEFAULT(Component.text("七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七").color(NamedTextColor.WHITE).font(Key.key("dreamin", "gui"))),
  GENERIC_9(Component.text("七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七0").color(NamedTextColor.WHITE).font(Key.key("dreamin", "gui"))),
  GENERIC_18(Component.text("七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七1").color(NamedTextColor.WHITE).font(Key.key("dreamin", "gui"))),
  GENERIC_27(Component.text("七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七2").color(NamedTextColor.WHITE).font(Key.key("dreamin", "gui"))),
  GENERIC_36(Component.text("七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七3").color(NamedTextColor.WHITE).font(Key.key("dreamin", "gui"))),
  GENERIC_45(Component.text("七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七4").color(NamedTextColor.WHITE).font(Key.key("dreamin", "gui"))),
  GENERIC_54(Component.text("七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七5").color(NamedTextColor.WHITE).font(Key.key("dreamin", "gui"))),
  MENU(Component.text("七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七七6").color(NamedTextColor.WHITE).font(Key.key("dreamin", "gui")));

  private final Component label;
}