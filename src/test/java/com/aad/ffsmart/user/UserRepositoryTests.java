package com.aad.ffsmart.user;

import com.aad.ffsmart.db.MongoConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ContextConfiguration(classes = MongoConfig.class)
@ExtendWith(SpringExtension.class)
class UserRepositoryTests {
    @Autowired
    private UserRepository userRepository;

    @Test
    void givenEmail_whenFindUserByEmail_thenUserReturned() {
        Mono<User> userMono = userRepository.findByEmail("123abc@gmail.com");
        StepVerifier
                .create(userMono)
                .assertNext(usr -> {
                    assertNotNull(usr.getId());
                    assertEquals("Test", usr.getFirstName());
                    assertEquals("123abc@gmail.com", usr.getEmail());
                    assertEquals(Role.ROLE_HEAD_CHEF, usr.getRole());
                })
                .expectComplete()
                .verify();
    }
}
