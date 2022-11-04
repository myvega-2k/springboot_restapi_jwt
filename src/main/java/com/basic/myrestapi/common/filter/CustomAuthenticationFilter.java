package com.basic.myrestapi.common.filter;

import com.basic.myrestapi.accounts.Account;
import com.basic.myrestapi.accounts.AccountAdapter;
import com.basic.myrestapi.common.RequestLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private Environment env;

    public CustomAuthenticationFilter(Environment env, AuthenticationManager authenticationManager) {
        this.env = env;
        super.setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        try {
            RequestLogin requestLogin =
                    new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestLogin.getEmail(),
                            requestLogin.getPassword(),
                            new ArrayList<>()
                    )
            );
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        //String userName = ((User)authResult.getPrincipal()).getUsername();
        AccountAdapter accountAdapter = (AccountAdapter) authResult.getPrincipal();
        Account account = accountAdapter.getAccount();
        System.out.println("===> 인증성공!! : " + account.getEmail());

        String token = Jwts.builder()
                .setSubject(account.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() +
                        Long.parseLong(env.getProperty("token.expiration_time"))))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512,
                        env.getProperty("token.secret"))
                .compact();
        response.addHeader("token", token);
    }
}
