package org.lab.dental.util.letter;

import lombok.extern.slf4j.Slf4j;
import org.lab.exception.ApplicationCustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Stream;

@Slf4j
@Component
public class LetterTemplateUtility {

    private static final String CLASSPATH = "classpath:";
    private static final String DIRECTORY = "templates/";

    private final HashMap<String, String> templates;


    @Autowired
    public LetterTemplateUtility(ResourcePatternResolver resourcePatternResolver, ResourceLoader resourceLoader) {
        templates = init(resourcePatternResolver, resourceLoader);
    }


    public String construct(Locale locale, LetterTemplateKeys key, String... payload) {
        String template = templates.get(key.name());
        template = template.formatted((Object[]) payload);
        return template;
    }



    // *** INITIALIZATION METHODS ***

    private HashMap<String, String> init(ResourcePatternResolver resourcePatternResolver, ResourceLoader resourceLoader) {
        HashMap<String, String> map = new HashMap<>();
        Resource[] resources;
        try {
            resources = resourcePatternResolver.getResources(CLASSPATH + DIRECTORY + '*');
            if (resources == null) {
                throw new FileNotFoundException("No templates directory");
            }
        } catch (IOException e) {
            throw new ApplicationCustomException(e);
        }
        Stream.of(resources)
                .forEach(resource -> {
                    String filename = resource.getFilename();
                    String key = filename.split("\\.")[0];
                    map.put(key, readFile(resourceLoader, filename));
                    log.info("Has been loaded letter template '{}'", filename);
                });

        return map;
    }

    private String readFile(ResourceLoader resourceLoader, String fileName) {
        Resource resource = resourceLoader.getResource(CLASSPATH + DIRECTORY + fileName);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ApplicationCustomException(e);
        }
    }

    private boolean filter(Resource resource) {
        try {
            return resource.isReadable() &&
                    resource.getFile().isDirectory();
        } catch (IOException e) {
            throw new ApplicationCustomException(e);
        }
    }
}
