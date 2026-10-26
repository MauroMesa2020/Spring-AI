package com.agent.mailservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agent.mailservice.dto.LastMailsResponse;
import com.agent.mailservice.dto.MailRequest;
import com.agent.mailservice.dto.MailResponse;
import com.agent.mailservice.service.MailService;

import jakarta.validation.Valid;


@RestController 
@RequestMapping ("/api/v1/mail")
public class MailController {
    
    private final MailService _mailService;

    public MailController(MailService mailService){
        _mailService = mailService;
    }

    @PostMapping("/send")
    public ResponseEntity<MailResponse> sender(@Valid @RequestBody MailRequest request){
        
        return ResponseEntity.ok(_mailService.sender(request)); 
    }

    @GetMapping("/get-last/{number}")
    public ResponseEntity<List<LastMailsResponse>> getLast(@PathVariable int number){
        return ResponseEntity.ok(_mailService.getLast(number));
    }

}
