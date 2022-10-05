package com.zp.demo.config

import com.zp.demo.security.AccessDeniedHandlerJwt
import com.zp.demo.security.AuthenticationEntryPointJwt
import com.zp.demo.security.jwt.JWTConfigurer
import com.zp.demo.security.jwt.TokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class SecurityConfig(
    private val applicationProperties: ApplicationProperties,
    private val tokenProvider: TokenProvider,
    private val authenticationEntryPointJwt: AuthenticationEntryPointJwt,
    private val accessDeniedHandlerJwt: AccessDeniedHandlerJwt,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder? {
        return BCryptPasswordEncoder(10)
    }

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer? {
        return WebSecurityCustomizer { web: WebSecurity ->
            web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**").antMatchers("/swagger-ui/**").antMatchers("/test/**")
        }
    }

    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config: CorsConfiguration = applicationProperties.cors
        if (config.allowedOrigins?.isNotEmpty() == true || config.allowedOriginPatterns?.isNotEmpty() == true) {
            source.registerCorsConfiguration("/api/**", config)
            source.registerCorsConfiguration("/management/**", config)
            source.registerCorsConfiguration("/v3/api-docs", config)
            source.registerCorsConfiguration("/swagger-ui/**", config)
        }
        return CorsFilter(source)
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain? {
        // @formatter:off
        http
            .csrf()
            .disable()
            .addFilterBefore(corsFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPointJwt)
            .accessDeniedHandler(accessDeniedHandlerJwt)
            .and()
            .headers()
            .contentSecurityPolicy(applicationProperties.security.contentSecurityPolicy)
            .and()
            .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            .and()
            .permissionsPolicy()
            .policy("camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()")
            .and()
            .frameOptions()
            .deny()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/**").permitAll()
            .and()
            .httpBasic()
            .and()
            .apply(securityConfigurerAdapter())
        return http.build()
        // @formatter:on
    }


    private fun securityConfigurerAdapter(): JWTConfigurer {
        return JWTConfigurer(tokenProvider)
    }
}