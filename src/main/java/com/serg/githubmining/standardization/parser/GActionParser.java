package com.serg.githubmining.standardization.parser;

import com.serg.githubmining.standardization.model.print.BuildConfigJson;
import com.serg.githubmining.standardization.model.print.BuildStepJson;
import com.serg.githubmining.standardization.model.print.BuildTriggerJson;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.serg.gmodel.MultiTypeValue;
import org.serg.gmodel.On;
import org.serg.gmodel.Step;
import org.serg.gmodel.Workflow;
import org.serg.gparser.GWorkflowParser;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class GActionParser {
    public static BuildConfigJson load(String repoName, String repoPath) throws IOException, XmlPullParserException {
        String workflowsPath = repoPath + "/.github/workflows";
        File file = new File(workflowsPath);
        // Check if workflowsPath exists
        if (!file.exists()) {
            return null;
        }
        List<String> files = Arrays.asList(file.list((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml")));

        if (files.isEmpty()) {
            return null;
        }

        BuildConfigJson json = new BuildConfigJson();
        json.setRepoName(repoName);
        json.setFrameWork("github action");
        List<String> sourceFiles = new ArrayList<>();


        List<BuildStepJson> tasks = new ArrayList<>();
        for (String fileName : files) {
            if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
                sourceFiles.add(fileName);
                tasks.addAll(getTasks(json, new File(file, fileName).getAbsolutePath()));
            }
        }

        filterTask(tasks);
        json.setSteps(tasks);
        json.setSourceFile(sourceFiles);

        return json;
    }

    private static void filterTask(List<BuildStepJson> tasks) {
        // Set to keep track of unique triggers
        Set<String> seenTriggers = new HashSet<>();

        // Iterator to safely remove elements while iterating
        Iterator<BuildStepJson> iterator = tasks.iterator();

        while (iterator.hasNext()) {
            BuildStepJson task = iterator.next();
            BuildTriggerJson trigger = task.getTrigger();

            if (trigger != null) {
                String triggerString = trigger.toString();

                // Check if the trigger has been seen before
                if (seenTriggers.contains(triggerString)) {
                    // Remove task if the trigger is a duplicate
                    iterator.remove();
                } else {
                    // Add the trigger to the set of seen triggers
                    seenTriggers.add(triggerString);
                }
            }
        }
    }


    /**
     * task=job
     *
     * @param json
     * @param filePath
     * @throws FileNotFoundException
     */


    private static List<BuildStepJson> getTasks(BuildConfigJson json, String filePath) throws FileNotFoundException {
        List<BuildStepJson> tasks = new ArrayList<>();

        GWorkflowParser gWorkflowParser = new GWorkflowParser();
        Workflow gworkFlow = gWorkflowParser.parseWorkflow(filePath);

        if (gworkFlow == null) {
            return null;
        }

        // all the triggers
        BuildTriggerJson triggerJson = new BuildTriggerJson();
        if (gworkFlow.getOns() != null) {
            triggerJson.setActions(extractActions(gworkFlow.getOns()));
        }

        if (!gworkFlow.getJobs().isEmpty()) {
            for (org.serg.gmodel.Job job : gworkFlow.getJobs()) {
                BuildStepJson taskJson = new BuildStepJson();

                taskJson.setStepName(job.getJobId());
                // all the task should have the same trigger reason in the ons(default trigger)
//                if (job.getIf_string() != null) {
//                    triggerJson.setConditions(Arrays.asList(job.getIf_string()));
                if (job.getEnvironment() != null) {
                    MultiTypeValue env = job.getEnvironment();
                    Optional.ofNullable(env.getSingleValue()).ifPresent(value -> triggerJson.setEnvironment_profile(Arrays.asList(value)));
                    Optional.ofNullable(env.getListValue()).ifPresent(triggerJson::setEnvironment_profile);
                    Optional.ofNullable(env.getMapValue()).ifPresent(map -> triggerJson.setEnvironment_profile(new ArrayList<>(map.values())));
                }
                taskJson.setTrigger(triggerJson);

                // the sequence
                if (job.getNeeds() != null) {
                    taskJson.setPre(job.getNeeds().toString());

                }

                if (Objects.nonNull(job.getSteps()) && !job.getSteps().isEmpty()) {
                    List<BuildStepJson.BuildJobJson> jobJsons = new ArrayList<>();
                    for (Step step : job.getSteps()) {
                        BuildStepJson.BuildJobJson jobJson = new BuildStepJson.BuildJobJson();
                        if (Objects.nonNull(step.getStep_id())) {
                            jobJson.setJobName(step.getStep_id());
                        } else {
                            jobJson.setJobName(step.getName());
                        }

                        if (step.getIf_string() != null) {
                            jobJson.setConditions(step.getIf_string());
//                            jobJson.setTrigger(new BuildTriggerJson(null, Arrays.asList(step.getIf_string())));
                        }
                        if (step.getRun() != null) {
                            jobJson.setCommands(step.getRun().toString());
                        }
                        if (step.getUses() != null) {
                            jobJson.setCommands(step.getUses().toString());

                        }
                        if (jobJson.getJobName() == null && jobJson.getCommands() == null) {
                            continue;
                        } else if (jobJson.getJobName() == null) {
                            jobJson.setJobName(jobJson.getCommands());
                        } else if (jobJson.getCommands() == null) {
                            jobJson.setCommands(jobJson.getJobName());
                        }


                        jobJsons.add(jobJson);
                    }

                    if (!CollectionUtils.isEmpty(jobJsons)) {
                        taskJson.setJobs(jobJsons);
                    }
                }

                tasks.add(taskJson);
            }
        }
        return tasks;
    }

    private static List<String> extractActions(List<On> ons) {
        List<String> nonEmptyFields = new ArrayList<>();
        for (On on : ons) {
            nonEmptyFields.add(on.getEvent().getEventName());
            if (on.getTypes() != null && !on.getTypes().isEmpty()) nonEmptyFields.add("types");
            if (on.getTags() != null && !on.getTags().isEmpty()) nonEmptyFields.add("tags");
            if (on.getTags_ignore() != null && !on.getTags_ignore().isEmpty())
                nonEmptyFields.add("tags_ignore");
            if (on.getBranches() != null && !on.getBranches().isEmpty()) nonEmptyFields.add("branches");
            if (on.getBranches_ignore() != null && !on.getBranches_ignore().isEmpty())
                nonEmptyFields.add("branches_ignore");
            if (on.getPaths() != null && !on.getPaths().isEmpty()) nonEmptyFields.add("paths");
            if (on.getPaths_ignore() != null && !on.getPaths_ignore().isEmpty())
                nonEmptyFields.add("paths_ignore");
            if (on.getCrons() != null && !on.getCrons().isEmpty()) nonEmptyFields.add("crons");
            if (on.getSecrets() != null && !on.getSecrets().isEmpty()) nonEmptyFields.add("secrets");
            if (on.getInputs() != null && !on.getInputs().isEmpty()) nonEmptyFields.add("inputs");
            if (on.getOutputs() != null && !on.getOutputs().isEmpty()) nonEmptyFields.add("outputs");
            if (on.getWorkflows() != null && !on.getWorkflows().isEmpty()) nonEmptyFields.add("workflows");
        }
        return nonEmptyFields;

    }

}
