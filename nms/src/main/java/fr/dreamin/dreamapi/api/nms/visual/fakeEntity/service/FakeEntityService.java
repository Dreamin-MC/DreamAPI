package fr.dreamin.dreamapi.api.nms.visual.fakeEntity.service;

import fr.dreamin.dreamapi.api.nms.visual.fakeEntity.model.FakeEntity;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface FakeEntityService {

  FakeEntity npc(final @NotNull String name, final @NotNull Location location, final @NotNull Player... viewers);

  FakeEntity hologram(final @NotNull List<Component> lines, final @NotNull Location location, final @NotNull Player... viewers);

  void walkPath(final @NotNull FakeEntity entity, final @NotNull List<Location> path, final long durationTicks, final @NotNull Player... viewers);

  void lookAt(final @NotNull FakeEntity entity, final @NotNull Location target, final @NotNull Player... viewers);

}
