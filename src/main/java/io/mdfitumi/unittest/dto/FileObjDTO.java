package io.mdfitumi.unittest.dto;

import org.apache.tomcat.jni.Local;

import java.time.LocalDateTime;

public class FileObjDTO {
    private String data;
    private String ext;
    private String fileName;
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
        return new OwnerDto();
    }
    public void setOwnerDto(OwnerDto ownerDto) {

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
}
