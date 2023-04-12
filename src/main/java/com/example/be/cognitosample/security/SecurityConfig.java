package com.example.be.cognitosample.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    public static final String SIGNUP_URL = "/api/users/sign-up";
    public static final String SIGNIN_URL = "/api/users/sign-in";
    public static final String SIGNOUT_URL = "/api/users/sign-out";
    public static final String GETDETAIL_URL = "/api/users/detail";

    @Override
    protected void configure(HttpSecurity http)throws Exception{
        List<String> permitAllEndpointList = Arrays.asList(SIGNUP_URL, SIGNIN_URL, SIGNOUT_URL, GETDETAIL_URL);
        http.cors().and().csrf().disable().authorizeHttpRequests(
                expressionInterceptUrlRegistry -> expressionInterceptUrlRegistry
                        .antMatchers(permitAllEndpointList
                                .toArray(new String[permitAllEndpointList.size()]))
                        .permitAll().anyRequest().authenticated())
                .oauth2ResourceServer().jwt();
    }
}
