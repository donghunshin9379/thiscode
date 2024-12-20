package com.example.thiscode.config;

import com.example.thiscode.domain.CustomUserDetails;
import com.example.thiscode.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private final WebSocketService webSocketService;

    public SecurityConfig(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                                .requestMatchers("/**",
                                        "/login",
                                        "/signup",
                                        "/css/**",
                                        "/js/**",
                                        "/img/**",
                                        "/friends",
                                        "/users/**",
                                        "/api/**",
                                        "/messages/**").permitAll() // 모든 사용자가 접근 가능
                        //.anyRequest().authenticated() // 나머지 요청은 인증된 사용자만 가능
                )
                .formLogin(formLogin -> formLogin
                        .loginProcessingUrl("/auth")
                        .loginPage("/login")
                        .defaultSuccessUrl("/friends", true)
                        .successHandler((request, response, authentication) -> {
                            // 기본 성공 URL로 리다이렉트
                            response.sendRedirect("/friends");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            String email = ((CustomUserDetails) authentication.getPrincipal()).getEmail();
                            WebSocketSession session = webSocketService.findSessionByEmail(email);
                            if (session != null) {
                                webSocketService.removeSession(session);
                                // 클라이언트에 연결 종료 알림
                                session.sendMessage(new TextMessage("disconnect"));
                            }
                            response.sendRedirect("/login");
                        })
                );
        return http.build();
    }
}
