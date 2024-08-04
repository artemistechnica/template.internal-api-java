package com.artemistechnica.federation.datastore.repositories;

import com.artemistechnica.federation.datastore.models.SampleModels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<SampleModels.Message, Integer> {
    // Additional query methods can be defined here if needed
}