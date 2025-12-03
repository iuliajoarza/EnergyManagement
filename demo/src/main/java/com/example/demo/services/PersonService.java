package com.example.demo.services;


import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.dtos.builders.PersonBuilder;
import com.example.demo.entities.Person;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PersonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);
    private final PersonRepository personRepository;
    private final SyncPublisherService syncPublisherService;

    @Autowired
    public PersonService(PersonRepository personRepository,
                         SyncPublisherService syncPublisherService) {
        this.personRepository = personRepository;
        this.syncPublisherService = syncPublisherService;
    }

    public List<PersonDTO> findPersons() {
        List<Person> personList = personRepository.findAll();
        return personList.stream()
                .map(PersonBuilder::toPersonDTO)
                .collect(Collectors.toList());
    }

    public PersonDetailsDTO findPersonById(UUID id) {
        Optional<Person> prosumerOptional = personRepository.findById(id);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }
        return PersonBuilder.toPersonDetailsDTO(prosumerOptional.get());
    }

    public PersonDetailsDTO findPersonByUsername(String username) {
        Optional<Person> personOptional = personRepository.findByUsername(username);
        if (!personOptional.isPresent()) {
            LOGGER.error("Person with username {} was not found in db", username);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with username: " + username);
        }
        return PersonBuilder.toPersonDetailsDTO(personOptional.get());
    }

    public UUID insert(PersonDetailsDTO personDTO) {
        Person person = PersonBuilder.toEntity(personDTO);
        person = personRepository.save(person);
        LOGGER.debug("Person with id {} was inserted in db", person.getId());
        
        // Publish user sync event
        syncPublisherService.publishUserSync(person.getId().toString(), person.getName());
        
        return person.getId();
    }

    public void delete(UUID id) {
        Optional<Person> personOptional = personRepository.findById(id);
        if (!personOptional.isPresent()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }
        // Publish command to delete devices via RabbitMQ
        try {
            syncPublisherService.publishDeleteUserDevices(id.toString());
            LOGGER.debug("Published RabbitMQ command to delete all devices for user {}", id);
        } catch (Exception ex) {
            LOGGER.warn("Failed to publish delete devices command for user {}: {}", id, ex.getMessage());
            // Do not prevent user deletion; proceed regardless
        }
        personRepository.deleteById(id);
        LOGGER.debug("Person with id {} was deleted from db", id);
        try {
            syncPublisherService.publishUserDeleted(id.toString());
            LOGGER.debug("Published user_deleted sync event for user {}", id);
        } catch (Exception ex) {
            LOGGER.warn("Failed to publish user_deleted event for user {}: {}", id, ex.getMessage());
        }
    }

    public void update(UUID id, PersonDetailsDTO dto) {
        Optional<Person> personOptional = personRepository.findById(id);
        if (!personOptional.isPresent()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }
        Person p = personOptional.get();
        p.setName(dto.getName());
        p.setAddress(dto.getAddress());
        p.setAge(dto.getAge());
        personRepository.save(p);
        LOGGER.debug("Person with id {} was updated", id);
        try {
            syncPublisherService.publishUserSync(id.toString(), p.getName());
            LOGGER.debug("Published user sync event after update for {}", id);
        } catch (Exception ex) {
            LOGGER.warn("Failed to publish user sync event after update for {}: {}", id, ex.getMessage());
        }
    }

}
