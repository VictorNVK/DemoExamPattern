package ru.demoexam.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.demoexam.backend.storage.StoragePaths;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StoragePaths storagePaths;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/api/files/images/**")
                .addResourceLocations("file:" + storagePaths.imagesDirectory().toAbsolutePath() + "/");
    }
}
