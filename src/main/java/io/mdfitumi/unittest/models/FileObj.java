package io.mdfitumi.unittest.models;

import io.mdfitumi.unittest.dto.FileObjDTO;
import io.mdfitumi.unittest.entities.FolderData;
import io.mdfitumi.unittest.entities.Owner;

import java.time.LocalDateTime;
import java.util.Date;

public class FileObj {
    private Long id;
    private String data;
    private String fileName;
    private String ext;
    private FolderData folderData;
    private Owner owner;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileData() {
        return this.data;
    }

    public void setFolderData(FolderData data) {
        this.folderData = data;
    }
    public void setFileData(String data) {

    }
    public void setCreateDateTime(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getCreateDateTime() {
        return this.createdAt;
    }
    public void setUpdateDateTime(LocalDateTime date) {
        this.updatedAt = date;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public void setDeleted(boolean deleted) {
    }

    public boolean isDeleted() {
        return false;
    }

    public FileObjDTO toFileObjDto() {
        FileObjDTO result = new FileObjDTO();
        result.setFileName(this.fileName);
        result.setExt(this.ext);
        return result;
    }
}
