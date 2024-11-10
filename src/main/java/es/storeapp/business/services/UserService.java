package es.storeapp.business.services;

import es.storeapp.business.entities.User;
import es.storeapp.business.exceptions.AuthenticationException;
import es.storeapp.business.exceptions.DuplicatedResourceException;
import es.storeapp.business.exceptions.InstanceNotFoundException;
import es.storeapp.business.exceptions.ServiceException;
import es.storeapp.business.repositories.UserRepository;
import es.storeapp.business.utils.ExceptionGenerationUtils;
import es.storeapp.business.utils.ValidationUtils;
import es.storeapp.common.ConfigurationParameters;
import es.storeapp.common.Constants;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.mail.HtmlEmail;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    @Autowired
    ConfigurationParameters configurationParameters;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    ExceptionGenerationUtils exceptionGenerationUtils;

    private File resourcesDir;

    @PostConstruct
    public void init() {
        resourcesDir = new File(configurationParameters.getResources());
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public User login(String email, String clearPassword) throws AuthenticationException {
        if (!ValidationUtils.validateEmail(email) || !ValidationUtils.validatePassword(clearPassword)) {
            throw new IllegalArgumentException("Invalid email or password format.");
        }
        if (!userRepository.existsUser(email)) {
            throw exceptionGenerationUtils.toAuthenticationException(Constants.AUTH_INVALID_USER_MESSAGE, email);
        }
        User user = userRepository.findByEmail(email);
        if (user == null || !passwordEncoder.matches(clearPassword, user.getPassword())) {
            throw exceptionGenerationUtils.toAuthenticationException(Constants.AUTH_INVALID_PASSWORD_MESSAGE, email);
        }
        return user;
    }



    @Transactional
    public void sendResetPasswordEmail(String email, String url, Locale locale) throws AuthenticationException, ServiceException {
        if (!ValidationUtils.validateEmail(email)) {
            throw new IllegalArgumentException("Invalid email format.");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw exceptionGenerationUtils.toAuthenticationException(Constants.AUTH_INVALID_USER_MESSAGE, email);
        }

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        userRepository.update(user);  // Save the token before sending email

        try {
            HtmlEmail htmlEmail = new HtmlEmail();
            htmlEmail.setHostName(configurationParameters.getMailHost());
            htmlEmail.setSmtpPort(configurationParameters.getMailPort());
            htmlEmail.setAuthentication(configurationParameters.getMailUserName(), configurationParameters.getMailPassword());
            htmlEmail.setSSLOnConnect(configurationParameters.getMailSslEnable() != null && configurationParameters.getMailSslEnable());
            htmlEmail.setSSLCheckServerIdentity(true);
            if (Boolean.TRUE.equals(configurationParameters.getMailStartTlsEnable())) {
                htmlEmail.setStartTLSEnabled(true);
                htmlEmail.setStartTLSRequired(true);
            }

            htmlEmail.addTo(email, user.getName());
            htmlEmail.setFrom(configurationParameters.getMailFrom());
            htmlEmail.setSubject(messageSource.getMessage(Constants.MAIL_SUBJECT_MESSAGE, new Object[]{user.getName()}, locale));

            String link = url + Constants.PARAMS + Constants.TOKEN_PARAM + "=" + token + "&" + Constants.EMAIL_PARAM + "=" + email;
            htmlEmail.setHtmlMsg(messageSource.getMessage(Constants.MAIL_TEMPLATE_MESSAGE, new Object[]{user.getName(), link}, locale));
            htmlEmail.setTextMsg(messageSource.getMessage(Constants.MAIL_HTML_NOT_SUPPORTED_MESSAGE, new Object[0], locale));

            htmlEmail.send();
        } catch (Exception ex) {
            logger.error("Failed to send reset password email: {}", ex.getMessage());
            throw new ServiceException("Failed to send reset password email.");
        }
    }


    @Transactional
    public User create(String name, String email, String rawPassword, String address, String image, byte[] imageContents)
            throws DuplicatedResourceException, ServiceException {

        if (!ValidationUtils.validateName(name) || !ValidationUtils.validateEmail(email)
                || !ValidationUtils.validatePassword(rawPassword) || !ValidationUtils.validateAddress(address)) {
            throw new IllegalArgumentException("Invalid user data.");
        }
        if (userRepository.findByEmail(email) != null) {
            throw exceptionGenerationUtils.toDuplicatedResourceException(Constants.EMAIL_FIELD, email, Constants.DUPLICATED_INSTANCE_MESSAGE);
        }

        // VULN: Utiliza PasswordEncoder (que debe ser BCryptPasswordEncoder)
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = userRepository.create(new User(name, email, encodedPassword, address, image));

        if (ValidationUtils.validateImage(image, imageContents)) {
            saveProfileImage(user.getUserId(), image, imageContents);
        }
        return user;
    }



    @Transactional
    public User update(Long id, String name, String email, String address, String image, byte[] imageContents)
            throws DuplicatedResourceException, InstanceNotFoundException, ServiceException {
        User user = userRepository.findById(id);
        User emailUser = userRepository.findByEmail(email);
        if (emailUser != null && !Objects.equals(emailUser.getUserId(), user.getUserId())) {
            throw exceptionGenerationUtils.toDuplicatedResourceException(Constants.EMAIL_FIELD, email,
                    Constants.DUPLICATED_INSTANCE_MESSAGE);
        }
        user.setName(name);
        user.setEmail(email);
        user.setAddress(address);
        if (image != null && !image.trim().isEmpty() && imageContents != null) {
            try {
                deleteProfileImage(id, user.getImage());
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
            saveProfileImage(id, image, imageContents);
            user.setImage(image);
        }
        return userRepository.update(user);
    }

    @Transactional
    public User changePassword(Long id, String oldPassword, String newPassword)
            throws InstanceNotFoundException, AuthenticationException {

        if (!ValidationUtils.validatePassword(oldPassword) || !ValidationUtils.validatePassword(newPassword)) {
            throw new IllegalArgumentException("Invalid password format.");
        }

        User user = userRepository.findById(id);
        if (user == null) {
            throw exceptionGenerationUtils.toAuthenticationException(Constants.AUTH_INVALID_USER_MESSAGE, id.toString());
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw exceptionGenerationUtils.toAuthenticationException(Constants.AUTH_INVALID_PASSWORD_MESSAGE, id.toString());
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.update(user);
    }

    @Transactional
    public User changePassword(String email, String newPassword, String token) throws AuthenticationException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw exceptionGenerationUtils.toAuthenticationException(Constants.AUTH_INVALID_USER_MESSAGE, email);
        }
        if (user.getResetPasswordToken() == null || !user.getResetPasswordToken().equals(token)) {
            throw exceptionGenerationUtils.toAuthenticationException(Constants.AUTH_INVALID_TOKEN_MESSAGE, email);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        return userRepository.update(user);
    }

    @Transactional
    public User removeImage(Long id) throws InstanceNotFoundException, ServiceException {
        User user = userRepository.findById(id);
        deleteProfileImage(id, user.getImage());
        user.setImage(null);
        return userRepository.update(user);
    }

    @Transactional
    public byte[] getImage(Long id) throws InstanceNotFoundException {
        User user = userRepository.findById(id);
        try {
            return getProfileImage(id, user.getImage());
        } catch (ServiceException ex) {
            logger.error(ex.getMessage(), ex);
            return new byte[0];
        }
    }

    private synchronized void saveProfileImage(Long id, String image, byte[] imageContents) throws ServiceException {
        if (!ValidationUtils.validateImageName(image) || imageContents == null || imageContents.length > 5 * 1024 * 1024) {  // Limit to 5MB
            throw new IllegalArgumentException("Invalid image name or size exceeds limit.");
        }

        File userDir = new File(resourcesDir, id.toString());
        if (!userDir.exists() && !userDir.mkdirs()) {
            throw new ServiceException("Failed to create user directory for image storage.");
        }

        File profilePicture = new File(userDir, image);
        try (FileOutputStream outputStream = new FileOutputStream(profilePicture)) {
            IOUtils.copy(new ByteArrayInputStream(imageContents), outputStream);

            // Set permissions to restrict access (only the application should have access)
            profilePicture.setReadable(true, true);
            profilePicture.setWritable(true, true);
        } catch (IOException e) {
            logger.error("Error saving profile image for user {}: {}", id, e.getMessage());
            throw new ServiceException("Failed to save profile image.");
        }
    }


    private synchronized void deleteProfileImage(Long id, String image) throws ServiceException {
        if (!ValidationUtils.validateImageName(image)) {
            throw new IllegalArgumentException("Invalid image name.");
        }

        File userDir = new File(resourcesDir, id.toString());
        File profilePicture = new File(userDir, image);

        if (profilePicture.exists()) {
            try {
                Files.delete(profilePicture.toPath());
            } catch (IOException e) {
                logger.error("Error deleting profile image for user {}: {}", id, e.getMessage());
                throw new ServiceException("Failed to delete profile image.");
            }
        } else {
            logger.warn("Profile image for user {} not found: {}", id, image);
        }
    }


    private byte[] getProfileImage(Long id, String image) throws ServiceException {
        if (!ValidationUtils.validateImageName(image)) {
            throw new IllegalArgumentException("Invalid image name.");
        }

        File userDir = new File(resourcesDir, id.toString());
        File profilePicture = new File(userDir, image);

        if (!profilePicture.exists()) {
            logger.warn("Requested profile image for user {} not found: {}", id, image);
            return new byte[0];
        }

        try (FileInputStream input = new FileInputStream(profilePicture)) {
            return IOUtils.toByteArray(input);
        } catch (IOException e) {
            logger.error("Error retrieving profile image for user {}: {}", id, e.getMessage());
            throw new ServiceException("Failed to retrieve profile image.");
        }
    }


}
