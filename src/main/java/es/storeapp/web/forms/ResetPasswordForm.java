package es.storeapp.web.forms;

import jakarta.validation.constraints.*;

public class ResetPasswordForm {

    @NotNull(message = "El token no puede ser nulo")
    @Size(min = 10, max = 100, message = "El token debe tener entre 10 y 100 caracteres")
    private String token;
    
    @NotNull(message = "El correo electrónico no puede ser nulo")
    @Email(message = "Debe proporcionar un correo electrónico válido")
    private String email;
    
    @NotNull(message = "La contraseña no puede ser nula")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = ".*[0-9].*", message = "La contraseña debe contener al menos un número")
    @Pattern(regexp = ".*[a-zA-Z].*", message = "La contraseña debe contener al menos una letra")
    @Pattern(regexp = ".*[!@#$%^&*()].*", message = "La contraseña debe contener al menos un carácter especial (!@#$%^&*)")
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
