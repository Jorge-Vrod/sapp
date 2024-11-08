package es.storeapp.web.forms;

import jakarta.validation.constraints.*;

public class CommentForm {
    // VULN: CWE 20 - Validación de datos de entrada
    @NotNull(message = "El ID del producto no puede ser nulo")
    private Long productId;

    @NotBlank(message = "El texto del comentario no puede estar vacío")
    @Size(max = 500, message = "El texto del comentario no puede exceder los 500 caracteres")
    private String text;

    @NotNull(message = "La calificación no puede ser nula")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer rating;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    
}
