package com.artemistechnica.federation.datastore.repositories;

import com.artemistechnica.federation.datastore.models.SampleModels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<SampleModels.Account, UUID> {
    // Additional query methods can be defined here if needed
}