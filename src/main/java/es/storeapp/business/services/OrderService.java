package es.storeapp.business.services;

import es.storeapp.business.entities.CreditCard;
import es.storeapp.business.entities.Order;
import es.storeapp.business.entities.OrderLine;
import es.storeapp.business.entities.OrderState;
import es.storeapp.business.entities.Product;
import es.storeapp.business.entities.User;
import es.storeapp.business.exceptions.InstanceNotFoundException;
import es.storeapp.business.exceptions.InvalidStateException;
import es.storeapp.business.repositories.OrderLineRepository;
import es.storeapp.business.repositories.OrderRepository;
import es.storeapp.business.repositories.ProductRepository;
import es.storeapp.business.repositories.UserRepository;
import es.storeapp.business.utils.ExceptionGenerationUtils;
import es.storeapp.business.utils.ValidationConstants;
import es.storeapp.business.utils.ValidationUtils;
import es.storeapp.common.Constants;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderLineRepository orderLineRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    ExceptionGenerationUtils exceptionGenerationUtils;

    @Transactional()
    public Order create(User user, String name, String address, Integer price, List<Long> products)
            throws InstanceNotFoundException {

        if (user == null || !ValidationUtils.validateString(name, ValidationConstants.MAX_NAME_SIZE) || !ValidationUtils.validatePrice(price)
                || !ValidationUtils.validateProductList(products)) {
            throw new IllegalArgumentException("Invalid input for creating an order.");
        }

        Order order = new Order();
        order.setName(name);
        order.setUser(user);
        order.setAddress(StringUtils.defaultIfBlank(address, user.getAddress()));
        order.setPrice(price);
        order.setState(OrderState.PENDING);
        order.setTimestamp(System.currentTimeMillis());
        orderRepository.create(order);

        for (Long productId : products) {
            Product product = productRepository.findById(productId);
            if (product == null) {
                throw new InstanceNotFoundException(productId, Constants.INSTANCE_NOT_FOUND_MESSAGE, "Product not found with ID: " + productId);
            }
            product.setSales(product.getSales() + 1);

            OrderLine orderLine = new OrderLine();
            orderLine.setPrice(product.getPrice());
            orderLine.setProduct(product);
            orderLine.setOrder(order);
            orderLineRepository.create(orderLine);
        }
        return orderRepository.findById(order.getOrderId());
    }


    @Transactional()
    public Order pay(User user, Long orderId, String creditCard, Integer cvv,
                     Integer expirationMonth, Integer expirationYear, Boolean setAsDefault)
            throws InstanceNotFoundException, InvalidStateException {

        if (user == null || orderId == null || !ValidationUtils.validateCreditCard(creditCard)
                || !ValidationUtils.validateCVV(cvv) || !ValidationUtils.validateExpirationMonth(expirationMonth)
                || !ValidationUtils.validateExpirationYear(expirationYear)) {
            throw new IllegalArgumentException("Invalid payment details.");
        }

        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new InstanceNotFoundException(orderId, Constants.INSTANCE_NOT_FOUND_MESSAGE, "Order not found with ID: " + orderId);
        }
        if (order.getState() != OrderState.PENDING) {
            logger.warn("Trying to pay an order in an invalid state: {}", order);
            throw exceptionGenerationUtils.toInvalidStateException(Constants.INVALID_STATE_EXCEPTION_MESSAGE);
        }

        order.setState(OrderState.COMPLETED);

        if (Boolean.TRUE.equals(setAsDefault)) {
            CreditCard card = new CreditCard();
            card.setCard(creditCard);
            card.setCvv(cvv);
            card.setExpirationMonth(expirationMonth);
            card.setExpirationYear(expirationYear);
            user.setCard(card);
            userRepository.update(user);
        }
        return orderRepository.update(order);
    }


    @Transactional()
    public Order cancel(User user, Long orderId)
            throws InstanceNotFoundException, InvalidStateException {

        if (user == null || orderId == null) {
            throw new IllegalArgumentException("User and order ID must not be null.");
        }

        Order order = orderRepository.findById(orderId);
        if (order == null) {
            throw new InstanceNotFoundException(orderId, Constants.INSTANCE_NOT_FOUND_MESSAGE, "Order not found with ID: " + orderId);
        }
        if (order.getState() != OrderState.PENDING) {
            throw exceptionGenerationUtils.toInvalidStateException(Constants.INVALID_STATE_EXCEPTION_MESSAGE);
        }

        order.setState(OrderState.CANCELLED);
        return orderRepository.update(order);
    }

    
    @Transactional(readOnly = true)
    public List<Order> findByUserById(Long userId) throws InstanceNotFoundException {
        User user = userRepository.findById(userId);
        if(logger.isDebugEnabled()) {
            logger.debug(MessageFormat.format("Searching the orders of the user {0}", user.getEmail()));
        }
        return orderRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Order findById(Long id) throws InstanceNotFoundException {
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean findIfUserBuyProduct(Long userId, Long productId) throws InstanceNotFoundException {
        User user = userRepository.findById(userId);
        if(logger.isDebugEnabled()) {
            logger.debug(MessageFormat.format("Checking if user {0} buy the product {1}", 
                user.getEmail(), productId));
        }
        return orderLineRepository.findIfUserBuyProduct(userId, productId);
    }
    
}
