/*
 COPYRIGHT (c) NEXTREE Inc. 2014
 This software is the proprietary of NEXTREE Inc.
 @since 2014. 6. 10.
 */

package io.naraway.metro.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnMissingClass("org.springframework.test.context.junit4.SpringJUnit4ClassRunner")
public class MongoTransactionConfig extends AbstractMongoClientConfiguration {
    //
    @Value("${spring.data.mongodb.uri:}")
    private String uri;

    @Value("${spring.data.mongodb.host:}")
    private String host;
    @Value("${spring.data.mongodb.port:27017}")
    private int port;
    @Value("${spring.data.mongodb.authentication-database:}")
    private String authenticationDatabase;
    @Value("${spring.data.mongodb.database:}")
    private String database;
    @Value("${spring.data.mongodb.username:}")
    private String username;
    @Value("${spring.data.mongodb.password:}")
    private String password;

    @Bean
    @Profile("!default & !local")
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        //
        return new MongoTransactionManager(dbFactory);
    }

    @Override
    protected String getDatabaseName() {
        //
        if (StringUtils.hasText(uri)) {
            return new ConnectionString(uri).getDatabase();
        }

        return database;
    }

    @Override
    public MongoClient mongoClient() {
        //
        if (StringUtils.hasText(uri)) {
            return getUriMongoClient();
        }

        return getCredentialMongoClient();
    }

    private MongoClient getUriMongoClient() {
        //
        ConnectionString connectionString = new ConnectionString(uri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        return MongoClients.create(settings);
    }

    private MongoClient getCredentialMongoClient() {
        //
        MongoCredential credential = MongoCredential.createCredential(
                username,
                authenticationDatabase,
                password.toCharArray());
        MongoClientSettings settings = MongoClientSettings.builder()
                .credential(credential)
                .retryWrites(true)
                .applyToConnectionPoolSettings(builder -> builder.maxConnectionIdleTime(5000, TimeUnit.MILLISECONDS))
                .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(host, port))))
                .build();

        return MongoClients.create(settings);
    }
}
