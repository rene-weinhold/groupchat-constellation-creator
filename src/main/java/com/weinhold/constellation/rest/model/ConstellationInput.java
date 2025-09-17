package com.weinhold.constellation.rest.model;

import java.util.List;

import lombok.Data;

@Data
public class ConstellationInput {

    private List<String> people;
    private int numberOfGroups;
    private Rotation rotation;
    private int year;

}
