package servlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonResponse {
    private String status;
    private int code;
    private Object data;
    
    public JsonResponse() {
    }
    
    public JsonResponse(String status, int code, Object data) {
        this.status = status;
        this.code = code;
        this.data = data;
    }
    
    // Méthodes statiques pour créer des réponses
    public static JsonResponse success(Object data) {
        return new JsonResponse("success", 200, data);
    }
    
    public static JsonResponse error(int code, String message) {
        Map<String, String> errorData = new HashMap<>();
        errorData.put("message", message);
        return new JsonResponse("error", code, errorData);
    }
    
    // Getters et Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    // Conversion en JSON simple (sans bibliothèque externe)
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"status\":\"").append(status).append("\",");
        json.append("\"code\":").append(code).append(",");
        json.append("\"data\":");
        
        if (data == null) {
            json.append("null");
        } else if (data instanceof String) {
            json.append("\"").append(escapeJson(data.toString())).append("\"");
        } else if (data instanceof Number || data instanceof Boolean) {
            json.append(data);
        } else if (data instanceof List) {
            json.append(convertListToJson((List<?>) data));
        } else if (data instanceof Map) {
            json.append(convertMapToJson((Map<?, ?>) data));
        } else {
            // Pour les objets complexes, on utilise la réflexion
            json.append(convertObjectToJson(data));
        }
        
        json.append("}");
        return json.toString();
    }
    
    private String convertListToJson(List<?> list) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) json.append(",");
            first = false;
            
            if (item == null) {
                json.append("null");
            } else if (item instanceof String) {
                json.append("\"").append(escapeJson(item.toString())).append("\"");
            } else if (item instanceof Number || item instanceof Boolean) {
                json.append(item);
            } else if (item instanceof Map) {
                json.append(convertMapToJson((Map<?, ?>) item));
            } else if (item instanceof List) {
                json.append(convertListToJson((List<?>) item));
            } else {
                json.append(convertObjectToJson(item));
            }
        }
        json.append("]");
        return json.toString();
    }
    
    private String convertMapToJson(Map<?, ?> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) json.append(",");
            first = false;
            
            json.append("\"").append(escapeJson(entry.getKey().toString())).append("\":");
            
            Object value = entry.getValue();
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append("\"").append(escapeJson(value.toString())).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else if (value instanceof List) {
                json.append(convertListToJson((List<?>) value));
            } else if (value instanceof Map) {
                json.append(convertMapToJson((Map<?, ?>) value));
            } else {
                json.append(convertObjectToJson(value));
            }
        }
        json.append("}");
        return json.toString();
    }
    
    private String convertObjectToJson(Object obj) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        try {
            // Récupérer tous les champs de la classe
            Class<?> clazz = obj.getClass();
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                
                if (!first) json.append(",");
                first = false;
                
                json.append("\"").append(field.getName()).append("\":");
                
                if (value == null) {
                    json.append("null");
                } else if (value instanceof String) {
                    json.append("\"").append(escapeJson(value.toString())).append("\"");
                } else if (value instanceof Number || value instanceof Boolean) {
                    json.append(value);
                } else if (value instanceof List) {
                    json.append(convertListToJson((List<?>) value));
                } else if (value instanceof Map) {
                    json.append(convertMapToJson((Map<?, ?>) value));
                } else {
                    json.append(convertObjectToJson(value));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        json.append("}");
        return json.toString();
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
