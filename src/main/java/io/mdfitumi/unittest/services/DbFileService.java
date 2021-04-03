package io.mdfitumi.unittest.services;

import io.mdfitumi.unittest.exceptions.NoSuchElementInMinioException;
import io.mdfitumi.unittest.models.FileObj;
import io.mdfitumi.unittest.models.FileResponse;
import io.mdfitumi.unittest.dto.FileFilterDto;
import io.mdfitumi.unittest.dto.FileObjDTO;

import java.util.UUID;

public interface DbFileService {
    FileObj create(FileObjDTO fileObjDTO);
    FileResponse filter(FileFilterDto fileFilterDto);
    FileObj update(UUID uuid, FileObjDTO fileObjDTO);
    FileResponse deleted(UUID uuid);
    FileObjDTO getFileById(UUID uuid) throws NoSuchElementInMinioException;
}
