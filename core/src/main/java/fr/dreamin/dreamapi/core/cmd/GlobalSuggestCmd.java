package fr.dreamin.dreamapi.core.cmd;

import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import org.bukkit.command.CommandSender;

import java.util.List;

public class GlobalSuggestCmd {

  @Suggestions("onoff")
  public List<String> suggestOnOff(CommandContext<CommandSender> sender, String input) {
    return List.of("on", "off");
  }

}
