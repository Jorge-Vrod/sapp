package es.storeapp.web.forms;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

public class LoginForm {

    // VULN: CWE 20 - Validación de datos de entrada
    
    @NotNull(message = "Email cannot be null")
    @Size(min = 1, message = "Email cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Invalid email format")
    private String email;

    @NotNull(message = "Password cannot be null")
    @Size(min = 4, message = "Password cannot be empty and must have at least 4 characters")
    private String password;

    @AssertTrue(message = "Remember Me must be true or false")
    private Boolean rememberMe;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
