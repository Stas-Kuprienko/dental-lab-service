package org.lab.dental.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.lab.dental.repository.redis.DentalWorkList;
import org.lab.exception.ApplicationCustomException;
import org.lab.model.ProductMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@EnableAsync
@EnableScheduling
@Configuration
public class DentalLabConfiguration {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private Keycloak keycloak;
    private MinioClient minioClient;
    private RedisConnectionFactory redisConnectionFactory;
    private ExecutorService executorService;


    // KEYCLOAK ********** \/

    @Bean
    public Keycloak keycloak(@Value("${project.variables.keycloak.url}") String url,
                             @Value("${project.variables.keycloak.username}") String username,
                             @Value("${project.variables.keycloak.password}") String password) {
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(url)
                .clientId("admin-cli")
                .realm("master")
                .username(username)
                .password(password)
                .build();
        log.info("Keycloak Admin REST API Client has been initialized on URL '{}'", url);
        return keycloak;
    }

     @Bean
    public RealmResource realmResource(Keycloak keycloak, @Value("${project.variables.keycloak.realm}") String realm) {
        RealmResource resource = keycloak.realm(realm);
        log.info("Keycloak RealmResource '{}' has been initialized", realm);
        return resource;
    }
    // ******************* /\

    // \/ S3 ************* \/

    @Bean
    public MinioClient minioClient(@Value("${project.variables.minio.url}") String url,
                                   @Value("${project.variables.minio.bucket}") String bucketName,
                                   @Value("${project.variables.minio.access-key}") String accessKey,
                                   @Value("${project.variables.minio.secret-key}") String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .region("us-east-1")
                .build();
        BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
                .bucket(bucketName)
                .build();
        try {
            if (minioClient.bucketExists(bucketExistsArgs)) {
                log.info("MinIO S3 already has got bucket '{}'", bucketName);
            } else {
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build();
                minioClient.makeBucket(makeBucketArgs);
                log.info("MinIO S3 has been created bucket '{}'", bucketName);
            }
        } catch (Exception e) {
            throw new ApplicationCustomException(e);
        }
        log.info("MinIO S3 REST API Client has been initialized on URL '{}'", url);
        return minioClient;
    }
    // ********************* /\

    // REDIS *************** \/

    @Bean
    public RedisConnectionFactory redisConnectionFactory(@Value("${spring.redis.host}") String host,
                                                         @Value("${spring.redis.port}") String port) {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(host, Integer.parseInt(port));
        log.info("Redis connection factory initialized on URL '{}:{}'", host, port);
        return this.redisConnectionFactory = connectionFactory;
    }

    @Bean
    public StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer();
    }

    @Bean("dentalWorkRedisSerializer")
    public Jackson2JsonRedisSerializer<DentalWorkList> dentalWorkRedisSerializer(ObjectMapper objectMapper) {
        return new Jackson2JsonRedisSerializer<>(objectMapper, DentalWorkList.class);
    }

    @Bean("dentalWorkRedisTemplate")
    public RedisTemplate<String, DentalWorkList> dentalWorkRedisTemplate(RedisConnectionFactory redisConnectionFactory,
                                                                         StringRedisSerializer stringRedisSerializer,
                                                                         Jackson2JsonRedisSerializer<DentalWorkList> dentalWorkRedisSerializer) {

        RedisTemplate<String, DentalWorkList> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(dentalWorkRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(dentalWorkRedisSerializer);
        template.afterPropertiesSet();
        if (template.getConnectionFactory().getConnection().ping().equals("PONG")) {
            log.info("RedisTemplate for DentalWorks has been initialized");
            return template;
        } else {
            throw new ApplicationCustomException("Redis connection is failure");
        }
    }

    @Bean("productMapRedisSerializer")
    public Jackson2JsonRedisSerializer<ProductMap> productMapRedisSerializer(ObjectMapper objectMapper) {
        return new Jackson2JsonRedisSerializer<>(objectMapper, ProductMap.class);
    }

    @Bean("productMapRedisTemplate")
    public RedisTemplate<String, ProductMap> productMapRedisTemplate(RedisConnectionFactory redisConnectionFactory,
                                                                     StringRedisSerializer stringRedisSerializer,
                                                                     Jackson2JsonRedisSerializer<ProductMap> productMapRedisSerializer) {

        RedisTemplate<String, ProductMap> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(productMapRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(productMapRedisSerializer);
        template.afterPropertiesSet();
        if (template.getConnectionFactory().getConnection().ping().equals("PONG")) {
            log.info("RedisTemplate for ProductMap has been initialized");
            return template;
        } else {
            throw new ApplicationCustomException("Redis connection is failure");
        }
    }
    // ******************* /\

    // OPEN-API ********** \/

    @Bean
    public OpenAPI openAPI(@Value("${springdoc.info.title}") String title,
                           @Value("${springdoc.info.description}") String description,
                           @Value("${springdoc.info.version}") String version,
                           @Value("${project.variables.service-api-url}") String url) {
        Info info = new Info()
                .title(title)
                .description(description)
                .version(version);
        OpenAPI openAPI = new OpenAPI()
                .info(info)
                .addServersItem(new Server().description(title).url(url));
        log.info("OpenAPI for '{}' version={} by URL='{}' is created", title, version, url);
        return openAPI;
    }
    // /\ **************** /\

    // MAPPING ************* \/

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }
    // /\ ****************** /\

    // CONTEXT *********** \/

    @Bean(name = "virtualThreadPerTaskExecutor")
    public ExecutorService virtualThreadPerTaskExecutor() {
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        log.info("VirtualThreadPerTaskExecutor has been initialized");
        return executorService;
    }

    @Bean(name = "taskExecutor")
    public AsyncTaskExecutor taskExecutor() {
        return new ConcurrentTaskExecutor(virtualThreadPerTaskExecutor());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        log.info("BCryptPasswordEncoder has been initialized");
        return passwordEncoder;
    }

    @PreDestroy
    public void close() {
        if (keycloak != null) {
            keycloak.close();
            log.info("Keycloak Admin REST API Client is closed");
        }
        if (minioClient != null) {
            try {
                minioClient.close();
                log.info("MinIO S3 REST API Client is closed");
            } catch (Exception e) {
                log.error("The process of closing Minio REST API Client is failure", e);
            }
        }
        if (redisConnectionFactory != null) {
            redisConnectionFactory.getConnection().close();
            log.info("RedisConnectionFactory is closed");
        }
        if (executorService != null) {
            executorService.close();
            log.info("ExecutorService is closed");
        }
    }
    // ******************* /\
}
