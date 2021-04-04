package io.mdfitumi.unittest.services.impl;

import io.mdfitumi.unittest.dto.*;
import io.mdfitumi.unittest.entities.FolderData;
import io.mdfitumi.unittest.entities.Owner;
import io.mdfitumi.unittest.exceptions.NoSuchElementInMinioException;
import io.mdfitumi.unittest.repositories.FileFilterRepository;
import io.mdfitumi.unittest.repositories.FolderDataRepository;
import io.mdfitumi.unittest.repositories.OwnerRepository;
import io.mdfitumi.unittest.services.DbFileService;
import io.mdfitumi.unittest.models.FileObj;
import io.mdfitumi.unittest.models.FileResponse;
import io.mdfitumi.unittest.repositories.FileRepository;
import io.mdfitumi.unittest.services.MinioService;
import io.mdfitumi.unittest.services.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.JoinType;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DbFileServiceImpl implements DbFileService {
    private final FileRepository fileRepository;
    private final FileFilterRepository fileFilterRepository;
    private final OwnerRepository ownerRepository;
    private final FolderDataRepository folderDataRepository;
    private final ObjectMapper objectMapper;
    private final MinioService minioService;

    private static final String UNDEFINED_OWNER_NAME = "notNameOwner";
    private static final UUID UNDEFINED_OWNER_UUID =
            UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final LocalDateTime DEFAULT_DATE = LocalDateTime.of(2000,
            1, 1, 0, 0, 0);
    private static final LocalDateTime DEFAULT_END_DATE =
            LocalDateTime.now().plusDays(1L);

    @Autowired
    public DbFileServiceImpl(FileRepository fileRepository, FileFilterRepository fileFilterRepository, OwnerRepository ownerRepository, FolderDataRepository folderDataRepository, ObjectMapper objectMapper, MinioService minioService) {
        this.fileRepository = fileRepository;
        this.fileFilterRepository = fileFilterRepository;
        this.ownerRepository = ownerRepository;
        this.folderDataRepository = folderDataRepository;
        this.objectMapper = objectMapper;
        this.minioService = minioService;
    }

    @Override
    public FileObj create(FileObjDTO fileObjDTO) {
        //fileObjDTO.setFolder("12-02-2021");
        FileObj fileObj = objectMapper.fileToFileObj(fileObjDTO);


        fileObj.setCreateDateTime(Optional.ofNullable(fileObjDTO.getCreateDateTime()).orElse(LocalDateTime.now()));
        if (fileObj.getCreateDateTime().isAfter(LocalDateTime.now())) throw new RuntimeException("Invalid date");
        if (fileObj.getCreateDateTime().isBefore(DEFAULT_DATE.minusYears(10))) throw new RuntimeException("Incorrect date");

        fileObj.setUpdateDateTime(Optional.ofNullable(fileObjDTO.getUpdateDateTime()).orElse(LocalDateTime.now()));

        if (fileObjDTO.getOwnerDto() == null) {
            fileObjDTO.setOwnerDto(new OwnerDto());
        }

        UUID uuidOwner = Optional
                .ofNullable(fileObjDTO.getOwnerDto().getUuid())
                .orElse(UNDEFINED_OWNER_UUID);
        String ownerName = Optional.ofNullable(fileObjDTO.getOwnerDto().getName()).orElse(UNDEFINED_OWNER_NAME);
        Owner owner = ownerRepository.findById(uuidOwner).orElseGet(() -> ownerRepository.save(new Owner(uuidOwner, ownerName)));
        fileObj.setOwner(owner);

        FolderData folderData = new FolderData();
        folderData.setFolderData(minioService.getFolderData());

        FolderData f = folderDataRepository.findByName(folderData.getFolderData());

        if (f == null) {
            FolderData folderData1 = folderDataRepository.save(folderData);
            fileObj.setFolderData(folderData1);
            fileRepository.save(fileObj);
        } else {
            fileObj.setFolderData(f);
            fileRepository.save(fileObj);
        }



        fileObj.setFileData(null);
        return fileObj;
    }

    @Override
    public FileResponse filter(FileFilterDto fileFilterDto) {

        FileResponse fileResponse = new FileResponse();
        PagerDTO pagerDTO = new PagerDTO();

        Paginating paginating = fileFilterDto.getPaginating();

        Pageable pageable = PageRequest.of(paginating.getNumber(), paginating.getSize());

        if (fileFilterDto.getBeginDateCreate() == null) {
            fileFilterDto.setBeginDateCreate(DEFAULT_DATE.toLocalDate());
        }
        if (fileFilterDto.getEndDateCreate() == null) {
            fileFilterDto.setEndDateCreate(DEFAULT_END_DATE.toLocalDate());
        }

        //flag for native filter
        boolean checkDeleted = false;
        boolean checkExt = false;
        boolean checkOwnerId = false;
        //fields for native filter
        boolean deleted = false;
        LocalDateTime beginDateTime =
                LocalDateTime.of(fileFilterDto.getBeginDateCreate(), LocalTime.MIN);
        LocalDateTime endDateTime =
                LocalDateTime.of(fileFilterDto.getEndDateCreate(),
                        LocalTime.MAX.truncatedTo(ChronoUnit.SECONDS));
        String ext = "";
        UUID ownerUuid = UUID.randomUUID();

        if (fileFilterDto.getDeleted() != null) {
            deleted = fileFilterDto.getDeleted();
        }
        if (fileFilterDto.getIdOwner() != null) {
            ownerUuid = fileFilterDto.getIdOwner();
        }
        if (fileFilterDto.getExt() != null) {
            ext = fileFilterDto.getExt();
        }

        if (fileFilterDto.getDeleted() != null) checkDeleted = true;
        if (fileFilterDto.getExt() != null) checkExt = true;
        if (fileFilterDto.getIdOwner() != null) checkOwnerId = true;

        Page<FileObj> pageFilesObj = fileFilterRepository.findByFilter(
                checkDeleted, deleted, checkExt, ext,
                checkOwnerId, ownerUuid, beginDateTime, endDateTime, pageable
        );

        List<FileObjDTO> fileObjDTOList = pageFilesObj
                .getContent()
                .stream()
                .map(objectMapper::fileToFileObjDTO)
                .collect(Collectors.toList());

        fileResponse.setPagerDTO(pagerDTO);
        fileResponse.getPagerDTO().setNext(pageFilesObj.hasNext());
        fileResponse.getPagerDTO().setPrev(pageFilesObj.hasPrevious());
        fileResponse.getPagerDTO().setSize(pageFilesObj.getSize());
        fileResponse.getPagerDTO().setTotal(pageFilesObj.getTotalElements());

        fileResponse.setFileObjPages(fileObjDTOList);

        fileResponse.setMessage("");
        fileResponse.setSuccess(true);

        return fileResponse;
    }

    @Override
    public FileObj update(UUID uuid, FileObjDTO fileObjDTO) {
        //проверить при несущ
        FileObj fileObjToUpdate = fileRepository.getOne(uuid);

        if (fileObjDTO.getFileData() != null) {
            fileObjToUpdate.setFileData(fileObjDTO.getFileData());
        }
        if (fileObjDTO.getFileName() != null) {
            fileObjToUpdate.setFileName(fileObjDTO.getFileName());
        }
        if (fileObjDTO.getExt() != null) {
            fileObjToUpdate.setExt(fileObjDTO.getExt());
        }
        if (fileObjDTO.getDeleted() != null) {
            fileObjToUpdate.setDeleted(fileObjDTO.getDeleted());
        }
        if (fileObjDTO.getOwnerDto() != null) {
            Owner owner = objectMapper.ownerToOwner(fileObjDTO.getOwnerDto());
            fileObjToUpdate.setOwner(owner);
        }

        fileObjToUpdate.setUpdateDateTime(LocalDateTime.now());
        fileRepository.save(fileObjToUpdate);
        return fileObjToUpdate;
    }

    @Override
    public FileResponse deleted(UUID uuid) {
        FileResponse fileResponse = new FileResponse();
        FileObj fileObj = fileRepository.findById(uuid).orElseThrow();
        if (!fileObj.isDeleted()) {
            fileObj.setUpdateDateTime(LocalDateTime.now());
            fileObj.setDeleted(true);
            fileRepository.save(fileObj);
            fileResponse.setMessage("");
        } else {
            fileResponse.setMessage("Файл уже помечен на удаление");
        }
        fileResponse.setSuccess(true);
        fileResponse.setFileObjPages(null);
        return fileResponse;
    }

    @Override
    public FileObjDTO getFileById(UUID uuid) throws NoSuchElementInMinioException {
        Optional<FileObj> fileObj = fileRepository.findById(uuid);
        if (!fileObj.isPresent()) {
            throw new NoSuchElementInMinioException("No such element");
        }
        FileObjDTO fileObjDTO =
                objectMapper.fileToFileObjDTO(fileObj.get());
        OwnerDto ownerDto =
                objectMapper.ownerToOwnerDto(fileObj.get().getOwner());
        fileObjDTO.setOwnerDto(ownerDto);
        return fileObjDTO;
    }

    public static Specification<FileObj> ownerFetch() {
        return ((root, criteriaQuery, criteriaBuilder) -> {
            root.fetch("owner", JoinType.LEFT);
            root.get("owner").get("id");
            return null;
        });
    }
}
