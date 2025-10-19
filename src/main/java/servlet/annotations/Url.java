package servlet.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

// On précise où et combien de temps l’annotation est conservée
@Retention(RetentionPolicy.RUNTIME) // Disponible à l’exécution
@Target(ElementType.METHOD) // Applicable sur une classe
public @interface Url {
    String value(); // attribut de l'annotation (ex: le chemin URL)
}
