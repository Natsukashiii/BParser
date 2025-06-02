package com.parser.githubmining.standardization.model.print;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

/**
 * A build profile in one repo(a repo can have different profiles based on the envs)
 */
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuildConfigJson {
    @JsonProperty("repo_name")
    private String repoName;


    @JsonProperty("source_files")
    private List<String> sourceFile;

    @JsonProperty("framework")
    private String frameWork;

    /**
     * can be prod/test/pre...
     */
    @JsonIgnore
    @JsonProperty("environment")
    private String environment;

    /**
     * all the args need
     */
    @JsonProperty("build_args")
    private BuildEnvJson args;
//
//    @JsonProperty("trigger")
//    private BuildTriggerJson trigger;

    /**
     *
     */
    @JsonProperty("build_dependencies")
    private List<Object> dependencies;

    @JsonProperty("sequence")
    private List<String> sequences;

    @JsonProperty("build_steps")
    private List<BuildStepJson> steps;

    @Override
    public String toString() {
        return BuildConfigJson.class.getSimpleName() + "{" +
                "repoName=" + Optional.ofNullable(repoName).orElse("") +
                ", sourceFile=" + Optional.ofNullable(sourceFile).orElse(List.of()) +
                ", environment=" + Optional.ofNullable(environment).orElse("") +
                ", frameWork=" + Optional.ofNullable(frameWork).orElse("") +
                ", args=" + Optional.ofNullable(args).orElse(null) +
                ", dependencies=" + Optional.ofNullable(dependencies).orElse(List.of()) +
                ", sequences=" + Optional.ofNullable(sequences).orElse(List.of()) +
                ", tasks=" + Optional.ofNullable(steps).orElse(List.of()) +
                '}';
    }

}
