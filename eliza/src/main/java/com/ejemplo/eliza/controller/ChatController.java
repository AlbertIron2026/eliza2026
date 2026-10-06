package com.ejemplo.eliza.controller;

import com.ejemplo.eliza.service.ElizaService;

import com.ejemplo.eliza.model.Message;
import com.ejemplo.eliza.repository.MessageRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final MessageRepository repo;
    private final ElizaService eliza;

    public ChatController(MessageRepository repo, ElizaService eliza) {
        this.repo = repo;
        this.eliza = eliza;
    }

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> body) {
        String sessionId = body.get("sessionId");
        String message = body.get("message");

        List<Message> history = repo.findBySessionIdOrderByCreatedAtAsc(sessionId);

        save(sessionId, "user", message);

        String reply = eliza.respond(message, history);
        save(sessionId, "bot", reply);

        return Map.of("reply", reply);
    }

    @GetMapping("/{sessionId}")
    public List<Message> history(@PathVariable String sessionId) {
        return repo.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    private void save(String sessionId, String sender, String text) {
        Message m = new Message();
        m.setSessionId(sessionId);
        m.setSender(sender);
        m.setText(text);
        repo.save(m);
    }
}