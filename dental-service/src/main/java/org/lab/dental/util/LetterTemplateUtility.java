package org.lab.dental.util;

import lombok.extern.slf4j.Slf4j;
import org.lab.exception.InternalCustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Stream;

@Slf4j
@Component
public class LetterTemplateUtility {


    private static final String CLASSPATH = "classpath:";
    private static final String DIRECTORY = "templates";

    private final HashMap<String, String> templates;


    @Autowired
    public LetterTemplateUtility() {
        templates = init();
    }


    public String construct(Locale locale, LetterTemplateKey key, String... payload) {

        return "example";
    }



    // *** INITIALIZATION METHODS ***

    private HashMap<String, String> init() {
        HashMap<String, String> map = new HashMap<>();
        File[] templateArray;
        try {
            templateArray = ResourceUtils.getFile(CLASSPATH + DIRECTORY).listFiles();
            if (templateArray == null) {
                throw new FileNotFoundException("No templates directory");
            }
        } catch (FileNotFoundException e) {
            throw new InternalCustomException(e);
        }
        Stream.of(templateArray)
                .filter(file -> !file.isDirectory())
                .forEach(file -> {
                    String key = file.getName().split("\\.")[0];
                    map.put(key, readFile(file));
                });

        return map;
    }

    private String readFile(File file) {
        try {
            InputStream input = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            try (input; reader) {
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            }
            log.info("Template file ({}) is loaded", file.getName());
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new InternalCustomException(e);
        }
    }
}
