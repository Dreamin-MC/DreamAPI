package fr.dreamin.dreamapi.core.recipe.module;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import fr.dreamin.dreamapi.api.recipe.model.CustomRecipe;
import fr.dreamin.dreamapi.api.recipe.model.IngredientDefinition;
import fr.dreamin.dreamapi.api.recipe.model.RecipeTag;
import fr.dreamin.dreamapi.api.recipe.model.RecipeType;
import fr.dreamin.dreamapi.api.recipe.condition.RecipeCondition;
import fr.dreamin.dreamapi.core.recipe.data.FurnaceData;
import fr.dreamin.dreamapi.core.recipe.data.SmithingData;
import fr.dreamin.dreamapi.core.recipe.impl.CustomRecipeImpl;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CustomRecipeDeserializer extends JsonDeserializer<CustomRecipe> {
  @Override
  public CustomRecipe deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
    final var codec = p.getCodec();
    final var root = (JsonNode) codec.readTree(p);

    final var key = root.get("key").asText();
    final var type = RecipeType.valueOf(root.get("type").asText());

    final var tags = new HashSet<RecipeTag>();
    final var tagsNode = root.get("tags");
    if (tagsNode != null && tagsNode.isArray()) {
      for (final var t : tagsNode)
        tags.add(RecipeTag.of(t.asText()));
    }

    final var result = codec.treeToValue(root.get("result"), ItemStack.class);

    RecipeCondition condition = null;
    final var condNode = root.get("condition");
    if (condNode != null && !condNode.isNull())
      condition = codec.treeToValue(condNode, RecipeCondition.class);

    return switch (type) {
      case SHAPED    -> deserializeShaped(key, tags, result, condition, root, codec);
      case SHAPELESS -> deserializeShapeless(key, tags, result, condition, root, codec);
      case FURNACE   -> deserializeFurnace(key, tags, result, condition, root, codec);
      case SMITHING  -> deserializeSmithing(key, tags, result, condition, root, codec);
    };
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private CustomRecipe deserializeShaped(
    final @NotNull String key,
    final @NotNull Set<RecipeTag> tags,
    final @NotNull ItemStack result,
    final @Nullable RecipeCondition condition,
    final @NotNull JsonNode root,
    final @NotNull ObjectCodec codec
  ) throws IOException {
    final var shapeNode = root.get("shape");
    if (shapeNode == null || !shapeNode.isArray())
      throw new IOException("Missing or invalid 'shape' for shaped recipe: " + key);

    final var shape = new ArrayList<List<IngredientDefinition>>();

    for (final var rowNode : shapeNode) {
      if (!rowNode.isArray())
        throw new IOException("Invalid row in 'shape' for recipe: " + key);

      final var row = new ArrayList<IngredientDefinition>();
      for (final var cellNode : rowNode) {
        if (cellNode.isNull())
          row.add(null);
        else
          row.add(codec.treeToValue(cellNode, IngredientDefinition.class));
      }
      shape.add(List.copyOf(row));
    }

    return new CustomRecipeImpl(
      key,
      RecipeType.SHAPED,
      tags,
      List.copyOf(shape),
      null,
      result,
      condition,
      null,
      null,
      0.0f,
      0,
      null,
      null,
      null
    );
  }

  private CustomRecipe deserializeShapeless(
    final @NotNull String key,
    final @NotNull Set<RecipeTag> tags,
    final @NotNull ItemStack result,
    final @Nullable RecipeCondition condition,
    final @NotNull JsonNode root,
    final @NotNull ObjectCodec codec
  ) throws IOException {

    final var ingNode = root.get("ingredients");
    if ( ingNode == null || !ingNode.isArray() )
      throw new IOException("Missing or invalid 'ingredients' for shapeless recipe: " + key);

    final var ingredients = new ArrayList<IngredientDefinition>();
    for (final var node : ingNode)
      ingredients.add(codec.treeToValue(node, IngredientDefinition.class));

    return new CustomRecipeImpl(
      key,
      RecipeType.SHAPELESS,
      tags,
      null,
      List.copyOf(ingredients),
      result,
      condition,
      null,
      null,
      0.0f,
      0,
      null,
      null,
      null
    );

  }

  private CustomRecipe deserializeFurnace(
    final @NotNull String key,
    final @NotNull Set<RecipeTag> tags,
    final @NotNull ItemStack result,
    final @Nullable RecipeCondition condition,
    final @NotNull JsonNode root,
    final @NotNull ObjectCodec codec
  ) throws IOException {

    final var furnaceNode = root.get("furnace");
    if (furnaceNode == null || furnaceNode.isNull())
      throw new IOException("Missing 'furnace' node for furnace recipe: " + key);

    final var data = codec.treeToValue(furnaceNode, FurnaceData.class);

    return new CustomRecipeImpl(
      key,
      RecipeType.FURNACE,
      tags,
      null,
      null,
      result,
      condition,
      null,
      data.input(),
      data.experience(),
      data.cookTime(),
      null,
      null,
      null
    );
  }

  private CustomRecipe deserializeSmithing(
    final @NotNull String key,
    final @NotNull Set<RecipeTag> tags,
    final @NotNull ItemStack result,
    final @Nullable RecipeCondition condition,
    final @NotNull JsonNode root,
    final @NotNull ObjectCodec codec
  ) throws IOException {

    final var smithNode = root.get("smithing");
    if (smithNode == null || smithNode.isNull())
      throw new IOException("Missing 'smithing' node for smithing recipe: " + key);

    final var data = codec.treeToValue(smithNode, SmithingData.class);

    return new CustomRecipeImpl(
      key,
      RecipeType.SMITHING,
      tags,
      null,
      null,
      result,
      condition,
      null,
      null,
      0.0f,
      0,
      data.template(),
      data.base(),
      data.addition()
    );
  }
}

