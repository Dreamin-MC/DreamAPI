package fr.dreamin.dreamapi.api.skin.event;

import fr.dreamin.dreamapi.api.event.ToolsCancelEvent;
import fr.dreamin.dreamapi.api.skin.model.Skin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public final class PlayerSkinResetEvent extends ToolsCancelEvent {

  private final @NotNull Player player;
  private final @Nullable Skin previousSkin;

}
