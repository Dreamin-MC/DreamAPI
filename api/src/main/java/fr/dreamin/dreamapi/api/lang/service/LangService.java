package fr.dreamin.dreamapi.api.lang.service;

import fr.dreamin.dreamapi.api.lang.model.LangFile;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;
import java.util.Set;

public interface LangService {

  void load();
  void load(final @NotNull File file);

  Optional<Translator> getTranslator(final @NotNull String value);
  Translator createTranslator(final @NotNull String label, final @NotNull String namespace, final @NotNull String value);

  @NotNull Set<String> getLoadedFiles();
  @NotNull Optional<LangFile> getLangFile(@NotNull String file);
  boolean unload(@NotNull String file);
  boolean reload(@NotNull String file);
  boolean add(@NotNull String file);
  void reset();

}
