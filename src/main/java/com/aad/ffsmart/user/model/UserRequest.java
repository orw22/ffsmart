package com.aad.ffsmart.user.model;

import com.aad.ffsmart.user.Role;
import lombok.Data;

@Data
public class UserRequest {

    private String id;

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private Role role;

    private String avatar;
}
