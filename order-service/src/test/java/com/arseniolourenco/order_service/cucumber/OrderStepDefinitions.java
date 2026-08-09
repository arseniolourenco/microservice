package com.arseniolourenco.order_service.cucumber;

import com.arseniolourenco.order_service.dto.OrderLineItemsDto;
import com.arseniolourenco.order_service.dto.OrderRequest;
import com.arseniolourenco.order_service.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    private ResultActions resultActions;
    private int initialOrderCount;

    @Given("the product {string} has enough stock")
    public void the_product_has_enough_stock(String skuCode) {
        // Mock the inventory service call for enough stock
        stubFor(get(urlPathEqualTo("/api/inventory"))
                .withQueryParam("skuCode", equalTo(skuCode))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"skuCode\":\"" + skuCode + "\",\"inStock\":true,\"quantity\":10}]")));

        // Mock the reduce inventory call
        stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlPathEqualTo("/api/inventory/reduce"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Inventory reduced")));

        initialOrderCount = (int) orderRepository.count();
    }

    @When("I submit an order for {string} with quantity {int}")
    public void i_submit_an_order_for_with_quantity(String skuCode, int quantity) throws Exception {
        OrderLineItemsDto item = OrderLineItemsDto.builder()
                .skuCode(skuCode)
                .price(BigDecimal.valueOf(1000))
                .quantity(quantity)
                .build();

        OrderRequest request = new OrderRequest(List.of(item));
        String requestJson = objectMapper.writeValueAsString(request);

        resultActions = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson));
    }

    @Then("the order should be created successfully")
    public void the_order_should_be_created_successfully() throws Exception {
        resultActions.andExpect(status().isCreated());

        long currentOrderCount = orderRepository.count();
        assertEquals(initialOrderCount + 1, currentOrderCount);
    }
}
