package org.lfenergy.letscoordinate.backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtils {

    public static <T> T jsonToObject(String path, Class<T> clazz) throws IOException {
        File file = ResourceUtils.getFile("classpath:" + path);
        String data = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return mapper.readValue(data, clazz);
    }
}
