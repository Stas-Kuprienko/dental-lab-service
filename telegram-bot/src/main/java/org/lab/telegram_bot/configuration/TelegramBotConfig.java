package org.lab.telegram_bot.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.dental.restclient.DentalLabRestClient;
import org.lab.event.EventMessage;
import org.lab.model.ProductMap;
import org.lab.telegram_bot.controller.TelegramBotController;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.web.client.RestClient;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class TelegramBotConfig {

    public static final String SERVICE_CLIENT_ID = "TELEGRAM-BOT-SERVICE";
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String bootstrapServers;
    private final String groupId;


    @Autowired
    public TelegramBotConfig(@Value("${spring.kafka.consumer.bootstrap-servers}") String bootstrapServers,
                             @Value("${spring.kafka.consumer.group-id}") String groupId) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
    }


    @Bean("setMyCommandsExecutor")
    public Consumer<SetMyCommands> setMyCommandsExecutor(TelegramBotController telegramBotController) {
        return telegramBotController::execute;
    }

    @Bean
    public DentalLabRestClient dentalLabRestClient(@Value("${project.variables.dental-lab-api.url}") String url,
                                                   RestClient.Builder restClientBuilder) {
        return new DentalLabRestClient(url, restClientBuilder);
    }

    // MAPPING ************* \/

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    // /\ ****************** /\

    // REDIS *************** \/

    @Bean
    public RedisConnectionFactory redisConnectionFactory(@Value("${spring.redis.host}") String host,
                                                         @Value("${spring.redis.port}") String port) {
        return new LettuceConnectionFactory(host, Integer.parseInt(port));
    }

    @Bean
    public StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer();
    }

    @Bean("chatSessionRedisSerializer")
    public Jackson2JsonRedisSerializer<ChatSession> chatSessionRedisSerializer(ObjectMapper objectMapper) {
        return new Jackson2JsonRedisSerializer<>(objectMapper, ChatSession.class);
    }

    @Bean("chatSessionRedisTemplate")
    public RedisTemplate<String, ChatSession> chatSessionRedisTemplate(RedisConnectionFactory redisConnectionFactory,
                                                                       StringRedisSerializer stringRedisSerializer,
                                                                       Jackson2JsonRedisSerializer<ChatSession> chatSessionRedisSerializer) {

        RedisTemplate<String, ChatSession> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(chatSessionRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(chatSessionRedisSerializer);
        template.afterPropertiesSet();
        return template;
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
        return template;
    }
    // ******************** /\

    // KAFKA ************** \/

    @Bean
    public <V> ConsumerFactory<String, V> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        config.put(
                JsonDeserializer.TRUSTED_PACKAGES, EventMessage.class.getPackage().toString());
        config.put(
                ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(
                JsonDeserializer.KEY_DEFAULT_TYPE, String.class);
        config.put(
                JsonDeserializer.VALUE_DEFAULT_TYPE, EventMessage.class);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public <V> ConcurrentKafkaListenerContainerFactory<String, V> kafkaListenerContainerFactory(
            ConsumerFactory<String, V> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
    // /\ ***************** /\

    // MESSAGES *********** \/

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        return messageSource;
    }
    // /\ ***************** /\
}
