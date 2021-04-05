package io.mdfitumi.unittest.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class FileFilterDto {
//    private Integer
    private Boolean deleted;
    private String ext;
    private LocalDate beginDateCreate;
    private LocalDate endDateCreate;
    private UUID idOwner;
    private Paginating pagination;

    public Paginating getPaginating() {
        return pagination;
    }
    public void setPaginating(Paginating pagination) {
        this.pagination = pagination;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public LocalDate getBeginDateCreate() {
        return beginDateCreate;
    }

    public void setBeginDateCreate(LocalDate beginDateCreate) {
        this.beginDateCreate = beginDateCreate;
    }

    public LocalDate getEndDateCreate() {
        return endDateCreate;
    }

    public void setEndDateCreate(LocalDate endDateCreate) {
        this.endDateCreate = endDateCreate;
    }

    public UUID getIdOwner() {
        return idOwner;
    }

    public void setIdOwner(UUID idOwner) {
        this.idOwner = idOwner;
    }
}
