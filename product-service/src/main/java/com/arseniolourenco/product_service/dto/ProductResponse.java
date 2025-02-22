package com.arseniolourenco.product_service.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;

    @JsonGetter("price")
    public String getFormattedPrice() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "AO"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00", symbols);
        return decimalFormat.format(price);
    }
}
