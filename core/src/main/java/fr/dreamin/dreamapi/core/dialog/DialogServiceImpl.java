package fr.dreamin.dreamapi.core.dialog;

import fr.dreamin.dreamapi.api.dialog.DialogService;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class DialogServiceImpl implements DialogService {

  @Override
  public void notice(@NotNull Player player, @NotNull String title, @NotNull String buttonLabel) {
    ActionButton button = ActionButton.builder(Component.text(buttonLabel))
      .width(200)
      .build();

    Dialog dialog = Dialog.create(build -> build
      .empty()
      .base(DialogBase.builder(Component.text(title)).build())
      .type(DialogType.notice(button))
    );

    player.showDialog(dialog);
  }

  @Override
  public void confirm(Player player, String title, String yes, String no, Consumer<Boolean> callback) {
//    ActionButton yesBtn = ActionButton.builder(Component.text(yes))
//      .width(200)
//      .action(DialogAction.customClick(
//        (executor, response) -> callback.accept(true),
//        DialogActionCallback.Options.defaults()
//      ))
//      .build();
//
//    ActionButton noBtn = ActionButton.builder(Component.text(no))
//      .width(200)
//      .action(DialogAction.customClick(
//        (executor, response) -> callback.accept(false),
//        DialogActionCallback.defaults()
//      ))
//      .build();
//
//    Dialog dialog = Dialog.create(build -> build
//      .empty()
//      .base(DialogBase.builder(Component.text(title)).build())
//      .type(DialogType.confirmation(yesBtn, noBtn))
//    );

//    player.showDialog(dialog);
  }

  @Override
  public DialogBuilder builder() {
    return new DialogBuilderImpl();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private static class DialogBuilderImpl implements DialogBuilder {

    private String title;
    private final List<PlainMessageDialogBody> bodies = new ArrayList<>();
    private final List<DialogInput> inputs = new ArrayList<>();

    private Consumer<Map<String, Object>> onConfirm;
    private Runnable onCancel;

    // ##############################################################
    // ---------------------- SERVICE METHODS -----------------------
    // ##############################################################

    @Override
    public DialogBuilder title(String title) {
      this.title = title;
      return this;
    }

    @Override
    public DialogBuilder message(String message) {
      this.bodies.add(DialogBody.plainMessage(Component.text(message)));
      return this;
    }

    @Override
    public DialogBuilder bool(String key, String label, boolean initial) {
      this.inputs.add(DialogInput.bool(key, Component.text(label), initial, "true", "false"));
      return this;
    }

    @Override
    public DialogBuilder text(String key, String label, String initial, int maxLength) {
      this.inputs.add(DialogInput.text(
        key,
        200,
        Component.text(label),
        true,
        initial,
        maxLength,
        null
      ));
      return this;
    }

    @Override
    public DialogBuilder number(String key, String label, float min, float max, float initial) {
      this.inputs.add(DialogInput.numberRange(
        key,
        200,
        Component.text(label),
        "%.1f",
        min,
        max,
        initial,
        0.1f
      ));
      return this;
    }

    @Override
    public DialogBuilder singleOption(String key, String label, String... entries) {

      return this;
    }

    @Override
    public DialogBuilder onConfirm(Consumer<Map<String, Object>> callback) {
      this.onConfirm = callback;
      return this;
    }

    @Override
    public DialogBuilder onCancel(Runnable callback) {
      this.onCancel = callback;
      return this;
    }

    @Override
    public void show(Player player) {

    }

  }

}
