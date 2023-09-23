package com.aad.ffsmart.user;

import com.aad.ffsmart.auth.JWTUtil;
import com.aad.ffsmart.user.model.LoginForm;
import com.aad.ffsmart.user.model.TokenUser;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureDataMongo
class UserTests {
    private static final String USER_ID = "63dc309b85f168081850a960";
    private static ParseContext jsonPath;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @BeforeAll
    static void setup() {
        Configuration jacksonConfig = Configuration.builder()
                .mappingProvider(new JacksonMappingProvider())
                .jsonProvider(new JacksonJsonProvider())
                .build();
        jsonPath = JsonPath.using(jacksonConfig);
    }

    @DisplayName("Login integration test")
    @WithAnonymousUser
    @Test
    void givenUsers_whenLogin_thenValidTokenReturned() {
        String json = webTestClient.post()
                .uri("/users/login")
                .body(Mono.just(new LoginForm("123abc@gmail.com", "password")), LoginForm.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(System.out::println)
                .returnResult()
                .getResponseBody();

        TokenUser res = jsonPath.parse(json).read("$.data", TokenUser.class);

        Mono<User> userMono = userRepository.findByEmail("123abc@gmail.com");
        StepVerifier
                .create(userMono)
                .consumeNextWith(user -> {
                    assertEquals(user.getId(), jwtUtil.getUsernameFromToken(res.getToken()));
                    assertSame(user.getRole(), res.getUser().getRole());
                })
                .verifyComplete();
    }

    @DisplayName("Register integration test")
    @WithAnonymousUser
    @Test
    void givenUsers_whenRegister_thenValidTokenReturned() {
        String newUserEmail = ((int) (Math.random() * 1000)) + "@gmail.com";

        String json = webTestClient.post()
                .uri("/users")
                .body(Mono.just(new User(
                        null, newUserEmail, "password", "Test",
                        "User", Role.ROLE_CHEF, null)), User.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .consumeWith(System.out::println)
                .returnResult()
                .getResponseBody();

        TokenUser res = jsonPath.parse(json).read("$.data", TokenUser.class);

        Mono<User> userMono = userRepository.findByEmail(newUserEmail);
        StepVerifier
                .create(userMono)
                .consumeNextWith(user -> {
                    assertEquals(user.getId(), jwtUtil.getUsernameFromToken(res.getToken()));
                    assertSame(user.getRole(), res.getUser().getRole());
                })
                .verifyComplete();
    }

    @DisplayName("Get all users integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenUsers_whenGetAllUsers_thenUsersReturned() {
        User firstUser = userRepository.findAll().blockFirst();

        webTestClient.mutate()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024))
                .build().get()
                .uri("/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].email").isEqualTo(Objects.requireNonNull(firstUser).getEmail())
                .jsonPath("$.data[0].id").isEqualTo(firstUser.getId())
                .consumeWith(System.out::println);
    }

    @DisplayName("Get user by id integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenUsers_whenGetUserById_thenUserReturned() {
        User user = userRepository.findById(USER_ID).block();

        assert user != null;
        webTestClient.get()
                .uri("/users/" + USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.email").isEqualTo(user.getEmail())
                .jsonPath("$.data.id").isEqualTo(user.getId())
                .consumeWith(System.out::println);
    }

    @DisplayName("Get user name from id integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenUsers_whenGetUserNameFromId_thenUserNameReturned() {
        User user = userRepository.findById(USER_ID).block();

        assert user != null;
        webTestClient.get()
                .uri("/users/" + USER_ID + "/name")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data").isEqualTo(user.getFirstName() + " " + user.getLastName())
                .consumeWith(System.out::println);
    }

    @DisplayName("Update user by id integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenUsers_whenUpdateUserById_thenUserUpdated() {
        String newEmail = "tu101@gmail.com";
        Role newRole = Role.ROLE_DELIVERY_DRIVER;

        webTestClient.put()
                .uri("/users/" + USER_ID)
                .body(Mono.just(new User(
                        USER_ID, newEmail, "password", "Test",
                        "User", newRole, null)), User.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(System.out::println);

        Mono<User> userMono = userRepository.findById(USER_ID);
        StepVerifier
                .create(userMono)
                .consumeNextWith(user -> {
                    assertEquals(USER_ID, user.getId());
                    assertEquals(newRole, user.getRole());
                    assertEquals(newEmail, user.getEmail());
                })
                .verifyComplete();
    }

    @DisplayName("Delete user by id integration test")
    @WithMockUser(roles = "HEAD_CHEF")
    @Test
    void givenUsers_whenDeleteUserById_thenUserDeleted() {
        webTestClient.delete()
                .uri("/users/63dac6cd90dc143f0528f2ee")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(String.class)
                .consumeWith(System.out::println)
                .returnResult();

        Mono<User> userMono = userRepository.findById("63dac6cd90dc143f0528f2ee");
        StepVerifier
                .create(userMono)
                .expectError(); // expect user not found
    }


}
