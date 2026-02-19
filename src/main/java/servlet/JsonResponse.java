package servlet;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * Classe pour générer des réponses JSON
 */
public class JsonResponse {
    private boolean success;
    private Object data;
    private String error;
    private int errorCode;

    private JsonResponse(boolean success, Object data, String error, int errorCode) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.errorCode = errorCode;
    }

    public static JsonResponse success(Object data) {
        return new JsonResponse(true, data, null, 0);
    }

    public static JsonResponse error(int code, String message) {
        return new JsonResponse(false, null, message, code);
    }

    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\":").append(success).append(",");
        
        if (success) {
            json.append("\"data\":");
            json.append(objectToJson(data));
        } else {
            json.append("\"error\":\"").append(escapeJson(error)).append("\",");
            json.append("\"errorCode\":").append(errorCode);
        }
        
        json.append("}");
        return json.toString();
    }

    private String objectToJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        if (obj instanceof String) {
            return "\"" + escapeJson(obj.toString()) + "\"";
        }
        
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        
        if (obj instanceof Map) {
            return mapToJson((Map<?, ?>) obj);
        }
        
        if (obj instanceof Collection) {
            return collectionToJson((Collection<?>) obj);
        }
        
        if (obj.getClass().isArray()) {
            return arrayToJson(obj);
        }
        
        // Objet personnalisé
        return pojoToJson(obj);
    }

    private String mapToJson(Map<?, ?> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(escapeJson(entry.getKey().toString())).append("\":");
            json.append(objectToJson(entry.getValue()));
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    private String collectionToJson(Collection<?> collection) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Object item : collection) {
            if (!first) json.append(",");
            json.append(objectToJson(item));
            first = false;
        }
        json.append("]");
        return json.toString();
    }

    private String arrayToJson(Object array) {
        StringBuilder json = new StringBuilder("[");
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) json.append(",");
            json.append(objectToJson(Array.get(array, i)));
        }
        json.append("]");
        return json.toString();
    }

    private String pojoToJson(Object obj) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        try {
            for (java.lang.reflect.Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(obj);
                
                if (!first) json.append(",");
                json.append("\"").append(field.getName()).append("\":");
                json.append(objectToJson(value));
                first = false;
            }
        } catch (Exception e) {
            return "\"Error serializing object\"";
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

    public boolean isSuccess() {
        return success;
    }

    public Object getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
