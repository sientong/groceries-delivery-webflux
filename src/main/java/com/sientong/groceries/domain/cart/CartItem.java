package com.sientong.groceries.domain.cart;

import com.sientong.groceries.domain.product.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String id;
    private String productId;
    private String name;
    private Money price;
    private int quantity;
    private String imageUrl;
}
