package es.storeapp.business.repositories;

import es.storeapp.business.entities.*;
import java.text.MessageFormat;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class OrderLineRepository extends AbstractRepository<OrderLine>{

    // VULN : SQL INJECTION
    private static final String USER_ID = "userId";
    private static final String PRODUCT_ID = "productId";
    private static final String FIND_BY_USER_AND_PRODUCT_QUERY = 
            "SELECT COUNT(*) FROM OrderLine o WHERE " +
            "o.order.state = es.storeapp.business.entities.OrderState.COMPLETED " + 
            "AND o.order.user.id = :userId AND o.product.id = :productId";

    public boolean findIfUserBuyProduct(Long userId, Long productId) {
        Query query = entityManager.createQuery(FIND_BY_USER_AND_PRODUCT_QUERY);
        query.setParameter(USER_ID, userId);  // Set the parameter safely
        query.setParameter(PRODUCT_ID, productId);  // Set the parameter safely
        return ((Long) query.getSingleResult()) > 0;
    }
    
}
