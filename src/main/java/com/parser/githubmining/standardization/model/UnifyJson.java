package com.parser.githubmining.standardization.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class UnifyJson {
    private String projectName;
    @JsonProperty("gactions")
    private List<SingleGAction> gActions;


    @Setter
    @Getter
    private static class SingleGAction{
        @JsonProperty("fileName")
        private String fileName;
        @JsonProperty("triggers")

        private List<String> triggers;
        @JsonProperty("structure")
        private BuildConfigStructure structure;
    }

    public static UnifyJson convertStructure2Json(List<BuildConfigStructure> structures) {
        if (CollectionUtils.isEmpty(structures)) {
            return null;
        }
        UnifyJson unifyJson = new UnifyJson();

        List<SingleGAction> gactions = new ArrayList<>();

        for (BuildConfigStructure structure : structures) {
            SingleGAction gAction = new SingleGAction();
            gAction.setFileName(structure.getFileName());

            if (structure.getTriggers() != null && !structure.getTriggers().isEmpty()) {
                gAction.setTriggers(structure.getTriggers().keySet().stream().collect(Collectors.toList()));
            }

            gAction.setStructure(structure);
            gactions.add(gAction);
        }
        unifyJson.setGActions(gactions);

        return unifyJson;
    }
}
