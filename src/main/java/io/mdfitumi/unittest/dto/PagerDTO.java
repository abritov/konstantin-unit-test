package io.mdfitumi.unittest.dto;

public class PagerDTO {
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Integer size;
    private Long total;

    public void setNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public void setPrev(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
