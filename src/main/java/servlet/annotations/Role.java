package servlet.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer une méthode qui nécessite un ou plusieurs rôles spécifiques.
 * L'utilisateur doit être authentifié ET avoir au moins un des rôles spécifiés.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Role {
    /**
     * Liste des rôles autorisés. L'utilisateur doit avoir au moins un de ces rôles.
     */
    String[] value();
}
