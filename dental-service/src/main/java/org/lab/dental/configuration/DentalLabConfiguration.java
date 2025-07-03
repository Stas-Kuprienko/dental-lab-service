package org.lab.dental.configuration;

import io.minio.MinioClient;
import jakarta.annotation.PreDestroy;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
