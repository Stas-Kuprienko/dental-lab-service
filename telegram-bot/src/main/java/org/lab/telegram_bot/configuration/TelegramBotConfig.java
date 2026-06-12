package org.lab.telegram_bot.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.lab.exception.ApplicationCustomException;
import org.lab.model.ProductMap;
import org.lab.telegram_bot.configuration.auth.RequestAuthorization;
import org.lab.telegram_bot.controller.TelegramBotController;
import org.lab.telegram_bot.datasource.redis.DentalWorkList;
import org.lab.telegram_bot.domain.session.ChatSession;
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
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class TelegramBotConfig {

    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final Locale DEFAULT_LOCALE = Locale.of("RU");


    /**
     * Configuration of proxy TG-bot options for local development. Just Russian realities... :|
     * @return Bot options with proxy configurations.
     */
    @Bean(name = "myBotOptions")
    public DefaultBotOptions myBotOptions(@Value("${project.telegram.proxy.enable:false}") Boolean isProxy,
                                          @Value("${project.telegram.proxy.host:127.0.0.1}") String host,
                                          @Value("${project.telegram.proxy.port:10808}") Integer port,
                                          @Value("${project.telegram.proxy.type:HTTP}") String type) {
        DefaultBotOptions botOptions = new DefaultBotOptions();
        if (isProxy) {
            botOptions.setProxyHost(host);
            botOptions.setProxyPort(port);
            botOptions.setProxyType(DefaultBotOptions.ProxyType.valueOf(type));
            log.info("Configured bot options with PROXY for Telegram API, type={}, address={}:{}", type, host, port);
        }
        return botOptions;
    }

    @Bean("setMyCommandsExecutor")
    public Consumer<SetMyCommands> setMyCommandsExecutor(TelegramBotController telegramBotController) {
        return telegramBotController::execute;
    }

    @Bean
    public RequestInterceptor myRequestInterceptor(RequestAuthorization requestAuthorization) {
        return requestAuthorization::intercept;
    }

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
            log.info("RedisTemplate has been initialized");
            return template;
        } else {
            throw new ApplicationCustomException("Redis connection is failure");
        }
    }
    // ******************** /\

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
