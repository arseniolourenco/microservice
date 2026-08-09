Feature: Order Management
  As a customer
  I want to be able to place an order
  So that I can purchase products

  Scenario: Successfully create an order when product is in stock
    Given the product "iphone_15" has enough stock
    When I submit an order for "iphone_15" with quantity 1
    Then the order should be created successfully
