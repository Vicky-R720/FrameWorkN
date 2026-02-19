package servlet;

/**
 * Interface représentant une session utilisateur avec authentification et rôles.
 * L'application peut implémenter cette interface pour représenter un utilisateur connecté.
 */
public interface UserSession {
    
    /**
     * Retourne les rôles de l'utilisateur connecté.
     * @return Tableau des rôles (ex: ["admin", "user"])
     */
    String[] getRoles();
    
    /**
     * Vérifie si l'utilisateur possède un rôle spécifique.
     * @param role Le rôle à vérifier
     * @return true si l'utilisateur a ce rôle, false sinon
     */
    boolean hasRole(String role);
}
