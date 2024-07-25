package com.sivalabs.geektalk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
class AiConfig {
    @Bean
    ApplicationRunner intVectorStore(VectorStore vectorStore) {
        return args -> {
            log.info("Initializing vector store");
        };
    }
}
