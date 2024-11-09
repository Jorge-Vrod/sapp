package es.storeapp.web.forms;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public class UserProfileForm {

    // VULN: CWE 20 - Validación de datos de entrada

    @NotNull(message = "Name cannot be null")
    @Size(max = 50, message = "Name cannot have more than 50 characters")
    private String name;

    @NotNull(message = "Email cannot be null")
    @Size(min = 1, message = "Email cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Invalid email format")
    private String email;

    @NotNull(message = "Password cannot be null")
    @Size(min = 4, message = "Password cannot be empty and must have at least 4 characters")
    private String password;

    @NotNull(message = "La dirección no puede ser nula")
    @Size(max = 100, message = "La dirección no puede tener más de 100 caracteres")
    private String address;

    private MultipartFile image;

    public UserProfileForm() {
    }

    public UserProfileForm(String name, String email, String address) {
        this.name = name;
        this.email = email;
        this.address = address;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }    
    
}
