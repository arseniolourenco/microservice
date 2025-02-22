package com.arseniolourenco.product_service.unitTest;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import com.arseniolourenco.product_service.dto.ProductRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.arseniolourenco.product_service.dto.ProductResponse;
import com.arseniolourenco.product_service.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    public ProductResponse createProduct(@RequestBody ProductRequest productRequest) {
//        return productService.createProduct(productRequest);
//    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody ProductRequest productRequest) {
        return productService.createProduct(productRequest);
    }

//    private BigDecimal parsePrice(String price) throws ParseException {
//        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "AO"));
//        symbols.setGroupingSeparator('.');
//        symbols.setDecimalSeparator(',');
//
//        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00", symbols);
//        decimalFormat.setParseBigDecimal(true);
//
//        return (BigDecimal) decimalFormat.parse(price);
//    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProductById(@PathVariable String id) {
        return productService.getProductById(id);
    }

}