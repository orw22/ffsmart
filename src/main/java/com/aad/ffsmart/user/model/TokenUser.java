package com.aad.ffsmart.user.model;

import com.aad.ffsmart.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TokenUser
 * <p>
 * For login and register responses with token and user data
 *
 * @author Oliver Wortley
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenUser {

    private String token;

    private User user;
}
