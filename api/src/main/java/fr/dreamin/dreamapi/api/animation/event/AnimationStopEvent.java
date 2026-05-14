package fr.dreamin.dreamapi.api.animation.event;

import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import fr.dreamin.dreamapi.api.event.ToolsEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public final class AnimationStopEvent extends ToolsEvent {

  public enum Reason {
    FINISHED,
    CANCELLED
  }

  private final @NotNull IAnimationProperty property;
  private final @NotNull Reason reason;

}

