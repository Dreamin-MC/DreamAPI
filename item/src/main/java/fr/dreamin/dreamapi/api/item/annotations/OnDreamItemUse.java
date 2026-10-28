package fr.dreamin.dreamapi.api.item.annotations;

import fr.dreamin.dreamapi.api.item.ItemAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnDreamItemUse {

  ItemAction action();
  String itemId() default "";
  String[] tags() default {};

}
