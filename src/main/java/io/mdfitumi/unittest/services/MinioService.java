package io.mdfitumi.unittest.services;

import io.mdfitumi.unittest.entities.FolderData;

import java.io.InputStream;

public interface MinioService {
    void putObjectIntoTheBucket(String bucketName, InputStream file, String fileId);
    byte[] getObjectFromBucket(String bucketName, String id);
    FolderData getFolderData();
}
