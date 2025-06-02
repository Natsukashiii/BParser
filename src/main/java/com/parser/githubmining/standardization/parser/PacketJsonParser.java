package com.parser.githubmining.standardization.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parser.githubmining.standardization.model.print.BuildConfigJson;
import com.parser.githubmining.standardization.model.print.BuildStepJson;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PacketJsonParser {
    public static BuildConfigJson load(String repoName, String repoPath) throws IOException {
        String packageJsonFilePath = repoPath + "/package.json";
        File file = new File(packageJsonFilePath);

        if (!file.exists()) {
            return null;
        }

        BuildConfigJson json = new BuildConfigJson();
        json.setRepoName(repoName);
        json.setFrameWork("npm");  // assuming npm as the framework for package.json

        // Parse package.json using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> config = objectMapper.readValue(new FileReader(file), new TypeReference<Map<String, Object>>() {
        });

        // Set scripts, dependencies, devDependencies, etc.
        setScriptsAndDependencies(config, json);

        return json;
    }

    private static void setScriptsAndDependencies(Map<String, Object> config, BuildConfigJson json) {
        // Parse and set scripts
        List<BuildStepJson.BuildJobJson> jobJsons = new ArrayList<>();
        if (config.containsKey("scripts")) {
            Map<String, String> scripts = (Map<String, String>) config.get("scripts");
            for (Map.Entry<String, String> entry : scripts.entrySet()) {
                BuildStepJson.BuildJobJson jobJson = new BuildStepJson.BuildJobJson();
                jobJson.setJobName(entry.getKey());
                jobJson.setCommands(entry.getValue());
                jobJsons.add(jobJson);
            }
        }
        if (!CollectionUtils.isEmpty(jobJsons)) {
            List<BuildStepJson> steps = new ArrayList<>();
            BuildStepJson stepJson = new BuildStepJson();
            stepJson.setJobs(jobJsons);
            steps.add(stepJson);
            json.setSteps(steps);
        }
    }




}
