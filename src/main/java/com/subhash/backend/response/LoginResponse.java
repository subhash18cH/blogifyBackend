package com.subhash.backend.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String jwtToken;

    private String userName;
}
