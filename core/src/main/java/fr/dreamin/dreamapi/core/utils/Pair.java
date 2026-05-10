package fr.dreamin.dreamapi.core.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
@AllArgsConstructor
public final class Pair<A, B> {

  private @NotNull A component1;
  private @NotNull B component2;

}
