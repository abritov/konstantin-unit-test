package io.mdfitumi.unittest.services;

import io.mdfitumi.unittest.dto.FileObjDTO;
import io.mdfitumi.unittest.dto.OwnerDto;
import io.mdfitumi.unittest.entities.Owner;
import io.mdfitumi.unittest.models.FileObj;

public interface ObjectMapper {
    FileObjDTO fileToFileObjDTO(FileObj file);
    FileObj fileToFileObj(FileObjDTO fileObjDTO);

    Owner ownerToOwner(OwnerDto ownerDto);

    OwnerDto ownerToOwnerDto(Owner owner);
}
