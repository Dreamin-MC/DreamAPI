package fr.dreamin.dreamapi.api.lang.service;

import fr.dreamin.dreamapi.api.lang.model.LangFile;
import net.kyori.adventure.translation.Translator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public interface LangService {

  void load();
  void load(final @NotNull File file);

  Optional<Locale> getLocale(final @NotNull UUID uuid);

  Optional<String> getTranslation(final @NotNull Player player, final @NotNull String key);
  Optional<String> getTranslation(final @NotNull Locale locale, final @NotNull String key);
  Optional<String> findTranslation(final @NotNull Locale locale, final @NotNull String key);
  Optional<String> findTranslation(final @NotNull UUID uuid, final @NotNull String key);

  void enableItem(final boolean value);
  boolean isEnableItem();

  void enableGUI(final boolean value);
  boolean isEnableGUI();

  Optional<Translator> getTranslator(final @NotNull String value);
  Translator createTranslator(final @NotNull String label, final @NotNull String namespace, final @NotNull String value);

  Map<String, LangFile> getLangFiles();
  @NotNull Set<String> getLoadedFiles();
  @NotNull Optional<LangFile> getLangFile(@NotNull String file);
  boolean unload(@NotNull String file);
  boolean reload(@NotNull String file);
  boolean add(@NotNull String file);
  void reset();

  boolean isSupportedLangFile(@NotNull File file);
  @NotNull String buildFileKey(@NotNull File langFolder, @NotNull File file);

}
