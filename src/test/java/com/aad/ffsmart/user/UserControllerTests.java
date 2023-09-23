package com.aad.ffsmart.user;

import com.aad.ffsmart.auth.JWTUtil;
import com.aad.ffsmart.exception.GlobalErrorAttributes;
import com.aad.ffsmart.exception.GlobalExceptionHandler;
import com.aad.ffsmart.user.model.LoginForm;
import com.aad.ffsmart.user.model.TokenUser;
import com.aad.ffsmart.web.WebFluxTestSecurityConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@WebFluxTest(UserController.class)
@Import(WebFluxTestSecurityConfig.class)
class UserControllerTests {

    private static final String USER_ID = "123456";
    private static User user;
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeAll
    static void setup() {
        user = new User(USER_ID, "email", "password", "Test", "User", Role.ROLE_DELIVERY_DRIVER, null);
    }

    @DisplayName("Login user, expect status Ok")
    @WithMockUser
    @Test
    void givenUser_whenLogin_thenStatusOk() {
        when(userService.login(any(LoginForm.class)))
                .thenReturn(Mono.just(
                        new TokenUser("TOKEN", null)
                ));

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/users/login")
                .body(Mono.just(new LoginForm("email", "password")), LoginForm.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.token").isEqualTo("TOKEN")
                .consumeWith(System.out::println);
        verify(userService, times(1)).login(any(LoginForm.class));
    }

    @DisplayName("Register user, expect status Created")
    @WithMockUser
    @Test
    void givenUser_whenRegister_thenStatusCreated() {
        when(userService.register(any(User.class)))
                .thenReturn(Mono.just(
                        new TokenUser("TOKEN", null)
                ));

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/users")
                .body(Mono.just(user), User.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.token").isEqualTo("TOKEN")
                .consumeWith(System.out::println);

        verify(userService, times(1)).register(any(User.class));
    }

    @DisplayName("Get all users with head chef role, expect status Ok")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenUsers_whenGetAllUsers_thenStatusOk() {
        when(userService.getAllUsers()).thenReturn(Flux.just());

        webTestClient.get()
                .uri("/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);

        verify(userService, times(1)).getAllUsers();
    }

    @DisplayName("Get user by id with head chef role, expect status Ok")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenUserId_whenGetUserById_thenStatusOk() {
        when(userService.getUserById(anyString())).thenReturn(Mono.just(user));

        webTestClient.get()
                .uri("/users/" + USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(USER_ID)
                .jsonPath("$.data.email").isEqualTo("email")
                .consumeWith(System.out::println);

        verify(userService, times(1)).getUserById(anyString());
    }

    @DisplayName("Get user name from id, expect status Ok")
    @WithMockUser(roles = "CHEF")
    @Test
    void givenUserId_whenGetUserNameFromId_thenStatusOk() {
        when(userService.getUserNameFromId(anyString())).thenReturn(Mono.just("Test User"));

        webTestClient.get()
                .uri("/users/abc/name")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data").isEqualTo("Test User")
                .consumeWith(System.out::println);

        verify(userService, times(1)).getUserNameFromId(anyString());
    }

    @DisplayName("Update user by id, expect status Ok")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenUserAndId_whenUpdateUserById_thenStatusOk() {
        when(userService.updateUserById(anyString(), any(User.class))).thenReturn(Mono.just(user));

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .put()
                .uri("/users/" + USER_ID)
                .body(Mono.just(user), User.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(USER_ID)
                .consumeWith(System.out::println);

        verify(userService, times(1)).updateUserById(anyString(), any(User.class));
    }

    @DisplayName("Delete user by id, expect status No Content")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenUserId_whenDeleteUserById_thenStatusNoContent() {
        when(userService.deleteUserById(anyString())).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .delete()
                .uri("/users/" + USER_ID)
                .exchange()
                .expectStatus().isNoContent();

        verify(userService, times(1)).deleteUserById(anyString());
    }

    @DisplayName("Get user by token, expect status Ok")
    @WithMockUser
    @Test
    void givenToken_whenGetUserByToken_thenStatusOk() {
        when(userService.getUserById(any())).thenReturn(Mono.just(user));

        webTestClient.get()
                .uri("/users/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(USER_ID)
                .consumeWith(System.out::println);

        verify(userService, times(1)).getUserById(any());
    }

    @DisplayName("Update user by token, expect status Ok")
    @WithMockUser
    @Test
    void givenToken_whenUpdateUserByToken_thenStatusOk() {
        when(userService.updateUserById(any(), any(User.class))).thenReturn(Mono.just(user));

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .put()
                .uri("/users/me")
                .body(Mono.just(user), User.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(USER_ID)
                .consumeWith(System.out::println);

        verify(userService, times(1)).updateUserById(any(), any(User.class));
    }

    @DisplayName("Delete user by token, expect status No Content")
    @WithMockUser
    @Test
    void givenToken_whenDeleteUserByToken_thenStatusNoContent() {
        when(userService.deleteUserById(any())).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.csrf())
                .delete()
                .uri("/users/me")
                .exchange()
                .expectStatus().isNoContent();

        verify(userService, times(1)).deleteUserById(any());
    }

}
