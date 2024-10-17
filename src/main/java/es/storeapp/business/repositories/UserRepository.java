package es.storeapp.business.repositories;

import es.storeapp.business.entities.User;
import java.text.MessageFormat;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends AbstractRepository<User> {
    // Safe version: Using parameterized queries to prevent SQL Injection
    private static final String FIND_USER_BY_EMAIL_QUERY = "SELECT u FROM User u WHERE u.email = :email";
    private static final String COUNT_USER_BY_EMAIL_QUERY = "SELECT COUNT(u) FROM User u WHERE u.email = :email";
    private static final String LOGIN_QUERY = "SELECT u FROM User u WHERE u.email = :email AND u.password = :password";

    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";

    public User findByEmail(String email) {
        try {
            // Use parameterized query to prevent SQL Injection
            Query query = entityManager.createQuery(FIND_USER_BY_EMAIL_QUERY);
            query.setParameter(EMAIL, email);  // Set the parameter safely
            return (User) query.getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public boolean existsUser(String email) {
        Query query = entityManager.createQuery(COUNT_USER_BY_EMAIL_QUERY);
        query.setParameter(EMAIL, email);  // Set the parameter safely
        return ((Long) query.getSingleResult() > 0);
    }

    public User findByEmailAndPassword(String email, String password) {
        try {
            // Use parameterized query to prevent SQL Injection
            Query query = entityManager.createQuery(LOGIN_QUERY);
            query.setParameter(EMAIL, email);      // Set the email parameter safely
            query.setParameter(PASSWORD, password);  // Set the password parameter safely
            return (User) query.getSingleResult();
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

}
