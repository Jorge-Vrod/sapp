package es.storeapp.business.utils;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class ValidationUtils {

    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("^[0-9]{16}$");  // Example: 16-digit numbers only
    private static final Pattern CVV_PATTERN = Pattern.compile("^[0-9]{3,4}$");  // CVV 3-4 digits
    private static final int MIN_EXPIRATION_YEAR = 2023;
    private static final int MAX_EXPIRATION_YEAR = 2050;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern IMAGE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]+\\.(jpg|jpeg|png)$");
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_IMAGE_NAME_LENGTH = 50;
    private static final int MAX_ADDRESS_LENGTH = 100;

    /* Helper method to validate column names to prevent injection in orderColumn */
    public static boolean isValidOrderColumn(String column) {
        // Replace with actual valid column names
        List<String> validColumns = Arrays.asList("price");
        return validColumns.contains(column);
    }

    /**
     * Validates image file name (only alphanumeric, hyphens, and underscores allowed, with valid extensions).
     */
    public static boolean validateImageName(String imageName) {
        return imageName != null && IMAGE_NAME_PATTERN.matcher(imageName).matches() && imageName.length() <= MAX_IMAGE_NAME_LENGTH;
    }

    public static boolean validateEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean validatePassword(String password) {
        return password != null;
    }

    public static boolean validateName(String name) {
        return StringUtils.isNotBlank(name) && name.length() <= MAX_NAME_LENGTH;
    }

    public static boolean validateAddress(String address) {
        return StringUtils.isBlank(address) || address.length() <= MAX_ADDRESS_LENGTH;
    }

    public static boolean validateImage(String image, byte[] imageContents) {
        return image != null && !image.trim().isEmpty() && imageContents != null;
    }

    /**
     * Validates that a string is non-null, non-empty, and does not exceed max length.
     */
    public static boolean validateString(String input, int maxLength) {
        return StringUtils.isNotBlank(input) && input.length() <= maxLength;
    }

    /**
     * Validates credit card format.
     */
    public static boolean validateCreditCard(String creditCard) {
        return creditCard != null && CREDIT_CARD_PATTERN.matcher(creditCard).matches();
    }

    /**
     * Validates CVV format.
     */
    public static boolean validateCVV(Integer cvv) {
        return cvv != null && CVV_PATTERN.matcher(cvv.toString()).matches();
    }

    /**
     * Validates expiration month (1 to 12).
     */
    public static boolean validateExpirationMonth(Integer month) {
        return month != null && month >= 1 && month <= 12;
    }

    /**
     * Validates expiration year (current year to a max year).
     */
    public static boolean validateExpirationYear(Integer year) {
        return year != null && year >= MIN_EXPIRATION_YEAR && year <= MAX_EXPIRATION_YEAR;
    }

    /**
     * Validates price is within acceptable range.
     */
    public static boolean validatePrice(Integer price) {
        return price != null && price > 0 && price <= 1_000_000;  // Example max limit
    }

    /**
     * Validates the list of product IDs.
     */
    public static boolean validateProductList(List<Long> products) {
        return products != null && !products.isEmpty() && products.stream().allMatch(id -> id != null && id > 0);
    }

    /**
     * Validates product ID (non-null, positive).
     */
    public static boolean validateProductId(Long id) {
        return id != null && id > 0;
    }

    /**
     * Validates category name (non-null, non-empty, within max length).
     */
    public static boolean validateCategory(String category) {
        return category != null && !category.isEmpty() && category.length() <= 50;
    }

    /**
     * Validates rating (within range 1 to 5).
     */
    public static boolean validateRating(Integer rating) {
        return rating != null && rating >= 1 && rating <= 5;
    }

    /**
     * Validates text content (non-null, non-empty).
     */
    public static boolean validateText(String text, int maxLength) {
        return text != null && !text.trim().isEmpty() && text.length() <= maxLength;
    }
}
