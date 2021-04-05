package io.mdfitumi.unittest.dto;

public class Paginating {
    private Integer number;
    private Integer size;

    public Paginating(Integer number, Integer size) {
        this.number = number;
        this.size = size;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
