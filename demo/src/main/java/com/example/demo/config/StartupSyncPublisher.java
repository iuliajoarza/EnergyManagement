package com.example.demo.config;

import com.example.demo.entities.Person;
import com.example.demo.repositories.PersonRepository;
import com.example.demo.services.SyncPublisherService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupSyncPublisher {
    
    private final PersonRepository personRepository;
    private final SyncPublisherService syncPublisherService;
    
    public StartupSyncPublisher(PersonRepository personRepository, SyncPublisherService syncPublisherService) {
        this.personRepository = personRepository;
        this.syncPublisherService = syncPublisherService;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void publishExistingUsers() {
        System.out.println("=== Publishing all existing users on startup ===");
        Iterable<Person> persons = personRepository.findAll();
        for (Person person : persons) {
            syncPublisherService.publishUserSync(
                person.getId().toString(), 
                person.getUsername()  // Use username instead of name
            );
            System.out.println("Published sync for user: " + person.getUsername() + " (ID: " + person.getId() + ")");
        }
        System.out.println("=== Finished publishing users ===");
    }
}
