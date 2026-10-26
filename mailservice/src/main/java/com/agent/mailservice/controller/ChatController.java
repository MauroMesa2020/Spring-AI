package com.agent.mailservice.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agent.mailservice.dto.ChatRequest;
import com.agent.mailservice.dto.ChatResponse;

import com.agent.mailservice.service.ChatService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("api/v1/chat") 
public class ChatController {
    private final ChatService _chatService;

    public ChatController(ChatService chatService){
        _chatService = chatService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<ChatResponse> analyze(
            @Valid @RequestBody ChatRequest request) {

        return ResponseEntity.ok(
            _chatService.analyzeEmails(request.getMessage())
        ); 
    }

    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sender(@Valid @RequestBody ChatRequest request){
        
        return ResponseEntity.ok(
            _chatService.analyzeEmails(request.getMessage())
        );
        
    }

}
