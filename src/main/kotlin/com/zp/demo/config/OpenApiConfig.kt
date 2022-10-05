package com.zp.demo.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    companion object {
        const val API_FIRST_PACKAGE = "com.zp.demo.interfaces"
    }

    private fun apiInfo(title: String, description: String, version: String): Info {
        return Info().title(title).description(description).version(version)
    }

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI().info(apiInfo("", "", ""))
    }

    @Bean
    fun demoGroup(): GroupedOpenApi {
        return GroupedOpenApi.builder().group("demo").packagesToScan(API_FIRST_PACKAGE).build()
    }
}
