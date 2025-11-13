package com.example.demo.controllers;

import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.services.PersonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RestController
@RequestMapping("/user")
@Validated
public class PersonController {
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private void checkOwnership(Authentication auth, UUID personId) {
        if (isAdmin(auth)) {
            return; // Admin poate accesa orice
        }
        
        // Client poate accesa doar propriul profil
        String username = auth.getName();
        PersonDetailsDTO person = personService.findPersonById(personId);
        
        if (!username.equals(person.getUsername())) {
            throw new SecurityException("Access denied: You can only access your own resources");
        }
    }

    @GetMapping
    public ResponseEntity<?> getPeople(Authentication auth) {
        if (isAdmin(auth)) {
            // Admin vede toți people
            return ResponseEntity.ok(personService.findPersons());
        }
        // Client vede doar pe el însuși (găsește după username din JWT)
        String username = auth.getName();
        PersonDetailsDTO person = personService.findPersonByUsername(username);
        return ResponseEntity.ok(List.of(person));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody PersonDetailsDTO person, Authentication auth) {
        UUID id = personService.insert(person);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonDetailsDTO> getPerson(@PathVariable UUID id, Authentication auth) {
        checkOwnership(auth, id);
        return ResponseEntity.ok(personService.findPersonById(id));
    }

    // New endpoint for finding person by username
    @GetMapping(params = "username")
    public ResponseEntity<PersonDetailsDTO> getPersonByUsername(@RequestParam String username) {
        return ResponseEntity.ok(personService.findPersonByUsername(username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication auth) {
        checkOwnership(auth, id);
        personService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @Valid @RequestBody PersonDetailsDTO person, Authentication auth) {
        checkOwnership(auth, id);
        personService.update(id, person);
        return ResponseEntity.noContent().build();
    }

}
