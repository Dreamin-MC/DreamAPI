package fr.dreamin.dreamapi.plugin;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.item.RegisteredItem;
import fr.dreamin.dreamapi.api.recipe.CustomRecipe;
import fr.dreamin.dreamapi.core.animation.AnimationServiceImpl;
import fr.dreamin.dreamapi.core.cmd.scanner.CmdAnnotationProcessor;
import fr.dreamin.dreamapi.api.recipe.service.RecipeRegistryService;
import fr.dreamin.dreamapi.api.item.ItemRegistryService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.ApiProviderImpl;
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
import fr.dreamin.dreamapi.plugin.cmd.admin.nms.visual.VisualCmd;
import fr.dreamin.dreamapi.plugin.cmd.admin.service.ServiceCmd;
import lombok.Getter;
import lombok.Setter;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
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

  private PaperCommandManager<CommandSender> manager;
  private AnnotationParser<CommandSender> annotationParser;

  /**
   * Reference to the active {@link DreamAPI.IApiProvider} instance.
   */
  @Getter
  protected DreamAPI.IApiProvider dreamAPI;

  @Getter
  protected @NotNull Set<Class<?>> preScannedClasses;

  @Getter
  protected @NotNull ServiceAnnotationProcessor serviceManager;

  @Getter
  protected @NotNull DreamServiceInspector serviceInspector;

  @Getter @Setter
  protected @NotNull BroadcastContext broadcastContext;

  @Getter @Setter
  protected boolean broadcastCmd = false, glowingCmd = false, nmsVisualCmd = false, itemRegistryCmd, debugCmd = false, serviceCmd = false;

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
      this.preScannedClasses = ClassScanner.getClasses(this, this.getClass().getPackageName(), true);
    } catch (IOException | ClassNotFoundException e) {
      getLogger().severe(String.format("[DreamAPI] Failed to scan classes: %s", e.getMessage()));
      throw new RuntimeException(e);
    }

    this.broadcastContext = BroadcastContext.builder().build();

    this.dreamAPI = DreamAPI.getAPI();

    this.serviceManager = new ServiceAnnotationProcessor(this, this.preScannedClasses);
    loadServices();
    this.serviceManager.process();
    this.serviceInspector = new DreamServiceInspector(this, this.serviceManager);

    new ItemAnnotationProcessor(this, getService(ItemRegistryService.class), this.serviceManager, this.preScannedClasses)
      .process();

    new RecipeAnnotationProcessor(this, getService(RecipeRegistryService.class), this.serviceManager, this.preScannedClasses)
      .process();

    initCmds();
    new CmdAnnotationProcessor(this, this.annotationParser, this.serviceManager, this.preScannedClasses)
      .process();

    new ListenerAnnotationProcessor(this, this.serviceManager, this.preScannedClasses)
      .process();

    onDreamEnable();
    loadCommands();

    getLogger().info(
      String.format("DreamAPI initialized successfully with provider: %s",
        this.dreamAPI.getClass().getSimpleName())
    );
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
  public @NotNull <T extends DreamService> Optional<T> findDreamService(@NotNull Class<T> clazz) {
    return Optional.ofNullable(this.serviceManager.getDreamService(clazz));
  }

  /**
   * Returns an internal DreamService instance (implementation class).
   *
   * @param clazz The class implementing DreamService
   * @param <T>   The concrete type extending DreamService
   * @return The loaded DreamService, or null if not found
   */
  public <T extends DreamService> @NotNull T getDreamService(@NotNull Class<T> clazz) {
    T service = this.serviceManager.getDreamService(clazz);
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
    this.serviceManager.loadServiceFromClass(PlayerDebugServiceImpl.class);
    this.serviceManager.loadServiceFromClass(DebugServiceImpl.class);
    this.serviceManager.loadServiceFromClass(RecipeRegistryServiceImpl.class);
    this.serviceManager.loadServiceFromClass(RecipeCategoryRegistryServiceImpl.class);

    this.serviceManager.loadServiceFromClass(ItemRegistryServiceImpl.class);

    this.serviceManager.loadServiceFromClass(AnimationServiceImpl.class);
    this.serviceManager.loadServiceFromClass(WorldServiceImpl.class);
    this.serviceManager.loadServiceFromClass(DayCycleServiceImpl.class);
    this.serviceManager.loadServiceFromClass(TeamServiceImpl.class);

    this.serviceManager.loadServiceFromClass(VisualServiceImpl.class);
    this.serviceManager.loadServiceFromClass(GlowingServiceImpl.class);
    this.serviceManager.loadServiceFromClass(CuboidServiceImpl.class);

    // FIXME (Scraven, 29/12/2025): load if TeamService is loaded
    if (isLuckPermsAvailable())
      this.serviceManager.loadServiceFromClass(LuckPermsServiceImpl.class);

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

    if (this.itemRegistryCmd)
      this.annotationParser.parse(new ItemRegistryCmd());

    if (this.glowingCmd)
      this.annotationParser.parse(new GlowingCmd());

    if (this.nmsVisualCmd)
      this.annotationParser.parse(new VisualCmd());

    if (this.debugCmd)
      this.annotationParser.parse(new DebugCmd());
    if (this.serviceCmd)
      this.annotationParser.parse(new ServiceCmd(this, this.serviceManager, this.serviceInspector));

  }

}
