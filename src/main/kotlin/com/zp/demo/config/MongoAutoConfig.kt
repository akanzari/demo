package com.zp.demo.config

import com.mongodb.client.MongoClient
import com.thoughtworks.xstream.XStream
import org.axonframework.config.Configurer
import org.axonframework.eventhandling.tokenstore.TokenStore
import org.axonframework.eventsourcing.eventstore.EventStorageEngine
import org.axonframework.extensions.mongo.DefaultMongoTemplate
import org.axonframework.extensions.mongo.MongoTemplate
import org.axonframework.extensions.mongo.eventhandling.saga.repository.MongoSagaStore
import org.axonframework.extensions.mongo.eventsourcing.eventstore.MongoEventStorageEngine
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore
import org.axonframework.modelling.saga.repository.SagaStore
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private const val AXON_SAGAS = "axon_sagas"
private const val AXON_TOKENS = "axon_tokens"

object SecureXStreamSerializer {
    fun xStream(): XStream {
        val xStream = XStream()
        xStream.classLoader = SecureXStreamSerializer::class.java.classLoader
        xStream.allowTypesByWildcard(
            arrayOf(
                "org.axonframework.**",
                "**",
            )
        )
        return XStreamSerializer.builder().xStream(xStream).build().xStream
    }
}

@Configuration
class MongoAutoConfig {

    private val logger: Logger = LoggerFactory.getLogger(MongoAutoConfig::class.java)

    /**
     * Create a Mongo based Event Storage Engine.
     */
    fun storageEngine(mongoTemplate: MongoTemplate): EventStorageEngine = MongoEventStorageEngine.builder()
        .eventSerializer(
            XStreamSerializer.builder()
                .xStream(SecureXStreamSerializer.xStream())
                .build()
        )
        .snapshotSerializer(
            XStreamSerializer.builder()
                .xStream(SecureXStreamSerializer.xStream())
                .build()
        )
        .mongoTemplate(mongoTemplate).build()

    /**
     * Uses the Configurer to wire everything together including Mongo as the Event and Token Store.
     */
    @Autowired
    fun configuration(configurer: Configurer, mongoClient: MongoClient, serializer: Serializer) {
        val mongoTemplate = DefaultMongoTemplate.builder()
            .mongoDatabase(mongoClient)
            .sagasCollectionName(AXON_SAGAS)
            .trackingTokensCollectionName(AXON_TOKENS)
            .build()

        configurer
            .configureEmbeddedEventStore { storageEngine(mongoTemplate) }
            .eventProcessing { conf -> conf
                .registerSagaStore { mongoSagaStore(mongoTemplate, it.serializer()) }
                .registerTokenStore { mongoTokenStore(mongoTemplate, it.serializer()) }
            }
    }

    /**
     * Create a Mongo based Token Store.
     */
    fun mongoTokenStore(mongoTemplate: MongoTemplate, serializer: Serializer): TokenStore {
        logger.info("Creating mongodb token store")
        return MongoTokenStore.builder()
            .mongoTemplate(mongoTemplate)
            .serializer(serializer)
            .build()
    }

    /**
     * Create a Mongo based Saga Store.
     */
    fun mongoSagaStore(mongoTemplate: MongoTemplate, serializer: Serializer): SagaStore<Any> {
        logger.info("Creating mongodb saga store")
        return MongoSagaStore.builder()
            .mongoTemplate(mongoTemplate)
            .serializer(serializer)
            .build()
    }
}