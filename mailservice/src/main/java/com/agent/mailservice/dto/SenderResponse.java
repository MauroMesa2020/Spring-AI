package com.agent.mailservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class SenderResponse {
    private String to;
    private String body;
    private String subject;
    private boolean success;
}
