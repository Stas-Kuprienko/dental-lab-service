package org.lab.dental.configuration;

import io.minio.MinioClient;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DentalLabConfiguration {

    private Keycloak keycloak;


    // KEYCLOAK ********** \/

    @Bean
    public Keycloak keycloak(@Value("${project.variables.keycloak.url}") String url,
                             @Value("${project.variables.keycloak.username}") String username,
                             @Value("${project.variables.keycloak.password}") String password) {
        return this.keycloak = KeycloakBuilder.builder()
                .serverUrl(url)
                .clientId("admin-cli")
                .realm("master")
                .username(username)
                .password(password)
                .build();
    }

     @Bean
    public RealmResource realmResource(Keycloak keycloak, @Value("${project.variables.keycloak.realm}") String realm) {
        return keycloak.realm(realm);
    }
    // ******************* /\

    // CONTEXT *********** \/

    @PreDestroy
    public void close() {
        if (keycloak != null) {
            keycloak.close();
        }
    }
    // ******************* /\

    // \/ S3 ************* \/

    @Bean
    public MinioClient minioClient(@Value("${project.variables.minio.url}") String url,
                                   @Value("${project.variables.minio.access-key}") String accessKey,
                                   @Value("${project.variables.minio.secret-key}") String secretKey) {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
    // ******************* /\

    // \/ KAFKA ********** \/

    @Bean
    public <E> ProducerFactory<String, E> producerFactory(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public <E> KafkaTemplate<String, E> kafkaTemplate(ProducerFactory<String, E> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
    // /\ **************** /\

}
