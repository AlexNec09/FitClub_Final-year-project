package com.project.fitclub.configuration;

import com.project.fitclub.security.CustomUserDetailsService;
import com.project.fitclub.security.TokenAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Autowired
    TokenAuthenticationFilter tokenAuthenticationFilter;

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .csrf().disable().authorizeRequests().and()
                .formLogin().disable()
                .httpBasic().disable()
                .headers().frameOptions().disable().and()

                .authorizeRequests()
                .antMatchers("/",
                        "/error",
                        "/favicon.ico",
                        "/**/*.png",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.jpg",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js").permitAll()

                .antMatchers("/images/**", "/api/1.0/messages/upload", "/api/1.0/login", "/auth/**").permitAll()
                .and()

                .authorizeRequests()

                .antMatchers(HttpMethod.PUT, "/api/1.0/users/{id:[0-9]+}").authenticated()
                .antMatchers(HttpMethod.POST, "/api/1.0/messages/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/1.0/messages/{id:[0-9]+}").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/1.0/messages/{id:[0-9]+}/like").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/1.0/messages/{id:[0-9]+}/dislike").authenticated()
                .antMatchers("/api/1.0/users/{id:[0-9]+}/follow", "/api/1.0/users/{id:[0-9]+}/unfollow").authenticated()
                .antMatchers(HttpMethod.GET, "/api/1.0/messages/{id:[0-9]+}").authenticated()
                .and().
                authorizeRequests().anyRequest().permitAll();

//        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // we avoid having a session object for each user

        http.headers().frameOptions().disable();
        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}