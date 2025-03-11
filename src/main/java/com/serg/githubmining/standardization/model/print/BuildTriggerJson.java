package com.serg.githubmining.standardization.model.print;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
import java.util.Optional;


@Getter
public class BuildTriggerJson {
    /**
     * prod/test/other custom result
     */
    @JsonProperty("environment")
    private List<String> environment_profile;

    @JsonProperty("actions")
    // can be actions(ons in github action)
    //
    private List<String> actions;

    @JsonProperty("conditions")
    // /phases(in maven)/ifString(in github action jobs)
    private List<String> conditions;


    @JsonProperty("content")
    private String content;

    public void setEnvironment_profile(List<String> environment_profile) {
        this.environment_profile = environment_profile;
        updateContent();
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
        updateContent();
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
        updateContent();
    }

    private void updateContent() {
        StringBuilder sb = new StringBuilder();

        if (environment_profile != null && !environment_profile.isEmpty()) {
            sb.append(String.join(",", environment_profile));
        }

        if (actions != null && !actions.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(String.join(",", actions));
        }

        if (conditions != null && !conditions.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(String.join(",", conditions));
        }

        this.content = sb.length() > 0 ? sb.toString() : "";
    }

    // Default constructor
    public BuildTriggerJson() {
    }

    // Parameterized constructor
    public BuildTriggerJson(List<String> environment_profile, List<String> conditions) {
        this.environment_profile = environment_profile;
        this.conditions = conditions;
        updateContent();
    }
    // Copy constructor
    public BuildTriggerJson(BuildTriggerJson other) {
        this.environment_profile = other.environment_profile;
        this.conditions = other.conditions;
        updateContent();
    }

    @Override
    public String toString() {
        return
                "environment_profile=" + Optional.ofNullable(environment_profile).orElse(List.of()) +
                ", conditions=" + Optional.ofNullable(conditions).orElse(List.of()) +
                        ", actions=" + Optional.ofNullable(actions).orElse(List.of())
              ;
    }
}
