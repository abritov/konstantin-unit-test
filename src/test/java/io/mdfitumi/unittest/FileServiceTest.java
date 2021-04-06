package io.mdfitumi.unittest;

import io.mdfitumi.unittest.dto.FileFilterDto;
import io.mdfitumi.unittest.dto.FileObjDTO;
import io.mdfitumi.unittest.dto.Paginating;
import io.mdfitumi.unittest.entities.FolderData;
import io.mdfitumi.unittest.entities.Owner;
import io.mdfitumi.unittest.exceptions.NoSuchElementInMinioException;
import io.mdfitumi.unittest.models.FileObj;
import io.mdfitumi.unittest.models.FileResponse;
import io.mdfitumi.unittest.repositories.FileFilterRepository;
import io.mdfitumi.unittest.repositories.FileRepository;
import io.mdfitumi.unittest.repositories.FolderDataRepository;
import io.mdfitumi.unittest.repositories.OwnerRepository;
import io.mdfitumi.unittest.services.DbFileService;
import io.mdfitumi.unittest.services.FileService;
import io.mdfitumi.unittest.services.MinioService;
import io.mdfitumi.unittest.services.ObjectMapper;
import io.mdfitumi.unittest.services.impl.DbFileServiceImpl;
import io.mdfitumi.unittest.services.impl.FileServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@RunWith(SpringRunner.class)
public class FileServiceTest {
    @TestConfiguration
    static class DbFileServiceConfigurationContext {
        @Bean
        public DbFileService dbFileService() {
            return new DbFileServiceImpl();
        }
    }
    @TestConfiguration
    static class FileServiceTestConfiguration {
        @Bean
        public FileServiceImpl fileService() {
            return new FileServiceImpl();
        }
    }

    @MockBean
    FileRepository fileRepository;
    @MockBean
    FileFilterRepository fileFilterRepository;
    @MockBean
    OwnerRepository ownerRepository;
    @MockBean
    FolderDataRepository folderDataRepository;
    @MockBean
    ObjectMapper objectMapper;
    @MockBean
    MinioService minioService;

    @Autowired
    DbFileService dbFileService;


    @Value("${minio.bucket.name}")
    String bucketName;

    @Autowired
    FileService fileService;

    @Test
    public void fileServiceCreate_shouldCreateRecord() {
        FileObj mockFileObj = new FileObj();

        String testFileData = "test file data";
        FileObjDTO createFileRequest = new FileObjDTO();
        createFileRequest.setFileData(testFileData);


        mockFileObj.setId(new UUID(1, 1));
        Mockito
                .when(objectMapper.fileToFileObj(createFileRequest))
                .thenReturn(mockFileObj);
        Mockito
                .when(ownerRepository.findById(new UUID(0,0)))
                .thenReturn(java.util.Optional.of(new Owner(new UUID(0, 0), "notOwnerName")));
        Mockito
                .when(minioService.getFolderData())
                .thenReturn(new FolderData());
        Mockito
                .when(folderDataRepository.findByName(Mockito.any(FolderData.class)))
                .thenReturn(new FolderData());

        Mockito
                .when(objectMapper.fileToFileObjDTO(Mockito.any()))
                .thenAnswer(arg -> arg.getArgument(0, FileObj.class).toFileObjDto());


        FileResponse result = fileService.create(createFileRequest);

        Mockito.verify(objectMapper).fileToFileObj(Mockito.eq(createFileRequest));
        Mockito.verify(ownerRepository).findById(new UUID(0,0));
        Mockito.verify(minioService).getFolderData();
        Mockito.verify(folderDataRepository).findByName(Mockito.any());
        Mockito.verify(fileRepository).save(mockFileObj);

        Mockito.verify(minioService).putObjectIntoTheBucket(
                Mockito.eq(bucketName),
                Mockito.any(InputStream.class),
                Mockito.eq(mockFileObj.getId().toString())
        );

        Mockito.verify(objectMapper).fileToFileObjDTO(mockFileObj);


        Assert.assertEquals("", result.getMessage());
        Assert.assertTrue(result.getSuccess());
        Assert.assertEquals(1, result.getFileObjPages().size());
        Assert.assertNotNull(result.getFileObjPages().get(0).getCreateDateTime());
        Assert.assertNotNull(result.getFileObjPages().get(0).getUpdateDateTime());
        Assert.assertNull(result.getFileObjPages().get(0).getFileData());
    }

    @Test
    public void fileServiceFilter_shouldReturnFileResponse() {
        FileObj mockFile = new FileObj();
        mockFile.setId(new UUID(1, 1));
        Page<FileObj> mockFileFilterResult = new PageImpl<>(Arrays.asList(mockFile));

        Mockito.when(fileFilterRepository.findByFilter(
                Mockito.anyBoolean(),
                Mockito.anyBoolean(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.anyBoolean(),
                Mockito.any(UUID.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(LocalDateTime.class),
                Mockito.any(Pageable.class)
        )).thenReturn(mockFileFilterResult);

        FileFilterDto filterRequest = new FileFilterDto();
        filterRequest.setPaginating(new Paginating(10, 256));

        FileResponse result = fileService.filter(filterRequest);

        Mockito.verify(objectMapper).fileToFileObjDTO(Mockito.eq(mockFile));

        Assert.assertEquals("", result.getMessage());
        Assert.assertTrue(result.getSuccess());
        Assert.assertEquals(1, result.getFileObjPages().size());
    }

    @Test
    public void fileServiceUpdateWithoutFileData_shouldUpdateFileNameAndExtFields() {
        FileObj mockFileObj = new FileObj();
        mockFileObj.setId(new UUID(1, 1));
        mockFileObj.setFileName("old");
        mockFileObj.setExt("txt");

        FileObjDTO updateRequest = new FileObjDTO();
        updateRequest.setExt("py");
        updateRequest.setFileName("new");

        Mockito
                .when(fileRepository.getOne(Mockito.any(UUID.class)))
                .thenReturn(mockFileObj);
        Mockito
                .when(objectMapper.fileToFileObjDTO(Mockito.eq(mockFileObj)))
                .thenReturn(updateRequest);

        FileResponse result = fileService
                .update(new UUID(1, 1), updateRequest);

        FileObj expectedFileObj = new FileObj();
        expectedFileObj.setFileName("new");
        expectedFileObj.setExt("py");
        Mockito.verify(fileRepository).save(Mockito.eq(mockFileObj));
        Mockito.verify(objectMapper).fileToFileObjDTO(mockFileObj);

        Assert.assertEquals("", result.getMessage());
        Assert.assertTrue(result.getSuccess());
        Assert.assertEquals(1, result.getFileObjPages().size());
        Assert.assertEquals("new", result.getFileObjPages().get(0).getFileName());
        Assert.assertEquals("py", result.getFileObjPages().get(0).getExt());
    }

    @Test
    public void fileServiceUpdateWithFileData_shouldUpdateFieldsAndCallMinioService() {
        FileObj mockFileObj = new FileObj();
        mockFileObj.setId(new UUID(1, 1));
        mockFileObj.setFileName("old");
        mockFileObj.setExt("txt");

        FileObjDTO updateRequest = new FileObjDTO();
        updateRequest.setExt("py");
        updateRequest.setFileName("new");
        updateRequest.setFileData("hello world!");

        Mockito
                .when(fileRepository.getOne(Mockito.any(UUID.class)))
                .thenReturn(mockFileObj);
        Mockito
                .when(objectMapper.fileToFileObjDTO(Mockito.eq(mockFileObj)))
                .thenReturn(updateRequest);

        FileResponse result = fileService
                .update(new UUID(1, 1), updateRequest);

        Mockito.verify(fileRepository).save(Mockito.eq(mockFileObj));
        Mockito.verify(minioService).putObjectIntoTheBucket(
                Mockito.eq(bucketName),
                Mockito.any(InputStream.class),
                Mockito.eq(new UUID(1, 1).toString())
        );
        Mockito.verify(objectMapper).fileToFileObjDTO(mockFileObj);

        Assert.assertEquals("", result.getMessage());
        Assert.assertTrue(result.getSuccess());
        Assert.assertEquals(1, result.getFileObjPages().size());
        Assert.assertEquals("new", result.getFileObjPages().get(0).getFileName());
        Assert.assertEquals("py", result.getFileObjPages().get(0).getExt());
        Assert.assertNull(result.getFileObjPages().get(0).getFileData());
    }

    @Test
    public void fileServiceDeleteExistingFile_shouldSetDeletedFlagAndUpdateDateTime() {
        UUID mockUUID = new UUID(1, 1);
        LocalDateTime now = LocalDateTime.now();
        FileObj mockFileObj = new FileObj();
        mockFileObj.setId(mockUUID);
        mockFileObj.setDeleted(false);
        mockFileObj.setUpdateDateTime(now);

        Mockito
                .when(fileRepository.findById(mockUUID))
                .thenReturn(java.util.Optional.of(mockFileObj));

        FileResponse result = fileService.setDeletedStatusForFileByUuid(mockUUID);

        Mockito.verify(fileRepository).save(Mockito.refEq(mockFileObj, "isDeleted", "updatedAt"));

        Assert.assertEquals("", result.getMessage());
        Assert.assertTrue(result.getSuccess());
        Assert.assertNull(result.getFileObjPages());
    }

    @Test
    public void fileServiceDeleteDeletedFile_shouldNotSaveNewFileObj() {
        UUID mockUUID = new UUID(1, 1);
        LocalDateTime now = LocalDateTime.now();
        FileObj mockFileObj = new FileObj();
        mockFileObj.setId(mockUUID);
        mockFileObj.setDeleted(true);
        mockFileObj.setUpdateDateTime(now);

        Mockito
                .when(fileRepository.findById(mockUUID))
                .thenReturn(java.util.Optional.of(mockFileObj));

        FileResponse result = fileService.setDeletedStatusForFileByUuid(mockUUID);

        Mockito.verify(fileRepository, Mockito.never()).save(Mockito.any());

        Assert.assertEquals("Файл уже помечен на удаление", result.getMessage());
        Assert.assertTrue(result.getSuccess());
        Assert.assertNull(result.getFileObjPages());
    }
}
