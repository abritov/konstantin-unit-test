package io.mdfitumi.unittest.entities;

import io.mdfitumi.unittest.dto.OwnerDto;

import java.util.UUID;

public class Owner {
    UUID id;
    String name;

    public Owner(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public OwnerDto toOwnerDto() {
        OwnerDto result = new OwnerDto();
        result.setUuid(id);
        result.setName(name);
        return result;
    }
}
