package com.sivalabs.geektalk;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
class ChatController {
    private final Profiles profiles;
    private final ChatClient chatClient;
    private final Parser parser;
    private final HtmlRenderer renderer;

    ChatController(Profiles profiles,
                   ChatClient.Builder builder,
                   VectorStore vectorStore) {
        this.profiles = profiles;
        this.chatClient = builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
                .build();
        this.parser = Parser.builder().build();
        this.renderer = HtmlRenderer.builder()
                .escapeHtml(false)
                .build();
    }

    @GetMapping("")
    public String home(Model model) {
        model.addAttribute("profiles", profiles.getProfiles());
        return "index";
    }

    @GetMapping("/profiles/{id}/chat")
    public String showChatPage(@PathVariable String id, Model model) {
        Profile profile = profiles.getProfileById(id).orElseThrow();
        model.addAttribute("profile", profile);
        return "chat";
    }

    @HxRequest
    @PostMapping("/profiles/{id}/chat")
    public HtmxResponse generateResponse(
            @PathVariable String id,
            @RequestParam String message, Model model) {
        log.info("User Message: {}", message);
        var profile = profiles.getProfileById(id).orElseThrow();
        String response = chatClient.prompt()
                        .system(profile.aiPersona())
                        .user(message)
                        .options(OllamaOptions.create()
                            //.withNumCtx(8192)
                            .withTopK(10)
                        )
                        .call().content();
        log.info("AI Response = {}", response);
        Node document = parser.parse(response);
        String htmlResponse = renderer.render(document);
        log.info("htmlResponse = {}", htmlResponse);

        model.addAttribute("profileName", profile.name());
        model.addAttribute("response", htmlResponse);
        model.addAttribute("message", message);

        return HtmxResponse.builder()
                .view("response :: responseFragment")
                .build();
    }
}
