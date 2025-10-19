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

    private Map<String, MethodInvoker> routes = new HashMap<>();

    // init est executé une seule fois au lancement de ce servlet
    @Override
    public void init() throws ServletException {
        try {
            // 1 Scanner les classes du package "controller"
            List<Class<?>> classes = getClasses("com.itu.gest_emp.controller");

            // 2 Parcourir leurs méthodes pour trouver celles annotées avec @Url
            for (Class<?> c : classes) {
                for (var m : c.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(Url.class)) {
                        Url annotation = m.getAnnotation(Url.class);
                        String path = annotation.value();
                        routes.put(path, new MethodInvoker(c, m));
                        System.out.println("Route enregistrée : " + path + " → " + c.getName() + "." + m.getName());
                    }
                }
            }
            
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // }
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getRequestURI().substring(req.getContextPath().length());
        MethodInvoker invoker = routes.get(path);

        resp.setContentType("text/plain");

        if (invoker != null) {
            try {
                Object controller = invoker.controllerClass.getDeclaredConstructor().newInstance();
                Object result = invoker.method.invoke(controller);
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