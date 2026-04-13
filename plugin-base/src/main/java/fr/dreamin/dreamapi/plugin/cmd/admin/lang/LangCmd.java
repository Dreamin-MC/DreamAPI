package fr.dreamin.dreamapi.plugin.cmd.admin.lang;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.lang.service.LangService;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public final class LangCmd {

  private final @NotNull LangService langService = DreamAPI.getAPI().getService(LangService.class);

  // ###############################################################
  // ----------------------- COMMANDS METHODS ----------------------
  // ###############################################################

  @CommandDescription("DreamAPI Lang Management Commands")
  @CommandMethod("lang list")
  @CommandPermission("dreamapi.cmd.lang.list")
  private void onList(CommandSender sender) {

    Component msg = Component.text("-------- Dream - Lang Files --------", NamedTextColor.GOLD);

    for (String file : langService.getLoadedFiles().stream().sorted().toList()) {
      msg = msg.appendNewline()
        .append(Component.text("- ", NamedTextColor.DARK_GRAY))
        .append(Component.text(file, NamedTextColor.WHITE));
    }

    msg = msg.appendNewline()
      .append(Component.text("-----------------------------------", NamedTextColor.GOLD));

    sender.sendMessage(msg);
  }

  @Suggestions("langFiles")
  public List<String> langFiles(CommandContext<CommandSender> sender, String input) {
    return langService.getLoadedFiles().stream()
      .sorted()
      .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT)))
      .toList();
  }

  @Suggestions("langDiskFiles")
  public List<String> langDiskFiles(CommandContext<CommandSender> sender, String input) {
    final File folder = new File(DreamAPI.getAPI().plugin().getDataFolder(), "lang");
    final File[] files = folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".json"));
    if (files == null) {
      return List.of();
    }

    return java.util.Arrays.stream(files)
      .map(File::getName)
      .map(this::getBaseName)
      .sorted(Comparator.naturalOrder())
      .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT)))
      .toList();
  }

  @CommandDescription("Show information about a loaded lang file")
  @CommandMethod("lang info <file>")
  @CommandPermission("dreamapi.cmd.lang.info")
  private void onInfo(
    CommandSender sender,
    @Argument(value = "file", suggestions = "langFiles") String file
  ) {
    final var langFile = langService.getLangFile(file).orElse(null);
    if (langFile == null) {
      sender.sendMessage(Component.text("Lang file " + file + " not found.", NamedTextColor.RED));
      return;
    }

    final int keyCount = langFile.keys == null ? 0 : langFile.keys.size();
    final long translationCount = langFile.keys == null ? 0 : langFile.keys.stream()
      .filter(java.util.Objects::nonNull)
      .mapToLong(k -> k.lang == null ? 0 : k.lang.size())
      .sum();

    Component msg = Component.text("--------------------------------", NamedTextColor.GOLD)
      .appendNewline()
      .append(Component.text("Lang File Info: " + file, NamedTextColor.GOLD))
      .appendNewline()
      .append(Component.text("Namespace: ", NamedTextColor.DARK_GRAY))
      .append(Component.text(langFile.namespace, NamedTextColor.WHITE))
      .appendNewline()
      .append(Component.text("Value: ", NamedTextColor.DARK_GRAY))
      .append(Component.text(langFile.value, NamedTextColor.WHITE))
      .appendNewline()
      .append(Component.text("Default locale: ", NamedTextColor.DARK_GRAY))
      .append(Component.text(String.valueOf(langFile.defaultLocale), NamedTextColor.WHITE))
      .appendNewline()
      .append(Component.text("Keys: ", NamedTextColor.DARK_GRAY))
      .append(Component.text(String.valueOf(keyCount), NamedTextColor.WHITE))
      .appendNewline()
      .append(Component.text("Translations: ", NamedTextColor.DARK_GRAY))
      .append(Component.text(String.valueOf(translationCount), NamedTextColor.WHITE))
      .appendNewline()
      .append(Component.text("--------------------------------", NamedTextColor.GOLD));

    sender.sendMessage(msg);
  }

  @CommandDescription("Reload all lang files")
  @CommandMethod("lang reload")
  @CommandPermission("dreamapi.cmd.lang.reload")
  private void onReloadAll(CommandSender sender) {
    langService.load();
    sender.sendMessage(Component.text("All lang files reloaded.", NamedTextColor.GOLD));
  }

  @CommandDescription("Reload one lang file")
  @CommandMethod("lang reload <file>")
  @CommandPermission("dreamapi.cmd.lang.reload")
  private void onReload(
    CommandSender sender,
    @Argument(value = "file", suggestions = "langDiskFiles") String file
  ) {
    if (!langService.reload(file)) {
      sender.sendMessage(Component.text("Lang file " + file + " not found on disk.", NamedTextColor.RED));
      return;
    }

    sender.sendMessage(Component.text("Lang file " + file + " reloaded.", NamedTextColor.GOLD));
  }

  @CommandDescription("Clear all lang files")
  @CommandMethod("lang clear")
  @CommandPermission("dreamapi.cmd.lang.clear")
  private void onClearAll(CommandSender sender) {
    langService.reset();
    sender.sendMessage(Component.text("All lang files cleared.", NamedTextColor.GOLD));
  }

  @CommandDescription("Clear one lang file")
  @CommandMethod("lang clear <file>")
  @CommandPermission("dreamapi.cmd.lang.clear")
  private void onClear(
    CommandSender sender,
    @Argument(value = "file", suggestions = "langFiles") String file
  ) {
    if (!langService.unload(file)) {
      sender.sendMessage(Component.text("Lang file " + file + " not loaded.", NamedTextColor.RED));
      return;
    }

    sender.sendMessage(Component.text("Lang file " + file + " cleared.", NamedTextColor.GOLD));
  }

  @CommandDescription("Add/load one lang file from disk")
  @CommandMethod("lang add <file>")
  @CommandPermission("dreamapi.cmd.lang.add")
  private void onAdd(
    CommandSender sender,
    @Argument(value = "file", suggestions = "langDiskFiles") String file
  ) {
    if (!langService.add(file)) {
      sender.sendMessage(Component.text("Lang file " + file + " not found on disk.", NamedTextColor.RED));
      return;
    }

    sender.sendMessage(Component.text("Lang file " + file + " loaded.", NamedTextColor.GOLD));
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private @NotNull String getBaseName(@NotNull String fileName) {
    final int index = fileName.lastIndexOf('.');
    return index == -1 ? fileName : fileName.substring(0, index);
  }

}
