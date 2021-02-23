package com.vv.personal.diurnal.dbi.auth;

//import feign.RequestInterceptor;
//import feign.auth.BasicAuthRequestInterceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author Vivek
 * @since 11/02/21
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${SPRING_SECURITY_USERNAME}")
    private String username;

    @Value("${SPRING_SECURITY_CRED}")
    private String cred;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        super.configure(http);
    }

    /*@Bean
    public RequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor(username, cred);
    }*/
}
