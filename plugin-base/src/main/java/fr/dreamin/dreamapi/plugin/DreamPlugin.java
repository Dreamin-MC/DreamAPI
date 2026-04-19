package fr.dreamin.dreamapi.plugin;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.LoadMode;
import fr.dreamin.dreamapi.api.annotations.EnableServices;
import fr.dreamin.dreamapi.api.item.ItemTag;
import fr.dreamin.dreamapi.api.item.RegisteredItem;
import fr.dreamin.dreamapi.api.recipe.CustomRecipe;
import fr.dreamin.dreamapi.core.animation.AnimationServiceImpl;
import fr.dreamin.dreamapi.core.cmd.scanner.CmdAnnotationProcessor;
import fr.dreamin.dreamapi.api.recipe.service.RecipeRegistryService;
import fr.dreamin.dreamapi.api.item.ItemRegistryService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.ApiProviderImpl;
import fr.dreamin.dreamapi.core.hologram.service.HologramServiceImpl;
import fr.dreamin.dreamapi.core.lang.service.LangServiceImpl;
import fr.dreamin.dreamapi.core.nms.visual.service.VisualServiceImpl;
import fr.dreamin.dreamapi.core.recipe.service.RecipeCategoryRegistryServiceImpl;
import fr.dreamin.dreamapi.core.recipe.service.RecipeRegistryServiceImpl;
import fr.dreamin.dreamapi.core.recipe.scanner.RecipeAnnotationProcessor;
import fr.dreamin.dreamapi.core.cuboid.service.CuboidServiceImpl;
import fr.dreamin.dreamapi.core.event.scanner.ListenerAnnotationProcessor;
import fr.dreamin.dreamapi.core.glowing.service.GlowingServiceImpl;
import fr.dreamin.dreamapi.api.item.ItemKeys;
import fr.dreamin.dreamapi.core.item.service.ItemRegistryServiceImpl;
import fr.dreamin.dreamapi.core.item.scanner.ItemAnnotationProcessor;
import fr.dreamin.dreamapi.core.logger.DebugServiceImpl;
import fr.dreamin.dreamapi.core.logger.PlayerDebugServiceImpl;
import fr.dreamin.dreamapi.core.luckperms.LuckPermsServiceImpl;
import fr.dreamin.dreamapi.core.scanner.ClassScanner;
import fr.dreamin.dreamapi.core.service.ServiceAnnotationProcessor;
import fr.dreamin.dreamapi.core.service.ui.DreamServiceInspector;
import fr.dreamin.dreamapi.core.team.TeamServiceImpl;
import fr.dreamin.dreamapi.core.time.day.impl.DayCycleServiceImpl;
import fr.dreamin.dreamapi.core.world.impl.WorldServiceImpl;
import fr.dreamin.dreamapi.plugin.cmd.GlobalSuggestCmd;
import fr.dreamin.dreamapi.plugin.cmd.admin.broadcast.BroadcastCmd;
import fr.dreamin.dreamapi.plugin.cmd.admin.broadcast.BroadcastContext;
import fr.dreamin.dreamapi.plugin.cmd.admin.debug.DebugCmd;
import fr.dreamin.dreamapi.plugin.cmd.admin.glowing.GlowingCmd;
import fr.dreamin.dreamapi.plugin.cmd.admin.item.ItemRegistryCmd;
import fr.dreamin.dreamapi.plugin.cmd.admin.lang.LangCmd;
import fr.dreamin.dreamapi.plugin.cmd.admin.nms.visual.VisualCmd;
import fr.dreamin.dreamapi.plugin.cmd.admin.service.ServiceCmd;
import lombok.Getter;
import lombok.Setter;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * <p>
 * Abstract base class for all Paper plugins built on top of DreamAPI.
 * </p>
 *
 * <p>
 * Automatically initializes the {@link DreamAPI} core if not already initialized
 * and provides lifecycle hooks for plugin developers:
 * {@link #onDreamEnable()} and {@link #onDreamDisable()}.
 * </p>
 *
 * <p>
 * Typical usage:
 * </p>
 *
 * <pre>
 * public final class MyPlugin extends DreamPlugin {
 *     &#64;Override
 *     public void onDreamEnable() {
 *         getLogger().info("DreamAPI is ready!");
 *     }
 *
 *     &#64;Override
 *     public void onDreamDisable() {
 *         getLogger().info("Plugin stopped.");
 *     }
 * }
 * </pre>
 *
 * @author Dreamin
 * @since 1.0.0
 */
public abstract class DreamPlugin extends JavaPlugin {

  @Getter
  public static DreamPlugin instance;

  @Getter
  public static @NotNull ServiceAnnotationProcessor serviceManager;

  @Getter
  public static @NotNull Set<Class<?>> preScannedClasses;

  private PaperCommandManager<CommandSender> manager;
  private AnnotationParser<CommandSender> annotationParser;

  /**
   * Reference to the active {@link DreamAPI.IApiProvider} instance.
   */
  @Getter
  protected DreamAPI.IApiProvider dreamAPI;

  @Getter
  protected @NotNull DreamServiceInspector serviceInspector;

  @Getter @Setter
  protected @NotNull BroadcastContext broadcastContext;

  @Getter @Setter
  protected boolean broadcastCmd = false, glowingCmd = false, nmsVisualCmd = false, itemRegistryCmd, debugCmd = false, serviceCmd = false, langCmd = false;

  // ##############################################################
  // -------------------- JAVAPLUGIN METHODS ----------------------
  // ##############################################################

  /**
   * Called by Bukkit when the plugin is enabled.
   * <p>
   * This method ensures DreamAPI is initialized before invoking
   * the developer-defined {@link #onDreamEnable()} method.
   * </p>
   */
  @Override
  public void onEnable() {
    if (!DreamAPI.isInitialized()) {
      getLogger().info("DreamAPI provider not found. Initializing core implementation...");
      new ApiProviderImpl(this);
    }

    instance = this;

    ItemKeys.init(this);

    try {
      preScannedClasses = ClassScanner.getClasses(this, this.getClass().getPackageName(), true, getClassScanParentPackageLevels());
    } catch (IOException | ClassNotFoundException e) {
      getLogger().severe(String.format("[DreamAPI] Failed to scan classes: %s", e.getMessage()));
      throw new RuntimeException(e);
    }

    this.broadcastContext = BroadcastContext.builder().build();

    this.dreamAPI = DreamAPI.getAPI();

    serviceManager = new ServiceAnnotationProcessor(this, preScannedClasses);
    loadServices();
    serviceManager.process();
    this.serviceInspector = new DreamServiceInspector(this, serviceManager);

    if (serviceManager.isLoaded(ItemRegistryServiceImpl.class))
      new ItemAnnotationProcessor(this, getService(ItemRegistryService.class), serviceManager, preScannedClasses)
        .process();

    if (serviceManager.isLoaded(RecipeCategoryRegistryServiceImpl.class))
      new RecipeAnnotationProcessor(this, getService(RecipeRegistryService.class), serviceManager, preScannedClasses)
        .process();

    initCmds();
    new CmdAnnotationProcessor(this, this.annotationParser, serviceManager, preScannedClasses)
      .process();

    new ListenerAnnotationProcessor(this, serviceManager, preScannedClasses)
      .process();

    onDreamEnable();
    loadCommands();

    getLogger().info(
      String.format("DreamAPI initialized successfully with provider: %s",
        this.dreamAPI.getClass().getSimpleName())
    );
  }

  /**
   * Number of package levels to trim before classpath scanning.
   * <p>
   * Example: if main package is {@code fr.dreamin.project.core} and this method
   * returns {@code 1}, scanner starts at {@code fr.dreamin.project} and can include
   * sibling packages such as {@code .api}.
   * </p>
   * <p>
   * Default is {@code 0} to restrict scan to the plugin package.
   * Override and return {@code 1} (or more) to include sibling packages.
   * </p>
   */
  protected int getClassScanParentPackageLevels() {
    return 0;
  }

  /**
   * Called by Bukkit when the plugin is disabled.
   * <p>
   * Invokes the developer-defined {@link #onDreamDisable()} method.
   * </p>
   */
  @Override
  public void onDisable() {
    onDreamDisable();
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  /**
   * Called once DreamAPI is fully initialized and ready.
   * <p>
   * Equivalent to {@code onEnable()} for standard Bukkit plugins.
   * </p>
   */
  public abstract void onDreamEnable();

  /**
   * Called before plugin shutdown.
   * <p>
   * Use this method to release resources, save data, etc.
   * </p>
   */
  public abstract void onDreamDisable();

  // ###############################################################
  // ---------------------- INTERNAL METHODS -----------------------
  // ###############################################################

  /**
   * Get a service from the ServicesManager
   *
   * @param serviceClass the class of the service to get
   * @param <T>          the type of the service
   * @return the service instance
   * @throws IllegalStateException if the service is not loaded
   */
  public static <T> T getService(Class<T> serviceClass) {
    final var sm = Bukkit.getServicesManager();
    T service = sm.load(serviceClass);
    if (service == null)
      throw new IllegalStateException("Service " + serviceClass.getName() + " is not loaded");
    return service;
  }

  /**
   * Call an event
   * @param event the event to call
   */
  public static void callEvent(final @NotNull Event event) {
    Bukkit.getPluginManager().callEvent(event);
  }

  public static void registerEvent(final @NotNull Listener listener) {
    Bukkit.getPluginManager().registerEvents(listener, DreamAPI.getAPI().plugin());
  }

  /**
   *
   * @param id
   * @return
   */
  public static RegisteredItem getItemRegistry(final @NotNull String id) {
    return Optional.ofNullable(
      getService(ItemRegistryService.class).get(id)
    ).orElseThrow(() ->
      new IllegalStateException(
        String.format(
          "ItemRegistryService#getItem returned null for id '%s'. " +
            "Make sure the item is properly registered during startup.",
          id
        )
      )
    );
  }

  public static boolean hasItemTag(final @NotNull String id, final @NotNull ItemTag... tags) {
    final var registered = getService(ItemRegistryService.class).get(id);
    if (registered == null) return false;

    return Arrays.stream(tags).anyMatch(registered::hasTag);
  }

  public static RegisteredItem getItemRegistry(final @NotNull ItemStack itemStack) {
    return Optional.ofNullable(
      getService(ItemRegistryService.class).get(itemStack)
    ).orElseThrow(() ->
      new IllegalStateException(
        String.format(
          "ItemRegistryService#getItem returned null for item stack '%s'. " +
            "Make sure the item is properly registered during startup.",
          itemStack
        )
      )
    );
  }

  public static boolean hasItemTag(final @NotNull ItemStack item, final @NotNull ItemTag... tags) {
    final var registered = getService(ItemRegistryService.class).get(item);
    if (registered == null) return false;

    return Arrays.stream(tags).anyMatch(registered::hasTag);
  }


  /**
   *
   * @param key
   * @return
   */
  public static CustomRecipe getRecipeRegistry(final @NotNull String key) {
    return Optional.ofNullable(
      getService(RecipeRegistryService.class).getRecipe(key)
    ).orElseThrow(() ->
      new IllegalStateException(
        String.format(
          "RecipeRegistryService#getRecipe returned null for key '%s'. " +
            "Make sure the recipe is properly registered during startup.",
          key
        )
      )
    );
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   *
   * @param clazz
   * @return
   * @param <T>
   */
  public @NotNull <T extends DreamService> Optional<T> findDreamService(final @NotNull Class<T> clazz) {
    return Optional.ofNullable(serviceManager.getDreamService(clazz));
  }

  /**
   * Returns an internal DreamService instance (implementation class).
   *
   * @param clazz The class implementing DreamService
   * @param <T>   The concrete type extending DreamService
   * @return The loaded DreamService, or null if not found
   */
  public <T extends DreamService> @NotNull T getDreamService(@NotNull Class<T> clazz) {
    T service = serviceManager.getDreamService(clazz);
    if (service == null) {
      throw new IllegalStateException(String.format(
        "DreamService '%s' is not loaded or not registered.",
        clazz.getSimpleName()
      ));
    }
    return service;
  }

  /**
   * Registers a command handler to the plugin's command framework. The given
   * {@code commandHandler} object is parsed by the annotation parser to identify
   * command definitions or related metadata. This enables dynamic command
   * registration and execution based on the annotated methods within the handler.
   *
   * @param commandHandler The object containing command annotations and logic to be registered.
   *                       Must not be {@code null}.
   */
  public void registerCommand(final @NotNull Object commandHandler) {
    this.annotationParser.parse(commandHandler);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * <p>
   *  Loads and registers a set of services utilized by the plugin.
   * </p>
   * <p>
   *  This method initializes core gameplay and utility services by invoking the
   *  {@code loadServiceFromClass} method on each service implementation. These services
   *  include debug tools, recipe management, team functionality, and world-related operations.
   * </p>
   * <p>
   *  The method ensures that all specified services are instantiated and properly registered
   *  with the plugin's service manager. Additionally, an optional service related to the
   *  LuckPerms plugin is conditionally loaded based on its availability.
   * </p>
   * <p>
   *  The order of service loading respects the dependencies between certain services
   *  and their roles in the plugin's lifecycle.
   * </p>
   * <p>
   *  Note: The service list and dependencies may require updates if new features are added
   *  or existing ones are modified.
   * </p>
   * <p>
   *  FIXME: The LuckPerms service is only loaded if the TeamService is already loaded.
   *  The conditional logic for this dependency may need refinement (see issue note).
   * </p>
   */
  private void loadServices() {
    final var annotation = getClass().getAnnotation(EnableServices.class);
    final var services = new HashSet<Class<? extends DreamService>>();

    if (annotation != null) {

      for (final var m : annotation.mode()) {
        services.addAll(LOAD_MODE_SERVICES.getOrDefault(m, Set.of()));
      }

      services.addAll(Set.of(annotation.include()));
      services.removeAll(Set.of(annotation.exclude()));
    }
    else
      services.addAll(LOAD_MODE_SERVICES.get(LoadMode.ALL));

    for (Class<? extends DreamService> service : services) {
      serviceManager.loadServiceFromClass(service);
    }

    // FIXME (Scraven, 29/12/2025): load if TeamService is loaded
    if (isLuckPermsAvailable())
      serviceManager.loadServiceFromClass(LuckPermsServiceImpl.class);

  }

  /**
   *
   * @return
   */
  private boolean isLuckPermsAvailable() {
    if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null)
      return false;

    var service = Bukkit.getServicesManager().load(LuckPerms.class);
    return service != null;
  }

  /**
   *
   */
  private void initCmds() {
    try {
      this.manager = new PaperCommandManager<>(
        this,
        CommandExecutionCoordinator.simpleCoordinator(),
        Function.identity(),
        Function.identity()
      );

      if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION))
        manager.registerAsynchronousCompletions();

      this.annotationParser = new AnnotationParser<>(manager, CommandSender.class, p -> SimpleCommandMeta.empty());
    } catch (Exception e) {
      this.getLogger().log(Level.SEVERE, "Unable to register commands", e);
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  /**
   *
   */
  private void loadCommands() {
    this.annotationParser.parse(new GlobalSuggestCmd());

    if (this.broadcastCmd)
      this.annotationParser.parse(new BroadcastCmd(this));

    if (this.itemRegistryCmd) {
      if (!serviceManager.isLoaded(ItemRegistryServiceImpl.class))
        serviceManager.loadServiceFromClass(ItemRegistryServiceImpl.class);
      this.annotationParser.parse(new ItemRegistryCmd());
    }

    if (this.glowingCmd) {
      if (!serviceManager.isLoaded(GlowingServiceImpl.class))
        serviceManager.loadServiceFromClass(GlowingServiceImpl.class);
      this.annotationParser.parse(new GlowingCmd());
    }

    if (this.nmsVisualCmd) {
      if (!serviceManager.isLoaded(VisualServiceImpl.class))
        serviceManager.loadServiceFromClass(VisualServiceImpl.class);
      this.annotationParser.parse(new VisualCmd());
    }

    if (this.debugCmd) {
      if (!serviceManager.isLoaded(PlayerDebugServiceImpl.class))
        serviceManager.loadServiceFromClass(PlayerDebugServiceImpl.class);

      if (!serviceManager.isLoaded(DebugServiceImpl.class))
        serviceManager.loadServiceFromClass(DebugServiceImpl.class);

      this.annotationParser.parse(new DebugCmd());
    }

    if (this.langCmd) {
      if (!serviceManager.isLoaded(LangServiceImpl.class))
        serviceManager.loadServiceFromClass(LangServiceImpl.class);

      this.annotationParser.parse(new LangCmd());
    }

    if (this.serviceCmd)
      this.annotationParser.parse(new ServiceCmd(this, serviceManager, this.serviceInspector));

  }

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  private static final Map<LoadMode, Set<Class<? extends DreamService>>> LOAD_MODE_SERVICES = Map.of(
    LoadMode.ALL, Set.of(
      PlayerDebugServiceImpl.class,
      DebugServiceImpl.class,
      RecipeRegistryServiceImpl.class,
      RecipeCategoryRegistryServiceImpl.class,

      ItemRegistryServiceImpl.class,

      AnimationServiceImpl.class,
      WorldServiceImpl.class,
      DayCycleServiceImpl.class,
      TeamServiceImpl.class,

      VisualServiceImpl.class,
      GlowingServiceImpl.class,
      CuboidServiceImpl.class,
      
      HologramServiceImpl.class,
      LangServiceImpl.class
    ),

    LoadMode.MINIMAL, Set.of(
      PlayerDebugServiceImpl.class,
      DebugServiceImpl.class
    ),

    LoadMode.DATA, Set.of(
      ItemRegistryServiceImpl.class,
      RecipeRegistryServiceImpl.class,
      RecipeCategoryRegistryServiceImpl.class,
      HologramServiceImpl.class
    ),

    LoadMode.GAMEPLAY, Set.of(
      WorldServiceImpl.class,
      DayCycleServiceImpl.class,
      TeamServiceImpl.class,
      AnimationServiceImpl.class
    ),

    LoadMode.VISUAL, Set.of(
      VisualServiceImpl.class,
      GlowingServiceImpl.class,
      CuboidServiceImpl.class
    ),

    LoadMode.DEBUG, Set.of(
      PlayerDebugServiceImpl.class,
      DebugServiceImpl.class
    ),

    LoadMode.NONE, Set.of()

  );

}
