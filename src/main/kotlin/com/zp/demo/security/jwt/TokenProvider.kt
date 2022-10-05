package com.zp.demo.security.jwt

import com.zp.demo.config.ApplicationProperties
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import org.springframework.util.ObjectUtils
import java.security.Key
import java.util.*
import java.util.stream.Collectors

@Component
class TokenProvider(applicationProperties: ApplicationProperties) {
    private val log = LoggerFactory.getLogger(TokenProvider::class.java)
    private val key: Key
    private val jwtParser: JwtParser
    private val tokenValidityInMilliseconds: Long
    private val tokenValidityInMillisecondsForRememberMe: Long

    init {
        var keyBytes = ByteArray(0)
        val secret: String = applicationProperties.security.authentication.jwt.base64Secret
        if (!ObjectUtils.isEmpty(secret)) {
            log.debug("Using a Base64-encoded JWT secret key")
            keyBytes = Decoders.BASE64.decode(secret)
        }
        key = Keys.hmacShaKeyFor(keyBytes)
        jwtParser = Jwts.parserBuilder().setSigningKey(key).build()
        tokenValidityInMilliseconds =
            1000 * applicationProperties.security.authentication.jwt.tokenValidityInSeconds
        tokenValidityInMillisecondsForRememberMe =
            1000 * applicationProperties.security.authentication.jwt.tokenValidityInSecondsForRememberMe
    }

    fun createToken(authentication: Authentication, rememberMe: Boolean): String {
        val authorities = authentication.authorities.stream().map { obj: GrantedAuthority -> obj.authority }
            .collect(Collectors.joining(","))
        val now = Date().time
        val validity: Date = if (rememberMe) {
            Date(now + tokenValidityInMillisecondsForRememberMe)
        } else {
            Date(now + tokenValidityInMilliseconds)
        }
        return Jwts
            .builder()
            .setSubject(authentication.name)
            .claim(AUTHORITIES_KEY, authorities)
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact()
    }

    fun getAuthentication(token: String?): Authentication {
        val claims: Claims = jwtParser.parseClaimsJws(token).body
        val authorities: Collection<GrantedAuthority> =  claims[AUTHORITIES_KEY].toString().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            .filter { it.trim().isNotEmpty() }
            .map { SimpleGrantedAuthority(it) }
        val principal = User(claims.subject, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(authToken: String?): Boolean {
        try {
            jwtParser.parseClaimsJws(authToken)
            return true
        } catch (e: ExpiredJwtException) {
            log.trace(INVALID_JWT_TOKEN, e)
        } catch (e: UnsupportedJwtException) {
            log.trace(INVALID_JWT_TOKEN, e)
        } catch (e: MalformedJwtException) {
            log.trace(INVALID_JWT_TOKEN, e)
        } catch (e: SignatureException) {
            log.trace(INVALID_JWT_TOKEN, e)
        } catch (e: IllegalArgumentException) { // TODO: should we let it bubble (no catch), to avoid defensive programming and follow the fail-fast principle?
            log.error("Token validation error {}", e.message)
        }
        return false
    }

    companion object {
        private const val AUTHORITIES_KEY = "auth"
        private const val INVALID_JWT_TOKEN = "Invalid JWT token."
    }
}
