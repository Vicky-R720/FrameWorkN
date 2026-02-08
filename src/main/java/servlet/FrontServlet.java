package servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import servlet.annotations.Url;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import servlet.annotations.Url;

public class FrontServlet extends HttpServlet {

    private Map<String, Map<servlet.http.HttpMethod, MethodInvoker>> routes = new HashMap<>();

    // init est executé une seule fois au lancement de ce servlet
    @Override
    public void init() throws ServletException {
        try {
            // 1 Scanner les classes du package "controller"
            List<Class<?>> classes = getClasses("com.itu.gest_emp.controller");

            // 2 Parcourir leurs méthodes pour trouver celles annotées avec @Url
            for (Class<?> c : classes) {
                for (var m : c.getDeclaredMethods()) {

                    if (m.isAnnotationPresent(servlet.annotations.GetMapping.class)) {
                        String path = m.getAnnotation(servlet.annotations.GetMapping.class).value();
                        registerRoute(path, servlet.http.HttpMethod.GET, c, m);
                    }

                    if (m.isAnnotationPresent(servlet.annotations.PostMapping.class)) {
                        String path = m.getAnnotation(servlet.annotations.PostMapping.class).value();
                        registerRoute(path, servlet.http.HttpMethod.POST, c, m);
                    }
                }
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void registerRoute(String path,
            servlet.http.HttpMethod method,
            Class<?> controller,
            java.lang.reflect.Method m) {

        routes.computeIfAbsent(path, k -> new HashMap<>())
                .put(method, new MethodInvoker(controller, m));

        System.out.println("Mapped [" + method + "] " + path +
                " -> " + controller.getName() + "." + m.getName());
    }

    // }
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getRequestURI().substring(req.getContextPath().length());
        String httpMethodStr = req.getMethod(); // GET, POST
        servlet.http.HttpMethod httpMethod = servlet.http.HttpMethod.valueOf(httpMethodStr);

        Map<servlet.http.HttpMethod, MethodInvoker> methods = routes.get(path);

        if (methods == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("404 - No mapping for " + path);
            return;
        }

        MethodInvoker invoker = methods.get(httpMethod);

        if (invoker == null) {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            resp.getWriter().print("405 - Method " + httpMethod + " not allowed for " + path);
            return;
        }

        resp.setContentType("text/plain");

        if (invoker != null) {
            try {
                Object controller = invoker.controllerClass.getDeclaredConstructor().newInstance();

                // --- Nouvelle partie : injection des paramètres ---
                java.lang.reflect.Method method = invoker.method;
                java.lang.reflect.Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    java.lang.reflect.Parameter p = parameters[i];
                    Class<?> paramType = p.getType();

                    if (Map.class.isAssignableFrom(paramType)) {

                        Map<String, Object> map = new HashMap<>();

                        Enumeration<String> paramNames = req.getParameterNames();
                        while (paramNames.hasMoreElements()) {
                            String name = paramNames.nextElement();
                            String value = req.getParameter(name);
                            map.put(name, value);
                        }

                        args[i] = map;
                        continue;
                    }

                    // 1️⃣ Récupérer le nom du paramètre depuis @RequestParam si présent
                    String paramName;
                    if (p.isAnnotationPresent(servlet.annotations.RequestParam.class)) {
                        paramName = p.getAnnotation(servlet.annotations.RequestParam.class).value();
                    } else {
                        paramName = p.getName(); // fallback (si javac -parameters)
                    }

                    // 2️⃣ Récupérer la valeur depuis req.getParameter
                    String valueStr = req.getParameter(paramName);

                    // 3️⃣ Conversion type
                    if (valueStr == null && (paramType == int.class || paramType == Integer.class)) {
                        args[i] = 0;
                    } else if (valueStr == null) {
                        args[i] = null;
                    } else if (paramType == int.class || paramType == Integer.class) {
                        args[i] = Integer.parseInt(valueStr);
                    } else if (paramType == String.class) {
                        args[i] = valueStr;
                    } else {
                        args[i] = null;
                    }
                }

                // --- Appel de la méthode avec injection ---
                Object result = method.invoke(controller, args);

                // --- Gestion retour (ton code existant) ---
                if (result instanceof String) {
                    System.out.println(invoker.method.getName() + " -> String : " + result);
                } else if (result == null) {
                    System.out.println(invoker.method.getName() + " -> null");
                } else if (result instanceof ModelView mv) {
                    for (var entry : mv.getData().entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }
                    req.getRequestDispatcher("/pages/" + mv.getView()).forward(req, resp);
                } else {
                    System.out.println(
                            invoker.method.getName() + " -> NON-String : " + result.getClass().getSimpleName());
                }
                resp.getWriter().print(result);

            } catch (Exception e) {
                e.printStackTrace(resp.getWriter());
            }
        } else {
            resp.getWriter().print("404 - Aucun contrôleur trouvé pour " + path);
        }
    }

    // ---- Classe utilitaire ----
    private static class MethodInvoker {
        Class<?> controllerClass;
        java.lang.reflect.Method method;

        MethodInvoker(Class<?> c, java.lang.reflect.Method m) {
            this.controllerClass = c;
            this.method = m;
        }
    }

    // ---- Scanner récursif des classes ----
    private List<Class<?>> getClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();

        // Récupérer tous les noms de classes chargées dans le classpath
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File directory = new File(resource.toURI());

            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().endsWith(".class")) {
                            String className = packageName + '.' + file.getName().replace(".class", "");
                            classes.add(Class.forName(className));
                        }
                    }
                }
            }
        }

        // ✅ Debug : affichage de ce qui a été trouvé
        System.out.println("Classes trouvées dans " + packageName + " :");
        for (Class<?> c : classes) {
            System.out.println(" → " + c.getName());
        }

        return classes;
    }

}