package es.storeapp.web.forms;

import jakarta.validation.constraints.*;

public class ChangePasswordForm {
    
    // VULN: CWE 20 - Validación de datos de entrada

    @NotNull(message = "La contraseña actual no puede ser nula")
    @Size(min = 4, message = "La contraseña actual debe tener al menos 4 caracteres")
    private String oldPassword;

    @NotNull(message = "La nueva contraseña no puede ser nula")
    @Size(min = 4, message = "La nueva contraseña debe tener al menos 4 caracteres")
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
