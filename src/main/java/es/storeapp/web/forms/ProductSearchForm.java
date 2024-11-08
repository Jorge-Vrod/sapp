package es.storeapp.web.forms;

import jakarta.validation.constraints.*;

public class ProductSearchForm {
    
    @NotNull(message = "La categoría no puede ser nula")
    @Size(min = 3, max = 50, message = "La categoría debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-]+$", message = "La categoría solo puede contener letras, números, espacios y guiones")
    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
}
