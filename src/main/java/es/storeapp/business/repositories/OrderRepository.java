package es.storeapp.business.repositories;

import es.storeapp.business.entities.Order;
import java.text.MessageFormat;
import java.util.List;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository extends AbstractRepository<Order> {
    // VULN : SQL INJECTION
    private static final String USER_ID = "userId";
    private static final String FIND_BY_USER_QUERY = 
            "SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.timestamp DESC";
        
    @SuppressWarnings("unchecked")
    public List<Order> findByUserId(Long userId) {
        Query query = entityManager.createQuery(FIND_BY_USER_QUERY);
        query.setParameter(USER_ID, userId);  // Set the parameter safely
        return query.getResultList();
    }
   
}
