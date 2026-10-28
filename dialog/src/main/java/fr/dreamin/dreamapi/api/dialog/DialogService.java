package fr.dreamin.dreamapi.api.dialog;

import org.bukkit.entity.Player;
import java.util.Map;
import java.util.function.Consumer;

public interface DialogService {

  // Simple notice sans body
  void notice(Player player, String title, String buttonLabel);

  // Confirmation oui/non
  void confirm(Player player, String title, String yes, String no, Consumer<Boolean> callback);

  // Builder complet type "multi action"
  DialogBuilder builder();

  interface DialogBuilder {

    DialogBuilder title(String title);
    DialogBuilder message(String message); // Ajoute un body

    // Inputs simplifiés
    DialogBuilder bool(String key, String label, boolean initial);
    DialogBuilder text(String key, String label, String initial, int maxLength);
    DialogBuilder number(String key, String label, float min, float max, float initial);
    DialogBuilder singleOption(String key, String label, String... entries);

    DialogBuilder onConfirm(Consumer<Map<String, Object>> callback);
    DialogBuilder onCancel(Runnable callback);

    void show(Player player);
  }
}