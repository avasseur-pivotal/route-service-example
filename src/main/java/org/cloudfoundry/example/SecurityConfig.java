package org.cloudfoundry.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Read
 * http://docs.spring.io/spring-security/site/docs/current/reference/html/jc.html
 * http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-security.html
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                //.antMatchers("/unsecured").permitAll()//this has /unsecured to be not auth. (could be removed)
                .anyRequest().authenticated()
                .and().httpBasic()//this activates BASIC AUTH, else it is only form login
/*                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll()*/;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .inMemoryAuthentication()
                .withUser("user").password("password").roles("USER");
        // user & password is hardcoded here and should be changed
        // one could use System.getProperties with cf set-env ... to set a password in the route service app
    }
}