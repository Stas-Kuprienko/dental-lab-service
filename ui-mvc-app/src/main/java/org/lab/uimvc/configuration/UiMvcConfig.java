package org.lab.uimvc.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.RequestInterceptor;
import org.lab.model.ProductMap;
import org.lab.uimvc.configuration.auth.RequestAuthorization;
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
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class UiMvcConfig {


    @Bean
    public RequestInterceptor myRequestInterceptor(RequestAuthorization requestAuthorization) {
        return requestAuthorization::intercept;
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("ru"));
        //TODO temporary
        resolver.setCookieName("lang");
        resolver.setCookieMaxAge(60 * 60 * 24 * 30); // 30 дней
        return resolver;
    }

    // MAPPING *********** \/

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }
    // ******************* /\

    // REDIS ************* \/

    @Bean
    public RedisConnectionFactory redisConnectionFactory(@Value("${spring.redis.host}") String host,
                                                         @Value("${spring.redis.port}") String port) {
        return new LettuceConnectionFactory(host, Integer.parseInt(port));
    }

    @Bean
    public StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer();
    }

    @Bean
    public Jackson2JsonRedisSerializer<ProductMap> jsonRedisSerializer(ObjectMapper objectMapper) {
        return new Jackson2JsonRedisSerializer<>(objectMapper, ProductMap.class);
    }

    @Bean
    public RedisTemplate<String, ProductMap> productMapRedisTemplate(RedisConnectionFactory redisConnectionFactory,
                                                                     StringRedisSerializer stringRedisSerializer,
                                                                     Jackson2JsonRedisSerializer<ProductMap> jsonRedisSerializer) {

        RedisTemplate<String, ProductMap> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
    // ******************* /\

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
