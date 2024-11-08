package es.storeapp.web.forms;

import jakarta.validation.constraints.*;

public class ChangePasswordForm {
    // VULN: CWE 20 - Validación de datos de entrada
    @NotNull(message = "La contraseña actual no puede ser nula")
    @NotEmpty(message = "La contraseña actual no puede estar vacía")
    @Size(min = 8, message = "La contraseña actual debe tener al menos 8 caracteres")
    private String oldPassword;

    @NotNull(message = "La nueva contraseña no puede ser nula")
    @NotEmpty(message = "La nueva contraseña no puede estar vacía")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = ".*[A-Z].*", message = "La nueva contraseña debe contener al menos una letra mayúscula")
    @Pattern(regexp = ".*[0-9].*", message = "La nueva contraseña debe contener al menos un número")
    @Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "La nueva contraseña debe contener al menos un carácter especial")
    private String password;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
