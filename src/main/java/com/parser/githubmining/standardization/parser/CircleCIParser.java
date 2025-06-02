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

public class CircleCIParser {
    public static BuildConfigJson load(String repoName, String repoPath) throws IOException {
        String circleCIFilePath = repoPath + "/.circleci/config.yml";
        File file = new File(circleCIFilePath);

        if (!file.exists()) {
            return null;
        }

        BuildConfigJson json = new BuildConfigJson();
        json.setRepoName(repoName);
        json.setFrameWork("circleci");

        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.load(new FileReader(file));

        HashMap<String, BuildStepJson> stepsMap = getStepMap(config);
        if (CollectionUtils.isEmpty(stepsMap)) {
            return null;
        }

        setStepsFromWorkflows(json, config, stepsMap);


        return json;
    }

    private static void setStepsFromWorkflows(BuildConfigJson json, Map<String, Object> config, HashMap<String, BuildStepJson> stepsMap) {
        List<BuildStepJson> steps = new ArrayList<>();

        if (config.containsKey("workflows") ){
            Map<String, Object> workflowsMap = (Map<String, Object>) config.get("workflows");
            // key is the phase name, value is the requires ([,])
            Map<String, String> buildStepMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : workflowsMap.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    Object jobConfigsList = ((Map<?, ?>) entry.getValue()).get("jobs");
                    if (jobConfigsList != null && jobConfigsList instanceof List) {
                        List<Object> jobConfigs = (List<Object>) jobConfigsList;
                        for (Object jobConfig : jobConfigs) {
                            if (jobConfig instanceof Map) {
                                Map<String, Object> jobConfigMap = (Map<String, Object>) jobConfig;
                                for (Map.Entry<String, Object> objectEntry : jobConfigMap.entrySet()) {
                                    if (objectEntry.getValue() instanceof Map<?,?>) {
                                        Map<String, Object> jobConfigMap2 = (Map<String, Object>) objectEntry.getValue();
                                        if (!jobConfigMap2.containsKey("requires")) {
                                            buildStepMap.put(objectEntry.getKey(), null);

                                        }else {
                                            List<String> requires = (List<String>) jobConfigMap2.get("requires");

                                            buildStepMap.put(objectEntry.getKey(),String.join(",", requires));
                                        }
                                    }
                                }

                            }
                        }
                    }
                }else if (entry.getValue() instanceof List<?>){
                    List<Object> jobConfigs = (List<Object>) entry.getValue();
                    for (Object jobConfig : jobConfigs) {
                        buildStepMap.put(jobConfig.toString(), null);
                    }
                }else if (entry.getValue() instanceof String) {
                    buildStepMap.put(entry.getValue().toString(), null);
                }
            }
            // build step sequecne exists
            if (!CollectionUtils.isEmpty(stepsMap)) {

            }

        }else {
            BuildStepJson step = new BuildStepJson();
            step.setStepName("circle ci");

            List<BuildStepJson.BuildJobJson> buildJobs = new ArrayList<>();
            for (Map.Entry<String, BuildStepJson> stepsInMap : stepsMap.entrySet()) {
                List<BuildStepJson.BuildJobJson> stepJobs = stepsInMap.getValue().getJobs();
                if (CollectionUtils.isEmpty(stepJobs)) {continue;}
                buildJobs.addAll(stepJobs);
            }
            if(!CollectionUtils.isEmpty(buildJobs)) {
                step.setJobs(buildJobs);
            }
            steps.add(step);
        }




        if (!CollectionUtils.isEmpty(stepsMap)) {json.setSteps(steps);}
    }

    private static void filterSteps(BuildConfigJson json) {
        if (CollectionUtils.isEmpty(json.getSteps())) {
            return;
        }
        List<BuildStepJson> steps = json.getSteps();

        List<BuildStepJson> newSteps = new ArrayList<>();

        Map<String, BuildStepJson> triggerMap = new HashMap<>();
        for (BuildStepJson step : steps) {
            BuildTriggerJson trigger = step.getTrigger();
            if (CollectionUtils.isEmpty(step.getJobs())) {
                if (Objects.isNull(step.getContent())) {
                    BuildStepJson newStep = new BuildStepJson();
                    newStep.setStepName(step.getStepName());
                    newStep.setContent(step.getStepName());
                }
            } else {
                if (triggerMap.containsKey(trigger.getContent())) {
                    continue;
                } else {
                    triggerMap.put(trigger.getContent(), step);
                }

                List<BuildStepJson.BuildJobJson> jobs = step.getJobs();
                for (BuildStepJson.BuildJobJson job : jobs) {
                    BuildStepJson newStep = new BuildStepJson();
                    String jobName = job.getJobName();
                    String commands = job.getCommands();
                    if (Objects.isNull(jobName) && Objects.isNull(commands)) {
                        continue;
                    } else if (Objects.isNull(job.getJobName())) {
                        jobName = commands;
                    } else if (Objects.isNull(job.getCommands())) {
                        commands = jobName;
                    }
                    newStep.setStepName(jobName);
                    newStep.setContent(commands);
                    newStep.setTrigger(trigger);
                    newSteps.add(newStep);
                }
            }

        }
        json.setSteps(newSteps);
    }


    private static HashMap<String, BuildStepJson> getStepMap(Map<String, Object> config) {
        HashMap<String, BuildStepJson> stepJsonHashMap = new HashMap<>();
        if (config.containsKey("jobs")) {
            Map<String, Object> jobs = (Map<String, Object>) config.get("jobs");
            for (Map.Entry<String, Object> entry : jobs.entrySet()) {
                Map<String, Object> jobConfig = (Map<String, Object>) entry.getValue();

                BuildStepJson taskJson = new BuildStepJson();
                taskJson.setStepName(entry.getKey());

                BuildTriggerJson triggerJson = new BuildTriggerJson();
                // docker is the conditions
                if (jobConfig.containsKey("docker")) {
                    triggerJson.setConditions(Arrays.asList(jobConfig.get("docker").toString()));
                }
                if (jobConfig.containsKey("environment")) {
                    triggerJson.setEnvironment_profile(Arrays.asList(jobConfig.get("environment").toString()));
                }
                taskJson.setTrigger(triggerJson);

                // value is the steps
                if (jobConfig.containsKey("steps")) {
                    List<Object> steps = (List<Object>) jobConfig.get("steps");
                    List<BuildStepJson.BuildJobJson> jobJsons = new ArrayList<>();


                    for (Object step : steps) {
                        BuildStepJson.BuildJobJson jobJson = new BuildStepJson.BuildJobJson();
                        if (step instanceof String) {
                            jobJson.setJobName(step.toString());
                        } else if (step instanceof Map<?, ?>) {
                            Map<String, Object> stepMap = (Map<String, Object>) step;
                            for (Map.Entry<String, Object> stepEntry : stepMap.entrySet()) {
                                String stepName = stepEntry.getKey();
                                jobJson.setJobName(stepName);

                                Optional.ofNullable(findName(stepEntry.getValue())).ifPresent(jobJson::setJobName);
                                Optional.ofNullable(findValue(stepEntry)).ifPresent(jobJson::setCommands);
                                if (Objects.isNull(jobJson.getCommands())) {
                                    jobJson.setCommands(jobJson.getJobName());
                                }else if (Objects.isNull(jobJson.getJobName())) {
                                    jobJson.setJobName(jobJson.getCommands());
                                }
                            }
                        }
                        jobJsons.add(jobJson);
                    }
                    taskJson.setJobs(jobJsons);
                }
                stepJsonHashMap.put(entry.getKey(), taskJson);
            }
        }
        return stepJsonHashMap;
    }


    private static String findValue(Map.Entry<String, Object> stepEntry) {
        String stepName = stepEntry.getKey();
        Object o = stepEntry.getValue();
        if (Objects.isNull(o)) {
            return null;
        }
        if (o instanceof String) {
            return o.toString();
        } else if (o instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) o;
            if (map.containsKey("value")) {
                return map.get("value").toString();
            } else if (map.containsKey("command")) {
                return map.get("command").toString();
            } else if (map.containsKey("key")) {
                return map.get("key").toString();
            } else if (map.containsKey("keys")) {
                return map.get("keys").toString();
            } else if (map.containsKey("run")) {
                return map.get("run").toString();
            } else if (map.containsKey(stepName)) {
                return map.get(stepName).toString();
            } else if (map.containsKey("path")) {
                return map.get("path").toString();
            }

        } else if (o instanceof List) {
            List<Object> list = (List<Object>) o;
            StringBuilder commands = new StringBuilder();
            for (Object item : list) {
                if (item instanceof String) {
                    commands.append(item.toString()).append("\n");
                } else if (item instanceof Map) {
                    Map<String, Object> itemConfig = (Map<String, Object>) item;
                    if (itemConfig.containsKey("command")) {
                        commands.append(itemConfig.get("command").toString()).append("\n");
                    } else if (itemConfig.containsKey("run")) {
                        commands.append(itemConfig.get("run").toString()).append("\n");
                    }
                }
            }
            return commands.toString();
        }

        return null;
    }

    private static String findName(Object o) {
        if (o instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) o;
            if (map.containsKey("name")) {
                return map.get("name").toString();
            }
        }
        return null;
    }

}