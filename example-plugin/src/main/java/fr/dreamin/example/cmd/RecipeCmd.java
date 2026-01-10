package fr.dreamin.example.cmd;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.cmd.DreamCmd;
import fr.dreamin.dreamapi.api.recipe.RecipeCraftingType;
import fr.dreamin.dreamapi.api.recipe.service.RecipeRegistryService;
import fr.dreamin.dreamapi.api.recipe.RecipeTag;
import fr.dreamin.dreamapi.api.recipe.condition.RecipeConditionResult;
import fr.dreamin.dreamapi.core.recipe.builder.FurnaceBuilder;
import fr.dreamin.dreamapi.core.recipe.builder.ShapedBuilder;
import fr.dreamin.dreamapi.core.recipe.builder.ShapelessBuilder;
import fr.dreamin.dreamapi.core.recipe.builder.SmithingBuilder;
import fr.dreamin.dreamapi.core.recipe.ui.RecipeCategoryGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@DreamCmd
public final class RecipeCmd {
  
  private final RecipeRegistryService craftService = DreamAPI.getAPI().getService(RecipeRegistryService.class);

  @CommandMethod("recipe shaped")
  @CommandDescription("Test shaped recipe creation")
  @CommandPermission("recipe.test")
  private void shaped(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    final var recipe = ShapedBuilder.builder("test_shaped")
      .shape("AB", "BA")
      .ingredient('A', Material.DIAMOND)
      .ingredient('B', Material.GOLD_INGOT)
      .result(new ItemStack(Material.EMERALD))
      .condition(condition -> {
        if (condition.craftingType().equals(RecipeCraftingType.PLAYER))
          return RecipeConditionResult.deny();
        else return RecipeConditionResult.allow();
      })
      .tag(RecipeTag.SHAPED)
      .build();

    this.craftService.registerRecipe(recipe);
    sender.sendMessage(Component.text("Shaped test recipe registered!"));

  }

  @CommandMethod("recipe shapeless")
  @CommandDescription("Test shapeless recipe creation")
  @CommandPermission("recipe.test")
  private void shapeless(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    var recipe = ShapelessBuilder.builder("test_shapeless")
      .add(Material.DIRT)
      .add(Material.STICK)
      .tag(RecipeTag.SHAPELESS)
      .result(new ItemStack(Material.BRICK))
      .build();

    this.craftService.registerRecipe(recipe);
    sender.sendMessage(Component.text("Shapeless test recipe registered!"));
  }

  @CommandMethod("recipe furnace")
  @CommandDescription("Test furnace recipe creation")
  @CommandPermission("recipe.test")
  private void furnace(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    var recipe = FurnaceBuilder.builder("test_furnace")
      .input(Material.ARROW)
      .experience(0.5f)
      .cookTime(200)
      .tag(RecipeTag.FURNACE)
      .result(new ItemStack(Material.IRON_INGOT))
      .build();

    craftService.registerRecipe(recipe);
    sender.sendMessage(Component.text("Furnace test recipe registered!"));
  }

  @CommandMethod("recipe smithing")
  @CommandDescription("Test smithing recipe creation")
  @CommandPermission("recipe.test")
  private void smithing(CommandSender sender) {
    if (!(sender instanceof Player player)) return;

    var recipe = SmithingBuilder.builder("test_smithing")
      .template(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
      .base(Material.DIAMOND_SWORD)
      .addition(Material.NETHERITE_INGOT)
      .result(new ItemStack(Material.NETHERITE_SWORD))
      .tag(RecipeTag.SMITHING)
      .build();

    craftService.registerRecipe(recipe);
    sender.sendMessage(Component.text("Smithing test recipe registered!"));
  }

  @CommandMethod("recipe gui")
  @CommandDescription("Open the recipe category browser")
  @CommandPermission("recipe.gui")
  private void gui(CommandSender sender) {
    if (!(sender instanceof Player player)) return;
    new RecipeCategoryGUI().open(player);
  }
  
  
}
