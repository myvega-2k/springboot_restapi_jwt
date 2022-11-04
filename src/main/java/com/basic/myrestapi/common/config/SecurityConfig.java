package com.basic.myrestapi.common.config;

import com.basic.myrestapi.accounts.AccountService;
import com.basic.myrestapi.common.filter.AuthenticationFilter;
import com.basic.myrestapi.common.filter.CustomAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    AccountService accountService;

    @Autowired
    Environment env;

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.anonymous()
//                .and()
//                .formLogin()
//                .and()
//                .authorizeRequests()
//                .mvcMatchers(HttpMethod.GET, "/api/**").permitAll()
//                .anyRequest().authenticated();
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/api/**").permitAll()
                .antMatchers("/*/login", "/*/signup").permitAll()
                .and()
                .addFilter(getAuthenticationFilter())
                .addFilterBefore(new CustomAuthorizationFilter(authenticationManager(),accountService,env),
                        UsernamePasswordAuthenticationFilter.class);
        http.headers().frameOptions().disable();
    }

    private AuthenticationFilter getAuthenticationFilter() throws Exception {
        AuthenticationFilter authenticationFilter =
                new AuthenticationFilter(env, authenticationManager());
        return authenticationFilter;
    }

}
