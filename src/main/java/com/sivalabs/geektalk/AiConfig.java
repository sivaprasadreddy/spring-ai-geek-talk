package com.sivalabs.geektalk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

@Configuration
@Slf4j
class AiConfig {

    private final List<String> classpathResources = List.of(
        "kb/spring-boot-learning-resources.md",
        "kb/popular-java-spring-boot-blogs-ytchannels.md"
    );

    @Bean
    ApplicationRunner intVectorStore(VectorStore vectorStore) {
        return args -> {
            load(vectorStore, classpathResources);
        };
    }

    private void load(VectorStore vectorStore, List<String> classpathResources) {
        classpathResources.forEach(file -> {
            log.info("Loading document from {}", file);
            var resource = new ClassPathResource(file);
            var htmlReader = new TikaDocumentReader(resource);
            List<Document> documents = htmlReader.get();
            vectorStore.add(documents);
        });
    }
}
