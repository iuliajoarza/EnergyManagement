package com.example.demo.repositories;

import com.example.demo.entities.Person;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {

    /**
     * Example: JPA generate query by existing field
     */
    List<Person> findByName(String name);

    /**
     * Find person by username
     */
    Optional<Person> findByUsername(String username);

    /**
     * Example: Custom query
     */
    @Query(value = "SELECT p " +
            "FROM Person p " +
            "WHERE p.name = :name " +
            "AND p.age >= 60  ")
    Optional<Person> findSeniorsByName(@Param("name") String name);


    /**
     * Delete by ID using a custom query (in same style)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Person p WHERE p.id = :id")
    void deleteByIdCustom(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("UPDATE Person p SET p.name = :name, p.address = :address, p.age = :age WHERE p.id = :id")
    void updateByIdCustom(@Param("id") UUID id,
                          @Param("name") String name,
                          @Param("address") String address,
                          @Param("age") int age);


}
