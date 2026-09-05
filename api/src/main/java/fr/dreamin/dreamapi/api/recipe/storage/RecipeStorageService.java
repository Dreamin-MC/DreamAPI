package fr.dreamin.dreamapi.api.recipe.storage;

import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.api.recipe.CustomRecipe;
import fr.dreamin.dreamapi.api.recipe.RecipeType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public final class RecipeStorageService {

  private final @NotNull Plugin plugin;

  private final File rootFolder, shapedFolder, shapelessFolder, furnaceFolder, smithingFolder;

  public RecipeStorageService(final @NotNull Plugin plugin) {
    this.plugin = plugin;

    this.rootFolder = new File(plugin.getDataFolder(), "recipes");
    this.shapedFolder = new File(rootFolder, "shaped");
    this.shapelessFolder = new File(rootFolder, "shapeless");
    this.furnaceFolder = new File(rootFolder, "furnace");
    this.smithingFolder = new File(rootFolder, "smithing");

    createFolders();
  }

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public int loadAllRecipes(final @NotNull RecipeLoaderCallback callback) {
    var count = 0;

    count += loadFolder(this.shapedFolder, callback);
    count += loadFolder(this.shapelessFolder, callback);
    count += loadFolder(this.furnaceFolder, callback);
    count += loadFolder(this.smithingFolder, callback);

    plugin.getLogger().info("[Crafting] Loaded " + count + " recipes.");
    return count;

  }

  public void saveRecipe(CustomRecipe recipe) {
    final var folder = folderForType(recipe.getType());
    final var file = new File(folder, recipe.getKey() + ".json");

    try {
      final var json = Configurations.MAPPER.writeValueAsString(recipe);
      Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      plugin.getLogger().warning("[Crafting] Failed to save recipe: " + recipe.getKey());
      ex.printStackTrace();
    }
  }

  public boolean deleteRecipe(String key, RecipeType type) {
    final var file = new File(folderForType(type), key + ".json");
    return file.exists() && file.delete();
  }

  private File folderForType(RecipeType type) {
    return switch (type) {
      case SHAPED -> shapedFolder;
      case SHAPELESS -> shapelessFolder;
      case FURNACE -> furnaceFolder;
      case SMITHING -> smithingFolder;
    };
  }

  @FunctionalInterface
  public interface RecipeLoaderCallback {
    void handle(CustomRecipe recipe);
  }


  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private int loadFolder(File folder, RecipeLoaderCallback callback) {
    if (!folder.exists()) return 0;

    int count = 0;

    for (final var f : Objects.requireNonNull(folder.listFiles((dir, name) -> name.endsWith(".json")))) {
      try {
        final var recipe = Configurations.MAPPER.readValue(f, CustomRecipe.class);
        callback.handle(recipe);
        count++;
      } catch (IOException ex) {
        plugin.getLogger().warning("[Crafting] Failed to load recipe file " + f.getName());
        ex.printStackTrace();
      }
    }
    return count;
  }

  private void createFolders() {
    this.rootFolder.mkdirs();
    this.shapedFolder.mkdirs();
    this.shapelessFolder.mkdirs();
    this.furnaceFolder.mkdirs();
    this.smithingFolder.mkdirs();
  }

}
