package com.serg.githubmining.standardization.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Optional;

@Setter
@Getter
public class Step {
    private String stageId;

    private String stepId;
    private String stepName;
    private String command;


    private Map<String, Object>  environments;

    private Map<String, Object> strategy;
    private Map<String, Object>  inputs;
    private Map<String, Object>  outputs;
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Step:");

        Optional.ofNullable(stageId).ifPresent(value -> sb.append("stageId='").append(value).append("', "));
        Optional.ofNullable(stepId).ifPresent(value -> sb.append("stepId='").append(value).append("', "));
        Optional.ofNullable(stepName).ifPresent(value -> sb.append("stepName='").append(value).append("', "));
        Optional.ofNullable(command).ifPresent(value -> sb.append("command='").append(value).append("', "));
        Optional.ofNullable(environments).ifPresent(value -> sb.append("environments=").append(value).append(", "));
        Optional.ofNullable(strategy).ifPresent(value -> sb.append("strategy=").append(value).append(", "));
        Optional.ofNullable(inputs).ifPresent(value -> sb.append("inputs=").append(value).append(", "));
        Optional.ofNullable(outputs).ifPresent(value -> sb.append("outputs=").append(value).append(""));


        return sb.toString();
    }


}
