package io.mdfitumi.unittest;

import io.mdfitumi.unittest.dto.FileFilterDto;
import io.mdfitumi.unittest.dto.FileObjDTO;
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
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RunWith(SpringRunner.class)
public class FileServiceTest {
//    @TestConfiguration
//    static class DbFileServiceConfigurationContext {
//        @Bean
//        public DbFileService dbFileService() {
//            return new DbFileService() {
//                @Override
//                public FileObj create(FileObjDTO fileObjDTO) {
//                    return null;
//                }
//
//                @Override
//                public FileResponse filter(FileFilterDto fileFilterDto) {
//                    return null;
//                }
//
//                @Override
//                public FileObj update(UUID uuid, FileObjDTO fileObjDTO) {
//                    return null;
//                }
//
//                @Override
//                public FileResponse deleted(UUID uuid) {
//                    return null;
//                }
//
//                @Override
//                public FileObjDTO getFileById(UUID uuid) throws NoSuchElementInMinioException {
//                    return null;
//                }
//            };
//        }
//    }

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

    @MockBean
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
//        Mockito.when(minioService.putObjectIntoTheBucket(
//                Mockito.anyString(),
//                Mockito.any(),
//                Mockito.anyString()
//        )).thenReturn();
        Mockito.when(objectMapper.fileToFileObjDTO(mockFileObj)).thenReturn(new FileObjDTO());

        String testFileData = "test file data";
        FileObjDTO createFileRequest = new FileObjDTO();
        createFileRequest.setFileData(testFileData);
        fileService.create(createFileRequest);

        Mockito.verify(minioService).putObjectIntoTheBucket(
                bucketName,
                new ByteArrayInputStream(testFileData.getBytes(StandardCharsets.UTF_8)),
                mockFileObj.getId().toString()
        );
    }
}
