package com.aad.ffsmart.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * LoginForm
 * <p>
 * email and password data class for user login requests
 *
 * @author Oliver Wortley
 */
@Data
@AllArgsConstructor
public class LoginForm {

    private String email;

    private String password;
}
