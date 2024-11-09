package es.storeapp.web.forms;

import jakarta.validation.constraints.*;

public class ProductSearchForm {
    
    // VULN: CWE 20 - Validación de datos de entrada

    @NotNull(message = "La categoría no puede ser nula")
    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
}
