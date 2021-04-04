package io.mdfitumi.unittest.services.impl;

import io.mdfitumi.unittest.dto.FileFilterDto;
import io.mdfitumi.unittest.dto.FileObjDTO;
import io.mdfitumi.unittest.exceptions.NoSuchElementInMinioException;
import io.mdfitumi.unittest.models.FileObj;
import io.mdfitumi.unittest.models.FileResponse;
import io.mdfitumi.unittest.services.DbFileService;
import io.mdfitumi.unittest.services.FileService;
import io.mdfitumi.unittest.services.MinioService;
import io.mdfitumi.unittest.services.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Value("${minio.bucket.name}")
    private String bucketName;
    @Autowired
    private DbFileService dbFileService;
    @Autowired
    private MinioService minioService;
    @Autowired
    private ObjectMapper objectMapper;

//    @Autowired
//    public FileServiceImpl(DbFileService dbFileService, MinioService minioService, ObjectMapper objectMapper) {
//        this.dbFileService = dbFileService;
//        this.minioService = minioService;
//        this.objectMapper = objectMapper;
//    }

    @Transactional
    @Override
    public FileResponse create(FileObjDTO fileObjDTO) {
        FileObj fileObj = dbFileService.create(fileObjDTO);
        minioService.putObjectIntoTheBucket(bucketName,
                getInputStreamForFileObjDto(fileObjDTO),
                fileObj.getId().toString());
        FileResponse fileResponse = new FileResponse();
        fileResponse.setMessage("");
        fileResponse.setSuccess(true);
        fileResponse.getFileObjPages().add(objectMapper.fileToFileObjDTO(fileObj));
        return fileResponse;
    }

//получение ИнпутСтрима для минио.

    public InputStream getInputStreamForFileObjDto(FileObjDTO fileObjDTO) {
        return new ByteArrayInputStream(Optional.of(fileObjDTO
                .getFileData()
                .getBytes())
                .orElseThrow(RuntimeException::new));
    }


    @Transactional(readOnly = true)
    @Override
    public FileResponse filter(FileFilterDto fileFilterDto) {
        return dbFileService.filter(fileFilterDto);
    }

    @Transactional
    @Override
    public FileResponse update(UUID uuid, FileObjDTO fileObjDTO) {
        FileObj fileObj = dbFileService.update(uuid, fileObjDTO);
        if (fileObjDTO.getFileData() != null) {
            minioService.putObjectIntoTheBucket(bucketName,
                    getInputStreamForFileObjDto(fileObjDTO),
                    uuid.toString());
        }
        fileObjDTO = objectMapper.fileToFileObjDTO(fileObj);
        fileObjDTO.setFileData(null);
        FileResponse fileResponse = new FileResponse();
        fileResponse.setMessage("");
        fileResponse.setSuccess(true);
        fileResponse.getFileObjPages().add(fileObjDTO);
        return fileResponse;
    }

    @Transactional
    @Override
    public FileResponse setDeletedStatusForFileByUuid(UUID uuid) {
        return dbFileService.deleted(uuid);
    }

    @Transactional(readOnly = true)
    @Override
    public FileResponse getFileByUuid(UUID uuid) {
        FileObjDTO fileObjDTO = null;
        try {
            fileObjDTO = dbFileService.getFileById(uuid);
        } catch (NoSuchElementInMinioException e) {
            e.printStackTrace();
            return null;
        }
        byte[] file = minioService.getObjectFromBucket(bucketName, uuid.toString());
        fileObjDTO.setFileData(convertArrayByteToString(file));
        FileResponse fileResponse = new FileResponse();
        fileResponse.setMessage("");
        fileResponse.setSuccess(true);
        fileResponse.getFileObjPages().add(fileObjDTO);
        return fileResponse;
    }

    /**перевод массива байта в стрингу для минио.
     */
    protected String convertArrayByteToString(byte[] arr) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            str.append((char) arr[i]);
        }
        return new String(str);
    }
}