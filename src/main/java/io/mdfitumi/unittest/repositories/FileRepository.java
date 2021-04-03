package io.mdfitumi.unittest.repositories;

import io.mdfitumi.unittest.models.FileObj;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRepository extends JpaRepository<FileObj, UUID> {
}
