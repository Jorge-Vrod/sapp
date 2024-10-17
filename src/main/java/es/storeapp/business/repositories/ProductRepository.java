package es.storeapp.business.repositories;

import es.storeapp.business.entities.Product;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository extends AbstractRepository<Product> {

    // SQL INJECTION
    private static final String FIND_BY_CATEGORY_QUERY =
            "SELECT p FROM Product p WHERE p.category.name = :category ORDER BY p.{orderColumn}";

    @SuppressWarnings("unchecked")
    public List<Product> findByCategory(String category, String orderColumn) {

        String dynamicQuery = FIND_BY_CATEGORY_QUERY.replace("{orderColumn}", orderColumn);

        Query query = entityManager.createQuery(dynamicQuery);
        query.setParameter("category", category);

        return query.getResultList();
    }

    
}
