package com.sivalabs.geektalk;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final Profiles profiles;
    private final ChatClient chatClient;
    private final Parser parser;
    private final HtmlRenderer renderer;

    ChatController(Profiles profiles,
                   ChatClient.Builder chatClientBuilder) {
        this.profiles = profiles;
        this.chatClient = chatClientBuilder.build();
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
        UserMessage userMessage = new UserMessage(message);
        SystemMessage systemMessage = new SystemMessage(profile.aiPersona());
        ChatResponse chatResponse = chatClient.prompt(new Prompt(
                List.of(systemMessage, userMessage)))
                .call().chatResponse();
        String response = chatResponse.getResult().getOutput().getContent();
        System.out.println("AI Response = " + response);
        Node document = parser.parse(response);
        String htmlResponse = renderer.render(document);
        System.out.println("htmlResponse = " + htmlResponse);

        model.addAttribute("profileName", profile.name());
        model.addAttribute("response", htmlResponse);
        model.addAttribute("message", message);

        return HtmxResponse.builder()
                .view("response :: responseFragment")
                //.view("recent-message-list :: messageFragment")
                .build();
    }
}
