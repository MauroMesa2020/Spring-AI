package com.agent.mailservice.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

    
@Data 
@AllArgsConstructor 
@NoArgsConstructor
public class LastMailsResponse {

    private String from;
    private String body;
    private String subject; 
    private LocalDateTime receivedAt;

}
