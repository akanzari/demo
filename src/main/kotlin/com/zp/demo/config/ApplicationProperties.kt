package com.zp.demo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.web.cors.CorsConfiguration

@ConstructorBinding
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = true)
class ApplicationProperties {
    var apiDocs = ApiDocs()
    var cors = CorsConfiguration()
    var security = Security()

    class ApiDocs {
        lateinit var title: String
        lateinit var description: String
        lateinit var version: String
    }

    class Security {
        lateinit var contentSecurityPolicy: String
        var authentication = Authentication()
    }

    class Authentication {
        var jwt = Jwt()
    }

    class Jwt {
        lateinit var secret: String
        lateinit var base64Secret: String
        var tokenValidityInSeconds: Long = 0
        var tokenValidityInSecondsForRememberMe: Long = 0
    }

}