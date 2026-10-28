package fr.dreamin.dreamapi.api.logger;

import org.jetbrains.annotations.NotNull;

public interface DebugWriter {

  default boolean isEnabled() {
    return true;
  }

  default void setEnabled(boolean enabled) {}

  void write(final @NotNull LogEntry entry) throws Exception;

}
