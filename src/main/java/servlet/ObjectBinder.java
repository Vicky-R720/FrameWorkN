package servlet;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.*;
import java.util.*;

public class ObjectBinder {

    public static Object bindObject(HttpServletRequest req, Class<?> targetType) 
            throws Exception {
        
        // Pour les types simples
        if (targetType == String.class || 
            targetType == int.class || targetType == Integer.class ||
            targetType == double.class || targetType == Double.class ||
            targetType == boolean.class || targetType == Boolean.class) {
            return null; // Géré par le code existant
        }

        // Pour les tableaux
        if (targetType.isArray()) {
            return bindArray(req, targetType.getComponentType());
        }

        // Pour les Map (déjà géré)
        if (Map.class.isAssignableFrom(targetType)) {
            return bindMap(req);
        }

        // Pour les objets complexes (comme Employee)
        Object instance = targetType.getDeclaredConstructor().newInstance();
        bindFields(req, instance, "");
        return instance;
    }

    private static void bindFields(HttpServletRequest req, Object obj, String prefix) 
            throws Exception {
        
        Class<?> clazz = obj.getClass();
        
        // Parcourir tous les paramètres de la requête
        Enumeration<String> paramNames = req.getParameterNames();
        
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = req.getParameter(paramName);
            
            // Vérifier si ce paramètre correspond à cet objet (avec ou sans préfixe)
            if (prefix.isEmpty() || paramName.startsWith(prefix + ".") || 
                paramName.startsWith(prefix + "[")) {
                
                String fieldPath = prefix.isEmpty() ? paramName : paramName.substring(prefix.length() + 1);
                setFieldValue(obj, fieldPath, paramValue, req);
            }
        }
    }

    private static void setFieldValue(Object obj, String fieldPath, String value, 
                                      HttpServletRequest req) throws Exception {
        
        // Gérer la notation avec index : department[0].name
        if (fieldPath.contains("[") && fieldPath.contains("]")) {
            int bracketStart = fieldPath.indexOf('[');
            int bracketEnd = fieldPath.indexOf(']');
            
            String fieldName = fieldPath.substring(0, bracketStart);
            String indexStr = fieldPath.substring(bracketStart + 1, bracketEnd);
            int index = Integer.parseInt(indexStr);
            
            String remainingPath = fieldPath.substring(bracketEnd + 1);
            if (remainingPath.startsWith(".")) {
                remainingPath = remainingPath.substring(1);
            }
            
            // Accéder au champ
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            
            // Récupérer ou créer le tableau/liste
            Object fieldValue = field.get(obj);
            
            if (fieldValue == null) {
                // Déterminer le type de collection/tableau
                Class<?> fieldType = field.getType();
                
                if (fieldType.isArray()) {
                    fieldValue = Array.newInstance(fieldType.getComponentType(), index + 1);
                    field.set(obj, fieldValue);
                } else if (List.class.isAssignableFrom(fieldType)) {
                    fieldValue = new ArrayList<>();
                    field.set(obj, fieldValue);
                }
            }
            
            // Continuer le binding récursivement
            if (field.getType().isArray()) {
                Object[] array = (Object[]) fieldValue;
                if (array.length <= index) {
                    // Redimensionner le tableau (simplifié)
                    array = Arrays.copyOf(array, index + 1);
                    field.set(obj, array);
                }
                
                if (array[index] == null) {
                    array[index] = field.getType().getComponentType().getDeclaredConstructor().newInstance();
                }
                
                if (!remainingPath.isEmpty()) {
                    setFieldValue(array[index], remainingPath, value, req);
                } else {
                    // Convertir la valeur selon le type
                    setSimpleValue(array[index], value);
                }
            }
            
        } else if (fieldPath.contains(".")) {
            // Notation pointée : e.name
            String[] parts = fieldPath.split("\\.", 2);
            String fieldName = parts[0];
            String remainingPath = parts[1];
            
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            
            Object fieldValue = field.get(obj);
            if (fieldValue == null) {
                fieldValue = field.getType().getDeclaredConstructor().newInstance();
                field.set(obj, fieldValue);
            }
            
            setFieldValue(fieldValue, remainingPath, value, req);
            
        } else {
            // Champ simple
            Field field = obj.getClass().getDeclaredField(fieldPath);
            field.setAccessible(true);
            
            setSimpleValue(obj, field, value);
        }
    }

    private static void setSimpleValue(Object obj, Field field, String value) 
            throws Exception {
        
        Class<?> fieldType = field.getType();
        
        if (value == null || value.isEmpty()) {
            return;
        }
        
        if (fieldType == String.class) {
            field.set(obj, value);
        } else if (fieldType == int.class || fieldType == Integer.class) {
            field.set(obj, Integer.parseInt(value));
        } else if (fieldType == double.class || fieldType == Double.class) {
            field.set(obj, Double.parseDouble(value));
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            field.set(obj, Boolean.parseBoolean(value));
        }
        // Ajouter d'autres types au besoin
    }

    private static void setSimpleValue(Object obj, String value) throws Exception {
        // Méthode simplifiée pour les objets terminaux
        // À adapter selon vos besoins
    }

    private static Map<String, Object> bindMap(HttpServletRequest req) {
        Map<String, Object> map = new HashMap<>();
        Enumeration<String> paramNames = req.getParameterNames();
        
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            map.put(name, req.getParameter(name));
        }
        
        return map;
    }

    private static Object bindArray(HttpServletRequest req, Class<?> componentType) 
            throws Exception {
        
        // Compter les paramètres avec cette forme : param[0], param[1], etc.
        Map<Integer, String> indexedValues = new HashMap<>();
        Enumeration<String> paramNames = req.getParameterNames();
        
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            
            if (name.matches(".*\\[\\d+\\].*")) {
                // Extraire l'index
                int start = name.indexOf('[');
                int end = name.indexOf(']');
                String indexStr = name.substring(start + 1, end);
                int index = Integer.parseInt(indexStr);
                
                indexedValues.put(index, req.getParameter(name));
            }
        }
        
        if (indexedValues.isEmpty()) {
            return null;
        }
        
        // Créer le tableau
        int maxIndex = Collections.max(indexedValues.keySet());
        Object array = Array.newInstance(componentType, maxIndex + 1);
        
        for (Map.Entry<Integer, String> entry : indexedValues.entrySet()) {
            Object value = convertValue(entry.getValue(), componentType);
            Array.set(array, entry.getKey(), value);
        }
        
        return array;
    }

    private static Object convertValue(String strValue, Class<?> targetType) {
        if (strValue == null) return null;
        
        if (targetType == String.class) return strValue;
        if (targetType == int.class || targetType == Integer.class) 
            return Integer.parseInt(strValue);
        if (targetType == double.class || targetType == Double.class) 
            return Double.parseDouble(strValue);
        if (targetType == boolean.class || targetType == Boolean.class) 
            return Boolean.parseBoolean(strValue);
        
        return strValue;
    }
}