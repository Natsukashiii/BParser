package com.parser.githubmining.standardization.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Map;

@Setter
@Getter
public class BuildConfigStructure {
    @JsonIgnore
    private BasicInfo basic;

    /**
     * the triggers
     */
    private Map<String, Object> triggers;

    private Map<String, Object> environments;



    private String projectName;

    private String fileName;

    /**
     * one repo may have multiple config files
     */
    private String configName;

    /**
     * including all the steps inside all the process
     */
    private LinkedList<Step> steps;

    /**
     * All the stages(stage may have orders)
     */
    private LinkedList<Stage> stages;

    private FrameworkEnum CiTool;

}
