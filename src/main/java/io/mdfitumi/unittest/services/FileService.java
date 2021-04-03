package io.mdfitumi.unittest.services;

import io.mdfitumi.unittest.dto.FileFilterDto;
import io.mdfitumi.unittest.dto.FileObjDTO;
import io.mdfitumi.unittest.models.FileResponse;

import java.util.UUID;

public interface FileService {
    FileResponse create(FileObjDTO fileObjDTO);
    FileResponse filter(FileFilterDto fileFilterDto);
    FileResponse update(UUID uuid, FileObjDTO fileObjDTO);
    FileResponse setDeletedStatusForFileByUuid(UUID uuid);
    FileResponse getFileByUuid(UUID uuid);
}
