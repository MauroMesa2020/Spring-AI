package com.agent.mailservice.dto;

public record ChatResponse(
    String type,
    String message,
    ChatData data // Cambiar este.
    // poner Objeto Data. El cual tendra los objetos para: si ejecuto Sender: SenderResponse, Si ejecuto Last: LastResponse ... 
) {}