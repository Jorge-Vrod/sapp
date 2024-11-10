package es.storeapp.web.forms;

import jakarta.validation.constraints.*;

public class OrderForm {
    
    // VULN: CWE 20 - Validación de datos de entrada

    @NotNull(message = "El nombre no puede ser nulo")
    @Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
    private String name;
    
    @Min(value = 0, message = "El precio debe ser un valor positivo")
    private int price;
    
    @Size(max = 100, message = "La dirección no puede tener más de 100 caracteres")
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
