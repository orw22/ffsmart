package com.aad.ffsmart.user;

import com.aad.ffsmart.auth.JWTUtil;
import com.aad.ffsmart.user.model.LoginForm;
import com.aad.ffsmart.user.model.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * User service implementation class
 * <p>
 * Implements UserService functions such as login/register and admin functionality
 *
 * @author Oliver Wortley
 */
@Service
public class UserServiceImpl implements UserService {
    private static final String USER_NOT_FOUND_MSG = "User not found";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JWTUtil jwtUtil;

    public Mono<TokenUser> login(LoginForm loginForm) {
        return userRepository.findByEmail(loginForm.getEmail())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MSG)))
                .flatMap(usr -> {
                    if (passwordEncoder.matches(loginForm.getPassword(), usr.getPassword())) {
                        return Mono.just(new TokenUser(jwtUtil.generateToken(usr), usr));
                    } else {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect email or password");
                    }
                });
    }

    public Mono<TokenUser> register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // hash password
        return userRepository.save(user).flatMap(u -> Mono.just(new TokenUser(jwtUtil.generateToken(user), u)));
    }

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> getUserById(String userId) {
        return userRepository.findById(userId).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MSG)));
    }

    public Mono<String> getUserNameFromId(String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MSG)))
                .map(usr -> usr.getFirstName() + " " + usr.getLastName());
    }

    public Mono<User> updateUserById(String userId, User user) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MSG)))
                .map(usr -> {
                    if (user.getId() == null) {
                        user.setId(usr.getId());
                    }
                    if (user.getEmail() == null) {
                        user.setEmail(usr.getEmail());
                    }
                    if (user.getFirstName() == null) {
                        user.setFirstName(usr.getFirstName());
                    }
                    if (user.getLastName() == null) {
                        user.setLastName(usr.getLastName());
                    }
                    if (user.getPassword() == null) {
                        user.setPassword(usr.getPassword());
                    } else {
                        user.setPassword(passwordEncoder.encode(user.getPassword()));
                    }
                    if (user.getRole() == null) {
                        user.setRole(usr.getRole());
                    }
                    if (user.getAvatar() == null) {
                        user.setAvatar(usr.getAvatar());
                    }
                    return user;
                })
                .flatMap(userRepository::save);
    }

    public Mono<Void> deleteUserById(String userId) {
        return userRepository
                .findById(userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_MSG)))
                .flatMap(userRepository::delete);
    }

}
