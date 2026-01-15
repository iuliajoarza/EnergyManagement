package com.example.chat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class RuleBasedChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(RuleBasedChatbotService.class);
    
    private final List<ChatRule> rules = new ArrayList<>();
    
    @Autowired
    private AIService aiService;

    public RuleBasedChatbotService() {
        initializeRules();
    }

    private void initializeRules() {
        // Rule 1: Add Device
        rules.add(new ChatRule(
            Arrays.asList("add device", "adauga dispozitiv"),
            "Mergi în 'My Devices' și apasă 'Add Device'. Introdu nume și max consumption. | Go to 'My Devices' and tap 'Add Device'. Enter name and max consumption."
        ));

        // Rule 2: Delete Device
        rules.add(new ChatRule(
            Arrays.asList("delete device", "sterge dispozitiv"),
            "Mergi în 'My Devices', selectează device și apasă 'Delete'. ATENȚIE: aceasta este ireversibilă. | Go to 'My Devices', select device and tap 'Delete'. WARNING: irreversible."
        ));

        // Rule 3: View Consumption
        rules.add(new ChatRule(
            Arrays.asList("view consumption", "vad consumul"),
            "Mergi în 'Dashboard' și selectează perioada (zi, săptămână, lună). Vei vedea graficele. | Go to 'Dashboard' and select period (day, week, month). View charts."
        ));

        // Rule 4: Set Alert
        rules.add(new ChatRule(
            Arrays.asList("set alert", "seteaza alerta"),
            "Mergi în 'Settings' -> 'Devices', selectează device și modifică 'Max Consumption' valoarea. | Go to 'Settings' -> 'Devices', select device and change 'Max Consumption' value."
        ));

        // Rule 5: Reset Password
        rules.add(new ChatRule(
            Arrays.asList("reset password", "recuperez parola"),
            "Apasă 'Forgot Password' pe login. Introduce email și urmează link-ul din email. | Click 'Forgot Password' on login. Enter email and follow email link."
        ));

        // Rule 6: View Profile
        rules.add(new ChatRule(
            Arrays.asList("view profile", "profilul meu"),
            "Apasă pictograma de profil (top right) pentru a vedea detaliile contului tău. | Tap profile icon (top right) to view your account details."
        ));

        // Rule 7: Logout
        rules.add(new ChatRule(
            Arrays.asList("logout", "deconectare"),
            "Apasă butonul 'Logout' din meniu. Datele sunt salvate automat. La revedere! | Tap 'Logout' button in menu. Data is auto-saved. Goodbye!"
        ));

        // Rule 8: Help
        rules.add(new ChatRule(
            Arrays.asList("help", "ajutor"),
            "Puteți cere ajutor cu: add device, delete device, view consumption, set alert, reset password, view profile, logout, check billing, security info, contact support."
        ));

        // Rule 9: Check Billing
        rules.add(new ChatRule(
            Arrays.asList("check billing", "factura"),
            "Mergi în 'Account' -> 'Invoices' pentru a vedea facturile tale și detaliile de plată. | Go to 'Account' -> 'Invoices' to view your invoices and payment details."
        ));

        // Rule 10: Security Info
        rules.add(new ChatRule(
            Arrays.asList("security", "data safe"),
            "Datele tale sunt criptate și stocate în siguranță. Nu partajăm date cu terți. Citește Privacy Policy în Settings. | Your data is encrypted and stored securely. We don't share data with third parties. Read Privacy Policy in Settings."
        ));
    }

    private static final String DEFAULT_RESPONSE = "Nu am gasit raspuns la aceasta intrebare. Scrie exact una din: \"add device\", \"delete device\", \"view consumption\", \"set alert\", \"reset password\", \"view profile\", \"logout\", \"help\", \"check billing\", \"security\". | Could not find answer. Try: \"add device\", \"delete device\", \"view consumption\", \"set alert\", \"reset password\", \"view profile\", \"logout\", \"help\", \"check billing\", \"security\".";

    /**
     * Process user message and return bot response based on rules or AI
     */
    public String processMessage(String userMessage, String userId) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Te rog să introduci un mesaj valid.";
        }

        String normalizedMessage = userMessage.toLowerCase().trim();

        // Check each rule first (STRICT MATCHING)
        for (ChatRule rule : rules) {
            if (rule.matches(normalizedMessage)) {
                logger.info("Matched rule for user {}: {}", userId, userMessage);
                return rule.getResponse();
            }
        }

        // No rule matched - use AI to generate response
        logger.info("No rule matched, using AI for user {}: {}", userId, userMessage);
        try {
            return aiService.generateResponse(userMessage);
        } catch (Exception e) {
            logger.error("Error generating AI response: {}", e.getMessage());
            return "Nu am putut procesa mesajul. Incearca sa reformulezi sau contacteaza support. | Could not process your message. Try rephrasing or contact support.";
        }
    }

    /**
     * Overloaded method for backward compatibility
     */
    public String processMessage(String userMessage) {
        return processMessage(userMessage, "unknown");
    }

    /**
     * Check if the response is the default response (no rule matched)
     */
    public boolean isDefaultResponse(String response) {
        return response != null && response.equals(DEFAULT_RESPONSE);
    }

    /**
     * Inner class representing a chat rule with STRICT EXACT MATCHING
     */
    private static class ChatRule {
        private final List<String> keywords;
        private final String response;

        public ChatRule(List<String> keywords, String response) {
            this.keywords = keywords;
            this.response = response;
        }

        /**
         * STRICT EXACT MATCHING - no contains(), no fuzzy
         * Must be exact string equality after lowercase normalization
         */
        public boolean matches(String message) {
            String normalizedMessage = message.toLowerCase().trim();
            
            // Check for exact match with any keyword
            for (String keyword : keywords) {
                if (normalizedMessage.equals(keyword.toLowerCase().trim())) {
                    return true;
                }
            }
            return false;
        }

        public String getResponse() {
            return response;
        }
    }
}
