package fr.dreamin.dreamapi.api.dependency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a service, listener, command, or module class requires a specific
 * Minecraft server version range (e.g., {@code @RequiresVersion(min = "26.2")}).
 */
@Target({ElementType.TYPE, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresVersion {
  /**
   * Minimum Minecraft version required (inclusive), e.g. "26.1.2", "26.2", "26.3", "26.4".
   */
  String min();

  /**
   * Optional maximum Minecraft version supported (inclusive), e.g. "26.3.9".
   */
  String max() default "";
}
