package fr.dreamin.example;

import fr.dreamin.dreamapi.plugin.DreamPlugin;
import lombok.Getter;
import org.bukkit.event.Listener;

@Getter
public final class ExamplePlugin extends DreamPlugin implements Listener {

  @Override
  public void onDreamEnable() {
    getLogger().info("DreamAPI good");

    setBroadcastCmd(true);
    setItemRegistryCmd(true);
    setDebugCmd(true);
    setGlowingCmd(true);
    setServiceCmd(true);

  }

  @Override
  public void onDreamDisable() {
    getLogger().info("DreamAPI dead");
  }

}
