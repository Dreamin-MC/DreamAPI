package fr.dreamin.dreamapi.core.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public final class Pair<A, B> {

  private A first;
  private B second;

}
