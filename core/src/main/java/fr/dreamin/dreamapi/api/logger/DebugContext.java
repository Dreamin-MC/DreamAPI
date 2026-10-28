package fr.dreamin.dreamapi.api.logger;

import org.jetbrains.annotations.NotNull;

public record DebugContext(
  @NotNull String className,
  @NotNull String methodName,
  int lineNumber
) {

  public static DebugContext auto(final @NotNull Object ownerInstance, int skip) {
    final var stack = Thread.currentThread().getStackTrace();

    for (var i = skip; i < stack.length; i++) {
      final var elem = stack[i];
      if (elem.getClassName().equals(ownerInstance.getClass().getName())) {
        return new DebugContext(
          ownerInstance.getClass().getSimpleName(),
          elem.getMethodName(),
          elem.getLineNumber()
        );
      }
    }
    return new DebugContext("Unknown", "Unknown", -1);
  }

}
