package com.example.chat.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AIService {

    private static final String MODEL = "gemini-2.5-flash";
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    @Value("${google.api.key}")
    private String apiKey;

    public String generateResponse(String userMessage) {
        try (Client client = new Client()) {

            // API key is set via GEMINI_API_KEY environment variable in Dockerfile

            String prompt = "You are a helpful AI assistant. Answer questions clearly and briefly in Romanian and English. " +
                          "You can answer both general knowledge questions and questions about an energy management system. " +
                          "Be friendly and informative.\n\n" +
                          "User: " + userMessage;

            GenerateContentResponse response = client.models.generateContent(
                    MODEL,
                    prompt,
                    null
            );

            String aiText = response.text();
            logger.info("Gemini SDK response: {}", aiText);

            return aiText != null ? aiText.trim() : "Sorry, no response from AI.";

        } catch (Exception e) {
            logger.error("Error calling Gemini API: {}", e.getMessage(), e);
            return "Scuze, nu am putut procesa cererea. Incearca din nou. | Sorry, couldn't process your request. Try again.";
        }
    }
}
