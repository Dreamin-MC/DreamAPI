package fr.dreamin.dreamapi.api.dependency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresDependency {
  String value();

  boolean hard() default false;

  boolean strict() default false;

  boolean loadAfter() default false;
}
