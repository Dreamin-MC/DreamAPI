package fr.dreamin.dreamapi.plugin.cmd.admin.service;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.logger.DebugService;
import fr.dreamin.dreamapi.api.services.DreamAutoService;
import fr.dreamin.dreamapi.api.services.DreamService;
import fr.dreamin.dreamapi.core.service.ServiceAnnotationProcessor;
import fr.dreamin.dreamapi.core.service.ui.DreamServiceInspector;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class ServiceCmd {

  private final @NotNull Plugin plugin;
  private final @NotNull ServiceAnnotationProcessor manager;
  private final @NotNull DreamServiceInspector inspector;

  @CommandDescription("DreamAPI Service Management Commands")
  @CommandMethod("service list")
  @CommandPermission("dreamapi.cmd.service.list")
  private void onList(CommandSender sender) {
    AtomicReference<Component> msg = new AtomicReference<>(Component.text("-------- Dream - Services --------", NamedTextColor.GOLD));

    manager.getAllLoadedServices().forEach((clazz, service) -> {
      msg.set(msg.get().appendNewline()
        .append(Component.text("- ", NamedTextColor.DARK_GRAY))
        .append(Component.text(clazz.getSimpleName(), NamedTextColor.WHITE))
        .append(Component.text(" : ", NamedTextColor.DARK_GRAY))
        .append(Component.text(String.format("[%s]", service.getStatus().toString()), NamedTextColor.GOLD))
        .clickEvent(ClickEvent.runCommand("/service info " + clazz.getSimpleName()))
      );
    });

    msg.set(msg.get().appendNewline()
      .append(Component.text("--------------------------------", NamedTextColor.GOLD))
    );

    sender.sendMessage(msg.get());
  }

  @Suggestions("services")
  public List<String> services(CommandContext<CommandSender> sender, String input) {
    return this.manager.getAllLoadedServices().keySet().stream()
      .map(Class::getSimpleName)
      .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
      .toList();
  }

  @CommandDescription("DreamAPI Service Management Commands")
  @CommandMethod("service info <name>")
  @CommandPermission("dreamapi.cmd.service.info")
  private void onInfo(
    CommandSender sender,
    @Argument(value = "name", suggestions = "services") String name
  ) {
    final var service = getServiceByName(name);

    if (service == null)
      sender.sendMessage(Component.text(String.format("Service %s not found.", name), NamedTextColor.RED));

    Component msg = Component.text("--------------------------------", NamedTextColor.GOLD)
      .appendNewline()
      .append(Component.text(String.format("Service Info: %s", name), NamedTextColor.GOLD))
      .appendNewline()
      .append(Component.text(String.format("Status: %s", service.getStatus()), NamedTextColor.GOLD))
      .appendNewline()
      .append(Component.text(String.format("CanReload: %s", service.canReload()), NamedTextColor.GOLD))
      .appendNewline()
      .append(Component.text(String.format("Depends: %s", getDepsText(service)), NamedTextColor.GOLD))
      .appendNewline()
      .append(Component.text("--------------------------------", NamedTextColor.GOLD));

    sender.sendMessage(msg);
  }

  @CommandDescription("DreamAPI Service Management Commands")
  @CommandMethod("service reload <name>")
  @CommandPermission("dreamapi.cmd.service.reload")
  private void onReload(
    CommandSender sender,
    @Argument(value = "name", suggestions = "services") String name
  ) {
    final var service = getServiceByName(name);
    if (service == null) {
      sender.sendMessage(Component.text(String.format("Service %s not found.", name), NamedTextColor.RED));
      return;
    }

    this.manager.reloadService(service);
    sender.sendMessage(Component.text(String.format("Service %s reloaded.", name), NamedTextColor.GOLD));
  }

  @CommandDescription("DreamAPI Service Management Commands")
  @CommandMethod("service reload all")
  @CommandPermission("dreamapi.cmd.service.reloadall")
  private void onReloadAll(CommandSender sender) {
    this.manager.reloadAllServices();
    sender.sendMessage(Component.text("All services reloaded.", NamedTextColor.GOLD));
  }

  @CommandDescription("DreamAPI Service Management Commands")
  @CommandMethod("service close <name>")
  @CommandPermission("dreamapi.cmd.service.close")
  private void onClose(
    CommandSender sender,
    @Argument(value = "name", suggestions = "services") String name
  ) {
    final var service = getServiceByName(name);
    if (service == null) {
      sender.sendMessage(Component.text(String.format("Service %s not found.", name), NamedTextColor.RED));
      return;
    }

    this.manager.closeService(service);
    sender.sendMessage(Component.text(String.format("Service %s closed.", name), NamedTextColor.GOLD));
  }

  @CommandDescription("DreamAPI Service Management Commands")
  @CommandMethod("service reset <name>")
  @CommandPermission("dreamapi.cmd.service.reset")
  private void onReset(
    CommandSender sender,
    @Argument(value = "name", suggestions = "services") String name
  ) {
    final var service = getServiceByName(name);
    if (service == null) {
      sender.sendMessage(Component.text(String.format("Service %s not found.", name), NamedTextColor.RED));
      return;
    }

    this.manager.resetService(service);
    sender.sendMessage(Component.text(String.format("Service %s reset.", name), NamedTextColor.GOLD));
  }

  @CommandDescription("DreamAPI Service Management Commands")
  @CommandMethod("service debug <name> <mode>")
  @CommandPermission("dreamapi.cmd.service.debug")
  private void onDebug(
    CommandSender sender,
    @Argument(value = "name", suggestions = "services") String name,
    @Argument(value = "mode", suggestions = "onoff") String mode
  ) {
    final var service = getServiceByName(name);
    if (service == null) {
      sender.sendMessage(Component.text(String.format("Service %s not found.", name), NamedTextColor.RED));
      return;
    }

    final var enable = mode.equalsIgnoreCase("on");

    DreamAPI.getAPI().getService(DebugService.class)
      .setCategory(service.getClass().getSimpleName(), enable);

    sender.sendMessage(Component.text(String.format("Service %s debug %s.", name, (enable ? "enabled" : "disabled")), NamedTextColor.GOLD));
  }

  @CommandDescription("DreamAPI Service Management Commands")
  @CommandMethod("service deps <name>")
  @CommandPermission("dreamapi.cmd.service.deps")
  private void onDeps(
    CommandSender sender,
    @Argument(value = "name", suggestions = "services") String name
  ) {
    final var service = getServiceByName(name);
    if (service == null) {
      sender.sendMessage(Component.text(String.format("Service %s not found.", name), NamedTextColor.RED));
      return;
    }

    final var deps = getDepsText(service);
    sender.sendMessage(Component.text(String.format("Service %s dependencies: %s", name, deps), NamedTextColor.GOLD));
  }

  @CommandDescription("DreamAPI service inspector")
  @CommandMethod("service inspector")
  @CommandPermission("dreamapi.cmd.service.inspector")
  private void openInspector(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    this.inspector.openMainMenu(player);
  }

  @CommandDescription("Generate a diagram of service dependencies")
  @CommandMethod("service diagram")
  @CommandPermission("dreamapi.cmd.service.diagram")
  private void generateDiagram(CommandSender sender) {
    final var folder = new File(this.plugin.getDataFolder(), "debug");
    if (!folder.exists()) folder.mkdirs();

    final var file = new File(folder, "services.puml");

    this.manager.exportServiceDiagram(file);

    sender.sendMessage(Component.text("Diagram UML generated: " + file.getName()));
    sender.sendMessage(Component.text("Emplacement: " + file.getAbsolutePath()));
    sender.sendMessage(Component.text("You can use https://www.plantuml.com/plantuml to visualize it."));
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private DreamService getServiceByName(final @NotNull String name) {
    return this.manager.getAllLoadedServices().values().stream()
      .filter(s -> s.getName().equalsIgnoreCase(name) || s.getClass().getSimpleName().equalsIgnoreCase(name))
      .findFirst()
      .orElse(null);
  }

  private String getDepsText(final @NotNull DreamService service) {
    final var ann = service.getClass().getAnnotation(DreamAutoService.class);
    if (ann == null || ann.dependencies().length == 0) return "none";

    return Arrays.stream(ann.dependencies())
      .map(Class::getSimpleName)
      .collect(Collectors.joining(", "));
  }

}
