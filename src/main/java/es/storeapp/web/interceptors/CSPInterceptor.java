package es.storeapp.web.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class CSPInterceptor implements HandlerInterceptor {
    /*
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Establecer Content-Security-Policy
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +  // Solo contenido de mismo origen
            "img-src 'self'; " +  // Se puede quitar el data: xq no hace falta soportar imagenes externas
            "script-src 'self' 'unsafe-inline'; " +  // Se puede quitar unsafe-eval para evitar ataques XSS, pero si se intenta quitar unsafe-inline no funcionar los scripts (ejem: ratings)
            "style-src 'self' 'unsafe-inline'; " +  // Si se quita el unsafe cambia tamaño svg de la página y se desplazan muchos elementos
            "object-src 'none'; " +  // Evitar objetos, applets, etc., en la página
            "font-src 'self'; " +  // Fuentes solo del mismo origen
            "frame-ancestors 'none';"); // Evitar ser cargado en un iframe

        // Permitir imágenes solo de tipo SVG
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Añadir el encabezado X-Frame-Options para prevenir ataques de clickjacking
        response.addHeader("X-Frame-Options", "SAMEORIGIN");

        response.setHeader("Content-Security-Policy", "upgrade-insecure-requests;");


        return true;
    }*/

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +  // Solo contenido de mismo origen
            "img-src * 'self' data:; " +
            "script-src  * 'self' 'unsafe-eval' 'unsafe-inline'; " +
            "style-src   * 'self' 'unsafe-inline';" +
            "object-src 'none'; " + 
            "font-src 'self'; " + 
            "media-src 'self'; " +
            "worker-src 'self'; " +
            "base-uri 'self'; " +
            "frame-ancestors 'none';"); 

        // Añadir el encabezado X-Frame-Options para prevenir ataques de clickjacking
        response.addHeader("X-Frame-Options", "SAMEORIGIN");

        // Añadir el encabezado X-Content-Type-Options para prevenir la interpretación incorrecta del tipo de contenido
        response.setHeader("X-Content-Type-Options", "nosniff");

        return true;
    }

    
    
}
