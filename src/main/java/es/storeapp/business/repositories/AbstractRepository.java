package es.storeapp.business.repositories;

import es.storeapp.business.exceptions.InstanceNotFoundException;
import es.storeapp.business.utils.ExceptionGenerationUtils;
import es.storeapp.business.utils.ValidationUtils;
import es.storeapp.common.Constants;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;

public abstract class AbstractRepository<T> {

    protected final Logger logger;

    // VULN : Posible inyeccion SQL en los nombres de tablas y columnas. Checkear
    private static final String FIND_ALL_QUERY = "SELECT t FROM {0} t";
    private static final String FIND_ALL_ORDERED_QUERY = "SELECT t FROM {0} t ORDER BY t.{1}";
    private static final String FIND_BY_TEXT_ATTRIBUTE_QUERY = "SELECT t FROM {0} t WHERE t.{1} = :value ORDER BY t.{2}";

    private final Class<T> genericType;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    ExceptionGenerationUtils exceptionGenerationUtils;

    public AbstractRepository() {
        this.genericType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), AbstractRepository.class);
        this.logger = LoggerFactory.getLogger(this.genericType);
    }

    public T create(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    public T update(T entity) {
        entityManager.merge(entity);
        return entity;
    }

    public void remove(T entity) {
        entityManager.remove(entity);
    }

    public T findById(Long id) throws InstanceNotFoundException {
        try {
            T t = entityManager.find(genericType, id);
            if (t == null) {
                throw new NoResultException(Long.toString(id));
            }
            return t;
        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);
            throw exceptionGenerationUtils.toInstanceNotFoundException(id, genericType.getSimpleName(),
                    Constants.INSTANCE_NOT_FOUND_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        Query query = entityManager.createQuery(MessageFormat.format(FIND_ALL_QUERY,
                genericType.getSimpleName()));
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll(String orderColumn) {
        // Validate and sanitize order column to prevent injection
        if (!ValidationUtils.isValidOrderColumn(orderColumn)) {
            throw new IllegalArgumentException("Invalid column for ordering");
        }

        Query query = entityManager.createQuery(MessageFormat.format(FIND_ALL_ORDERED_QUERY,
                genericType.getSimpleName(), orderColumn));
        return (List<T>) query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<T> findByStringAttribute(String attribute, String value, String orderColumn) {
        // Validate and sanitize order column to prevent injection
        if (!ValidationUtils.isValidOrderColumn(attribute) || !ValidationUtils.isValidOrderColumn(orderColumn)) {
            throw new IllegalArgumentException("Invalid column for ordering");
        }

        Query query = entityManager.createQuery(MessageFormat.format(FIND_BY_TEXT_ATTRIBUTE_QUERY,
                genericType.getSimpleName(), attribute, orderColumn));
        query.setParameter("value", value); // Safely set the parameter value
        return query.getResultList();
    }


}
