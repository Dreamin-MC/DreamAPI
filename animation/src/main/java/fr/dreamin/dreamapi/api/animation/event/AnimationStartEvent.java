package fr.dreamin.dreamapi.api.animation.event;

import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import fr.dreamin.dreamapi.api.event.ToolsCancelEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public final class AnimationStartEvent extends ToolsCancelEvent {

  private final @NotNull IAnimationProperty property;

}

