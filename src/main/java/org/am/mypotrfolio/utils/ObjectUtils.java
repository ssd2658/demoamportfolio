package org.am.mypotrfolio.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class ObjectUtils {

    public static <T> Optional<T> readJsonFile(TypeReference<T> typeReference, String payload) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Resource resource = new ClassPathResource(payload);
            InputStream input = resource.getInputStream();
            return Optional.of(objectMapper.readValue(input, typeReference));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

}
