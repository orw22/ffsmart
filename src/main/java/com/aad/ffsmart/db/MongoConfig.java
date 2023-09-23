package com.aad.ffsmart.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import java.util.concurrent.TimeUnit;

/**
 * Mongo configuration class
 * <p>
 * Configures Mongo client connection
 * Sets base packages for Mongo repository search
 *
 * @author Oliver Wortley
 */
@EnableReactiveMongoRepositories(basePackages = {"com.aad.ffsmart"})
@Configuration
public class MongoConfig {
    private static final String APP_NAME = "FFsmart";

    @Value("${mongodb.uri}")
    private String connectionString;

    @Bean
    public MongoClient mongoClient() {
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .retryWrites(true)
                .applyConnectionString(new ConnectionString(connectionString))
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(2000, TimeUnit.MILLISECONDS)
                )
                .applicationName(APP_NAME)
                .build();

        return MongoClients.create(clientSettings);
    }
}
