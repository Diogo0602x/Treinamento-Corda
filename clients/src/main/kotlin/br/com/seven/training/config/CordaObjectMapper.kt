package br.com.seven.training.config

import com.fasterxml.jackson.databind.ObjectMapper
import net.corda.client.jackson.JacksonSupport
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class CordaObjectMapper {
    @Bean
    open fun createMapper(): ObjectMapper {
        return JacksonSupport.createNonRpcMapper();
    }
}