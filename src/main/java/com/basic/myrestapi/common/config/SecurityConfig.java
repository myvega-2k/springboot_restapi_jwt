package com.basic.myrestapi.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.anonymous()
                .and()
                .formLogin()
                .and()
                .authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/api/**").permitAll()
                .anyRequest().authenticated();
    }

}
