package es.storeapp.web.forms;

import jakarta.validation.constraints.*;

public class ResetPasswordForm {

    // VULN: CWE 20 - Validación de datos de entrada
    
    @NotNull(message = "El token no puede ser nulo")
    @Size(min = 10, max = 100, message = "El token debe tener entre 10 y 100 caracteres")
    private String token;
    
    @NotNull(message = "Email cannot be null")
    @Size(min = 1, message = "Email cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Invalid email format")
    private String email;
    
    @NotNull(message = "Password cannot be null")
    @Size(min = 4, message = "Password cannot be empty and must have at least 4 characters")
    private String password;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

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

}
