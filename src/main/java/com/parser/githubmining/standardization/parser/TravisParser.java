package com.parser.githubmining.standardization.parser;

import com.parser.githubmining.standardization.model.print.BuildConfigJson;
import com.parser.githubmining.standardization.model.print.BuildStepJson;
import com.parser.githubmining.standardization.model.print.BuildTriggerJson;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TravisParser {
    public static BuildConfigJson load(String repoName, String repoPath) throws IOException {
        String travisFilePath = repoPath + "/.travis.yml";
        File file = new File(travisFilePath);

        if (!file.exists()) {
            return null;
        }

        BuildConfigJson json = new BuildConfigJson();
        json.setRepoName(repoName);
        json.setFrameWork("travis");

        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.load(new FileReader(file));


        setJobJsons(config, json);

        return json;
    }

    private static void setJobJsons(Map<String, Object> config, BuildConfigJson json) {
        // other elements
        List<BuildStepJson> steps = new ArrayList<>();
        BuildStepJson stepJson = new BuildStepJson();

        stepJson.setStepName("travis ci");
        // whole task will invoke on these branches
        if (config.containsKey("branches")) {
            BuildTriggerJson triggerJson = new BuildTriggerJson();
            triggerJson.setActions(Arrays.asList("branches"));
            stepJson.setTrigger(triggerJson);
        }

        if (config.containsKey("if")) {
            BuildTriggerJson triggerJson = new BuildTriggerJson();
            triggerJson.setConditions(Arrays.asList("if"));
            stepJson.setTrigger(triggerJson);
        }

        List<BuildStepJson.BuildJobJson> jobs = new ArrayList<>();
        getJobJson(config, "before_install", jobs);
        getJobJson(config, "install", jobs);
        getJobJson(config, "before_script", jobs);
        getJobJson(config, "script", jobs);
        getJobJson(config, "after_success", jobs);
        getJobJson(config, "after_failure", jobs);
        getJobJson(config, "after_script", jobs);
        getJobJson(config, "before_deploy", jobs);
        getJobJson(config, "deploy", jobs);
        getJobJson(config, "after_script", jobs);

        if (!CollectionUtils.isEmpty(jobs)) {
            stepJson.setJobs(jobs);
            steps.add(stepJson);
            json.setSteps(steps);
        }
    }


    private static void getJobJson(Map<String, Object> config, String key, List<BuildStepJson.BuildJobJson> jobs) {

        if (config.containsKey(key)) {
            Object content = config.get(key);
            if (Objects.nonNull(content)) {
                BuildStepJson.BuildJobJson jobJson = new BuildStepJson.BuildJobJson();
                jobJson.setJobName(key);
                if (key.equals("deploy")) {
                    Object deployObject = config.get("deploy");
                    if (Objects.isNull(deployObject)) {
                        for (Object object : (List<?>) deployObject) {
                            Map<String, Object> omap = (Map<String, Object>) object;
                            if (omap.containsKey(true)) {
                                BuildTriggerJson triggerJson = new BuildTriggerJson();
                                Map<String, Object> onObject = (Map<String, Object>) omap.get(true);
                                List<String> conditions = new LinkedList<>();
                                Optional.ofNullable(onObject.get("tags")).ifPresent(o -> conditions.add("tags"));
                                Optional.ofNullable(onObject.get("repo")).ifPresent(o -> conditions.add("repo"));
                                Optional.ofNullable(onObject.get("condition")).ifPresent(o -> conditions.add("condition"));
                                Optional.ofNullable(onObject.get("all_branches")).ifPresent(o -> conditions.add("all_branches"));
                                Optional.ofNullable(onObject.get("branch")).ifPresent(o -> conditions.add("branch"));
                                if (!CollectionUtils.isEmpty(conditions)) {
                                    jobJson.setConditions(String.join(",", conditions));
                                }
                            }
                        }
                    }
                }
                jobJson.setCommands((config.get(key)).toString());
                if (Objects.isNull(jobJson.getCommands())) {
                    jobJson.setCommands(jobJson.getJobName());

                }
                jobs.add(jobJson);
            }
        }
    }


}