package es.storeapp.web.interceptors;

import es.storeapp.business.entities.Order;
import es.storeapp.business.entities.User;
import es.storeapp.business.services.OrderService;
import es.storeapp.common.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class ValidOrderInterceptor implements HandlerInterceptor {

    private final OrderService orderService;

    public ValidOrderInterceptor(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession();

        if (session.getAttribute(Constants.USER_SESSION) != null) {
            User user = (User) session.getAttribute(Constants.USER_SESSION);

            String path = request.getRequestURI();
            Long orderId = extractOrderIdFromPath(path);

            if (orderId != null) {
                List<Order> orderList = orderService.findByUserById(user.getUserId());

                boolean ownsOrder = orderList.stream().anyMatch(order -> order.getOrderId().equals(orderId));

                if (!ownsOrder) {
                    response.sendRedirect(Constants.ROOT_ENDPOINT);
                    return false;
                }
            }
        }
        return true;
    }

    private Long extractOrderIdFromPath(String path) {
        try {
            String[] pathList = path.split("/");
            if (pathList.length > 2) {
                return Long.parseLong(pathList[2]);
            }
        } catch (NumberFormatException e) {
            // Log or handle invalid order ID format if needed
        }
        return null;
    }
}
