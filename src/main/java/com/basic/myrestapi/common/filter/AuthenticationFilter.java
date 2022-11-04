package com.basic.myrestapi.common.filter;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private Environment env;

    public AuthenticationFilter(Environment env, AuthenticationManager authenticationManager) {
        this.env = env;
        super.setAuthenticationManager(authenticationManager);
    }

}
