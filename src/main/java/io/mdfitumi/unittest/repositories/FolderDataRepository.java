package io.mdfitumi.unittest.repositories;

import io.mdfitumi.unittest.entities.FolderData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderDataRepository extends JpaRepository<FolderData, Long> {
    FolderData findByName(FolderData folderData);
}
