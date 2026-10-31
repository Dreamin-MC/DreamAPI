package fr.dreamin.dreamapi.api.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Bukkit {@link org.bukkit.event.Listener} class for automatic discovery
 * and registration by DreamAPI's {@code ListenerAnnotationProcessor}.
 * <p>
 * This is an alias for {@link DreamEvent}.
 * </p>
 *
 * @author Dreamin
 * @since 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterListener { }
