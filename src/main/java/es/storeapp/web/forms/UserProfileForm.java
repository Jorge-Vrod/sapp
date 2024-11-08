package es.storeapp.web.forms;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

public class UserProfileForm {

    @NotNull(message = "El nombre no puede ser nulo")
    @Size(min = 4, max = 100, message = "El nombre debe tener entre 4 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "El nombre solo puede contener letras, números y espacios")
    private String name;

    @NotNull(message = "El correo electrónico no puede ser nulo")
    @Email(message = "El correo electrónico debe tener un formato válido")
    private String email;

    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Pattern(regexp = "(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}", 
             message = "La contraseña debe contener al menos una letra mayúscula, una minúscula, un número y un carácter especial")
    private String password;

    @NotNull(message = "La dirección no puede ser nula")
    @Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9 ,.-]+$", message = "La dirección contiene caracteres no permitidos")
    private String address;

    @Size(max = 5242880, message = "El tamaño de la imagen no puede superar 5 MB")
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
