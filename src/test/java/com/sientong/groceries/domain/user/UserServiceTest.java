package com.sientong.groceries.domain.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sientong.groceries.domain.notification.NotificationService;
import com.sientong.groceries.domain.notification.NotificationType;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private NotificationService notificationService;

    private UserService userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, notificationService);

        testUser = User.builder()
                .id("user1")
                .email("test@example.com")
                .password("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .build();
    }

    @Test
    void shouldCreateUser() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
        when(notificationService.createNotification(
            testUser.getId(),
            "Welcome to Groceries Delivery System!",
            "Thank you for registering with us.",
            NotificationType.USER_REGISTERED,
            testUser.getId()
        )).thenReturn(Mono.empty());

        StepVerifier.create(userService.createUser(testUser))
                .expectNext(testUser)
                .verifyComplete();
    }

    @Test
    void shouldNotCreateUserWhenEmailExists() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.createUser(testUser))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldFindUserByEmail() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.findByEmail(testUser.getEmail()))
                .expectNext(testUser)
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenUserNotFoundByEmail() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Mono.empty());

        StepVerifier.create(userService.findByEmail(testUser.getEmail()))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldFindUserById() {
        when(userRepository.findById(testUser.getId())).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.findById(testUser.getId()))
                .expectNext(testUser)
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenUserNotFoundById() {
        when(userRepository.findById(testUser.getId())).thenReturn(Mono.empty());

        StepVerifier.create(userService.findById(testUser.getId()))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
