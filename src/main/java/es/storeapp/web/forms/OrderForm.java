package es.storeapp.web.forms;

import jakarta.validation.constraints.*;

public class OrderForm {
    
    @NotNull(message = "El nombre no puede ser nulo")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "El nombre solo puede contener letras, números y espacios")
    private String name;
    
    @Min(value = 0, message = "El precio debe ser un valor positivo")
    private int price;
    
    @NotNull(message = "La dirección no puede ser nula")
    @Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
    private String address;
    
    @NotNull(message = "Debe indicar si desea pagar ahora")
    private Boolean payNow;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getPayNow() {
        return payNow;
    }

    public void setPayNow(Boolean payNow) {
        this.payNow = payNow;
    }
        
}
