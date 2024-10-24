package es.storeapp.business.services;

import es.storeapp.business.entities.Category;
import es.storeapp.business.entities.Comment;
import es.storeapp.business.entities.Product;
import es.storeapp.business.entities.User;
import es.storeapp.business.exceptions.InstanceNotFoundException;
import es.storeapp.business.repositories.CategoryRepository;
import es.storeapp.business.repositories.CommentRepository;
import es.storeapp.business.repositories.ProductRepository;
import es.storeapp.business.utils.ExceptionGenerationUtils;
import es.storeapp.common.ConfigurationParameters;
import es.storeapp.common.Constants;
import java.text.MessageFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, CommentRepository rateRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.rateRepository = rateRepository;
    }

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final CommentRepository rateRepository;


    @Transactional(readOnly = true)
    public List<Category> findHighlightedCategories() {
        return categoryRepository.findHighlighted();
    }

    @Transactional(readOnly = true)
    public List<Product> findAllProducts() {
        return productRepository.findAll(Constants.PRICE_FIELD);
    }

    @Transactional(readOnly = true)
    public List<Product> findProducts(String category) {
        if (category == null || category.isEmpty()) {
            return productRepository.findAll(Constants.PRICE_FIELD);
        } else {
            return productRepository.findByCategory(category, Constants.PRICE_FIELD);
        }
    }

    @Transactional(readOnly = true)
    public Product findProductById(Long id) throws InstanceNotFoundException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(Constants.INVALID_PRODUCT_ID);
        }
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Comment findCommentByUserAndProduct(User user, Long productId) throws InstanceNotFoundException {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException(Constants.INVALID_PRODUCT_ID);
        }
        Product product = productRepository.findById(productId);
        logger.debug("Searching if the user with ID {} has commented on product {}", user.getUserId(), product.getName());
        return rateRepository.findByUserAndProduct(user.getUserId(), productId);
    }

    @Transactional
    public Comment comment(User user, Long productId, String text, Integer rating) throws InstanceNotFoundException {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException(Constants.INVALID_PRODUCT_ID);
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Invalid rating value");
        }
        Product product = productRepository.findById(productId);
        Comment comment = rateRepository.findByUserAndProduct(user.getUserId(), productId);
        if (comment != null) {
            logger.debug("{} has modified their comment for product {}", user.getName(), product.getName());
            product.setTotalScore(product.getTotalScore() - comment.getRating() + rating);
            comment.setRating(rating);
            comment.setText(text);
        } else {
            logger.debug("{} created a comment for product {}", user.getName(), product.getName());
            comment = new Comment(user, product, text, rating);
            product.setTotalComments(product.getTotalComments() + 1);
            product.setTotalScore(product.getTotalScore() + rating);
        }
        productRepository.update(product);
        return rateRepository.create(comment);
    }
}