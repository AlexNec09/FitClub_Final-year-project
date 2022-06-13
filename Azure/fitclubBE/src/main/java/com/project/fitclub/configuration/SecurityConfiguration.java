package com.project.fitclub.configuration;

import com.project.fitclub.security.CustomUserDetailsService;
import com.project.fitclub.security.JwtAuthenticationEntryPoint;
import com.project.fitclub.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().and().csrf().disable();

        http.httpBasic().authenticationEntryPoint(new BasicAuthenticationEntryPoint());

        http.
                authorizeRequests().and()
                .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/images/**", "/api/1.0/users", "/api/1.0/users/find/{search}", "/api/1.0/login", "/api/1.0/auth/**").permitAll()
                .antMatchers(HttpMethod.PUT, "/api/1.0/users/{id:[0-9]+}").authenticated()
                .antMatchers(HttpMethod.GET, "/api/1.0/users/{username}/posts").authenticated()
                .antMatchers(HttpMethod.POST, "/api/1.0/posts/upload").authenticated()
                .antMatchers(HttpMethod.POST, "/api/1.0/posts/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/1.0/login").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/1.0/posts/{id:[0-9]+}").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/1.0/posts/{id:[0-9]+}/like").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/1.0/posts/{id:[0-9]+}/dislike").authenticated()
                .antMatchers("/api/1.0/users/{id:[0-9]+}/follow", "/api/1.0/users/{id:[0-9]+}/unfollow").authenticated()
                .antMatchers(HttpMethod.GET, "/api/1.0/posts/{id:[0-9]+}").authenticated()
                .antMatchers(HttpMethod.GET, "/api/1.0/posts/**").authenticated()
                .and().
                authorizeRequests().anyRequest().permitAll();

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

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