package com.artemistechnica.federation.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonAutoDetect
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleModel {

    public final String valueA;
    public final String valueB;

    public static SampleModel mk(String vA, String vB) {
        return SampleModel.builder().valueA(vA).valueB(vB).build();
    }
}
