package fr.dreamin.dreamapi.core.animation.service;

import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.animation.model.KeyFrame;
import fr.dreamin.dreamapi.api.animation.model.MessageKeyFrame;
import fr.dreamin.dreamapi.api.animation.model.ParticleKeyFrame;
import fr.dreamin.dreamapi.api.animation.model.SoundKeyFrame;
import fr.dreamin.dreamapi.api.animation.model.TitleKeyFrame;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimationServiceImplTest {

  @BeforeAll
  static void setupDreamApiProvider() {
    if (!DreamAPI.isInitialized()) {
      DreamAPI.setProvider(new DreamAPI.IApiProvider() {
        private final Logger logger = Logger.getLogger("AnimationServiceImplTest");

        @Override
        public <T> T getService(Class<T> serviceClass) {
          return null;
        }

        @Override
        public <T extends Event> T callEvent(T event) {
          return event;
        }

        @Override
        public Plugin plugin() {
          return null;
        }

        @Override
        public Logger getLogger() {
          return this.logger;
        }
      });
    }
  }

  @Test
  void shouldDeserializeSingleMessageKeyFrame() {
    final var service = new AnimationServiceImpl();
    final String json = """
      {
        "type": "MESSAGE",
        "value": "hello world",
        "sendType": "ACTION_BAR",
        "translatable": false
      }
      """;

    final var result = service.deserializeKeyFrames(json);

    assertEquals(1, result.size());
    final var keyFrame = assertInstanceOf(MessageKeyFrame.class, result.getFirst());
    assertEquals(KeyFrame.Type.MESSAGE, keyFrame.getType());
    assertEquals("hello world", keyFrame.getValue());
    assertEquals(MessageKeyFrame.SendType.ACTION_BAR, keyFrame.getSendType());
    assertFalse(keyFrame.isTranslatable());
  }

  @Test
  void shouldDeserializeSingleTitleKeyFrameWithFades() {
    final var service = new AnimationServiceImpl();
    final String json = """
      {
        "type": "TITLE",
        "value": "title.key",
        "subtitle": "subtitle.key",
        "fades": [500, 3000, 750]
      }
      """;

    final var result = service.deserializeKeyFrames(json);

    assertEquals(1, result.size());
    final var keyFrame = assertInstanceOf(TitleKeyFrame.class, result.getFirst());
    assertEquals(KeyFrame.Type.TITLE, keyFrame.getType());
    assertEquals("title.key", keyFrame.getValue());
    assertEquals("subtitle.key", keyFrame.getSubtitle());
    assertEquals(Duration.ofMillis(500), keyFrame.getTime().fadeIn());
    assertEquals(Duration.ofMillis(3000), keyFrame.getTime().stay());
    assertEquals(Duration.ofMillis(750), keyFrame.getTime().fadeOut());
  }

  @Test
  void shouldDeserializeSingleSoundKeyFrameWithDefaults() {
    final var service = new AnimationServiceImpl();
    final String json = """
      {
        "type": "SOUND",
        "value": "entity.player.levelup",
        "volume": 0.7,
        "pitch": 1.2
      }
      """;

    final var result = service.deserializeKeyFrames(json);

    assertEquals(1, result.size());
    final var keyFrame = assertInstanceOf(SoundKeyFrame.class, result.getFirst());
    assertEquals(KeyFrame.Type.SOUND, keyFrame.getType());
    assertEquals("entity.player.levelup", keyFrame.getValue());
    assertEquals(0.7f, keyFrame.getVolume());
    assertEquals(1.2f, keyFrame.getPitch());
    assertEquals(SoundCategory.MASTER, keyFrame.getCategory());
    assertNull(keyFrame.getBoneValue());
  }

  @Test
  void shouldDeserializeSingleParticleKeyFrameWithOffsets() {
    final var service = new AnimationServiceImpl();
    final String json = """
      {
        "type": "PARTICLE",
        "value": "FLAME",
        "bone": "head",
        "count": 10,
        "offsets": [0.1, 0.2, 0.3],
        "speed": 0.5,
        "force": true
      }
      """;

    final var result = service.deserializeKeyFrames(json);

    assertEquals(1, result.size());
    final var keyFrame = assertInstanceOf(ParticleKeyFrame.class, result.getFirst());
    assertEquals(KeyFrame.Type.PARTICLE, keyFrame.getType());
    assertEquals(Particle.FLAME, keyFrame.getParticle());
    assertEquals("head", keyFrame.getBoneValue());
    assertEquals(10, keyFrame.getCount());
    assertArrayEquals(new Double[]{0.1, 0.2, 0.3}, keyFrame.getOffsets());
    assertEquals(0.5, keyFrame.getSpeed());
    assertTrue(keyFrame.isForce());
  }

  @Test
  void shouldDeserializeParticleKeyFrameWithDefaultOffsetsWhenMissing() {
    final var service = new AnimationServiceImpl();
    final String json = """
      {
        "type": "PARTICLE",
        "value": "FLAME",
        "bone": "head",
        "count": 4,
        "speed": 0.0,
        "force": false
      }
      """;

    final var result = service.deserializeKeyFrames(json);

    assertEquals(1, result.size());
    final var keyFrame = assertInstanceOf(ParticleKeyFrame.class, result.getFirst());
    assertArrayEquals(new Double[]{0.0, 0.0, 0.0}, keyFrame.getOffsets());
  }

  @Test
  void shouldDeserializeMultipleKeyFramesFromArray() {
    final var service = new AnimationServiceImpl();
    final String json = """
      [
        {
          "type": "MESSAGE",
          "value": "first"
        },
        {
          "type": "MESSAGE",
          "value": "second",
          "translatable": true
        }
      ]
      """;

    final var result = service.deserializeKeyFrames(json);

    assertEquals(2, result.size());
    assertInstanceOf(MessageKeyFrame.class, result.get(0));
    assertInstanceOf(MessageKeyFrame.class, result.get(1));
    assertEquals("first", result.get(0).getValue());
    assertEquals("second", result.get(1).getValue());
  }

  @Test
  void shouldDeserializeMixedArrayWithAllFormats() {
    final var service = new AnimationServiceImpl();
    final String json = """
      [
        {
          "type": "MESSAGE",
          "value": "hello"
        },
        {
          "type": "TITLE",
          "value": "title.key",
          "subtitle": "subtitle.key"
        },
        {
          "type": "SOUND",
          "value": "entity.player.levelup",
          "volume": 1.0,
          "pitch": 1.0,
          "category": "MASTER"
        },
        {
          "type": "PARTICLE",
          "value": "FLAME",
          "bone": "head",
          "count": 2,
          "offsets": [0.0, 0.1, 0.0],
          "speed": 0.0,
          "force": false
        }
      ]
      """;

    final var result = service.deserializeKeyFrames(json);

    assertEquals(4, result.size());
    assertInstanceOf(MessageKeyFrame.class, result.get(0));
    assertInstanceOf(TitleKeyFrame.class, result.get(1));
    assertInstanceOf(SoundKeyFrame.class, result.get(2));
    assertInstanceOf(ParticleKeyFrame.class, result.get(3));
  }

  @Test
  void shouldThrowOnUnknownType() {
    final var service = new AnimationServiceImpl();
    final String json = """
      {
        "type": "UNKNOWN",
        "value": "invalid"
      }
      """;

    final var exception = assertThrows(IllegalArgumentException.class, () -> service.deserializeKeyFrames(json));
    assertNotNull(exception.getMessage());
  }

  @Test
  void shouldThrowOnMalformedJson() {
    final var service = new AnimationServiceImpl();
    final String json = "{";

    assertThrows(IllegalArgumentException.class, () -> service.deserializeKeyFrames(json));
  }

  @Test
  void shouldThrowOnMissingType() {
    final var service = new AnimationServiceImpl();
    final String json = """
      {
        "value": "invalid"
      }
      """;

    final var exception = assertThrows(IllegalArgumentException.class, () -> service.deserializeKeyFrames(json));
    assertNotNull(exception.getMessage());
    assertTrue(exception.getMessage().contains("type") || exception.getCause() != null);
  }

  @Test
  void shouldRegisterAndUnregisterRunnableByProperty() {
    final var service = new AnimationServiceImpl();
    final var property = newPropertyProxy();
    final BukkitRunnable runnable = new BukkitRunnable() {
      @Override
      public void run() {
      }
    };

    service.registerRunnable(property, runnable);
    assertEquals(runnable, service.getRunnable(property));

    service.unregisterRunnable(property);
    assertNull(service.getRunnable(property));
  }

  @Test
  void shouldContinueApplyingKeyFramesWhenOneFails() {
    final var service = new AnimationServiceImpl();
    final var property = newPropertyProxy();
    final var appliedCount = new AtomicInteger(0);

    final KeyFrame failing = new TestKeyFrame() {
      @Override
      public void apply(IAnimationProperty p) {
        throw new RuntimeException("boom");
      }
    };

    final KeyFrame succeeding = new TestKeyFrame() {
      @Override
      public void apply(IAnimationProperty p) {
        appliedCount.incrementAndGet();
      }
    };

    assertDoesNotThrow(() -> service.applyKeyFrames(property, List.of(failing, succeeding)));
    assertEquals(1, appliedCount.get());
  }

  private static IAnimationProperty newPropertyProxy() {
    return (IAnimationProperty) Proxy.newProxyInstance(
      IAnimationProperty.class.getClassLoader(),
      new Class[]{IAnimationProperty.class},
      (proxy, method, args) -> {
        if ("toString".equals(method.getName()))
          return "TestProperty";

        final Class<?> returnType = method.getReturnType();
        if (!returnType.isPrimitive())
          return null;

        if (returnType == boolean.class)
          return false;
        if (returnType == byte.class)
          return (byte) 0;
        if (returnType == short.class)
          return (short) 0;
        if (returnType == int.class)
          return 0;
        if (returnType == long.class)
          return 0L;
        if (returnType == float.class)
          return 0f;
        if (returnType == double.class)
          return 0d;
        if (returnType == char.class)
          return '\0';

        return null;
      }
    );
  }

  private abstract static class TestKeyFrame extends KeyFrame {
    protected TestKeyFrame() {
      super(Type.MESSAGE, "test", null);
    }
  }
}
