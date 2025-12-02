package org.ldv.AppStarter_ToDoList.config

import org.ldv.AppStarter_ToDoList.service.AuditLogService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
// AJOUT TP2 - Import du logger SLF4J
import org.slf4j.LoggerFactory


@Configuration
@EnableWebSecurity
class SecurityConfig (
    private val auditLogService: AuditLogService,
    private val userDetailsService: UserDetailsService
){
    // AJOUT TP2 - Logger dédié à l’audit (redirigé vers audit.log via logbackspring.xml)
    private val auditLogger = LoggerFactory.getLogger("AUDIT")

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
        config.authenticationManager

    @Bean
    fun authenticationProvider(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder
    ): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService)
        authProvider.setPasswordEncoder(passwordEncoder)
        return authProvider
    }

    @Bean
    fun filterChain(
        http: HttpSecurity
    ): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/register", "/css/**", "/h2-console/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form
                    .loginPage("/login")
                    .successHandler(customAuthenticationSuccessHandler())
                    .permitAll()
            }
            .logout { logout ->
                logout
                    .logoutSuccessHandler(customLogoutSuccessHandler())
                    .permitAll()
            }
            .csrf { csrf ->
                csrf.ignoringRequestMatchers("/h2-console/**")
            }
            .headers { headers ->
                headers.frameOptions { it.disable() }
            }

        return http.build()
    }

    private fun customAuthenticationSuccessHandler(): AuthenticationSuccessHandler =
        AuthenticationSuccessHandler { request, response, authentication ->
            val username = authentication.name
            val ip = request.remoteAddr

            auditLogService.log(
                username = username,
                action = "LOGIN",
                details = "Connexion réussie",
                request = request
            )

            // AJOUT TP2 - écriture dans audit.log
            auditLogger.info("LOGIN user={} ip={}", username, ip)

            response.sendRedirect("/tasks")
        }

    private fun customLogoutSuccessHandler(): LogoutSuccessHandler =
        LogoutSuccessHandler { request, response, authentication ->
            val username = authentication?.name ?: "anonymous"
            val ip = request.remoteAddr

            auditLogService.log(
                username = username,
                action = "LOGOUT",
                details = "Déconnexion",
                request = request
            )

            // AJOUT TP2 - écriture dans audit.log
            auditLogger.info("LOGOUT user={} ip={}", username, ip)

            response.sendRedirect("/login?logout")
        }
}
