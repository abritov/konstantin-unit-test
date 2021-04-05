package io.mdfitumi.unittest;

import io.mdfitumi.unittest.dto.FileFilterDto;
import io.mdfitumi.unittest.dto.FileObjDTO;
import io.mdfitumi.unittest.dto.Paginating;
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
import java.time.LocalDateTime;
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
        mockFileObj.setId(1L);
        Mockito.when(dbFileService.create(Mockito.any())).thenReturn(mockFileObj);
        Mockito.when(objectMapper.fileToFileObjDTO(mockFileObj)).thenReturn(new FileObjDTO());

        String testFileData = "test file data";
        FileObjDTO createFileRequest = new FileObjDTO();
        createFileRequest.setFileData(testFileData);
        fileService.create(createFileRequest);

        Mockito.verify(minioService).putObjectIntoTheBucket(
                Mockito.eq(bucketName),
                Mockito.any(InputStream.class),
                Mockito.eq(mockFileObj.getId().toString())
        );
    }

    @Test
    public void fileServiceFilter_shouldReturnFileResponse() {
        FileObj mockFile = new FileObj();
        mockFile.setId(1L);
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
        mockFileObj.setId(1L);
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
        mockFileObj.setId(1L);
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
}
