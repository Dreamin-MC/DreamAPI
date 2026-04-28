package fr.dreamin.dreamapi.core.lang.service;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.api.lang.event.LangUpdateEvent;
import fr.dreamin.dreamapi.api.lang.model.LangFile;
import fr.dreamin.dreamapi.api.lang.service.LangService;
import fr.dreamin.dreamapi.api.lang.utils.LangUtils;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.i18n.Languages;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@DreamAutoService(LangService.class)
public final class LangServiceImpl implements LangService, DreamService, Listener {

  private static final int CHECK_INTERVAL = 10;

  private final @NotNull Map<String, MiniMessageTranslationStore> translationStore = new HashMap<>();
  private final Map<String, LangFile> langFiles = new HashMap<>();

  private final @NotNull Map<UUID, Locale> playerLocales = new HashMap<>();
  private BukkitTask langUpdateTask;

  private boolean enableItem = true, enableGUI = true;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public void onLoad(@NotNull Plugin plugin) {
    load();

    startLangMonitoring(plugin);
  }

  @Override
  public void onClose() {
    reset();
    if (this.langUpdateTask != null) {
      this.langUpdateTask.cancel();
    }
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

    final var langFolder = new File(DreamAPI.getAPI().plugin().getDataFolder(), "lang");
    final var fileKey = buildFileKey(langFolder, file);

    final var store = createTranslator(fileKey, langFile.namespace, langFile.value);
    final var invuiTranslations = new HashMap<Locale, Map<String, String>>();

    if (langFile.defaultLocale != null && !langFile.defaultLocale.isBlank())
      store.defaultLocale(parseLocale(langFile.defaultLocale));

    if (langFile.keys != null) {
      for (final var entry : langFile.keys) {
        if (entry == null || entry.key == null || entry.key.isBlank() || entry.lang == null)
          continue;

        for (final var value : entry.lang) {
          if (value == null || value.locale == null || value.locale.isBlank() || value.value == null)
            continue;

          final var locale = parseLocale(value.locale);

          store.register(entry.key, locale, value.value);
          invuiTranslations
            .computeIfAbsent(locale, ignored -> new HashMap<>())
            .put(entry.key, value.value);
        }
      }
    }

    if (this.enableGUI)
      invuiTranslations.forEach((locale, translations) -> {
        if (locale == Locale.FRENCH) {
          Languages.getInstance().addLanguage(locale.FRANCE, translations);
          Languages.getInstance().addLanguage(locale, translations);
        }
      });

    this.langFiles.put(fileKey, langFile);
  }

  @Override
  public Optional<Locale> getLocale(@NotNull UUID uuid) {
    return Optional.ofNullable(this.playerLocales.get(uuid));
  }

  @Override
  public Optional<String> getTranslation(final @NotNull Player player, final @NotNull String key) {
    return findTranslation(player.locale(), key);
  }

  @Override
  public Optional<String> getTranslation(final @NotNull Locale locale, final @NotNull String key) {
    return findTranslation(locale, key);
  }

  @Override
  public Optional<String> findTranslation(final @NotNull UUID uuid, final @NotNull String key) {
    return getLocale(uuid).flatMap(locale -> findTranslation(locale, key));
  }

  @Override
  public Optional<String> findTranslation(final @NotNull Locale locale, final @NotNull String key) {
    if (key.isBlank()) {
      return Optional.empty();
    }

    return this.langFiles.entrySet().stream()
      .sorted(Map.Entry.comparingByKey())
      .map(entry -> findTranslationInFile(entry.getValue(), locale, key))
      .flatMap(Optional::stream)
      .findFirst();
  }

  @Override
  public void enableItem(boolean value) {
    this.enableItem = value;
  }

  @Override
  public boolean isEnableItem() {
    return this.enableItem;
  }

  @Override
  public void enableGUI(boolean value) {
    this.enableGUI = value;
    Languages.getInstance().enableServerSideTranslations(value);
  }

  @Override
  public boolean isEnableGUI() {
    return this.enableGUI;
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
  public Map<String, LangFile> getLangFiles() {
    return Collections.unmodifiableMap(this.langFiles);
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
  public boolean reload(@NotNull String fileKey) {
    final var langFile = resolveLangFile(fileKey).orElse(null);
    if (langFile == null)
      return false;

    unload(fileKey);
    load(langFile);
    return true;
  }

  @Override
  public boolean add(@NotNull String fileKey) {
    final var langFile = resolveLangFile(fileKey).orElse(null);
    if (langFile == null)
      return false;

    this.load(langFile);
    return true;
  }

  @Override
  public void reset() {
    this.translationStore.values()
      .forEach(m -> GlobalTranslator.translator().removeSource(m));
    this.translationStore.clear();
    this.langFiles.clear();
    this.playerLocales.clear();
  }

  @Override
  public boolean isSupportedLangFile(@NotNull File file) {
    return file.getName().toLowerCase(Locale.ROOT).endsWith(".json");
  }

  @Override
  public @NotNull String buildFileKey(@NotNull File langFolder, @NotNull File file) {
    final String relativePath = langFolder.toPath().relativize(file.toPath()).toString();
    final String withoutExtension = relativePath.replaceFirst("\\.[^.]+$", "");
    return withoutExtension.replace('\\', '_').replace('/', '_');
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private @NotNull Optional<File> resolveLangFile(@NotNull String fileKey) {
    final var langFolder = new File(DreamAPI.getAPI().plugin().getDataFolder(), "lang");
    if (!langFolder.exists() || !langFolder.isDirectory()) {
      return Optional.empty();
    }

    try (final var paths = Files.walk(langFolder.toPath())) {
      return paths
        .filter(Files::isRegularFile)
        .map(Path::toFile)
        .filter(this::isSupportedLangFile)
        .filter(file -> buildFileKey(langFolder, file).equals(fileKey))
        .findFirst();
    } catch (IOException e) {
      throw new RuntimeException("Failed to resolve lang file for key " + fileKey, e);
    }
  }

  private @NotNull Optional<String> findTranslationInFile(final @NotNull LangFile file, final @NotNull Locale locale, final @NotNull String key) {
    if (file.keys == null) {
      return Optional.empty();
    }

    for (final var entry : file.keys) {
      if (entry == null || entry.key == null || entry.lang == null || !entry.key.equals(key)) {
        continue;
      }

      String languageFallback = null;

      for (final var value : entry.lang) {
        if (value == null || value.locale == null || value.locale.isBlank() || value.value == null) {
          continue;
        }

        final var translationLocale = parseLocale(value.locale);
        if (translationLocale.equals(locale)) {
          return Optional.of(value.value);
        }

        if (languageFallback == null && translationLocale.getLanguage().equals(locale.getLanguage())) {
          languageFallback = value.value;
        }
      }

      if (languageFallback != null) {
        return Optional.of(languageFallback);
      }

      if (file.defaultLocale == null || file.defaultLocale.isBlank()) {
        return Optional.empty();
      }

      final var defaultLocale = parseLocale(file.defaultLocale);
      for (final var value : entry.lang) {
        if (value == null || value.locale == null || value.locale.isBlank() || value.value == null) {
          continue;
        }

        if (parseLocale(value.locale).equals(defaultLocale)) {
          return Optional.of(value.value);
        }
      }

      return Optional.empty();
    }

    return Optional.empty();
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

  private void startLangMonitoring(@NotNull Plugin plugin) {
    this.langUpdateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkAndUpdatePlayerItems, CHECK_INTERVAL, CHECK_INTERVAL);
  }

  private void checkAndUpdatePlayerItems() {
    for (final var player : Bukkit.getOnlinePlayers()) {
      final var currentLocale = player.locale();

      if (!currentLocale.equals(this.playerLocales.get(player.getUniqueId()))) {
        this.playerLocales.put(player.getUniqueId(), currentLocale);

        Bukkit.getScheduler().runTaskLater(DreamAPI.getAPI().plugin(), () -> {
          updatePlayerInventory(player);
          new LangUpdateEvent(player, currentLocale).callEvent();
        }, 2L);
      }
    }
  }

  private void updatePlayerInventory(@NotNull Player player) {
    final var inv = player.getInventory();

    for (var i = 0; i < inv.getSize(); i++) {
      final var item = inv.getItem(i);
      if (item == null || item.getType().isAir())
        continue;
      LangUtils.updateTranslate(player, item);
    }

    final var armor = inv.getArmorContents();
    for (final var item : armor) {
      if (item == null || item.getType().isAir())
        continue;
      LangUtils.updateTranslate(player, item);
    }

    player.updateInventory();
  }

  private void updatePlayerHeldItems(@NotNull Player player) {
    final var inv = player.getInventory();

    final var mainHand = inv.getItemInMainHand();
    if (!mainHand.getType().isAir())
      LangUtils.updateTranslate(player, mainHand);

    final var offHand = inv.getItemInOffHand();
    if (!offHand.getType().isAir())
      LangUtils.updateTranslate(player, offHand);

    player.updateInventory();
  }

  // ###############################################################
  // ---------------------- LISTENER METHODS -----------------------
  // ###############################################################

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  private void onSlotChange(final @NotNull PlayerInventorySlotChangeEvent event) {
    final var player = event.getPlayer();
    final var slot = event.getSlot();

    final var oldItem = event.getOldItemStack();
    final var newItem = event.getNewItemStack();

    if (newItem.getType().isAir())
      return;

    if (oldItem.isSimilar(newItem) && oldItem.getAmount() == newItem.getAmount())
      return;

    Bukkit.getScheduler().runTask(DreamAPI.getAPI().plugin(), () -> {
      final var liveItem = player.getInventory().getItem(slot);
      if (liveItem == null || liveItem.getType().isAir())
        return;

      LangUtils.updateTranslate(player, liveItem);
    });
  }

  @EventHandler
  private void onJoin(final @NotNull PlayerJoinEvent event) {
    final var player = event.getPlayer();

    this.playerLocales.put(player.getUniqueId(), player.locale());

    updatePlayerInventory(player);
  }


}
