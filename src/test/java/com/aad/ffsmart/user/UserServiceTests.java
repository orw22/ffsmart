package com.aad.ffsmart.user;

import com.aad.ffsmart.auth.JWTUtil;
import com.aad.ffsmart.user.model.LoginForm;
import com.aad.ffsmart.user.model.TokenUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {
    private static User user;
    private static final String USER_ID = "123";
    @Mock
    private UserRepository userRepository;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeAll
    static void setup() {
        user = new User(USER_ID, "test@gmail.com", "password", "Test", "User", Role.ROLE_HEAD_CHEF, null);
    }

    @DisplayName("Login with correct password, expect user and token returned")
    @Test
    void givenUserExistsAndCorrectPassword_whenLogin_thenSuccessful() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(anyString(), any())).thenReturn(true);

        Mono<TokenUser> tokenUserMono = userService.login(new LoginForm("test@gmail.com", "password"));

        StepVerifier
                .create(tokenUserMono)
                .assertNext(tokenUser -> assertEquals(user, tokenUser.getUser()))
                .verifyComplete();

        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @DisplayName("Login with incorrect password, expect error")
    @Test
    void givenUserExistsAndIncorrectPassword_whenLogin_thenError() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(user));
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        Mono<TokenUser> tokenUserMono = userService.login(new LoginForm("test@gmail.com", "password"));

        StepVerifier
                .create(tokenUserMono)
                .expectError()
                .verify();

        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @DisplayName("Register new user, expect user and token returned")
    @Test
    void givenNewUser_whenRegister_thenSuccessful() {
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));
        Mono<TokenUser> tokenUserMono = userService.register(user);

        StepVerifier
                .create(tokenUserMono)
                .assertNext(tokenUser -> {
                    assertEquals("test@gmail.com", tokenUser.getUser().getEmail());
                    assertEquals("Test", tokenUser.getUser().getFirstName());
                    assertEquals(Role.ROLE_HEAD_CHEF, tokenUser.getUser().getRole());
                })
                .verifyComplete();

        verify(userRepository, times(1)).save(any(User.class));
    }

    @DisplayName("Get all users, expect users returned")
    @Test
    void givenUsers_whenGetAllUsers_thenSuccessful() {
        when(userRepository.findAll()).thenReturn(Flux.just(user));
        Flux<User> userFlux = userService.getAllUsers();

        StepVerifier
                .create(userFlux)
                .assertNext(usr -> assertEquals(user, usr))
                .verifyComplete();

        verify(userRepository, times(1)).findAll();
    }

    @DisplayName("Get user by id, expect user returned")
    @Test
    void givenUserId_whenGetUserById_thenSuccessful() {
        when(userRepository.findById(anyString())).thenReturn(Mono.just(user));
        Mono<User> userMono = userService.getUserById(USER_ID);

        StepVerifier
                .create(userMono)
                .assertNext(usr -> assertEquals(user, usr))
                .verifyComplete();

        verify(userRepository, times(1)).findById(anyString());
    }

    @DisplayName("Get user by id, expect user not found error")
    @Test
    void givenInvalidUserId_whenGetUserById_thenError() {
        when(userRepository.findById(anyString())).thenReturn(Mono.empty());
        Mono<User> userMono = userService.getUserById(USER_ID);

        StepVerifier
                .create(userMono)
                .expectError()
                .verify();
    }

    @DisplayName("Get user name from id, expect user name returned")
    @Test
    void givenUserId_whenGetUserNameFromId_thenSuccessful() {
        when(userRepository.findById(anyString())).thenReturn(Mono.just(user));
        Mono<String> userNameMono = userService.getUserNameFromId(USER_ID);

        StepVerifier
                .create(userNameMono)
                .assertNext(name -> assertEquals("Test User", name))
                .verifyComplete();

        verify(userRepository, times(1)).findById(anyString());
    }

    @DisplayName("Update user by id, expect user updated")
    @Test
    void givenUserId_whenUpdateUserById_thenSuccessful() {
        when(userRepository.findById(anyString())).thenReturn(Mono.just(user));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));

        Mono<User> userMono = userService.updateUserById(USER_ID, user);

        StepVerifier
                .create(userMono)
                .assertNext(usr -> assertEquals(user, usr))
                .verifyComplete();

        verify(userRepository, times(1)).findById(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @DisplayName("Delete user by id, expect user deleted")
    @Test
    void givenUserId_whenDeleteUserById_thenSuccessful() {
        when(userRepository.findById(anyString())).thenReturn(Mono.just(user));
        when(userRepository.delete(any(User.class))).thenReturn(Mono.empty());

        userService.deleteUserById(USER_ID).block();

        verify(userRepository, times(1)).findById(anyString());
        verify(userRepository, times(1)).delete(any(User.class));
    }

}
