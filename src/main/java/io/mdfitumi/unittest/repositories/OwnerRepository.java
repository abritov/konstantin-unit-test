package io.mdfitumi.unittest.repositories;

import io.mdfitumi.unittest.entities.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OwnerRepository extends JpaRepository<Owner, UUID> {
}
