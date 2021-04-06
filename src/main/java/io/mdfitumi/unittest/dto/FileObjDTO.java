package io.mdfitumi.unittest.dto;

import io.mdfitumi.unittest.entities.Owner;
import org.apache.tomcat.jni.Local;

import java.time.LocalDateTime;

public class FileObjDTO {
    private String data;
    private String ext;
    private String fileName;
    private OwnerDto ownerDto;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getFileData() {
        return data;
    }

    public void setFileData(String data) {
        this.data = data;
    }
    public LocalDateTime getCreateDateTime() {
        return this.createdAt;
    }
    public LocalDateTime getUpdateDateTime() {
        return this.updatedAt;
    }

    public OwnerDto getOwnerDto() {
        return ownerDto;
    }
    public void setOwnerDto(OwnerDto ownerDto) {
        this.ownerDto = ownerDto;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExt() {
        return this.ext;
    }
    public void setExt(String ext) {
        this.ext = ext;
    }

    public Boolean getDeleted() {
        return false;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
