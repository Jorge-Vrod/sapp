package es.storeapp.web.controller;

import es.storeapp.business.entities.User;
import es.storeapp.business.exceptions.AuthenticationException;
import es.storeapp.business.exceptions.DuplicatedResourceException;
import es.storeapp.business.exceptions.InstanceNotFoundException;
import es.storeapp.business.exceptions.ServiceException;
import es.storeapp.business.services.UserService;
import es.storeapp.business.utils.ValidationUtils;
import es.storeapp.common.Constants;
import es.storeapp.web.exceptions.ErrorHandlingUtils;
import es.storeapp.web.forms.LoginForm;
import es.storeapp.web.forms.ChangePasswordForm;
import es.storeapp.web.forms.ResetPasswordForm;
import es.storeapp.web.forms.UserProfileForm;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserService userService;
    
    @Autowired
    ErrorHandlingUtils errorHandlingUtils;
    
    @GetMapping(Constants.LOGIN_ENDPOINT)
    public String doGetLoginPage(Model model) {
        model.addAttribute(Constants.LOGIN_FORM, new LoginForm());
        return Constants.LOGIN_PAGE;
    }

    @GetMapping(Constants.LOGOUT_ENDPOINT)
    public String doLogout(HttpSession session, 
                           HttpServletResponse response,
                           @CookieValue(value = Constants.PERSISTENT_USER_COOKIE, required = false) String userInfo) {
        if (userInfo != null) {
            Cookie userCookie = new Cookie(Constants.PERSISTENT_USER_COOKIE, null);
            userCookie.setSecure(true);
            userCookie.setHttpOnly(true);
            userCookie.setMaxAge(0); // remove
            response.addCookie(userCookie);
        }
        if (session != null) {
            session.invalidate();
        }
        return Constants.SEND_REDIRECT + Constants.ROOT_ENDPOINT;
    }

    @GetMapping(Constants.REGISTRATION_ENDPOINT)
    public String doGetRegisterPage(Model model) {
        model.addAttribute(Constants.USER_PROFILE_FORM, new UserProfileForm());
        return Constants.USER_PROFILE_PAGE;
    }

    @GetMapping(Constants.USER_PROFILE_ENDPOINT)
    public String doGetProfilePage(@SessionAttribute(Constants.USER_SESSION) User user,
                                   Model model) {
        UserProfileForm form = new UserProfileForm(user.getName(),
                user.getEmail(), user.getAddress());
        model.addAttribute(Constants.USER_PROFILE_FORM, form);
        return Constants.USER_PROFILE_PAGE;
    }

    @GetMapping(Constants.CHANGE_PASSWORD_ENDPOINT)
    public String doGetChangePasswordPage(Model model, @SessionAttribute(Constants.USER_SESSION) User user) {
        ChangePasswordForm form = new ChangePasswordForm();
        model.addAttribute(Constants.PASSWORD_FORM, form);
        return Constants.PASSWORD_PAGE;
    }

    @GetMapping(Constants.SEND_EMAIL_ENDPOINT)
    public String doGetSendEmailPage(Model model) {
        return Constants.SEND_EMAIL_PAGE;
    }

    @GetMapping(Constants.RESET_PASSWORD_ENDPOINT)
    public String doGetResetPasswordPage(@RequestParam(value = Constants.TOKEN_PARAM) String token,
                                         @RequestParam(value = Constants.EMAIL_PARAM) String email,
                                         Model model) {
        ResetPasswordForm form = new ResetPasswordForm();
        form.setEmail(email);
        form.setToken(token);
        model.addAttribute(Constants.RESET_PASSWORD_FORM, form);
        return Constants.RESET_PASSWORD_PAGE;
    }
    @PostMapping(Constants.LOGIN_ENDPOINT)
    public String doLogin(@Valid @ModelAttribute LoginForm loginForm,
                          BindingResult result,
                          @RequestParam(value = Constants.NEXT_PAGE, required = false) String next,
                          HttpSession session,
                          HttpServletResponse response,
                          Locale locale,
                          Model model) {
        if (result.hasErrors()) {
            errorHandlingUtils.handleInvalidFormError(result,
                    Constants.AUTH_INVALID_USER_OR_PASSWORD_MESSAGE, model, locale);
            return Constants.LOGIN_PAGE;
        }

        User user;
        try {
            user = userService.login(loginForm.getEmail(), loginForm.getPassword());
            session.setAttribute(Constants.USER_SESSION, user);

            if (logger.isDebugEnabled()) {
                logger.debug("User {} logged in", user.getEmail());
            }

            // Redirect logic
            if (next != null && !next.trim().isEmpty()) {
                if (isLocalRedirect(next)) {
                    return Constants.SEND_REDIRECT + next;
                } else {
                    return Constants.SEND_REDIRECT + Constants.ROOT_ENDPOINT;
                }
            }

            return Constants.SEND_REDIRECT + Constants.ROOT_ENDPOINT;

        } catch (AuthenticationException ex) {
            logger.debug("User {} not logged in", loginForm.getEmail());
            return errorHandlingUtils.handleAuthenticationException(ex, loginForm.getEmail(),
                    Constants.LOGIN_PAGE, model, locale);
        }
    }


    /* Helper method to check if a URL is local to avoid open redirect */
    private boolean isLocalRedirect(String url) {
        return url.startsWith("/") && !url.startsWith("//") && !url.contains("://");
    }

    @PostMapping(Constants.REGISTRATION_ENDPOINT)
    public String doRegister(@Valid @ModelAttribute(Constants.USER_PROFILE_FORM) UserProfileForm userProfileForm,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             HttpSession session,
                             Locale locale,
                             Model model) {
        if (result.hasErrors()) {
            errorHandlingUtils.handleInvalidFormError(result,
                    Constants.REGISTRATION_INVALID_PARAMS_MESSAGE, model, locale);
            return Constants.USER_PROFILE_PAGE;
        }
        User user;
        try {
            String safeFileName = null;
            byte[] imageBytes = null;

            if (userProfileForm.getImage() != null && !userProfileForm.getImage().isEmpty()) {
                String originalFilename = FilenameUtils.getName(userProfileForm.getImage().getOriginalFilename());
                if (ValidationUtils.validateImageName(originalFilename)) {
                    safeFileName = UUID.randomUUID() + "_" + originalFilename;
                    imageBytes = userProfileForm.getImage().getBytes();
                } else {
                    throw new IllegalArgumentException("Invalid file name.");
                }
            }

            user = userService.create(userProfileForm.getName(), userProfileForm.getEmail(),
                    userProfileForm.getPassword(), userProfileForm.getAddress(), safeFileName, imageBytes);

            logger.debug("User {} with name {} registered", user.getEmail(), user.getName());
            session.setAttribute(Constants.USER_SESSION, user);
            redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, messageSource.getMessage(
                    Constants.REGISTRATION_SUCCESS_MESSAGE, new Object[]{user.getName()}, locale));

        } catch (DuplicatedResourceException ex) {
            return errorHandlingUtils.handleDuplicatedResourceException(ex, Constants.USER_PROFILE_PAGE, model, locale);
        } catch (Exception ex) {
            return errorHandlingUtils.handleUnexpectedException(ex, model);
        }
        return Constants.SEND_REDIRECT + Constants.ROOT_ENDPOINT;
    }


    @PostMapping(Constants.USER_PROFILE_ENDPOINT)
    public String doUpdateProfile(@Valid @ModelAttribute(Constants.USER_PROFILE_FORM) UserProfileForm userProfileForm,
                                  BindingResult result,
                                  @SessionAttribute(Constants.USER_SESSION) User user,            
                                  HttpSession session,
                                  Locale locale, 
                                  Model model) {
        if (result.hasErrors()) {
            errorHandlingUtils.handleInvalidFormError(result, 
                Constants.UPDATE_PROFILE_INVALID_PARAMS_MESSAGE, model, locale);
            return Constants.USER_PROFILE_PAGE;
        }
        User updatedUser;
        try {
            updatedUser = userService.update(user.getUserId(), userProfileForm.getName(), userProfileForm.getEmail(),
                    userProfileForm.getAddress(),
                    userProfileForm.getImage() != null ? userProfileForm.getImage().getOriginalFilename() : null,
                    userProfileForm.getImage() != null ? userProfileForm.getImage().getBytes() : null);
            
            if(logger.isDebugEnabled()) {
                logger.debug(MessageFormat.format("User {0} with name {1} updated", 
                        updatedUser.getEmail(), updatedUser.getName()));
            }
            
            session.setAttribute(Constants.USER_SESSION, updatedUser);
            model.addAttribute(Constants.SUCCESS_MESSAGE, messageSource.getMessage(
                    Constants.PROFILE_UPDATE_SUCCESS, new Object[]{}, locale));
        } catch (InstanceNotFoundException ex) {
            return errorHandlingUtils.handleInstanceNotFoundException(ex, model, locale);
        } catch (DuplicatedResourceException ex) {
            return errorHandlingUtils.handleDuplicatedResourceException(ex, Constants.ERROR_PAGE, model, locale);
        } catch (Exception ex) {
            return errorHandlingUtils.handleUnexpectedException(ex, model);
        }
        return Constants.USER_PROFILE_PAGE;
    }

    @PostMapping(Constants.CHANGE_PASSWORD_ENDPOINT)
    public String doChangePassword(@Valid @ModelAttribute(Constants.PASSWORD_FORM) ChangePasswordForm passwordForm,
                                   BindingResult result,
                                   @SessionAttribute(Constants.USER_SESSION) User user,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes,
                                   Locale locale, 
                                   Model model) {
        if (result.hasErrors()) {       
            errorHandlingUtils.handleInvalidFormError(result, 
                Constants.CHANGE_PASSWORD_INVALID_PARAMS_MESSAGE, model, locale);            
            return Constants.PASSWORD_PAGE;
        }
        User updatedUser;
        try {
            updatedUser = userService.changePassword(user.getUserId(), passwordForm.getOldPassword(), passwordForm.getPassword());
            session.setAttribute(Constants.USER_SESSION, updatedUser);
            redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, messageSource.getMessage(
                    Constants.PROFILE_UPDATE_SUCCESS, new Object[]{}, locale));
        } catch (InstanceNotFoundException ex) {
            return errorHandlingUtils.handleInstanceNotFoundException(ex, model, locale);
        } catch (AuthenticationException ex) {
            return errorHandlingUtils.handleAuthenticationException(ex, user.getEmail(), 
                    Constants.PASSWORD_PAGE, model, locale);
        }
        return Constants.SEND_REDIRECT + Constants.ROOT_ENDPOINT;
    }
    @GetMapping(Constants.USER_PROFILE_IMAGE_ENDPOINT)
    public ResponseEntity<byte[]> doGetProfileImage(@SessionAttribute(Constants.USER_SESSION) User user,
                                                    HttpServletResponse response,
                                                    Locale locale,
                                                    Model model) {
        try {
            String safeEmail = URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
            String safeImageName = URLEncoder.encode(user.getImage(), StandardCharsets.UTF_8);
            response.setHeader(Constants.CONTENT_TYPE_HEADER, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(Constants.CONTENT_DISPOSITION_HEADER,
                    MessageFormat.format(Constants.CONTENT_DISPOSITION_HEADER_VALUE, safeEmail, safeImageName));

            byte[] contents = userService.getImage(user.getUserId());
            if (contents == null) {
                String message = messageSource.getMessage(Constants.INVALID_PROFILE_IMAGE_MESSAGE, new Object[]{}, locale);
                model.addAttribute(Constants.MESSAGE, message);
                return new ResponseEntity<>(new byte[0], HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity<>(contents, HttpStatus.OK);
        } catch (InstanceNotFoundException ex) {
            errorHandlingUtils.handleInstanceNotFoundException(ex, model, locale);
            return new ResponseEntity<>(new byte[0], HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            errorHandlingUtils.handleUnexpectedException(ex, model);
            return new ResponseEntity<>(new byte[0], HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping(Constants.USER_PROFILE_IMAGE_REMOVE_ENDPOINT)
    public String doRemoveProfileImage(@SessionAttribute(Constants.USER_SESSION) User user,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes,
                                       Locale locale, 
                                       Model model) {

        User updatedUser;
        try {
            updatedUser = userService.removeImage(user.getUserId());
            session.setAttribute(Constants.USER_SESSION, updatedUser);
            redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, messageSource.getMessage(
                    Constants.PROFILE_UPDATE_SUCCESS, new Object[]{}, locale));
        } catch (InstanceNotFoundException ex) {
            return errorHandlingUtils.handleInstanceNotFoundException(ex, model, locale);
        } catch (ServiceException ex) {
            return errorHandlingUtils.handleUnexpectedException(ex, model);
        }
        return Constants.SEND_REDIRECT + Constants.USER_PROFILE_ENDPOINT;
    }

    @PostMapping(Constants.SEND_EMAIL_ENDPOINT)
    public String doSendEmail(@RequestParam(Constants.EMAIL_PARAM) String email,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request,
                              Locale locale,
                              Model model) {
        try {
            if (email == null || email.trim().isEmpty()) {
                String message = messageSource.getMessage(Constants.INVALID_EMAIL_MESSAGE, new Object[]{}, locale);
                model.addAttribute(Constants.ERROR_MESSAGE, message);
                return Constants.SEND_EMAIL_PAGE;
            }

            // Sanitize email input to prevent SQL injection
            if (!ValidationUtils.validateEmail(email)) {
                String message = messageSource.getMessage(Constants.INVALID_EMAIL_MESSAGE, new Object[]{}, locale);
                model.addAttribute(Constants.ERROR_MESSAGE, message);
                return Constants.SEND_EMAIL_PAGE;
            }

            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int portNumber = request.getServerPort();
            String contextPath = request.getContextPath();

            // Safe usage: pass sanitized or validated email into the query
            userService.sendResetPasswordEmail(email, MessageFormat.format(Constants.URL_FORMAT, scheme,
                    serverName, Integer.toString(portNumber), contextPath, Constants.RESET_PASSWORD_ENDPOINT), locale);

            redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, messageSource.getMessage(
                    Constants.MAIL_SUCCESS_MESSAGE, new Object[] { email }, locale));

        } catch (AuthenticationException ex) {
            return errorHandlingUtils.handleAuthenticationException(ex, email,
                    Constants.SEND_EMAIL_PAGE, model, locale);
        } catch (Exception ex) {
            return errorHandlingUtils.handleUnexpectedException(ex, model);
        }
        return Constants.SEND_REDIRECT + Constants.SEND_EMAIL_ENDPOINT;
    }


    
    @PostMapping(Constants.RESET_PASSWORD_ENDPOINT)
    public String doResetPassword(@Valid @ModelAttribute(Constants.RESET_PASSWORD_FORM) ResetPasswordForm passwordForm,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes,
                                  HttpServletRequest request,
                                  Locale locale, 
                                  Model model) {
        try {
            if (result.hasErrors()) {
                errorHandlingUtils.handleInvalidFormError(result, 
                    Constants.RESET_PASSWORD_INVALID_PARAMS_MESSAGE, model, locale);
                return Constants.RESET_PASSWORD_PAGE;
            }
            userService.changePassword(passwordForm.getEmail(), passwordForm.getPassword(), passwordForm.getToken());
            redirectAttributes.addFlashAttribute(Constants.SUCCESS_MESSAGE, messageSource.getMessage(
                    Constants.CHANGE_PASSWORD_SUCCESS, new Object[0], locale));
        } catch (AuthenticationException ex) {
            return errorHandlingUtils.handleAuthenticationException(ex, passwordForm.getEmail(), 
                    Constants.SEND_EMAIL_PAGE, model, locale);
        } catch (Exception ex) {
            return errorHandlingUtils.handleUnexpectedException(ex, model);
        }
        return Constants.SEND_REDIRECT + Constants.ROOT_ENDPOINT;
    }

}
