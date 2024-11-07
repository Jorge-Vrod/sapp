package es.storeapp.business.utils;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class InputSanitizer {
    private static final PolicyFactory POLICY = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    public static String sanitize(String input) {
        return POLICY.sanitize(input);
    }
}