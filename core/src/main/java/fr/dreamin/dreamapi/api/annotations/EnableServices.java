package fr.dreamin.dreamapi.api.annotations;

import fr.dreamin.dreamapi.api.LoadMode;
import fr.dreamin.dreamapi.api.services.DreamService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableServices {

  LoadMode[] mode() default {LoadMode.ALL};

  Class<? extends DreamService>[] include() default {};
  Class<? extends DreamService>[] exclude() default {};

}
