package com.serg.githubmining.standardization.model.print;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@Setter
@Getter
public class BuildStepJson {

    @JsonProperty("step_name")
    private String stepName;

    @JsonProperty("step_description")
    private String description;

    @JsonProperty("step_args")
    private BuildEnvJson args;


    /**
     * Jobs is NOT null means this task can have multiple jobs to execute
     */
    @JsonProperty("step_jobs")
    private List<BuildJobJson> jobs;
    /**
     * content is NOT null means this task only have one job to execute
     */
    @JsonProperty("step_content")
    private String content;

    @JsonProperty("step_trigger")
    private BuildTriggerJson trigger;

    @JsonProperty("step_pre")
    private String pre;


    @Setter
    @Getter
    public static class BuildJobJson {
        @JsonProperty("job_name")
        private String jobName;

        @JsonProperty("job_args")
        private BuildEnvJson args;

        @JsonProperty("job_command")
        private String commands;

        @JsonProperty("job_pre")
        private String pre;

        @JsonProperty("conditions")
        private String conditions;

//        @JsonProperty("job_trigger")
//        private BuildTriggerJson trigger;


        @Override
        public String toString() {
            return BuildJobJson.class.getSimpleName() + "{" +
                    "jobName=" + Optional.ofNullable(jobName).orElse("") +
                    ", args=" + Optional.ofNullable(args).orElse(null) +
                    ", commands=" + Optional.ofNullable(commands).orElse("") +
                    ", conditions=" + Optional.ofNullable(conditions).orElse(null) +
                    ", pre=" + Optional.ofNullable(pre).orElse("") +
                    '}';
        }
    }

    @Override
    public String toString() {
        return BuildStepJson.class.getSimpleName() + "{" +
                "taskName=" + Optional.ofNullable(stepName).orElse("") +
                ", description=" + Optional.ofNullable(description).orElse("") +
                ", args=" + Optional.ofNullable(args).orElse(null) +
                ", jobs=" + Optional.ofNullable(jobs).orElse(List.of()) +
                ", content=" + Optional.ofNullable(content).orElse("") +
                ", trigger=" + Optional.ofNullable(trigger).orElse(null) +
                ", pre=" + Optional.ofNullable(pre).orElse("") +
                '}';
    }
}


