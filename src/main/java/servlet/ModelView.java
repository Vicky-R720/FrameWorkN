package servlet;

import java.util.HashMap;
import java.util.Map;

public class ModelView {

    private String view;
    private Map<String, Object> data = new HashMap<>();

    // Getter et Setter pour la vue
    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    // Ajouter une variable
    public void addObject(String key, Object value) {
        data.put(key, value);
    }

    // Récupérer les variables
    public Map<String, Object> getData() {
        return data;
    }
}
