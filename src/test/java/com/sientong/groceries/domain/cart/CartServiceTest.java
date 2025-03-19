package com.sientong.groceries.domain.cart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sientong.groceries.domain.common.Money;
import com.sientong.groceries.domain.common.Quantity;
import com.sientong.groceries.domain.product.Category;
import com.sientong.groceries.domain.product.Product;
import com.sientong.groceries.domain.product.ProductService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private CartItem testItem;
    private Product testProduct;
    private static final String TEST_USER_ID = "test-user";
    private static final String TEST_CART_ID = "test-cart";
    private static final String TEST_PRODUCT_ID = "test-product";

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(TEST_PRODUCT_ID)
                .imageUrl("https://example.com/test-product.jpg")
                .category(Category.of("fruits", "Fruits"))
                .name("Test Product")
                .description("Test Description")
                .price(Money.of(BigDecimal.TEN, "USD"))
                .quantity(Quantity.of(100, "pcs"))
                .build();

        testItem = CartItem.builder()
                .id(UUID.randomUUID().toString())
                .productId(TEST_PRODUCT_ID)
                .name(testProduct.getName())
                .description(testProduct.getDescription())
                .price(testProduct.getPrice())
                .quantity(1)
                .unit("pcs")
                .build();

        testCart = Cart.builder()
                .id(TEST_CART_ID)
                .userId(TEST_USER_ID)
                .items(new ArrayList<>())
                .total(Money.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldGetCartForUser() {
        when(cartRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.getCart(TEST_USER_ID))
                .expectNextMatches(cart -> 
                    cart.getId().equals(TEST_CART_ID) &&
                    cart.getUserId().equals(TEST_USER_ID) &&
                    cart.getItems().isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldCreateNewCartIfNotExists() {
        Cart emptyCart = Cart.builder()
                .id(UUID.randomUUID().toString())
                .userId(TEST_USER_ID)
                .items(new ArrayList<>())
                .total(Money.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();

        when(cartRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Mono.empty());
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(Mono.just(emptyCart));

        StepVerifier.create(cartService.getCart(TEST_USER_ID))
                .expectNextMatches(cart -> 
                    cart.getUserId().equals(TEST_USER_ID) &&
                    cart.getItems().isEmpty() &&
                    cart.getTotal().equals(Money.ZERO))
                .verifyComplete();
    }

    @Test
    void shouldAddItemToCart() {
        Cart cartWithItem = Cart.builder()
                .id(TEST_CART_ID)
                .userId(TEST_USER_ID)
                .items(new ArrayList<>())
                .total(Money.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();
        cartWithItem.addItem(testItem);

        when(cartRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Mono.just(testCart));
        when(productService.findById(TEST_PRODUCT_ID))
                .thenReturn(Mono.just(testProduct));
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(Mono.just(cartWithItem));

        StepVerifier.create(cartService.addToCart(TEST_USER_ID, testItem))
                .expectNextMatches(cart -> 
                    cart.getItems().size() == 1 &&
                    cart.getItems().get(0).getProductId().equals(TEST_PRODUCT_ID) &&
                    cart.getItems().get(0).getUnit().equals("pcs"))
                .verifyComplete();
    }

    @Test
    void shouldRemoveCartItem() {
        Cart cartWithItem = Cart.builder()
                .id(TEST_CART_ID)
                .userId(TEST_USER_ID)
                .items(new ArrayList<>())
                .total(Money.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();
        cartWithItem.addItem(testItem);

        when(cartRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Mono.just(cartWithItem));
        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cartService.removeItem(TEST_USER_ID, testItem.getId()))
                .expectNextMatches(cart -> cart.getItems().isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldClearCart() {
        Cart cartWithItem = Cart.builder()
                .id(TEST_CART_ID)
                .userId(TEST_USER_ID)
                .items(new ArrayList<>())
                .total(Money.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();
        cartWithItem.addItem(testItem);

        when(cartRepository.findByUserId(TEST_USER_ID))
                .thenReturn(Mono.just(cartWithItem));
        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cartService.clearCart(TEST_USER_ID))
                .expectNextMatches(cart -> 
                    cart.getItems().isEmpty() &&
                    cart.getTotal().equals(Money.ZERO))
                .verifyComplete();
    }
}
