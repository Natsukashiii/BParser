package com.serg.githubmining.standardization.model.print;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class BuildEnvJson {
    /**
     * all the parameters
     */
    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    @JsonProperty("strategies")
    private Map<String, Object> strategies;

    @JsonProperty("outputs")
    private Map<String, Object> outputs;

}
