package es.storeapp.web.controller;

import es.storeapp.business.entities.CreditCard;
import es.storeapp.business.entities.Order;
import es.storeapp.business.entities.Product;
import es.storeapp.business.entities.User;
import es.storeapp.business.exceptions.InstanceNotFoundException;
import es.storeapp.business.exceptions.InvalidStateException;
import es.storeapp.business.services.OrderService;
import es.storeapp.common.Constants;
import es.storeapp.web.exceptions.ErrorHandlingUtils;
import es.storeapp.web.forms.OrderForm;
import es.storeapp.web.forms.PaymentForm;
import es.storeapp.web.session.ShoppingCart;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    ErrorHandlingUtils errorHandlingUtils;

    @GetMapping(Constants.ORDERS_ENDPOINT)
    public String doGetOrdersPage(@SessionAttribute(Constants.USER_SESSION) User user,
                                  Model model, Locale locale) {
        try {
            model.addAttribute(Constants.ORDERS, orderService.findByUserById(user.getUserId()));
        } catch (InstanceNotFoundException ex) {
            return errorHandlingUtils.handleInstanceNotFoundException(ex, model, locale);
        }
        return Constants.ORDERS_PAGE;
    }

    @GetMapping(Constants.ORDER_ENDPOINT)
    public String doGetOrderPage(@SessionAttribute(Constants.USER_SESSION) User user,
                                 @PathVariable Long id,
                                 Model model,
                                 Locale locale) {
        if (id == null || id <= 0) {
            model.addAttribute(Constants.ERROR_MESSAGE, messageSource.getMessage(Constants.INVALID_ORDER_ID, null, locale));
            return Constants.ERROR_PAGE;  // Redirect to a generic error page
        }

        try {
            Order order = orderService.findById(id);
            if (!order.getUser().getUserId().equals(user.getUserId())) {
                model.addAttribute(Constants.ERROR_MESSAGE, messageSource.getMessage(Constants.INVALID_ORDER_USER, null, locale));
                return Constants.ERROR_PAGE;  // Unauthorized access error page
            }
            model.addAttribute(Constants.ORDER, order);
        } catch (InstanceNotFoundException ex) {
            return errorHandlingUtils.handleInstanceNotFoundException(ex, model, locale);
        } catch (Exception ex) {
            logger.error("Error retrieving order page for order ID {}: {}", id, ex.getMessage());
            model.addAttribute(Constants.ERROR_MESSAGE, messageSource.getMessage(Constants.GENERIC_ERROR, null, locale));
            return Constants.ERROR_PAGE;
        }
        return Constants.ORDER_PAGE;
    }

    @GetMapping(Constants.ORDER_PAYMENT_ENDPOINT)
    public String doGetPaymentPage(@SessionAttribute(Constants.USER_SESSION) User user, 
                                   @PathVariable() Long id,
                                   Model model, 
                                   Locale locale) {
        try {
            model.addAttribute(Constants.ORDER, orderService.findById(id));
        } catch (InstanceNotFoundException ex) {
            return errorHandlingUtils.handleInstanceNotFoundException(ex, model, locale);
        }
        PaymentForm paymentForm = new PaymentForm();
        model.addAttribute(Constants.PAYMENT_FORM, paymentForm);
        return Constants.ORDER_PAYMENT_PAGE;
    }
    
    @GetMapping(value = {Constants.ORDER_CONFIRM_ENDPOINT})
    public String doCompleteOrder(@SessionAttribute(Constants.SHOPPING_CART_SESSION) ShoppingCart shoppingCart,
                                  Model model, 
                                  Locale locale) {

        OrderForm orderForm = new OrderForm();
        orderForm.setPrice(shoppingCart.getTotalPrice());
        List<Product> products = new ArrayList<>(shoppingCart.getProducts());
        if (products.size() == 1) {
            String orderName = messageSource.getMessage(Constants.ORDER_SINGLE_PRODUCT_AUTOGENERATED_NAME_MESSAGE,
                    new Object[]{products.getFirst().getName(),
                        products.getFirst().getCategory().getName()}, locale);
            orderForm.setName(orderName);
        } else if (products.size() > 1) {
            String orderName = messageSource.getMessage(Constants.ORDER_AUTOGENERATED_NAME,
                    new Object[]{products.getFirst().getName(),
                        products.getFirst().getCategory().getName(), products.size() - 1}, locale);
            orderForm.setName(orderName);
        }

        if(logger.isDebugEnabled()) {
            logger.debug(MessageFormat.format("Go to complete order page {0}", orderForm.getName()));
        }
        model.addAttribute(Constants.ORDER_FORM, orderForm);
        model.addAttribute(Constants.PRODUCTS, products);
        return Constants.ORDER_COMPLETE_PAGE;
    }

    @PostMapping(Constants.ORDERS_ENDPOINT)
    public String doCreateOrder(@Valid @ModelAttribute(Constants.ORDER_FORM) OrderForm orderForm,
                                BindingResult result,
                                @RequestParam(value = Constants.PRODUCTS_ARRAY) Long[] products,
                                @SessionAttribute(Constants.USER_SESSION) User user,
                                @SessionAttribute(Constants.SHOPPING_CART_SESSION) ShoppingCart shoppingCart,
                                RedirectAttributes redirectAttributes,
                                Locale locale, Model model) {
        if (result.hasErrors()) {
            return errorHandlingUtils.handleInvalidFormError(result,
                    Constants.CREATE_ORDER_INVALID_PARAMS_MESSAGE, model, locale);
        }

        if (orderForm.getAddress() == null || orderForm.getAddress().trim().isEmpty()) {
            orderForm.setAddress(user.getAddress());
        }

        if (products == null || products.length == 0) {
            model.addAttribute(Constants.ERROR_MESSAGE, messageSource.getMessage(Constants.INVALID_PRODUCT_LIST, null, locale));
            return Constants.ERROR_PAGE;
        }

        Order order;
        try {
            order = orderService.create(user, orderForm.getName(), orderForm.getAddress(), orderForm.getPrice(),
                    Arrays.asList(products));
        } catch (InstanceNotFoundException ex) {
            return errorHandlingUtils.handleInstanceNotFoundException(ex, model, locale);
        } catch (Exception ex) {
            logger.error("Error creating order for user ID {}: {}", user.getUserId(), ex.getMessage());
            model.addAttribute(Constants.ERROR_MESSAGE, messageSource.getMessage(Constants.GENERIC_ERROR, null, locale));
            return Constants.ERROR_PAGE;
        }

        String message = messageSource.getMessage(Constants.ORDER_CREATED_MESSAGE, new Object[]{orderForm.getName()}, locale);
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, message);
        shoppingCart.clear();

        if (orderForm.getPayNow() != null && orderForm.getPayNow()) {
            return Constants.SEND_REDIRECT + MessageFormat.format(Constants.ORDER_PAYMENT_ENDPOINT_TEMPLATE, order.getOrderId());
        }
        return Constants.SEND_REDIRECT + Constants.ORDERS_ENDPOINT;
    }


    @PostMapping(Constants.ORDER_PAYMENT_ENDPOINT)
    public String doPayOrder(@Valid @ModelAttribute(Constants.ORDER_FORM) PaymentForm paymentForm,
                             BindingResult result,
                             @PathVariable Long id,
                             @SessionAttribute(Constants.USER_SESSION) User user,
                             HttpSession session,
                             RedirectAttributes redirectAttributes,
                             Locale locale,
                             Model model) throws InvalidStateException {
        if (result.hasErrors()) {
            return errorHandlingUtils.handleInvalidFormError(result,
                    Constants.PAY_ORDER_INVALID_PARAMS_MESSAGE, model, locale);
        }

        if (id == null || id <= 0) {
            model.addAttribute(Constants.ERROR_MESSAGE, messageSource.getMessage(Constants.INVALID_ORDER_ID, null, locale));
            return Constants.ERROR_PAGE;
        }

        Order order;
        try {
            if (paymentForm.getDefaultCreditCard() != null && paymentForm.getDefaultCreditCard()) {
                CreditCard card = user.getCard();
                if (card == null) {
                    model.addAttribute(Constants.ERROR_MESSAGE, messageSource.getMessage(Constants.NO_SAVED_CARD_MESSAGE, null, locale));
                    return Constants.ERROR_PAGE;
                }
                order = orderService.pay(user, id, card.getCard(), card.getCvv(), card.getExpirationMonth(),
                        card.getExpirationYear(), false);
            } else {
                order = orderService.pay(user, id, paymentForm.getCreditCard(), paymentForm.getCvv(),
                        paymentForm.getExpirationMonth(), paymentForm.getExpirationYear(), paymentForm.getSave());
                if (Boolean.TRUE.equals(paymentForm.getSave())) {
                    session.setAttribute(Constants.USER_SESSION, user);  // Update session with new card info if saved
                }
            }
        } catch (InstanceNotFoundException ex) {
            return errorHandlingUtils.handleInstanceNotFoundException(ex, model, locale);
        } catch (Exception ex) {
            logger.error("Error processing payment for order ID {}: {}", id, ex.getMessage());
            model.addAttribute(Constants.ERROR_MESSAGE, messageSource.getMessage(Constants.GENERIC_ERROR, null, locale));
            return Constants.ERROR_PAGE;
        }

        String message = messageSource.getMessage(Constants.ORDER_PAYMENT_COMPLETE_MESSAGE, null, locale);
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, message);
        redirectAttributes.addFlashAttribute(Constants.ORDER, order);
        return Constants.SEND_REDIRECT + MessageFormat.format(Constants.ORDER_ENDPOINT_TEMPLATE, order.getOrderId());
    }

    
    @PostMapping(Constants.ORDER_CANCEL_ENDPOINT)
    public String doCancelOrder(@PathVariable() Long id, 
                                @SessionAttribute(Constants.USER_SESSION) User user, 
                                RedirectAttributes redirectAttributes,
                                Locale locale, 
                                Model model) throws InvalidStateException {

        if (id == null || id <= 0) {
            model.addAttribute(Constants.ERROR_MESSAGE, messageSource.getMessage(Constants.INVALID_ORDER_ID, null, locale));
            return Constants.ERROR_PAGE;
        }

        Order order;
        try {
            order = orderService.cancel(user, id);
        } catch (InstanceNotFoundException ex) {
            return errorHandlingUtils.handleInstanceNotFoundException(ex, model, locale);
        }
        String message = messageSource.getMessage(Constants.ORDER_CANCEL_COMPLETE_MESSAGE, new Object[0], locale);
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, message);
        redirectAttributes.addFlashAttribute(Constants.ORDER, order);
        return Constants.SEND_REDIRECT + MessageFormat.format(Constants.ORDER_ENDPOINT_TEMPLATE, order.getOrderId());
    }
    
}
