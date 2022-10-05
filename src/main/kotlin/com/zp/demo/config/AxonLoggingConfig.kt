package com.zp.demo.config

import org.axonframework.config.Configurer
import org.axonframework.config.ConfigurerModule
import org.axonframework.lifecycle.Phase
import org.axonframework.messaging.Message
import org.axonframework.messaging.interceptors.LoggingInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AxonLoggingConfig {

    @Bean
    fun loggingInterceptor(): LoggingInterceptor<Message<*>> {
        return LoggingInterceptor()
    }

    @Bean
    fun loggingInterceptorConfigurerModule(loggingInterceptor: LoggingInterceptor<Message<*>?>): ConfigurerModule {
        return ConfigurerModule { configurer: Configurer ->
            configurer.onInitialize { config: org.axonframework.config.Configuration ->
                config.onStart(
                    Phase.INSTRUCTION_COMPONENTS,
                    Runnable {
                        val commandBus = config.commandBus()
                        commandBus.registerDispatchInterceptor(loggingInterceptor)
                        commandBus.registerHandlerInterceptor(loggingInterceptor)
                        val eventBus = config.eventBus()
                        eventBus.registerDispatchInterceptor(loggingInterceptor)
                        val queryBus = config.queryBus()
                        queryBus.registerDispatchInterceptor(loggingInterceptor)
                        queryBus.registerHandlerInterceptor(loggingInterceptor)
                    })
            }
            configurer.eventProcessing()
                .registerDefaultHandlerInterceptor { c: org.axonframework.config.Configuration?, processorName: String? -> loggingInterceptor }
        }
    }
}