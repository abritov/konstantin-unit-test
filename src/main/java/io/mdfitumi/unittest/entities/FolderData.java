package io.mdfitumi.unittest.entities;

public class FolderData {
    private Long id;
    private String name;
    private FolderData data;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FolderData getFolderData() {
        return data;
    }

    public void setFolderData(FolderData data) {
        this.data = data;
    }
}
