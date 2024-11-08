package es.storeapp.web.forms;

import jakarta.validation.constraints.*;

public class PaymentForm {
    
    @NotNull(message = "Debe indicar si es la tarjeta predeterminada")
    private Boolean defaultCreditCard;
    
    @NotNull(message = "El número de tarjeta de crédito no puede ser nulo")
    @Pattern(regexp = "^\\d{16}$", message = "El número de tarjeta de crédito debe tener 16 dígitos")
    private String creditCard;
    
    @NotNull(message = "El CVV no puede ser nulo")
    @Size(min = 3, max = 4, message = "El CVV debe tener entre 3 y 4 dígitos")
    @Pattern(regexp = "^\\d+$", message = "El CVV debe contener solo números")
    private Integer cvv;
    
    @NotNull(message = "El mes de expiración no puede ser nulo")
    @Min(value = 1, message = "El mes de expiración debe ser entre 1 y 12")
    @Max(value = 12, message = "El mes de expiración debe ser entre 1 y 12")
    private Integer expirationMonth;
    
    @NotNull(message = "El año de expiración no puede ser nulo")
    @Min(value = 2024, message = "El año de expiración debe ser mayor o igual al año actual")
    private Integer expirationYear;
    
    @NotNull(message = "Debe indicar si desea guardar la tarjeta")
    private Boolean save;

    public Boolean getDefaultCreditCard() {
        return defaultCreditCard;
    }

    public void setDefaultCreditCard(Boolean defaultCreditCard) {
        this.defaultCreditCard = defaultCreditCard;
    }
    
    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

    public Integer getCvv() {
        return cvv;
    }

    public void setCvv(Integer cvv) {
        this.cvv = cvv;
    }

    public Integer getExpirationMonth() {
        return expirationMonth;
    }

    public void setExpirationMonth(Integer expirationMonth) {
        this.expirationMonth = expirationMonth;
    }

    public Integer getExpirationYear() {
        return expirationYear;
    }

    public void setExpirationYear(Integer expirationYear) {
        this.expirationYear = expirationYear;
    }

    public Boolean getSave() {
        return save;
    }

    public void setSave(Boolean save) {
        this.save = save;
    }
    
}
