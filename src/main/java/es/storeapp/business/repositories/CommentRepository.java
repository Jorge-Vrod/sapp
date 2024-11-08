package es.storeapp.business.repositories;

import es.storeapp.business.entities.Comment;
import java.text.MessageFormat;
import java.util.List;

import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class CommentRepository extends AbstractRepository<Comment> {

    // Safe queries, using placeholders for parameterized queries
    private static final String COUNT_BY_USER_AND_PRODUCT_QUERY =
            "SELECT COUNT(c) FROM Comment c WHERE c.user.id = :userId AND c.product.id = :productId";

    private static final String FIND_BY_USER_AND_PRODUCT_QUERY =
            "SELECT c FROM Comment c WHERE c.user.id = :userId AND c.product.id = :productId";

    // Use parameterized queries to prevent SQL injection
    public Integer countByUserAndProduct(Long userId, Long productId) {
        Query query = entityManager.createQuery(COUNT_BY_USER_AND_PRODUCT_QUERY);
        query.setParameter("userId", userId);  // Set the userId safely
        query.setParameter("productId", productId);  // Set the productId safely
        return ((Long) query.getSingleResult()).intValue();
    }

    public Comment findByUserAndProduct(Long userId, Long productId) {
        Query query = entityManager.createQuery(FIND_BY_USER_AND_PRODUCT_QUERY);
        query.setParameter("userId", userId);  // Set the userId safely
        query.setParameter("productId", productId);  // Set the productId safely

        List<Comment> results = query.getResultList();
        if (results.isEmpty()) {
            return null;  // Return null if there are no comments
        }
        return results.getFirst();  // Safely return the first comment
    }
}
