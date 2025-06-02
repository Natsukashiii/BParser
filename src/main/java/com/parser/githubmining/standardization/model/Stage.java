package com.parser.githubmining.standardization.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
public class Stage {
    private String stageId;
    private Map<String, Object> environment;
    private Map<String, Object> inputs;
    private Map<String, Object> outputs;
    @JsonIgnore
    private LinkedList<Step> steps;
    private Map<String, Object> strategy;

    private FrameworkEnum buildTool;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Stage: ");

        Optional.ofNullable(stageId).ifPresent(value -> sb.append("stageId='").append(value).append("', "));
        Optional.ofNullable(environment).ifPresent(value -> sb.append("environment=").append(value).append(", "));
        Optional.ofNullable(inputs).ifPresent(value -> sb.append("inputs=").append(value).append(", "));
        Optional.ofNullable(outputs).ifPresent(value -> sb.append("outputs=").append(value).append(", "));
        Optional.ofNullable(steps).ifPresent(value -> sb.append("steps=").append(value).append(", "));
        Optional.ofNullable(strategy).ifPresent(value -> sb.append("strategy=").append(value).append(", "));
        Optional.ofNullable(buildTool).ifPresent(value -> sb.append("buildTool=").append(value).append(""));
        return sb.toString();
    }
}
