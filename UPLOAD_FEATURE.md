# 📤 Fonctionnalité Upload de Fichiers

## Vue d'ensemble

Cette fonctionnalité permet d'uploader des fichiers via des formulaires multipart/form-data dans votre framework servlet.

## Architecture

### Classe `Upload`
Représente un fichier uploadé avec les propriétés :
- `filename` : Nom du fichier original
- `contentType` : Type MIME du fichier
- `size` : Taille en bytes
- `content` : Contenu binaire du fichier
- `savedPath` : Chemin où le fichier a été sauvegardé

### Modification du `FrontServlet`
- Ajout de `@MultipartConfig` pour supporter les requêtes multipart
- Détection automatique des paramètres de type `Map<String, List<Upload>>`
- Gestion many-to-one : plusieurs fichiers peuvent avoir le même nom de champ
- Sauvegarde automatique dans le dossier `upload/`

## 📋 Utilisation dans vos contrôleurs

### Exemple 1 : Upload simple avec texte

```java
@Controller
public class MyController {
    
    @PostMapping("/upload-files")
    public String uploadFiles(Map<String, List<Upload>> files) {
        StringBuilder result = new StringBuilder();
        
        for (Map.Entry<String, List<Upload>> entry : files.entrySet()) {
            String fieldName = entry.getKey();
            List<Upload> uploads = entry.getValue();
            
            for (Upload upload : uploads) {
                result.append("Fichier : ").append(upload.getFilename())
                      .append(" (").append(upload.getSize()).append(" bytes)\n");
            }
        }
        
        return result.toString();
    }
}
```

### Exemple 2 : Upload avec retour JSON

```java
@Controller
public class MyController {
    
    @Json
    @PostMapping("/api/upload-files")
    public JsonResponse uploadFilesJson(Map<String, List<Upload>> files) {
        Map<String, Object> data = new HashMap<>();
        int totalFiles = 0;
        
        for (Map.Entry<String, List<Upload>> entry : files.entrySet()) {
            totalFiles += entry.getValue().size();
        }
        
        data.put("totalFiles", totalFiles);
        data.put("files", files);
        
        return JsonResponse.success(data);
    }
}
```

## 🎯 Formulaire HTML

```html
<form action="/upload-files" method="post" enctype="multipart/form-data">
    <label>Documents :</label>
    <input type="file" name="document" multiple>
    
    <label>Images :</label>
    <input type="file" name="image" accept="image/*" multiple>
    
    <button type="submit">Envoyer</button>
</form>
```

## 🚀 Upload via JavaScript/Fetch

```javascript
const formData = new FormData();
formData.append('document', file1);
formData.append('document', file2);  // Many-to-one
formData.append('image', imageFile);

fetch('/api/upload-files', {
    method: 'POST',
    body: formData
})
.then(response => response.json())
.then(data => console.log(data));
```

## 📁 Structure des fichiers uploadés

Les fichiers sont automatiquement sauvegardés dans :
```
/upload/
  ├── 1234567890_document.pdf
  ├── 1234567891_image.jpg
  └── ...
```

Le nom du fichier est préfixé avec un timestamp pour éviter les conflits.

## 🔑 Points clés

1. **Many-to-one** : Plusieurs fichiers peuvent avoir le même nom de champ
   - Exemple : `<input type="file" name="document" multiple>`
   - Résultat : `Map.get("document")` retourne une `List<Upload>`

2. **Dossier upload/** : Créé automatiquement dans le contexte web

3. **Type de paramètre** : DOIT être exactement `Map<String, List<Upload>>`

4. **Multipart required** : Le formulaire HTML doit avoir `enctype="multipart/form-data"`

## 🧪 Test

1. Compiler et déployer le framework :
   ```powershell
   cd FrameWorkN
   .\launch.bat
   ```

2. Compiler et déployer TestFM :
   ```powershell
   cd TestFM
   .\launch.bat
   ```

3. Ouvrir dans le navigateur :
   ```
   http://localhost:8080/TestFM/pages/test-upload.html
   ```

## 📝 Notes techniques

- Le `FrontServlet` utilise la réflexion pour détecter le type générique `Map<String, List<Upload>>`
- Les fichiers sont chargés en mémoire (byte[]) pour un accès facile
- Le chemin de sauvegarde est disponible via `upload.getSavedPath()`
