package fr.dreamin.dreamapi.core.lang.service;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.api.lang.model.LangFile;
import fr.dreamin.dreamapi.api.lang.service.LangService;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@DreamAutoService(LangService.class)
public final class LangServiceImpl implements LangService, DreamService {

  private final @NotNull Map<String, MiniMessageTranslationStore> translationStore = new HashMap<>();
  private final Map<String, LangFile> langFiles = new HashMap<>();

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onLoad(@NotNull Plugin plugin) {
    load();
  }

  @Override
  public void onClose() {
    reset();
  }

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  @Override
  public void load() {
    if (!this.translationStore.isEmpty())
      reset();

    final var folder = new File(DreamAPI.getAPI().plugin().getDataFolder(), "lang");
    if (!folder.exists() || !folder.isDirectory()) return;


    try(final var paths = Files.walk(folder.toPath())) {
      paths.filter(Files::isRegularFile)
        .map(Path::toFile)
        .filter(this::isSupportedLangFile)
        .forEach(this::load);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load lang files in " + folder.getAbsolutePath(), e);
    }

  }

  @Override
  public void load(@NotNull File file) {
    final LangFile langFile;

    try {
      langFile = Configurations.MAPPER.readValue(file, LangFile.class);
    } catch (IOException exception) {
      throw new RuntimeException("Failed to read lang file " + file.getName(), exception);
    }

    if (langFile.namespace == null || langFile.namespace.isBlank())
      throw new IllegalArgumentException("Missing namespace for lang file " + file.getName());

    if (langFile.value == null || langFile.value.isBlank())
      throw new IllegalArgumentException("Missing value for lang file " + file.getName());

    final var fileKey = getBaseName(file.getName());
    final var store = createTranslator(fileKey, langFile.namespace, langFile.value);

    if (langFile.defaultLocale != null && !langFile.defaultLocale.isBlank())
      store.defaultLocale(parseLocale(langFile.defaultLocale));

    if (langFile.keys != null) {
      for (final var entry : langFile.keys) {
        if (entry == null || entry.key == null || entry.key.isBlank() || entry.lang == null)
          continue;

        for (final var value : entry.lang) {
          if (value == null || value.locale == null || value.locale.isBlank() || value.value == null)
            continue;

          store.register(entry.key, parseLocale(value.locale), value.value);
        }
      }
    }

    this.langFiles.put(fileKey, langFile);

  }

  @Override
  public Optional<Translator> getTranslator(@NotNull String value) {
    return Optional.ofNullable(this.translationStore.get(value));
  }

  @Override
  public MiniMessageTranslationStore createTranslator(@NotNull String label, @NotNull String namespace, @NotNull String value) {
    return this.translationStore.computeIfAbsent(label, v -> {
      final MiniMessageTranslationStore store = MiniMessageTranslationStore.create(Key.key(namespace, value));
      GlobalTranslator.translator().addSource(store);
      return store;
    });
  }

  @Override
  public @NotNull Set<String> getLoadedFiles() {
    return Collections.unmodifiableSet(this.translationStore.keySet());
  }

  @Override
  public @NotNull Optional<LangFile> getLangFile(@NotNull String file) {
    return Optional.ofNullable(this.langFiles.get(file));
  }

  @Override
  public boolean unload(@NotNull String file) {
    final MiniMessageTranslationStore store = this.translationStore.remove(file);
    this.langFiles.remove(file);

    if (store == null) {
      return false;
    }

    GlobalTranslator.translator().removeSource(store);
    return true;
  }

  @Override
  public boolean reload(@NotNull String file) {
    final File langFile = new File(new File(DreamAPI.getAPI().plugin().getDataFolder(), "lang"), file + ".json");
    if (!langFile.exists() || !langFile.isFile()) {
      return false;
    }

    this.unload(file);
    this.load(langFile);
    return true;
  }

  @Override
  public boolean add(@NotNull String file) {
    final File langFile = new File(new File(DreamAPI.getAPI().plugin().getDataFolder(), "lang"), file + ".json");
    if (!langFile.exists() || !langFile.isFile()) {
      return false;
    }

    this.load(langFile);
    return true;
  }

  @Override
  public void reset() {
    this.translationStore.values()
      .forEach(m -> GlobalTranslator.translator().removeSource(m));
    this.translationStore.clear();
    this.langFiles.clear();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private boolean isSupportedLangFile(@NotNull File file) {
    return file.getName().toLowerCase(Locale.ROOT).endsWith(".json");
  }

  private @NotNull String getBaseName(@NotNull String fileName) {
    final int index = fileName.lastIndexOf('.');
    return index == -1 ? fileName : fileName.substring(0, index);
  }

  private @NotNull Locale parseLocale(@NotNull String value) {
    return switch (value.toUpperCase(Locale.ROOT)) {
      case "US", "ENGLISH", "EN_US" -> Locale.US;
      case "UK", "EN_GB" -> Locale.UK;
      case "FRENCH", "FR", "FR_FR" -> Locale.FRENCH;
      case "GERMAN", "DE", "DE_DE" -> Locale.GERMAN;
      case "ITALIAN", "IT", "IT_IT" -> Locale.ITALIAN;
      case "JAPANESE", "JA", "JA_JP" -> Locale.JAPANESE;
      case "KOREAN", "KO", "KO_KR" -> Locale.KOREAN;
      case "CHINESE", "ZH", "ZH_CN" -> Locale.CHINESE;
      case "CANADA" -> Locale.CANADA;
      case "CANADA_FRENCH" -> Locale.CANADA_FRENCH;
      default -> {
        if (value.contains("_")) {
          String[] split = value.split("_", 2);
          yield new Locale(split[0].toLowerCase(Locale.ROOT), split[1].toUpperCase(Locale.ROOT));
        }
        yield new Locale(value.toLowerCase(Locale.ROOT));
      }
    };
  }

}
