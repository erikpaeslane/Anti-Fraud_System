package antifraud.config;

import antifraud.entity.Role;
import antifraud.logic.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(handing -> handing
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint()) // Handles auth error
                )
                // For modifying requests via Postman
                .authorizeHttpRequests(requests -> requests // manage access
                        .requestMatchers("/actuator/shutdown", "/error/**").permitAll()// needs to run test
                        .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasAuthority("ROLE_MERCHANT")
                        .requestMatchers("/api/antifraud/suspicious-ip").hasAuthority("ROLE_SUPPORT")
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/suspicious-ip/{ip}").hasAuthority("ROLE_SUPPORT")
                        .requestMatchers("/api/antifraud/stolencard").hasAuthority("ROLE_SUPPORT")
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/stolencard/{number}").hasAuthority("ROLE_SUPPORT")
                        .requestMatchers(HttpMethod.GET, "/api/auth/list")
                        .hasAnyAuthority("ROLE_SUPPORT", "ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/role").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/access").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/user/{username}").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/history").hasAuthority("ROLE_SUPPORT")
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/history/{number}").hasAuthority("ROLE_SUPPORT")
                        .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasAuthority("ROLE_SUPPORT")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
