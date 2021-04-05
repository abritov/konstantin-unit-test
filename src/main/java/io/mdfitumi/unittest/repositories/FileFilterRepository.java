package io.mdfitumi.unittest.repositories;

import io.mdfitumi.unittest.models.FileObj;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface FileFilterRepository extends JpaRepository<FileObj, Long> {
    Page<FileObj> findByFilter(boolean checkDeleted, boolean deleted, boolean checkExt, String ext, boolean checkOwnerId, UUID ownerUuid, LocalDateTime beginDateTime, LocalDateTime endDateTime, Pageable pageable);
}
