package es.storeapp.web.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class CSPInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {

        // Política de Seguridad de Contenido más estricta
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +  // Solo contenido de mismo origen
            "img-src 'self' data:; " +  // Imágenes solo desde el mismo origen y URLs de datos
            "script-src 'self' 'nonce-{random_nonce}' 'strict-dynamic'; " +  // Solo scripts de mismo origen, usar nonce para permitir scripts dinámicos seguros
            "style-src 'self' 'unsafe-inline'; " +  // Habilita solo estilos internos de mismo origen
            "font-src 'self'; " +  // Fuentes solo del mismo origen
            "connect-src 'self'; " +  // Limitado a solicitudes HTTP a mismo dominio
            "frame-src 'none'; " +  // No se permiten frames
            "object-src 'none'; " +  // Prohibir objetos, applets y embebidos
            "child-src 'none'; " +  // Prohibir sub-frames
            "form-action 'self'; " +  // Limitar envíos de formularios a mismo origen
            "base-uri 'self';" // Restringe el uso de <base> para evitar cambios maliciosos en URLs relativas
        );

        return true;
    }
    
}
